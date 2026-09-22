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
