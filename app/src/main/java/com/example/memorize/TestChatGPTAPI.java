package com.example.memorize;

public class TestChatGPTAPI {

    public static void main(String[] args) {
        ChatGPTConnector connector = new ChatGPTConnector();
        try {
            String prompt = "Olá, mundo! Como você está hoje?";
            String response = connector.getChatGPTResponse(prompt);
            System.out.println("Resposta do ChatGPT: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
