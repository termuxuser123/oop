# Object-Oriented Programming (OOP) & SOLID Design Document
## Library Management System

This document outlines how the **Library Management System** strictly adheres to Object-Oriented Programming (OOP) principles, SOLID architecture guidelines, and design patterns.

---

## 1. Architectural Overview

The application follows a **layered, decoupled architecture** where concerns are separated across distinct packages:

```
src/
├── model/       # Domain entities, business data, encapsulation & invariants
├── dao/         # Data Access Object interfaces and JDBC implementations
├── service/     # Core business rules, polymorphism, policies, and facade logic
├── database/    # Database connection management
└── ui/          # Presentation layer (Java Swing graphical interface)
```

---

## 2. The Four Pillars of OOP

### A. Encapsulation
Encapsulation bundles data (attributes) and methods that manipulate that data into a single unit while restricting direct access to internal state.

- **Private State & Controlled Access:** All entity fields across `model/` are declared `private` and accessed exclusively through public getters and setters.
- **Domain Invariants & Validation:**
  - **`User.java`**: Enforces format validation before setting values:
    ```java
    // Validates email syntax (@ and .) and contact number format
    if (isValidEmail(email)) {
        this.email = email;
    } else {
        throw new IllegalArgumentException("Invalid email format");
    }
    ```
  - **`LibraryItem.java`**: Guarantees physical book count consistency:
    - Invariant: `0 <= availableCopies <= totalCopies`.
    - Methods `decrementAvailable()` and `incrementAvailable()` protect against underflow and overflow.
  - **`Fine.java`**: Encapsulates fine amount computation, overdue day calculation, and paid/unpaid status.
- **Resource Encapsulation:** `DBConnectionManager.java` encapsulates database credentials and connection pooling mechanics.

---

### B. Abstraction
Abstraction hides complex implementation details and exposes only essential operations through abstract classes and interfaces.

- **Abstract Classes:**
  - **`User.java`**: Defines base user attributes while leaving role-specific behaviors abstract:
    ```java
    public abstract int getMaxBookAllowed();
    public abstract int getLoanPeriod();
    public abstract String getRole();
    ```
  - **`LibraryItem.java`**: Defines common catalog metadata (ISBN, title, author, publisher, copies) while declaring abstract methods:
    ```java
    public abstract boolean isIssuable();
    public abstract String getType();
    ```
  - **`Transaction.java`**: Abstract base class capturing transaction ID, user ID, ISBN, and date, extended by specialized transaction types.
- **Interfaces for Separation of Concerns:**
  - **`Searchable.java`**: Defines book search capabilities (`searchByTitle`, `searchByAuthor`, `searchBySubject`, `searchByIsbn`).
  - **`Borrowable.java`**: Defines lending operations (`issueBook`, `returnBook`).
  - **`Reportable.java`**: Defines system analytics (`generateInventoryReport`, `generateOverdueReport`).
  - **DAO Interfaces (`BookDAO`, `UserDAO`, `TransactionDAO`)**: Completely abstract away SQL queries and JDBC result sets from the rest of the application.

---

### C. Inheritance
Inheritance enables code reuse and models hierarchical "is-a" relationships.

- **User Hierarchy (`User` base class):**
  - `Student extends User`
  - `Faculty extends User`
  - `Admin extends User`
  - *Shared base code:* Common attributes (`userId`, `name`, `email`, `contact`) and validation logic are written once in `User` and reused via `super(...)`.
- **Item Hierarchy (`LibraryItem` base class):**
  - `TextBook extends LibraryItem`
  - `ReferenceBook extends LibraryItem`
  - `Journal extends LibraryItem`
  - *Shared base code:* Inventory tracking, cataloging fields, and shelf location management are inherited from `LibraryItem`.
- **Transaction Hierarchy (`Transaction` base class):**
  - `IssueTransaction extends Transaction` (adds `dueDate`)
  - `ReturnTransaction extends Transaction` (adds `fineAmount`)

---

### D. Polymorphism
Polymorphism allows objects of different types to be treated uniformly through common interfaces or base classes, with subtype-specific behavior executed at runtime (Dynamic Method Dispatch).

- **Role-Based Borrowing Limits & Loan Durations:**
  Calling `user.getMaxBookAllowed()` or `user.getLoanPeriod()` dispatches dynamically depending on the runtime user type:
  - `Student`: Max 3 books, 14-day loan duration.
  - `Faculty`: Max 5 books, 30-day loan duration.
  - `Admin`: Administrative role with inventory management and audit reporting access.
- **Lending Eligibility per Item Type:**
  Calling `item.isIssuable()`:
  - `TextBook`: Returns `true` (standard issue allowed).
  - `ReferenceBook`: Returns `false` (in-library reading only; checkout rejected).
  - `Journal`: Custom loan period (7 days).
