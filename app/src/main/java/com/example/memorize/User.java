package com.example.memorize;

public class User {
    private String username;
    private Double score;  // Alterado para usar a classe wrapper
    private String uid;
    private String nickname; // Campo opcional
    private double coins;
    private String email;    // Campo opcional
    // Outros campos adicionais conforme necessário

    // Construtor vazio para Firebase
    public User() {
        // Inicializar campos com valores padrão
        this.username = "";
        this.score = 0.0;
        this.uid = "";
        this.nickname = "";
        this.email = "";
        // Inicializar outros campos adicionais
    }

    // Construtor completo
    public User(String username, Double score, String uid, String nickname, String email) {
        this.username = username;
        this.score = score;
        this.uid = uid;
        this.nickname = nickname;
        this.email = email;
        // Inicializar outros campos adicionais
    }

    // Getters e Setters
    public String getUsername() {
        return username;
    }

    public Double getScore() {
        return score;
    }

    public String getUid() {
        return uid;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }


    public double getCoins() {
        return coins;
    }

    public void setCoins(double coins) {
        this.coins = coins;
    }

    // Setters para outros campos adicionais
    // ...
}
