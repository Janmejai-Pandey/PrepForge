# PrepForge

PrepForge is a JavaFX placement preparation platform. It stores live application data in a local MySQL database named `prepforge`.

## Requirements

- JDK 25
- Apache Maven 3.8 or newer
- MySQL Server 8.0 or newer

Verify Java and Maven are available in PowerShell:

```powershell
java -version
mvn -version
```

## First-time setup

1. Start the MySQL Server service.
2. From the project root, create the local database settings file if it does not already exist:

   ```powershell
   if (-not (Test-Path src\main\resources\db.properties)) {
       Copy-Item src\main\resources\db.properties.example src\main\resources\db.properties
   }
   notepad src\main\resources\db.properties
   ```

3. Set `db.user` and `db.password` in `src\main\resources\db.properties` to a MySQL account that can create databases. The file uses plain Java properties values; do not wrap the password in quotes. Keep this local file private; it is ignored by Git.
4. Launch PrepForge:

   ```powershell
   mvn clean javafx:run
   ```

   On first launch, PrepForge creates the `prepforge` database, its tables, and starter records. The seed data is imported once; subsequent launches do not reset your database.

The template defaults to `localhost:3306` and the MySQL user `root`. You can instead configure the connection with the `PREPFORGE_DB_URL`, `PREPFORGE_DB_USER`, and `PREPFORGE_DB_PASSWORD` environment variables; environment values take precedence over `db.properties`.

## Subsequent runs

Start MySQL Server, open PowerShell in the project root, and run:

```powershell
mvn javafx:run
```

Application data remains in MySQL between runs. `mvn clean` removes only generated build output, not database contents.

## Tests

Run the Maven test lifecycle from the project root:

```powershell
mvn test
```

## Persistence and migration

The runtime schema and one-time starter records are in `src/main/resources/db/`. The SQL seed script is a bootstrap resource; normal application reads and writes go to MySQL. If a legacy `prepforge_state.dat` file is present, PrepForge migrates it on startup and removes it only after successful migration.

The application does not drop existing tables. Back up and resolve any incompatible existing `prepforge` schema before launching.

## Seed data attribution

The seeded assessment questions are attributed to these sources:

| Category | Count | Source | License |
|---|---:|---|---|
| MCQ | 45 | [Open Trivia Database: Computers](https://opentdb.com/) | CC BY-SA 4.0 |
| Technical | 103 | [30 Seconds of Interviews](https://github.com/30-seconds/30-seconds-of-interviews) | CC0-1.0 |
| Coding | 63 | [LeetCode Company-Wise Problems](https://github.com/liquidslr/leetcode-company-wise-problems) | Open Source |
| HR | 29 | [Tech Interview Handbook](https://github.com/yangshun/tech-interview-handbook) | MIT |
