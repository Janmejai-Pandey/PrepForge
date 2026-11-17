package prepforge.model;

import java.util.Objects;

/**
 * Domain Model: Skill
 * 
 * Represents a technical or professional capability (e.g., "Java", "SQL", "Spring Boot").
 * Skills can be possessed by students or required by job roles.
 * 
 * Demonstrates:
 * 1. ENCAPSULATION: Private attributes with public getters/setters.
 * 2. OBJECT CONTRACT (equals & hashCode): Crucial for collections (Set, containsAll)
 *    when checking if a Student's skills match a JobRole's required skills.
 */
public class Skill {

    // ---------------- Private Fields ----------------
    private int skillId;
    private String skillName;
    private String category; // e.g. "Programming", "Database", "Web Development"

    // ---------------- Constructors (Overloading) ----------------
    public Skill() {
    }

    public Skill(String skillName) {
        this.skillName = skillName;
        this.category = "General";
    }

    public Skill(String skillName, String category) {
        this.skillName = skillName;
        this.category = category;
    }

    public Skill(int skillId, String skillName) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = "General";
    }

    public Skill(int skillId, String skillName, String category) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
    }

    // ---------------- Getters and Setters ----------------
    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // ---------------- equals & hashCode ----------------
    // Two skills are considered identical if they have the same ID or same name
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skill skill = (Skill) o;
        if (skillId > 0 && skill.skillId > 0) {
            return skillId == skill.skillId;
        }
        return Objects.equals(skillName != null ? skillName.toLowerCase() : null,
                              skill.skillName != null ? skill.skillName.toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        return skillId > 0 ? Objects.hash(skillId) : Objects.hash(skillName != null ? skillName.toLowerCase() : "");
    }

    @Override
    public String toString() {
        return skillName + (category != null && !category.isEmpty() ? " [" + category + "]" : "");
    }
}
