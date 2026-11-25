package prepforge.model;

import java.util.Objects;

public class Skill implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int skillId;
    private String skillName;
    private String category;

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

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getId() {
        return skillId;
    }

    public void setId(int id) {
        this.skillId = id;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getName() {
        return skillName;
    }

    public void setName(String name) {
        this.skillName = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

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
