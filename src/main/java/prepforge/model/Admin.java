package prepforge.model;

public class Admin extends User {

    private String department;
    private String employeeCode;

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

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public void showMenu() {
        System.out.println("1. Manage Companies");
        System.out.println("2. Post Roles");
        System.out.println("3. View Students");
        System.out.println("4. Delete Student");
        System.out.println("5. Logout");
    }

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
        return "Admin{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", department='" + department + '\'' +
                ", employeeCode='" + employeeCode + '\'' +
                '}';
    }
}
