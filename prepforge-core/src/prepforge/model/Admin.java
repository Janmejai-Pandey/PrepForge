package prepforge.model;

/**
 * Subclass: Admin
 * 
 * Demonstrates:
 * 1. INHERITANCE: Extends User to represent placement cell coordinators and staff.
 * 2. POLYMORPHISM: Overrides getRole() to return "ADMIN" and showMenu() for admin choices.
 * 3. ENCAPSULATION: Private fields with public getters and setters.
 */
public class Admin extends User {

    // ---------------- Admin Specific Fields ----------------
    private String department;
    private String employeeCode;

    // ---------------- Constructors (Overloading) ----------------
    public Admin() {
        super();
    }

    public Admin(String name, String email, String department, String employeeCode) {
        super(name, email);
        this.department = department;
        this.employeeCode = employeeCode;
    }

    public Admin(int id, String name, String email, String department, String employeeCode) {
        super(id, name, email);
        this.department = department;
        this.employeeCode = employeeCode;
    }

    // ---------------- Method Overriding (Polymorphism) ----------------
    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public void showMenu() {
        System.out.println("=== ADMIN / PLACEMENT CELL MENU ===");
        System.out.println("1. Manage Companies");
        System.out.println("2. Post Job Openings");
        System.out.println("3. View All Students");
        System.out.println("4. Delete Inactive Student");
        System.out.println("5. Manage System Skills");
        System.out.println("6. Logout");
    }

    // ---------------- Getters and Setters ----------------
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    @Override
    public String toString() {
        return "Admin [id=" + getId() + ", name=" + getName() + ", email=" + getEmail()
                + ", department=" + department + ", employeeCode=" + employeeCode + "]";
    }
}
