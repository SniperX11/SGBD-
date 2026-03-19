# SGBD Lab 1 – Raport Tehnic

## 1. Decizii de Design

### Domeniu ales
Am ales un domeniu simplu și realist: **departamente de companie (Departments)** ca entitate-părinte și **angajați (Employees)** ca entitate-copil.
Relația 1-N este naturală: un departament poate angaja mai mulți angajați, iar un angajat aparține unui singur departament.  
Pentru cerința M-N am adăugat tabelele `projects` și `employee_projects` (un angajat poate lucra la mai multe proiecte, iar un proiect implică mai mulți angajați).

### Arhitectura codului
Codul este organizat în patru niveluri:

| Pachet | Responsabilitate |
|--------|-----------------|
| `com.sgbd.model` | Clase POJO pure (Department, Employee) |
| `com.sgbd.db` | `DatabaseManager` – fabrica de conexiuni JDBC |
| `com.sgbd.dao` | `DepartmentDAO`, `EmployeeDAO` – logica SQL (CRUD) |
| `com.sgbd.ui` | `MainFrame`, `DepartmentPanel`, `EmployeePanel` – interfața Swing |

Separarea clară între DAO și UI permite înlocuirea ulterioară a bazei de date sau a interfeței fără a rescrie toată aplicația.

### Alegeri tehnice
- **JDBC pur** cu `PreparedStatement` pentru toate interogările – previne SQL injection.
- **try-with-resources** pentru fiecare conexiune/statement/resultset – garantează eliberarea resurselor.
- **Maven** pentru gestionarea dependențelor și crearea fat-JAR-ului executabil.
- **PostgreSQL** ca server SQL – driver `org.postgresql:postgresql:42.7.3`.
- **Swing** cu `JSplitPane` și `JTable` – framework nativ Java, fără dependențe externe suplimentare.
- `DefaultTableModel` cu `isCellEditable = false` – tabelul este read-only; editarea se face prin formularul lateral.

---

## 2. Funcționalități Implementate

### Obligatorii
- Vizualizare tabel Departments cu selecție de rând
- Vizualizare tabel Employees filtrat automat după departamentul selectat
- Operații CRUD complete pe angajați: Add, Save (Update), Delete cu dialog de confirmare
- Validare câmpuri: required fields, format email, salary > 0, dată ISO

### Bonus (+10 puncte)
- Căutare/filtrare live în tabelul Departments (după nume)
- Căutare/filtrare live în tabelul Employees (după nume sau email)
- Buton Refresh în ambele panouri
- Sortare pe toate coloanele tabelelor (click pe header)

---

## 3. Provocări Întâmpinate și Soluții

### Conversia index vizibil ↔ index model în JTable sortat
Când utilizatorul sortează tabelul și selectează un rând, indexul vizibil diferă de indexul din `List<Employee>`.  
**Soluție:** `table.convertRowIndexToModel(viewRow)` înainte de a accesa lista.

### Mesaje de eroare prietenoase pentru excepțiile SQL
Mesajele brute JDBC (ex. `ERROR: duplicate key value violates unique constraint "employees_email_key"`) nu sunt potrivite pentru utilizatori.  
**Soluție:** metoda `friendlyMessage(SQLException)` în `EmployeePanel` detectează cuvinte-cheie (`unique`, `duplicate`, `foreign key`, `check`) și returnează mesaje clare.

### Gestionarea modurilor Add / Edit în același formular
Formularul lateral servește atât pentru inserare, cât și pentru editare.  
**Soluție:** câmpul `editingEmployeeId` (valoare `-1` = mod add, altfel = mod edit) controlează ce buton este activ și ce operație SQL se execută.

---

## 4. Ce Am Învățat

- Utilizarea JDBC la nivel scăzut: `DriverManager`, `PreparedStatement`, `ResultSet`, `Statement.RETURN_GENERATED_KEYS`
- Importanța try-with-resources pentru prevenirea scurgerilor de conexiuni (connection leaks)
- Modelul de separare DAO – Model – UI, aplicabil indiferent de framework
- Construirea unui UI Swing funcțional cu master-detail view folosind `JSplitPane` și `JTable`
- Interogări SQL parametrizate ca practică esențială de securitate (prevenire SQL injection)
- Gestionarea constrângerilor de integritate referențială și afișarea lor prietenoasă utilizatorului
