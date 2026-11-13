package com.prepforge.app;

import java.util.ArrayList;
import java.util.Scanner;

import com.prepforge.exception.InvalidLoginException;
import com.prepforge.exception.RegistrationException;
import com.prepforge.model.*;
import com.prepforge.service.AuthService;

// INTERACTIVE MAIN CLASS:
// This version takes LIVE USER INPUT from the console using Scanner!
// No hardcoded sample users — you can register, log in, add skills, and apply for jobs interactively.
public class Main {

    // In-memory collections for our platform
    private static ArrayList<Company> companies = new ArrayList<>();
    private static ArrayList<JobRole> allRoles = new ArrayList<>();
    private static ArrayList<Skill> platformSkills = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AuthService authService = new AuthService();

        // Pre-load a few default companies & jobs so students have options to explore
        setupDefaultPlacementData();

        boolean running = true;

        while (running) {
            System.out.println("\n==================================================");
            System.out.println("             PREPFORGE PLACEMENT PORTAL           ");
            System.out.println("==================================================");
            System.out.println("1. Register as Student");
            System.out.println("2. Register as Admin (Placement Cell)");
            System.out.println("3. Login to Account");
            System.out.println("4. View All Companies & Open Job Roles");
            System.out.println("5. Exit");
            System.out.print("Enter your choice (1-5): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    registerStudentMenu(scanner, authService);
                    break;

                case "2":
                    registerAdminMenu(scanner, authService);
                    break;

                case "3":
                    loginMenu(scanner, authService);
                    break;

                case "4":
                    viewAllOpenings();
                    break;

                case "5":
                    System.out.println("\nThank you for using PrepForge! Good luck with placements!");
                    running = false;
                    break;

                default:
                    System.out.println(">> Invalid choice! Please enter a number between 1 and 5.");
            }
        }

