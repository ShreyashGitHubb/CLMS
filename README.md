# College Laboratory Equipment Management System

CLMS is a Java FSD project for managing college laboratory equipment, student borrowing requests, returns, overdue fines, and maintenance records.

## Project Status

### Phase 1: UI and database design

- Responsive HTML5/CSS3 dashboard
- JavaScript navigation, search, filtering, role switching, and validation
- Equipment catalog and borrowing request interface
- MySQL schema and sample data
- ER model, business rules, and task allocation

### Phase 2: Java backend and integration

- Java 17 domain models
- JDBC connection using MySQL Connector/J
- DAO layer with prepared statements
- Service layer for equipment and borrowing workflows
- Authentication user lookup
- HTTP API for live equipment and borrow requests
- Transaction-safe inventory updates and overdue fine calculation

The frontend currently uses the API for equipment and request operations. It falls back to demo catalog data when the API is offline.

## Features

- **Equipment inventory:** View equipment, categories, locations, quantities, conditions, and availability.
- **Borrow requests:** Students request available equipment with a return date.
- **Approvals and returns:** Staff can approve requests, issue equipment, process returns, and update inventory quantities.
- **Overdue fines:** Returns are charged at `10.00` per overdue day.
- **Maintenance tracking:** Equipment can be marked for maintenance and associated with service records.
- **Role support:** The design supports `ADMIN`, `LAB_ASSISTANT`, and `STUDENT` roles.

## Requirements

- Ubuntu/Linux or another Java-compatible operating system
- Java 17 or newer
- MySQL Server
- Python 3 for the static frontend server
- MySQL Connector/J 8.4.0

## Project Structure

```text
CLMS/
├── database/
│   ├── schema.sql              # Database, tables, keys, checks, and indexes
│   └── sample-data.sql         # Demo users, equipment, transactions, and maintenance
├── docs/
│   ├── TASK_GUIDE.md           # Project plan, ER model, user stories, and assessment checklist
│   ├── PHASE2_GUIDE.md         # Backend architecture and verification checklist
│   └── SUBMISSION_REPORT.md    # PDF-ready Formative Assessment 1 report
├── frontend/
│   ├── index.html              # Dashboard markup and views
│   ├── script.js               # UI behavior and API calls
│   ├── styles.css               # Responsive visual design
│   └── favicon.svg              # Local browser icon
├── src/
│   ├── api/                    # Built-in Java HTTP API
│   ├── config/                 # JDBC connection configuration
│   ├── dao/                    # Prepared SQL and result mapping
│   ├── exception/              # Domain-specific exceptions
│   ├── model/                  # Java records for domain entities
│   ├── service/                # Business rules and transactions
│   └── Main.java               # JDBC equipment-listing smoke test
├── lib/                        # Local Connector/J jar, ignored by Git
└── out/                        # Generated class files, ignored by Git
```

## Database Setup

Start MySQL:

```bash
sudo systemctl start mysql
```

Ubuntu commonly configures MySQL `root` to use socket authentication. Run the schema with `sudo mysql`, not `mysql -u root -p`:

```bash
sudo mysql < database/schema.sql
sudo mysql clms < database/sample-data.sql
```

Create the application user from the MySQL prompt:

```bash
sudo mysql
```

```sql
CREATE USER 'clms_app'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON clms.* TO 'clms_app'@'localhost';
FLUSH PRIVILEGES;
```

Verify the database:

```bash
sudo mysql -e "USE clms; SHOW TABLES; SELECT * FROM equipment;"
```

The main tables are:

| Table | Purpose |
|---|---|
| `users` | Student, lab assistant, and administrator accounts |
| `equipment` | Inventory, quantity, location, condition, and status |
| `borrow_transactions` | Requests, issues, returns, and fines |
| `maintenance_logs` | Breakdowns, repairs, service cost, and status |

## Install the JDBC Driver

Create the local library directory and download MySQL Connector/J:

```bash
mkdir -p lib
curl -fL -o lib/mysql-connector-j-8.4.0.jar \
  https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar
```

The jar is ignored by Git because it is a local dependency.

## Compile the Backend

From the project root:

```bash
rm -rf out
mkdir -p out
javac -cp lib/mysql-connector-j-8.4.0.jar \
  -d out $(find src -name '*.java')
```

