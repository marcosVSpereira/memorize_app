package com.example.memorize;

import java.util.List;

public class Quiz {
    private String id;
    private String question;
    private String correctAnswer;
    private List<String> incorrectAnswers;
    private String difficulty;
    private long lastReviewed;
    private long nextReviewInterval;

    private String collectionId;

    // Construtor, getters e setters...

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    // Construtor padrão (necessário para o Firebase)
    public Quiz() {
    }

    // Construtor com parâmetros
    public Quiz(String id, String question, String correctAnswer, List<String> incorrectAnswers, String difficulty, long lastReviewed, long nextReviewInterval) {
        this.id = id;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers;
        this.difficulty = difficulty;
        this.lastReviewed = lastReviewed;
        this.nextReviewInterval = nextReviewInterval;
    }

    // Getters e setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(List<String> incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public long getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(long lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public long getNextReviewInterval() {
        return nextReviewInterval;
    }

    public void setNextReviewInterval(long nextReviewInterval) {
        this.nextReviewInterval = nextReviewInterval;
    }
}
