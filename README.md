# SGBD Lab 1 – Parent-Child Desktop Application

A Java Swing desktop application that manages **Departments** (parent) and **Employees** (child) stored in a PostgreSQL database.  
All database access uses plain JDBC with `PreparedStatement` – no ORM.

---

## Prerequisites

| Tool | Minimum version |
|------|----------------|
| Java JDK | 11 |
| Apache Maven | 3.6 |
| PostgreSQL | 12 |

---

## 1. Database Setup

### 1.1 Create the database

```sql
CREATE DATABASE sgbd_lab1;
```

### 1.2 Run the schema script

```bash
psql -U postgres -d sgbd_lab1 -f sql/schema.sql
```

The script creates four tables:

| Table | Description |
|-------|-------------|
| `departments` | Parent table (1-N side) |
| `employees` | Child table (N side) – linked to `departments` |
| `projects` | Extra table for M-N demonstration |
| `employee_projects` | Junction table (M-N between employees and projects) |

Sample data is also inserted automatically (4 departments, 10 employees, 3 projects).

---

## 2. Configure the Database Connection

Copy the example properties file and edit it:

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Edit `src/main/resources/db.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/sgbd_lab1
db.username=postgres
db.password=YOUR_PASSWORD
```

> The file `db.properties` is **not committed to Git** (listed in `.gitignore`).  
> Never commit real credentials.

---

## 3. Build the Application

```bash
mvn clean package -q
```

This produces a fat JAR at:

```
target/sgbd-lab1-1.0-SNAPSHOT-jar-with-dependencies.jar
```

---

## 4. Run the Application

```bash
java -jar target/sgbd-lab1-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Or from source during development:

```bash
mvn exec:java -Dexec.mainClass=com.sgbd.Main
```

---

## 5. Features

### Parent panel (Departments)
- Displays all departments in a sortable table
- Live search/filter by name as you type
- Refresh button to reload from the database

### Child panel (Employees)
- Automatically refreshes when a department is selected
- Live search/filter by name or email
- Refresh button
- Sortable columns

### CRUD operations (Employees)
| Button | Action |
|--------|--------|
| **Add** | Insert a new employee into the selected department |
| **Save** | Update the employee selected in the table |
| **Delete** | Delete the selected employee (with confirmation dialog) |
| **Clear Form** | Reset the form without saving |

### Validation
- All fields are required before saving
- Email must match a valid format
- Salary must be a positive number
- Hire Date must be in `YYYY-MM-DD` format
- User-friendly error messages for DB constraint violations

---

## 6. Project Structure

```
SGBD-/
├── pom.xml                         Maven build file
├── sql/
│   └── schema.sql                  DB schema + sample data
├── src/main/
│   ├── java/com/sgbd/
│   │   ├── Main.java               Entry point
│   │   ├── db/
│   │   │   └── DatabaseManager.java  JDBC connection factory
│   │   ├── model/
│   │   │   ├── Department.java
│   │   │   └── Employee.java
│   │   ├── dao/
│   │   │   ├── DepartmentDAO.java
│   │   │   └── EmployeeDAO.java
│   │   └── ui/
│   │       ├── MainFrame.java
│   │       ├── DepartmentPanel.java
│   │       └── EmployeePanel.java
│   └── resources/
│       ├── db.properties           (ignored by Git – create from .example)
│       └── db.properties.example
└── README.md
```