The connection uses these defaults in `src/config/DatabaseConnection.java`:

```text
Database URL: jdbc:mysql://localhost:3306/clms?serverTimezone=UTC
Username:     clms_app
Password:     your_password
```

Environment variables can override the defaults when required:

```bash
export CLMS_DB_URL='jdbc:mysql://localhost:3306/clms?serverTimezone=UTC'
export CLMS_DB_USER='clms_app'
export CLMS_DB_PASSWORD='your_password'
```

## Run the JDBC Smoke Test

This command connects to MySQL and lists available equipment:

```bash
java -cp out:lib/mysql-connector-j-8.4.0.jar Main
```

Expected output includes equipment such as:

```text
Available equipment:
- Arduino Uno Kit (3/12 available)
- Digital Multimeter (14/18 available)
```

## Run the Full Application

Use two terminals from the project root.

### Terminal 1: Start the Java API

```bash
java -cp out:lib/mysql-connector-j-8.4.0.jar api.ApiServer
```

The API runs at `http://localhost:8080`.

### Terminal 2: Start the frontend

```bash
python3 -m http.server 8000 --directory frontend
```

Open `http://localhost:8000` in a browser.

Stop either server with `Ctrl+C`.

## API Endpoints

### Get available equipment

```http
GET http://localhost:8080/api/equipment
```

### Create a borrow request

```http
POST http://localhost:8080/api/requests
Content-Type: application/json
```

Request body:

```json
{
  "userId": 1,
  "equipmentId": 2,
  "dueDate": "2026-09-28"
}
```

The API creates a `PENDING` record in `borrow_transactions`. The due date must be after the current date, and the equipment must be available.

## Backend Architecture

```text
Frontend
   |
   | HTTP JSON
   v
ApiServer
   |
   v
Service layer       Business validation, transactions, fines
   |
   v
DAO layer           Prepared SQL statements and result mapping
   |
   v
DatabaseConnection JDBC configuration and MySQL connection
   |
   v
MySQL database
```

The service layer keeps database operations atomic. Approving a request decreases available quantity, while returning equipment increases it and calculates any overdue fine.

## Troubleshooting

### `No suitable driver found`

Make sure the connector jar exists and is included in both commands:

```bash
ls lib/mysql-connector-j-8.4.0.jar
javac -cp lib/mysql-connector-j-8.4.0.jar -d out $(find src -name '*.java')
java -cp out:lib/mysql-connector-j-8.4.0.jar Main
```

### `Access denied for user 'clms_app'`

Check that the MySQL user exists and that the password in `DatabaseConnection.java` matches:

```bash
sudo mysql -e "SELECT user, host FROM mysql.user WHERE user = 'clms_app';"
```

### API returns HTTP 500

Start the API from the project root after compiling. If it was already running before a code or credential change, stop it with `Ctrl+C` and restart it.

### Browser reports `ERR_INTERNET_DISCONNECTED`

The application does not require Google Fonts or external assets. Restart the frontend server and reload `http://localhost:8000`.

### Port already in use

Stop the old process or use another frontend port:

```bash
python3 -m http.server 8001 --directory frontend
```

The frontend API URL remains `http://localhost:8080`.

## Assessment Mapping

The PDF-ready assessment content and screenshot checklist are in `docs/SUBMISSION_REPORT.md`.

### Formative Assessment 1

- HTML5 structure: `frontend/index.html`
- CSS3 styling: `frontend/styles.css`
- JavaScript interactivity: `frontend/script.js`
- Database design: `database/schema.sql`
- Sample records: `database/sample-data.sql`
- ER model and planning: `docs/TASK_GUIDE.md`

### Formative Assessment 2

- OOP model classes: `src/model/`
- Exception handling: `src/exception/`
- JDBC integration: `src/config/` and `src/dao/`
- Business logic: `src/service/`
- Backend API: `src/api/ApiServer.java`
- Backend setup guide: `docs/PHASE2_GUIDE.md`

## Security Note

The current hardcoded `your_password` value is intended only for the local academic demo setup requested for this project. Before publishing the repository or deploying the application, move credentials to environment variables or a secrets manager and use hashed passwords instead of the sample database values.
Student:
Email: aarav@clms.edu
Password: demo-password

Lab assistant:
Email: meera@clms.edu
Password: demo-password

Admin:
Email: admin@clms.edu
Password: demo-password