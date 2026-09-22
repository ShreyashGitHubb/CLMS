# Formative Assessment 1 Submission Report

## College Laboratory Equipment Management System

**Course:** Industry Practices II, Java Full Stack Development  
**Assessment:** Formative Assessment 1  
**Submission deadline:** September 22, 2026  
**Group name:** ____________________  
**Team members:** Shreyash, Aniruddha, Dishita, Sakshi

## 1. Project Objective

The College Laboratory Equipment Management System (CLMS) provides a central system for managing laboratory assets and their use by students, faculty, lab assistants, and administrators.

The system is designed to:

- Maintain an accurate equipment inventory.
- Show the current availability of laboratory assets.
- Allow students to request equipment for practical work.
- Allow lab assistants to approve requests and process returns.
- Calculate overdue fines.
- Track damaged equipment and maintenance activity.

## 2. Assessment Requirements Completed

### Frontend/UI development

- HTML5 dashboard structure
- CSS3 responsive layout and visual design
- JavaScript navigation and role switching
- Equipment search and category filtering
- Borrow request form with date validation
- Live data loading from the Java API
- Student request and return workflow
- Lab assistant approval workflow
- Mobile-friendly layout

### Database design

- Relational schema for users, equipment, transactions, and maintenance
- Primary keys and foreign keys
- Availability and quantity validation checks
- Status fields for equipment and transactions
- Indexes for common status and lookup queries
- Sample records for demonstration
- ER model in `docs/TASK_GUIDE.md`

## 3. User Roles

| Role | Main responsibilities |
|---|---|
| Student | Search equipment, submit requests, view request status, return approved equipment |
| Lab assistant | Review requests, approve issues, process returns, monitor inventory and maintenance |
| Administrator | Manage users, equipment, permissions, and system records |

## 4. Main Workflows

### Student borrowing workflow

1. Student opens the equipment catalog.
2. Student searches by name or category.
3. Student chooses an available item.
4. Student selects a future return date.
5. The system validates the date and equipment availability.
6. A `PENDING` transaction is created in the database.
7. The student can monitor the request in the Requests view.

### Lab assistant workflow

1. Lab assistant switches the dashboard role to Lab assistant.
2. The Requests view loads all live transactions.
3. The assistant approves a pending request.
4. Inventory availability decreases atomically.
5. The equipment is issued and the transaction becomes `APPROVED`.
6. On return, the assistant or student completes the return action.
7. Inventory increases and any overdue fine is calculated.

## 5. Technology Stack

- **Frontend:** HTML5, CSS3, JavaScript
- **Backend:** Java 17
- **API:** JDK built-in `HttpServer`
- **Database:** MySQL
- **Database connectivity:** JDBC with MySQL Connector/J 8.4.0
- **Development tools:** IntelliJ IDEA, VS Code, Git, GitHub

## 6. Database Entities

### Users

Stores user identity, email, password value for the academic demo, role, and creation time.

### Equipment

Stores equipment name, category, description, total quantity, available quantity, location, condition, and status.

### Borrow transactions

Stores the borrower, equipment, issue date, due date, return date, transaction status, fine amount, and approving staff member.

### Maintenance logs

Stores the equipment issue, reporter, dates, repair cost, and maintenance status.

## 7. Business Rules

- Equipment can be requested only when its available quantity is greater than zero.
- Equipment in maintenance or retired status cannot be issued.
- The due date must be after the current date.
- Approving a request reduces available quantity by one.
- Returning equipment increases available quantity by one.
- The overdue fine is `10.00` for each overdue day.
- Only authorized staff roles should approve requests or manage maintenance.

## 8. Evidence Screenshots To Include

Capture these screens after starting the API and frontend:

1. Overview dashboard as a student.
2. Equipment catalog with search and category filter.
3. Borrow request modal with selected equipment and return date.
4. Successful request visible in the Requests view.
5. Lab assistant role selected with pending request.
6. Approved request and updated inventory quantity.
7. Maintenance queue.
8. MySQL terminal showing `SHOW TABLES` and sample equipment records.

Place the screenshots in this order in one document and export it as a PDF named with the group name, for example:

```text
A12.pdf
```

## 9. How To Demonstrate The Project

From the project root:

```bash
rm -rf out
mkdir -p out
javac -cp lib/mysql-connector-j-8.4.0.jar \
  -d out $(find src -name '*.java')
```

Terminal 1:

```bash
java -cp out:lib/mysql-connector-j-8.4.0.jar api.ApiServer
```

Terminal 2:

```bash
python3 -m http.server 8000 --directory frontend
```

Open `http://localhost:8000` and complete the workflows above.

## 10. File References

- Frontend: `frontend/index.html`, `frontend/styles.css`, `frontend/script.js`
- Schema: `database/schema.sql`
- Sample data: `database/sample-data.sql`
- Project plan and ER model: `docs/TASK_GUIDE.md`
- Phase 2 architecture: `docs/PHASE2_GUIDE.md`
- Java API: `src/api/ApiServer.java`
- Java business services: `src/service/`

## 11. Conclusion

CLMS demonstrates the required frontend/UI development and database design for Formative Assessment 1. The project also includes the foundation for Formative Assessment 2 through Java OOP classes, JDBC DAOs, business services, transaction handling, and live HTTP API integration.
