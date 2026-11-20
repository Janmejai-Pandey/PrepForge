package prepforge.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Student extends User {

    private String enrollmentNumber;
    private String branch;
    private double cgpa;
    private List<Skill> skills;
    private Map<Integer, String> skillProficiency;
    private Set<Integer> appliedRoleIds;

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

    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public void showMenu() {
        System.out.println("1. Profile");
        System.out.println("2. Skills");
        System.out.println("3. Eligible Roles");
        System.out.println("4. Apply");
        System.out.println("5. Logout");
    }

    public void updateProfile(String name, String email) {
        setName(name);
        setEmail(email);
    }

    public void updateProfile(String name, String email, String branch, double cgpa) {
        setName(name);
        setEmail(email);
        this.branch = branch;
        this.cgpa = cgpa;
    }

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

    public void addSkill(Skill skill, String level) {
        if (skill != null) {
            boolean present = false;
            for (Skill s : skills) {
                if (s.getSkillId() == skill.getSkillId()) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                skills.add(skill);
            }
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

    public void applyToRole(int roleId) {
        appliedRoleIds.add(roleId);
    }

    public boolean hasAppliedToRole(int roleId) {
        return appliedRoleIds.contains(roleId);
    }

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
        return "Student{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", enrollmentNumber='" + enrollmentNumber + '\'' +
                ", branch='" + branch + '\'' +
                ", cgpa=" + cgpa +
                ", skillsCount=" + skills.size() +
                '}';
    }
}
