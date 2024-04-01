package com.example.memorize;

public class Flashcard {
    private String id;
    private String front;
    private String back;
    private String difficulty;
    private long lastReviewed;
    private double nextReviewInterval;

    private boolean isTypingRevision;
    private String collectionId;




    public Flashcard() {
        // Construtor vazio necessário para Firebase
    }

    // Construtor completo
    public Flashcard(String id, String front, String back, String difficulty, long lastReviewed, double nextReviewInterval, boolean isTypingRevision) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.difficulty = difficulty;
        this.lastReviewed = lastReviewed;
        this.nextReviewInterval = nextReviewInterval;
        this.isTypingRevision = isTypingRevision;
    }

    public boolean getIsTypingRevision() {
        return isTypingRevision;
    }

    public String getId() {
        return id;
    }

    public String getFront() {
        return front;
    }

    public String getBack() {
        return back;
    }

    public String getDifficulty(){ return difficulty;}

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
        return (long) nextReviewInterval;
    }

    public void setNextReviewInterval(double nextReviewInterval) {
        this.nextReviewInterval = nextReviewInterval;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }
}
