package com.hackhub.domain.hackathon;

import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.exception.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "evaluation")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Judge judge;
    private int score;
    @Column(length = 1200)
    private String comment;
    private LocalDateTime evaluatedAt;

    protected Evaluation() {
        // For future ORM mapping.
    }

    public Evaluation(Judge judge, int score, String comment) {
        if (score < 0 || score > 10) {
            throw new ValidationException("Judge score must be between 0 and 10");
        }
        if (comment == null || comment.isBlank()) {
            throw new ValidationException("Evaluation comment cannot be blank");
        }

        this.judge = Objects.requireNonNull(judge, "judge cannot be null");
        this.score = score;
        this.comment = comment.trim();
        this.evaluatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Judge getJudge() {
        return judge;
    }

    public int getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }
}