- **Strategy Pattern for Fines (`FinePolicy`):**
  - Standard policy: `StandardFinePolicy` computes ₹10/day overdue.
  - Faculty policy: `FacultyFinePolicy` computes ₹5/day with a 3-day grace period.
  - Business logic invokes `finePolicy.calculateFine(overdueDays)` without needing `if-else` or `instanceof` checks.

---

## 3. SOLID Principles

| Principle | Description | Implementation in Project |
| :--- | :--- | :--- |
| **S — Single Responsibility (SRP)** | A class should have one, and only one, reason to change. | • **Models** (`User`, `LibraryItem`, `Fine`) only hold data and validate domain invariants.<br>• **DAOs** (`BookDAOImpl`, `UserDAOImpl`, `TransactionDAOImpl`) only execute SQL and map rows.<br>• **Services** (`Library`, `WaitlistManager`) only manage business rules and coordinate workflows.<br>• **UI Panels** (`BookSearchPanel`, `ManageBooksPanel`, `ReportPanel`) only handle Swing layout and button event handling. |
| **O — Open / Closed (OCP)** | Software entities should be open for extension, but closed for modification. | • Adding a new item type (e.g., `DVD` or `Magazine`) requires creating a class extending `LibraryItem` without modifying existing `Library` logic.<br>• Adding a new fine calculation (e.g., `HolidayFinePolicy`) requires implementing `FinePolicy` without altering core checkout/return routines. |
| **L — Liskov Substitution (LSP)** | Subtypes must be substitutable for their base types without breaking system correctness. | • `DashboardFrame(User u, Library lib)` and `Library.issueBook(User user, ...)` accept base `User` instances. The application functions correctly whether given a `Student`, `Faculty`, or `Admin`.<br>• Any method accepting `LibraryItem` handles `TextBook`, `ReferenceBook`, and `Journal` without type-casting or runtime errors. |
| **I — Interface Segregation (ISP)** | Clients should not be forced to depend upon interfaces that they do not use. | • Instead of a monolithic `ILibrary` interface with 30+ methods, responsibilities are split into narrow, role-specific interfaces:<br>&nbsp;&nbsp;- `Searchable` (catalog search only)<br>&nbsp;&nbsp;- `Borrowable` (issue/return only)<br>&nbsp;&nbsp;- `Reportable` (statistics and audit only)<br>• Panels depend solely on the interface they need (e.g., `ReportPanel` depends on `Reportable`). |
| **D — Dependency Inversion (DIP)** | High-level modules should depend on abstractions, not concrete implementations. | • `Library.java` depends on DAO interfaces (`BookDAO`, `UserDAO`, `TransactionDAO`), not on concrete MySQL classes.<br>• Database technologies can be swapped without touching business rules.<br>• Fine calculation depends on the abstract `FinePolicy` interface rather than a hardcoded formula. |

---

## 4. Design Patterns Implemented

1. **Strategy Pattern:**
   - Interface: `FinePolicy`
   - Concrete Strategies: `StandardFinePolicy`, `FacultyFinePolicy`
   - Benefit: Fine calculation algorithms can be swapped dynamically at runtime without altering the checkout/return lifecycle.
2. **Data Access Object (DAO) Pattern:**
   - Interfaces: `BookDAO`, `UserDAO`, `TransactionDAO`, `WaitlistDAO`, `FineDAO`
   - Implementations: `*DAOImpl`
   - Benefit: Completely decouples database queries, tables, and connection handling from business logic and the UI.
3. **Facade Pattern:**
   - Class: `Library.java`
   - Benefit: Exposes a simplified, unified API to the Swing UI layer (`issueBook`, `returnBook`, `searchBooks`, `generateReport`) while coordinating underlying subsystems (DAOs, WaitlistManager, FinePolicy, Transactions).

---

## 5. Viva / Presentation Quick Summary

> **Q: How does this project demonstrate OOP and SOLID?**
>
> *"Our Library Management System uses a layered architecture separating UI, Business Service, Persistence (DAO), and Domain Models. We used **Inheritance** and **Polymorphism** to model users (`Student`, `Faculty`, `Admin`) and items (`TextBook`, `ReferenceBook`, `Journal`) with type-specific borrowing constraints and loan periods. State is **Encapsulated** with validation invariants. We adhered to **SOLID** by segregating interfaces (`Searchable`, `Borrowable`, `Reportable`), applying the **Strategy Pattern** for fine calculations (OCP & DIP), maintaining **Single Responsibility** across classes, and ensuring safe subtype substitution (**LSP**)."*
