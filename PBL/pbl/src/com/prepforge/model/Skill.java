package com.prepforge.model;

// DOMAIN CLASS: Skill
// Represents a single skill a student has or a job requires (e.g. "Java", "SQL").
public class Skill {

    // ENCAPSULATION:
    // Fields are "private" so other classes cannot change them directly.
    private String skillName;
    private String proficiencyLevel; // e.g. "Beginner", "Intermediate", "Advanced"

    // Constructor to set values when creating a new Skill
    public Skill(String skillName, String proficiencyLevel) {
        setSkillName(skillName);
        setProficiencyLevel(proficiencyLevel);
    }

    // Shortcut constructor if level is not given
    public Skill(String skillName) {
        this(skillName, "Beginner");
    }

    // Getters and Setters (Encapsulation with basic validation)
    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        if (skillName == null || skillName.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be empty!");
        }
        this.skillName = skillName;
    }

    public String getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(String proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    // toString() lets us easily print the object using System.out.println(skill)
    @Override
    public String toString() {
        return skillName + " (" + proficiencyLevel + ")";
    }
}
