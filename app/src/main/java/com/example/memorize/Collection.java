package com.example.memorize;

public class Collection {
    private String id;
    private String name;

    public Collection() {
        // Construtor vazio necessário para Firebase
    }



    public Collection(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters e setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
