package prepforge.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Subclass: Student
 * 
 * Demonstrates:
 * 1. INHERITANCE: Extends User to inherit id, name, email, passwordHash.
 * 2. POLYMORPHISM (Method Overriding): Implements getRole() and showMenu().
 * 3. METHOD OVERLOADING: Multiple updateProfile() methods with different parameters.
 * 4. COLLECTIONS:
 *    - List<Skill>: Ordered collection of skills acquired by the student (Aggregation).
 *    - Map<Skill, String> / Map<Integer, String>: Maps skill to proficiency level ("Beginner", "Intermediate", "Advanced").
 *    - Set<Integer>: Stores unique job role IDs applied to (prevents duplicate applications).
 */
public class Student extends User {

    // ---------------- Student Specific Fields ----------------
    private String enrollmentNumber;
    private String branch;
    private double cgpa;

    // Aggregation: Student HAS-A list of skills
    private List<Skill> skills;

    // Map: Key = Skill ID, Value = Proficiency Level ("Beginner", "Intermediate", "Advanced")
    private Map<Integer, String> skillProficiency;

    // Set: Stores unique Job Role IDs the student has applied for
    private Set<Integer> appliedRoleIds;

    // ---------------- Constructors (Overloading) ----------------
    public Student() {
        super();
        this.skills = new ArrayList<>();
        this.skillProficiency = new HashMap<>();
        this.appliedRoleIds = new HashSet<>();
    }

    public Student(String name, String email, String enrollmentNumber, String branch, double cgpa) {
        super(name, email);
        this.enrollmentNumber = enrollmentNumber;
        this.branch = branch;
        this.cgpa = cgpa;
        this.skills = new ArrayList<>();
        this.skillProficiency = new HashMap<>();
        this.appliedRoleIds = new HashSet<>();
    }

    public Student(int id, String name, String email, String enrollmentNumber, String branch, double cgpa) {
        super(id, name, email);
        this.enrollmentNumber = enrollmentNumber;
        this.branch = branch;
        this.cgpa = cgpa;
        this.skills = new ArrayList<>();
        this.skillProficiency = new HashMap<>();
        this.appliedRoleIds = new HashSet<>();
    }

    // ---------------- Method Overriding (Polymorphism) ----------------
    @Override
    public String getRole() {
        return "STUDENT";
    }

    @Override
    public void showMenu() {
        System.out.println("=== STUDENT PORTAL MENU ===");
        System.out.println("1. View Profile");
        System.out.println("2. Update Profile");
        System.out.println("3. Manage Skills");
        System.out.println("4. View Eligible Job Openings");
        System.out.println("5. Apply for a Job Role");
        System.out.println("6. Logout");
    }

    // ---------------- Method Overloading: updateProfile ----------------
    // Version 1: Quick update for basic contact info
    public void updateProfile(String name, String email) {
        setName(name);
        setEmail(email);
    }

    // Version 2: Complete profile update including academic details
    public void updateProfile(String name, String email, String branch, double cgpa) {
        setName(name);
        setEmail(email);
        this.branch = branch;
        this.cgpa = cgpa;
    }

    // Version 3: Update from another Student object
    public void updateProfile(Student s) {
        if (s != null) {
            setName(s.getName());
            setEmail(s.getEmail());
            this.branch = s.getBranch();
            this.cgpa = s.getCgpa();
            if (s.getEnrollmentNumber() != null && !s.getEnrollmentNumber().isEmpty()) {
                this.enrollmentNumber = s.getEnrollmentNumber();
            }
        }
    }

    // ---------------- Skill Management Helper Methods ----------------
    public void addSkill(Skill skill, String level) {
        if (skill != null) {
            // Check if skill already in list to avoid duplicates
            boolean alreadyPresent = false;
            for (Skill s : skills) {
                if (s.getSkillId() == skill.getSkillId()) {
                    alreadyPresent = true;
                    break;
                }
            }
            if (!alreadyPresent) {
                skills.add(skill);
            }
            // Record proficiency in Map
            skillProficiency.put(skill.getSkillId(), level != null ? level : "Beginner");
        }
    }

    public void removeSkill(int skillId) {
        skills.removeIf(s -> s.getSkillId() == skillId);
        skillProficiency.remove(skillId);
    }

    public boolean hasSkill(int skillId) {
        return skillProficiency.containsKey(skillId);
    }

    public String getSkillLevel(int skillId) {
        return skillProficiency.getOrDefault(skillId, "Unknown");
    }

    /**
     * Returns a Map of Skill object to its proficiency level string,
     * directly fulfilling the contract: Map<Skill, String> of skill levels.
     */
    public Map<Skill, String> getSkillLevels() {
        Map<Skill, String> levels = new HashMap<>();
        for (Skill s : skills) {
            levels.put(s, skillProficiency.getOrDefault(s.getSkillId(), "Beginner"));
        }
        return levels;
    }

    public void setSkillLevels(Map<Skill, String> skillLevels) {
        if (skillLevels != null) {
            for (Map.Entry<Skill, String> entry : skillLevels.entrySet()) {
                addSkill(entry.getKey(), entry.getValue());
            }
        }
    }

    // ---------------- Application Helper Methods ----------------
    public void applyToRole(int roleId) {
        appliedRoleIds.add(roleId);
    }

    public boolean hasAppliedToRole(int roleId) {
        return appliedRoleIds.contains(roleId);
    }

    // ---------------- Getters and Setters ----------------
    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills != null ? skills : new ArrayList<>();
    }

    public Map<Integer, String> getSkillProficiency() {
        return skillProficiency;
    }

    public void setSkillProficiency(Map<Integer, String> skillProficiency) {
        this.skillProficiency = skillProficiency != null ? skillProficiency : new HashMap<>();
    }

    public Set<Integer> getAppliedRoleIds() {
        return appliedRoleIds;
    }

    public void setAppliedRoleIds(Set<Integer> appliedRoleIds) {
        this.appliedRoleIds = appliedRoleIds != null ? appliedRoleIds : new HashSet<>();
    }

    @Override
    public String toString() {
        return "Student [id=" + getId() + ", name=" + getName() + ", enrollmentNumber=" + enrollmentNumber
                + ", branch=" + branch + ", cgpa=" + cgpa + ", skillsCount=" + skills.size() + "]";
    }
}
