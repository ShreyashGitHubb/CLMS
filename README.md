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

## Phase 2 backend setup

The Java backend now includes model classes, JDBC DAOs, authentication lookup, equipment listing, borrow requests, approvals, returns, and overdue fine calculation. Java 17 is required.

MySQL Connector/J 8.4.0 is installed locally as `lib/mysql-connector-j-8.4.0.jar`. Keep this jar local because it is ignored by Git. Then compile and run from the project root:

```bash
mkdir -p lib out
javac -cp 'lib/mysql-connector-j-8.4.0.jar' -d out $(find src -name '*.java')
export CLMS_DB_URL='jdbc:mysql://localhost:3306/clms?serverTimezone=UTC'
export CLMS_DB_USER='clms_app'
export CLMS_DB_PASSWORD='your_password'
java -cp 'out:lib/mysql-connector-j-8.4.0.jar' Main
```

The backend defaults to `clms_app` and an empty password if environment variables are not provided. Do not commit database passwords or connector jars to Git. The current `Main` command lists available equipment and serves as the first JDBC smoke test.

### Run the API

Compile as above, then start the HTTP API in one terminal:

```bash
java -cp 'out:lib/mysql-connector-j-8.4.0.jar' api.ApiServer
```

Keep that process running. In a second terminal, serve the frontend:

```bash
python3 -m http.server 8000 --directory frontend
```

Open `http://localhost:8000`. The catalog calls `GET http://localhost:8080/api/equipment`, and borrow requests call `POST http://localhost:8080/api/requests`. If the API is stopped, the catalog displays its built-in Phase 1 demo data and requests show an offline message.



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

The current frontend still uses demo data from `script.js`. The Phase 2 Java layer now provides the first JDBC integration point; replacing frontend demo calls with a web/API layer is the next backend step.

I can install the JDBC driver and compile the backend for you. I cannot enter or discover your database password, so I’ll handle the non-secret setup, verify that Java sees the driver, and then tell you the one command where you must provide your local `clms_app` password.

Ran terminal command:  mkdir -p lib out && curl -fL -o lib/mysql-connector-j-8.4.0.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar && find lib -maxdepth 1 -type f -name '*.jar' -printf '%f\n'

The connector is now installed at `mysql-connector-j-8.4.0.jar`. I’m compiling with that exact jar and running the smoke test once; this will distinguish a fixed driver issue from the remaining database-user/password setup.

Ran terminal command:  rm -rf out && mkdir -p out && javac -cp 'lib/mysql-connector-j-*.jar' -d out $(find src -name '*.java' -print) && java -cp 'out:lib/mysql-connector-j-*.jar' Main

The jar is present and compilation succeeds, but the runtime error now reveals a second issue: the JDBC URL is being split after `localhost:`. That means an existing `CLMS_DB_URL` environment variable is malformed and is overriding the correct default. I’ll inspect only its safe shape, then run with a clean URL explicitly.

Ran terminal command:  printf 'CLMS_DB_URL bytes: '; printf '%s' "${CLMS_DB_URL:-<not set>}" | od -An -t x1c; printf '\nCLMS_DB_USER: %s\n' "${CLMS_DB_USER:-<not set>}"; if [[ -n "${CLMS_DB_PASSWORD:-}" ]]; then printf 'CLMS_DB_PASSWORD: set\n'; else printf 'CLMS_DB_PASSWORD: not set\n'; fi

Ran terminal command:  jar tf lib/mysql-connector-j-8.4.0.jar | grep -m 1 'com/mysql/cj/jdbc/Driver.class'

The environment is clean and the jar contains `com.mysql.cj.jdbc.Driver`, so the wildcard is the remaining suspect. I’m rerunning with the exact jar filename and also forcing the driver class to load in code, which makes the backend reliable across Java launchers and IDE configurations.

Generating patch (11 lines) in 


```

```


Ran terminal command:  rm -rf out && mkdir -p out && javac -cp lib/mysql-connector-j-8.4.0.jar -d out $(find src -name '*.java' -print) && java -cp out:lib/mysql-connector-j-8.4.0.jar Main

The JDBC driver problem is fixed: Java now loads Connector/J successfully and reaches MySQL. The remaining error is only authentication, and it confirms no password was supplied (`using password: NO`). I’m recording the exact jar path in the setup docs and running a final validation check.

Generating patch (12 lines) in 


```

```


Ran terminal command:  git diff --check && git status --short

Checked , no problems found

I installed the JDBC driver and fixed the driver-loading issue.

Installed:

```text
lib/mysql-connector-j-8.4.0.jar
```

The backend now reaches MySQL successfully. The only remaining issue is:

```text
Access denied for user 'clms_app'@'localhost'
```

Set the correct password in the same terminal:

```bash
export CLMS_DB_USER='clms_app'
export CLMS_DB_PASSWORD='the_password_for_clms_app'

rm -rf out
mkdir -p out

javac -cp lib/mysql-connector-j-8.4.0.jar \
  -d out $(find src -name '*.java')

java -cp out:lib/mysql-connector-j-8.4.0.jar Main
```

If you have not created `clms_app` yet, run:

```bash
sudo mysql
```

Then inside MySQL:

```sql
CREATE USER 'clms_app'@'localhost' IDENTIFIED BY 'choose-a-password';
GRANT ALL PRIVILEGES ON clms.* TO 'clms_app'@'localhost';
FLUSH PRIVILEGES;
```

Use that same password in `CLMS_DB_PASSWORD`.

Made changes.








Run it with two terminals.

Terminal 1:
java -cp out:lib/mysql-connector-j-8.4.0.jar api.ApiServer

Terminal 2:
python3 -m http.server 8000 --directory frontend

Open:


The API server was stopped after testing, so start it again before opening the frontend.