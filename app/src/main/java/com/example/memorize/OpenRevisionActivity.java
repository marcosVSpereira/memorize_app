package com.example.memorize;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OpenRevisionActivity extends AppCompatActivity {




    private List<Flashcard> flashcards = new ArrayList<>();

    private TextView textViewQuestion;

    private TextView textViewCorrectAnswer;
    private EditText editTextAnswer;
    private Button buttonSubmit;
    private Flashcard currentFlashcard;
    private String responseChar;
    private ActivityResultLauncher<android.content.Intent> speechResultLauncher;

    private String difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_revision);

        textViewQuestion = findViewById(R.id.textViewQuestion);
        editTextAnswer = findViewById(R.id.editTextAnswer);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewCorrectAnswer = findViewById(R.id.textViewCorrectAnswer);

        ImageButton buttonMic = findViewById(R.id.buttonMic);
        buttonMic.setOnClickListener(v -> promptSpeechInput());

        // Supondo que você passou o objeto Flashcard via Intent
        currentFlashcard = (Flashcard) getIntent().getSerializableExtra("FLASHCARD");

        if (currentFlashcard != null) {
            textViewQuestion.setText(currentFlashcard.getFront());
        }

        buttonSubmit.setOnClickListener(v -> checkAnswer());
        loadFlashcards();

        speechResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            String spokenText = matches.get(0);
                            // Faça algo com o texto falado
                            editTextAnswer.setText(spokenText);
                            checkAnswer();
                        }
                    }
                }
        );

    }

    private static final int REQ_CODE_SPEECH_INPUT = 100;

    private void promptSpeechInput() {
        android.content.Intent intent = new android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_now));
        try {
            speechResultLauncher.launch(intent);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Adiciona um log para verificar se o método está sendo chamado
        Log.d("OpenRevisionActivity", "onActivityResult called with requestCode: " + requestCode
                + ", resultCode: " + resultCode
                + ", data: " + data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            // Se o resultado estiver OK e houver dados extras, proceda com a captura do texto
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Log.d("OpenRevisionActivity", "Speech input received: " + result.get(0));
                editTextAnswer.setText(result.get(0));
                checkAnswer();
            } else {
                Log.d("OpenRevisionActivity", "Speech input failed or no data received.");
            }
        }
    }



    private void loadFlashcards() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userCollectionsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("collections");

            userCollectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    flashcards.clear();
                    long currentTime = System.currentTimeMillis();

                    for (DataSnapshot collectionSnapshot : dataSnapshot.getChildren()) {
                        String collectionId = collectionSnapshot.getKey();
                        DataSnapshot flashcardsSnapshot = collectionSnapshot.child("flashcards");

                        for (DataSnapshot flashcardSnapshot : flashcardsSnapshot.getChildren()) {
                            Flashcard flashcard = flashcardSnapshot.getValue(Flashcard.class);
                            if (flashcard != null && flashcard.getIsTypingRevision()) {
                                long nextReviewTime = flashcard.getLastReviewed() + flashcard.getNextReviewInterval();
                                if (currentTime >= nextReviewTime) {
                                    flashcards.add(flashcard);
                                    int index = flashcards.indexOf(flashcard);
                                    Log.d("ReviewActivity", "Typing Flashcard at index " + index + ": " + flashcard.toString());
                                }
                            }
                        }
                    }
                    showFlashcard(); // Aqui, você pode iniciar a revisão aberta
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(OpenRevisionActivity.this, "Erro ao carregar flashcards: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFlashcard() {
        if (!flashcards.isEmpty()) {
            currentFlashcard = flashcards.get(0); // Pega o primeiro flashcard para revisão
            textViewQuestion.setText(currentFlashcard.getFront());
            // Aqui, você pode resetar o campo de resposta, etc.
            editTextAnswer.setText("");
        } else {
            // Lidar com o caso de não haver flashcards para revisão
            Toast.makeText(this, "Nenhum flashcard para revisar.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAnswer() {
        String userAnswer = editTextAnswer.getText().toString().trim();
        compareAnswersUsingChatGPT(userAnswer, currentFlashcard.getBack());
    }

    private void compareAnswersUsingChatGPT(String userAnswer, String correctAnswer) {
        ChatGPTConnector connector = new ChatGPTConnector();
        String question = currentFlashcard != null ? currentFlashcard.getFront() : "";
      //  String prompt = "Responda com apenas 1 caracter s, para verdadeiro, e n para falso sobre a seguinte resposta '" + userAnswer + "' se é equivalante/correspondente em sentido a '" + correctAnswer + "'.";
        String prompt = "Dada a pergunta '" + question + "' a resposta do usuário '" + userAnswer +
                "' e a resposta armazenada'" +
                correctAnswer + "' se a resposta do usurio tiver de acordo com a pergunta ou a resposta armazenada responda s, caso contrario responda n, responda com 1 caracter";

        /*String prompt = "Dada a pergunta '" + question + "' a resposta do usuário '" + userAnswer +
                "', responda com apenas 1 caracter s, para verdadeiro, e n para falso se a resposta é equivalante/correspondente em sentido a '" +
                correctAnswer + "'";*/
        new Thread(() -> {
            try {
                String responseJson = connector.getChatGPTResponse(prompt);
                Log.d("ChatGPTResponse", "Resposta da API: " + responseJson);

                // Aqui você precisa extrair o "s" ou "n" da resposta JSON
                responseChar = extractResponseChar(responseJson); // Implemente este método

                runOnUiThread(() -> {
                    if (responseChar.equalsIgnoreCase("s")) {
                        Toast.makeText(OpenRevisionActivity.this, "Resposta correta!", Toast.LENGTH_SHORT).show();
                        currentFlashcard.setDifficulty("Fácil");
                        difficulty = "Fácil";
                        handleReview();
                        prepareForNextFlashcard(difficulty);
                    } else {
                        Toast.makeText(OpenRevisionActivity.this, "Resposta incorreta!", Toast.LENGTH_SHORT).show();
                        currentFlashcard.setDifficulty("Difícil");
                        difficulty = "Difícil";
                        handleReview();
                        prepareForNextFlashcard(difficulty);


                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String extractResponseChar(String jsonResponse) {
        try {
            JSONObject responseObj = new JSONObject(jsonResponse);
            JSONArray choicesArray = responseObj.getJSONArray("choices");
            if (choicesArray.length() > 0) {
                JSONObject firstChoice = choicesArray.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                return message.getString("content"); // Retorna 's' ou 'n'
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ""; // Retorna uma string vazia se não conseguir extrair a resposta
    }


    private void prepareForNextFlashcard(String difficulty) {
        if (responseChar.equalsIgnoreCase("s")) {
            updateFlashcardInDatabase(currentFlashcard, difficulty); // Atualiza no banco de dados
            loadNextFlashcard(); // Carrega o próximo flashcard
        } else {
            // Se a resposta estiver incorreta, mostrar a resposta correta e preparar o botão para o próximo flashcard
            currentFlashcard.setDifficulty("Difícil");
            Log.d("OpenRevisionActivity", "Definindo dificuldade como Difícil para: " + currentFlashcard.getId());
            updateFlashcardInDatabase(currentFlashcard, difficulty);
            textViewCorrectAnswer.setText("Resposta correta: " + currentFlashcard.getBack());
            textViewCorrectAnswer.setVisibility(View.VISIBLE);
            buttonSubmit.setText("Próximo Flashcard");
            buttonSubmit.setOnClickListener(v -> loadNextFlashcard()); // Muda o listener para carregar o próximo flashcard
        }
    }

    private void loadNextFlashcard() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // ... atualizações existentes ...

            // Agora, atualize as moedas do usuário
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        double newCoinBalance = user.getCoins() + 0.20; // Adiciona 0.2 moedas
                        userRef.child("coins").setValue(newCoinBalance);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("UpdateCoins", "Failed to read user data.", databaseError.toException());
                }
            });
        }

        if (flashcards.size() > 1) {
            flashcards.remove(0); // Remove o flashcard atual da lista
            currentFlashcard = flashcards.get(0); // Pega o próximo flashcard
            textViewQuestion.setText(currentFlashcard.getFront());
            textViewCorrectAnswer.setVisibility(View.GONE);
            editTextAnswer.setText("");
            buttonSubmit.setText("Enviar");
            buttonSubmit.setOnClickListener(v -> checkAnswer()); // Reset o listener para verificar resposta
        } else {
            // Se não houver mais flashcards, informar o usuário e talvez fechar a atividade
            Toast.makeText(this, "Revisão completa!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void playSound(int soundResourceId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResourceId);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.start();
    }

    private void handleReview() {
        if (!flashcards.isEmpty()) {
            // Tocar som baseado na dificuldade
            switch (difficulty) {
                case "Fácil":
                    playSound(R.raw.certo);
                    break;
                case "Médio":
                    playSound(R.raw.progresso);
                    break;
                case "Difícil":
                    playSound(R.raw.errado);
                    break;
            }
        }
    }


    private void updateFlashcardInDatabase(Flashcard flashcard, String difficulty) {
        // Dentro do updateFlashcardInDatabase
        Log.d("OpenRevisionActivity", "Atualizando flashcard no banco de dados: " + flashcard.getId());

        long currentTime = System.currentTimeMillis();
        long nextReviewInterval = calculateNextReviewInterval(flashcard, difficulty); // Implemente este método

        flashcard.setLastReviewed(currentTime);
        flashcard.setNextReviewInterval(nextReviewInterval);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference flashcardRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("collections")
                    .child(flashcard.getCollectionId()) // Aqui adicionamos o ID da coleção
                    .child("flashcards")
                    .child(flashcard.getId());
            flashcardRef.setValue(flashcard);

            //atualiza pontuação
            updateScore(userId, difficulty);
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }


    }

    private void updateScore(String userId, String difficulty) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("score").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double currentScore = dataSnapshot.exists() ? dataSnapshot.getValue(Double.class) : 0;
                switch (difficulty) {
                    case "Fácil":
                        currentScore += 4;
                        break;
                    case "Médio":
                        currentScore += 0;
                        break;
                    case "Difícil":
                        currentScore -= 2;
                        break;
                }
                userRef.child("score").setValue(currentScore);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("UpdateScore", "Failed to read score value.", databaseError.toException());
            }
        });
    }

    private long calculateNextReviewInterval(Flashcard flashcard, String difficulty) {
        double multiplier;
        switch (difficulty) {
            case "Fácil":
                multiplier = 2.5;
                break;
            case "Médio":
                multiplier = 1.5;
                break;
            case "Difícil":
                multiplier = 1.1;
                break;
            default:
                multiplier = 1;
        }

        long currentInterval;
        if (flashcard.getLastReviewed() == 0) {
            currentInterval = getInitialInterval(difficulty);
        } else {
            currentInterval = flashcard.getNextReviewInterval();
        }
        return (long) (currentInterval * multiplier);
    }

    private long getInitialInterval(String difficulty) {
        switch (difficulty) {
            case "Fácil":
                return 2 * 24 * 60 * 60 * 1000; // 2 dias
            case "Difícil":
                return (long) (0.5 * 24 * 60 * 60 * 1000); // 12 horas
            default:
                return 24 * 60 * 60 * 1000; // Padrão 1 dia
        }
    }
}