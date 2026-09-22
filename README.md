# College Laboratory Equipment Management System

CLMS is a Java FSD project for managing college laboratory equipment, borrowing requests, returns, and maintenance records.

## Phase 1 status

Phase 1 delivers the frontend/UI prototype and relational database design required for Formative Assessment 1:

- Responsive HTML5 dashboard screens
- CSS3 visual system
- JavaScript interactions and form validation
- Role switcher for student and lab staff views
- Equipment catalog and borrow request flow
- SQL schema and sample data
- ER documentation in `docs/TASK_GUIDE.md`

## Run the prototype

Open `frontend/index.html` in a browser, or serve the project directory with any static web server. No build tool or dependency installation is required for Phase 1.

## Project layout

```text
CLMS/
  database/
    schema.sql
    sample-data.sql
  docs/
    TASK_GUIDE.md
  frontend/
    index.html
    script.js
    styles.css
  src/
    Main.java
```

## Phase 2 direction

The static JavaScript data will be replaced with Java model, service, DAO, and JDBC layers. The SQL table names and field names are intentionally aligned with that future implementation.



## Run And Setup

## Run the Frontend

From the project root:

```bash
cd /home/shreyash/Project/Ip/miniproject/CLMS
python3 -m http.server 8000 --directory frontend
```

Open:

```text
http://localhost:8000
```

Stop the server with `Ctrl+C`.

## Setup MySQL Database

Install MySQL if required:

```bash
sudo apt update
sudo apt install mysql-server
```

Start MySQL:

```bash
sudo systemctl start mysql
```

On Ubuntu, MySQL often configures the `root` account to use Linux socket authentication. Use `sudo mysql` instead of `mysql -u root -p`:

```bash
sudo mysql < database/schema.sql
```

Insert sample users and equipment:

```bash
sudo mysql clms < database/sample-data.sql
```

Verify the setup:

```bash
sudo mysql
```

Then run:

```sql
USE clms;
SHOW TABLES;
SELECT * FROM equipment;
SELECT * FROM users;
```

If `sudo mysql` reports that the MySQL service is unavailable, start it first:

```bash
sudo systemctl start mysql
```

If you specifically need password-based access later, create a separate application user from the `sudo mysql` prompt rather than changing the local `root` authentication:

```sql
CREATE USER 'clms_app'@'localhost' IDENTIFIED BY 'choose-a-password';
GRANT ALL PRIVILEGES ON clms.* TO 'clms_app'@'localhost';
FLUSH PRIVILEGES;
```

The database scripts are `schema.sql` and `sample-data.sql`.

The current frontend uses demo data from `script.js`; it is not connected to MySQL yet. The Java file `Main.java` is still a starter placeholder. Database integration will be part of Phase 2 using Java and JDBC.