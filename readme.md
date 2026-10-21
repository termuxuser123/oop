# Library Management System

A Java-based Library Management System built using Object-Oriented Programming (OOP), SOLID principles, DAO architecture, and Java Swing for the graphical user interface.

## Features

- Role-based users: Student, Faculty, and Admin
- Library item types: TextBook, ReferenceBook, and Journal
- Book search and inventory management
- Issue and return workflows
- Fine calculation using configurable fine policies
- Database access separated through DAO interfaces and implementations
- Swing-based graphical user interface

## Project Structure

```text
src/
├── model/       # Domain entities and validation
├── dao/         # DAO interfaces and JDBC implementations
├── service/     # Business rules and workflows
├── database/    # Database connection management
└── ui/          # Java Swing interface
```

The project may include additional files such as SQL scripts, configuration files, and external libraries depending on your local setup.

## Requirements

- Java Development Kit (JDK) compatible with the source code
- MySQL Server, if the project is configured to use MySQL
- MySQL JDBC driver
- Any additional libraries used by the project, such as BCrypt, if referenced in the code
- An IDE such as IntelliJ IDEA, Eclipse, or VS Code with Java support, or a terminal

## Database Setup

1. Start MySQL Server.
2. Create the database used by the project.
3. Run the project's SQL script, if one is provided, to create the required tables and any initial data.
4. Configure the database connection details in the project's database connection manager or configuration mechanism.

**Security:** Do not commit database passwords to Git or share them in the README. Prefer environment variables or a local, untracked configuration file. If the project uses environment variables, set the variable names expected by `DBConnectionManager` before running the application.

## How to Run the Project

### Option A: Run from an IDE

1. Open the project folder in your Java IDE.
2. Make sure the JDK is configured for the project.
3. Add the MySQL JDBC driver and any other required libraries to the project dependencies. If the project has a Maven or Gradle build file, reload the project dependencies instead.
4. Set up the database using the steps above.
5. Find the application entry-point class—the class containing `public static void main(String[] args)`.
6. Check that the database connection settings point to your local database.
7. Run the main class. If the project starts through a Swing frame, the graphical interface should open.

### Option B: Run from a Terminal

The exact commands depend on the project's folder structure and dependencies. If the Java source files are under `src/` and all required JAR files are placed in a `lib/` folder, compile from the project root.

**Linux/macOS:**

```bash
mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java")
java -cp "out:lib/*" your.package.MainClass
```

**Windows Command Prompt:**

```bat
mkdir out
dir /s /b src\*.java > sources.txt
javac -cp "lib/*" -d out @sources.txt
java -cp "out;lib/*" your.package.MainClass
```

Replace `your.package.MainClass` with the fully qualified name of the class containing `main`, and adjust the classpath if your dependencies are stored elsewhere. If the project has no `lib/` folder or uses a build tool, use the IDE or the build tool's run command instead.

## Troubleshooting

- **Database connection error:** Confirm MySQL is running and verify the database URL, database name, username, password, and port.
- **Driver not found:** Add the MySQL Connector/J JAR or the appropriate dependency to the project.
- **Missing class or package:** Compile from the project root and check the package declaration and classpath.
- **BCrypt or other dependency errors:** Add the required library to the project dependencies.
- **No GUI appears:** Confirm that you ran the correct entry-point class and that it creates or displays the main Swing window.

## Team Contributions

| Team Member | Contribution |
|---|---|
| George Tomson | Model layer |
| Antony Joseph | Service layer |
| Aparna Anil Nair | Database and DAO layer |
| Nate Kurian | GUI layer |

## OOP and Design

The project documentation describes its use of encapsulation, abstraction, inheritance, polymorphism, SOLID principles, the DAO pattern, the Strategy pattern for fine calculation, and the Facade pattern through the library service.

See [`OOP_DESIGN_UPDATED.md`](OOP_DESIGN_UPDATED.md) for the design overview.

## Notes

The exact main class, database name, SQL script filename, dependency setup, and environment-variable names were not included in the supplied design document. Confirm these against the actual source code before running the project.