        scanner.close();
    }

    // ------------------------------------------------------------------------
    // 1. REGISTER STUDENT WITH USER INPUT
    // ------------------------------------------------------------------------
    private static void registerStudentMenu(Scanner sc, AuthService authService) {
        System.out.println("\n--- [STUDENT REGISTRATION] ---");

        try {
            System.out.print("Enter User ID (e.g. STU101): ");
            String userId = sc.nextLine().trim();

            System.out.print("Enter Full Name: ");
            String name = sc.nextLine().trim();

            System.out.print("Enter Email Address (must contain @ and .): ");
            String email = sc.nextLine().trim();

            System.out.print("Enter Enrollment Number: ");
            String enrollment = sc.nextLine().trim();

            System.out.print("Enter Branch (e.g. CSE, IT, ECE): ");
            String branch = sc.nextLine().trim();

            System.out.print("Enter Current CGPA (0.0 to 10.0): ");
            double cgpa = Double.parseDouble(sc.nextLine().trim());

            System.out.print("Enter Password (at least 6 characters): ");
            String password = sc.nextLine().trim();

            // Create student using OOP Constructor
            Student student = new Student(userId, name, email, enrollment, branch, cgpa);

            // Register in AuthService (validates email uniqueness & hashes password)
            authService.registerUser(student, password);
            System.out.println(">> Student account created successfully! You can now log in.");

        } catch (NumberFormatException e) {
            System.out.println(">> Registration Failed: CGPA must be a valid number (e.g. 8.5)!");
        } catch (IllegalArgumentException e) {
            System.out.println(">> Validation Error: " + e.getMessage());
        } catch (RegistrationException e) {
            System.out.println(">> Registration Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // 2. REGISTER ADMIN WITH USER INPUT
    // ------------------------------------------------------------------------
    private static void registerAdminMenu(Scanner sc, AuthService authService) {
        System.out.println("\n--- [ADMIN / PLACEMENT OFFICER REGISTRATION] ---");

        try {
            System.out.print("Enter User ID (e.g. ADM001): ");
            String userId = sc.nextLine().trim();

            System.out.print("Enter Full Name: ");
            String name = sc.nextLine().trim();

            System.out.print("Enter Email Address: ");
            String email = sc.nextLine().trim();

            System.out.print("Enter Department (e.g. Training & Placement Cell): ");
            String department = sc.nextLine().trim();

            System.out.print("Enter Password (at least 6 characters): ");
            String password = sc.nextLine().trim();

            Admin admin = new Admin(userId, name, email, department);
            authService.registerUser(admin, password);
            System.out.println(">> Admin account created successfully! You can now log in.");

        } catch (IllegalArgumentException e) {
            System.out.println(">> Validation Error: " + e.getMessage());
        } catch (RegistrationException e) {
            System.out.println(">> Registration Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // 3. LOGIN MENU (POLYMORPHIC USER RETURN)
    // ------------------------------------------------------------------------
    private static void loginMenu(Scanner sc, AuthService authService) {
        System.out.println("\n--- [LOGIN] ---");
        System.out.print("Enter Registered Email: ");
        String email = sc.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        try {
            // POLYMORPHISM IN ACTION:
            // login() returns the abstract "User" type!
            User loggedInUser = authService.login(email, password);

            // Check what subclass was returned and show appropriate dashboard
            if (loggedInUser instanceof Student) {
                studentDashboard(sc, (Student) loggedInUser);
            } else if (loggedInUser instanceof Admin) {
                adminDashboard(sc, (Admin) loggedInUser);
            }

        } catch (InvalidLoginException e) {
            System.out.println(">> Login Failed: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // 4. STUDENT DASHBOARD (Interactive options for student)
    // ------------------------------------------------------------------------
    private static void studentDashboard(Scanner sc, Student student) {
        boolean inSession = true;

        while (inSession) {
            System.out.println("\n==============================================");
            System.out.println("         STUDENT DASHBOARD: " + student.getName().toUpperCase());
            System.out.println("==============================================");
            System.out.println("1. View My Complete Profile");
            System.out.println("2. Select Target Dream Job Role");
            System.out.println("3. Check Placement Eligibility for Target Role");
            System.out.println("4. Add a Skill to My Profile");
            System.out.println("5. Add a Mock Interview Record");
            System.out.println("6. Logout");
            System.out.print("Choose an option (1-6): ");

            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1":
                    // Polymorphic method call
                    student.displayDetails();
                    break;

                case "2":
                    selectTargetRoleMenu(sc, student);
                    break;

                case "3":
                    checkEligibility(student);
                    break;

                case "4":
                    addSkillToStudentMenu(sc, student);
                    break;

                case "5":
                    addMockInterviewMenu(sc, student);
                    break;

                case "6":
                    System.out.println(">> Logged out successfully.");
                    inSession = false;
                    break;

                default:
                    System.out.println(">> Invalid option! Please enter 1 to 6.");
            }
        }
    }

    // ------------------------------------------------------------------------
    // 5. ADMIN DASHBOARD (Interactive options for admin)
    // ------------------------------------------------------------------------
    private static void adminDashboard(Scanner sc, Admin admin) {
        boolean inSession = true;

        while (inSession) {
            System.out.println("\n==============================================");
            System.out.println("         ADMIN DASHBOARD: " + admin.getName().toUpperCase());
            System.out.println("==============================================");
            System.out.println("1. View Admin Profile");
            System.out.println("2. Add a New Hiring Company");
            System.out.println("3. Post a New Job Role for a Company");
            System.out.println("4. Create a New Platform Skill");
            System.out.println("5. View All Current Companies & Job Roles");
            System.out.println("6. Logout");
            System.out.print("Choose an option (1-6): ");

            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1":
                    admin.displayDetails();
                    break;

                case "2":
                    addNewCompanyMenu(sc, admin);
                    break;

                case "3":
                    addNewJobRoleMenu(sc, admin);
                    break;

                case "4":
                    addNewSkillMenu(sc, admin);
                    break;

                case "5":
                    viewAllOpenings();
                    break;

                case "6":
                    System.out.println(">> Logged out successfully.");
                    inSession = false;
                    break;

                default:
                    System.out.println(">> Invalid option! Please enter 1 to 6.");
            }
        }
    }

    // ------------------------------------------------------------------------
    // INTERACTIVE SUB-MENUS
    // ------------------------------------------------------------------------

    private static void selectTargetRoleMenu(Scanner sc, Student student) {
        if (allRoles.isEmpty()) {
            System.out.println(">> No job roles currently posted on the platform!");
            return;
        }

        System.out.println("\nAvailable Job Roles:");
        for (int i = 0; i < allRoles.size(); i++) {
            System.out.println((i + 1) + ". " + allRoles.get(i));
        }

        System.out.print("Select a role number (1-" + allRoles.size() + "): ");
        try {
            int selectedIndex = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (selectedIndex >= 0 && selectedIndex < allRoles.size()) {
                JobRole chosen = allRoles.get(selectedIndex);
                student.setTargetRole(chosen); // ASSOCIATION!
                System.out.println(">> Target dream job set to: " + chosen.getRoleTitle() + " at " + chosen.getCompany().getCompanyName());
            } else {
                System.out.println(">> Invalid role number!");
            }
        } catch (NumberFormatException e) {
            System.out.println(">> Please enter a valid number!");
        }
    }

    private static void checkEligibility(Student student) {
        JobRole target = student.getTargetRole();
        if (target == null) {
            System.out.println(">> You have not selected a target job role yet! Go to option 2 first.");
            return;
        }

        System.out.println("\n--- PLACEMENT ELIGIBILITY EVALUATION ---");
        System.out.println("Student Name     : " + student.getName());
        System.out.println("Your CGPA        : " + student.getCgpa());
        System.out.println("Target Job Role  : " + target.getRoleTitle() + " (" + target.getCompany().getCompanyName() + ")");
        System.out.println("Required Min CGPA: " + target.getMinCgpa());

        if (student.isEligibleForTargetRole()) {
            System.out.println("STATUS: [ELIGIBLE] Your CGPA satisfies the company cutoff!");
        } else {
            double deficit = target.getMinCgpa() - student.getCgpa();
            System.out.printf("STATUS: [NOT ELIGIBLE] You need %.2f higher CGPA to qualify for this role.%n", deficit);
        }
    }

    private static void addSkillToStudentMenu(Scanner sc, Student student) {
        System.out.print("\nEnter Skill Name (e.g. Java, Python, SQL): ");
        String skillName = sc.nextLine().trim();

        System.out.print("Enter Proficiency Level (Beginner / Intermediate / Advanced): ");
        String level = sc.nextLine().trim();

        try {
            Skill newSkill = new Skill(skillName, level);
            student.addSkill(newSkill); // AGGREGATION!
            System.out.println(">> Added " + newSkill + " to your profile.");
        } catch (IllegalArgumentException e) {
            System.out.println(">> Error: " + e.getMessage());
        }
    }

    private static void addMockInterviewMenu(Scanner sc, Student student) {
        System.out.print("\nEnter Company Name for Mock Interview: ");
        String company = sc.nextLine().trim();

        System.out.print("Enter Role Title (e.g. Software Engineer): ");
        String role = sc.nextLine().trim();

        System.out.print("Enter Score Achieved (0 - 100): ");
        try {
            double score = Double.parseDouble(sc.nextLine().trim());
            String status = score >= 70.0 ? "Passed" : "Needs Improvement";

            InterviewRecord record = new InterviewRecord(company, role, score, status);
            student.addInterviewRecord(record); // AGGREGATION!
            System.out.println(">> Mock interview saved: " + record);

        } catch (NumberFormatException e) {
            System.out.println(">> Invalid score! Must be a number.");
        }
    }

    private static void addNewCompanyMenu(Scanner sc, Admin admin) {
        System.out.print("\nEnter Company Name: ");
        String compName = sc.nextLine().trim();

        System.out.print("Enter Company Office Location: ");
        String location = sc.nextLine().trim();

        try {
            Company newComp = new Company(compName, location);
            companies.add(newComp);
            admin.addCompany(newComp);
            System.out.println(">> Company successfully onboarded!");
        } catch (IllegalArgumentException e) {
            System.out.println(">> Error: " + e.getMessage());
        }
    }

    private static void addNewJobRoleMenu(Scanner sc, Admin admin) {
        if (companies.isEmpty()) {
            System.out.println(">> Please add at least one company first!");
            return;
        }

        System.out.println("\nSelect Company for the Job Opening:");
        for (int i = 0; i < companies.size(); i++) {
            System.out.println((i + 1) + ". " + companies.get(i).getCompanyName());
        }
        System.out.print("Enter choice (1-" + companies.size() + "): ");

        try {
            int compIdx = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (compIdx < 0 || compIdx >= companies.size()) {
                System.out.println(">> Invalid company number!");
                return;
            }

            Company chosenComp = companies.get(compIdx);

            System.out.print("Enter Job Role Title (e.g. Data Analyst): ");
            String roleTitle = sc.nextLine().trim();

            System.out.print("Enter Minimum CGPA Cutoff (0.0 to 10.0): ");
            double minCgpa = Double.parseDouble(sc.nextLine().trim());

            JobRole role = new JobRole(roleTitle, minCgpa, chosenComp);
            allRoles.add(role);
            admin.addJobRoleToCompany(chosenComp, role);
            System.out.println(">> Job Role successfully posted!");

        } catch (NumberFormatException e) {
            System.out.println(">> Invalid number input!");
        } catch (IllegalArgumentException e) {
            System.out.println(">> Error: " + e.getMessage());
        }
    }

    private static void addNewSkillMenu(Scanner sc, Admin admin) {
        System.out.print("\nEnter Skill Name: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter Default Level: ");
        String level = sc.nextLine().trim();

        try {
            Skill s = new Skill(name, level);
            platformSkills.add(s);
            admin.addSkill(s);
            System.out.println(">> Skill added to platform master curriculum!");
        } catch (IllegalArgumentException e) {
            System.out.println(">> Error: " + e.getMessage());
        }
    }

    private static void viewAllOpenings() {
        System.out.println("\n==============================================");
        System.out.println("         CURRENT COMPANIES & OPENINGS         ");
        System.out.println("==============================================");
        if (companies.isEmpty()) {
            System.out.println("No companies registered yet.");
            return;
        }

        for (Company c : companies) {
            System.out.println("\n* Company: " + c.getCompanyName() + " [" + c.getLocation() + "]");
            if (c.getJobRoles().isEmpty()) {
                System.out.println("    No active openings posted yet.");
            } else {
                for (JobRole r : c.getJobRoles()) {
                    System.out.println("    - " + r.getRoleTitle() + " (Min CGPA: " + r.getMinCgpa() + ")");
                }
            }
        }
        System.out.println("==============================================\n");
    }

    // Pre-populate some starter data so the platform isn't empty when launched
    private static void setupDefaultPlacementData() {
        Company google = new Company("Google", "Bangalore");
        Company microsoft = new Company("Microsoft", "Hyderabad");
        Company tcs = new Company("Tata Consultancy Services", "Pune / Mumbai");

        JobRole sdeGoogle = new JobRole("Software Development Engineer", 8.0, google);
        sdeGoogle.addSkill(new Skill("Java", "Advanced"));
        sdeGoogle.addSkill(new Skill("Data Structures", "Intermediate"));
        google.addJobRole(sdeGoogle);

        JobRole cloudMs = new JobRole("Cloud Support Engineer", 7.5, microsoft);
        cloudMs.addSkill(new Skill("Networking", "Intermediate"));
        microsoft.addJobRole(cloudMs);

        JobRole ninjaTcs = new JobRole("Ninja Developer", 6.5, tcs);
        ninjaTcs.addSkill(new Skill("C/C++", "Intermediate"));
        tcs.addJobRole(ninjaTcs);

        companies.add(google);
        companies.add(microsoft);
        companies.add(tcs);

        allRoles.add(sdeGoogle);
        allRoles.add(cloudMs);
        allRoles.add(ninjaTcs);
    }
}
