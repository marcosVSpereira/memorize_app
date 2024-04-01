package com.example.memorize;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGPTConnector {
gi
    OkHttpClient client = new OkHttpClient();

    public String getChatGPTResponse(String prompt) throws Exception {
        MediaType mediaType = MediaType.parse("application/json");
        String json = "{\"model\": \"gpt-4\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}], \"max_tokens\": 70}";
        RequestBody body = RequestBody.create(mediaType, json);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
