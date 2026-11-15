package ru.quentel.naujava.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Правило поиска и группировки событий в логах приложения.
 */
@Entity
@Table(name = "event_rules")
public class EventRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Название правила. */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /** Шаблон (подстрока или регулярное выражение) для поиска. */
    @Column(name = "pattern", nullable = false, length = 1000)
    private String pattern;

    /** Группа событий, к которой относится правило. */
    @Column(name = "group_name", length = 200)
    private String groupName;

    /** Признак включённого правила. */
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @OneToMany(mappedBy = "rule")
    private List<EventMatch> matches = new ArrayList<>();

    public EventRule() {
    }

    public EventRule(String name, String pattern, String groupName, boolean enabled, Application application) {
        this.name = name;
        this.pattern = pattern;
        this.groupName = groupName;
        this.enabled = enabled;
        this.application = application;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<EventMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<EventMatch> matches) {
        this.matches = matches;
    }

    @Override
    public String toString() {
        return "EventRule{id=" + id + ", name='" + name + "', groupName='" + groupName + "', enabled=" + enabled + "}";
    }
}