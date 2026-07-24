package com.hackhub.domain.hackathon;

import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.exception.ValidationException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "submission")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Hackathon hackathon;

    @ManyToOne
    private Team team;
    private String projectName;
    @Column(length = 1200)
    private String repositoryUrl;
    private LocalDateTime submittedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evaluation> evaluations = new ArrayList<>();

    protected Submission() {
        // For future ORM mapping.
    }

    public Submission(Hackathon hackathon, Team team, String projectName, String repositoryUrl) {
        this.hackathon = Objects.requireNonNull(hackathon, "hackathon cannot be null");
        this.team = Objects.requireNonNull(team, "team cannot be null");
        this.projectName = requireText(projectName, "projectName");
        this.repositoryUrl = requireText(repositoryUrl, "repositoryUrl");
        this.submittedAt = LocalDateTime.now();
    }

    public Evaluation addEvaluation(Judge judge, int score, String comment) {
        if (hasEvaluationFrom(judge)) {
            throw new ValidationException("Judge has already evaluated this submission");
        }
        Evaluation evaluation = new Evaluation(judge, score, comment);
        evaluations.add(evaluation);
        return evaluation;
    }

    public boolean hasEvaluationFrom(Judge judge) {
        return evaluations.stream().anyMatch(evaluation -> evaluation.getJudge().equals(judge));
    }

    public void update(String projectName, String repositoryUrl) {
        this.projectName = requireText(projectName, "projectName");
        this.repositoryUrl = requireText(repositoryUrl, "repositoryUrl");
        this.submittedAt = LocalDateTime.now();
    }

    public double averageScore() {
        return evaluations.stream()
                .mapToInt(Evaluation::getScore)
                .average()
                .orElse(0.0);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    public Long getId() {
        return id;
    }

    public Hackathon getHackathon() {
        return hackathon;
    }

    public Team getTeam() {
        return team;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public List<Evaluation> getEvaluations() {
        return Collections.unmodifiableList(evaluations);
    }
}
