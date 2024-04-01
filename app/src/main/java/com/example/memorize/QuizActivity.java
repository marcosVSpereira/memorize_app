package com.example.memorize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.List;

import javax.security.auth.callback.Callback;

public class QuizActivity extends AppCompatActivity {

    // Variáveis para componentes do layout, dados do quiz, etc.
    private TextView questionTextView;
    private RadioButton[] answerOptions;
    private Button submitButton;

    // Variáveis para manter os dados do quiz
    private List<Flashcard> flashcards = new ArrayList<>();
    private Flashcard currentFlashcard;
    private int currentQuestionIndex;
    private int currentFlashcardIndex = 0;
    private RadioGroup answerOptionsRadioGroup;
    private List<Quiz> quizzes = new ArrayList<>();
    private Quiz currentQuiz;

    boolean isCorrect;

    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        loadQuizzes();



        // Inicializar componentes do layout
        questionTextView = findViewById(R.id.questionTextView);
        answerOptions = new RadioButton[4]; // Supondo 4 opções de resposta
        submitButton = findViewById(R.id.submitButton);

        questionTextView = findViewById(R.id.questionTextView);
        answerOptions = new RadioButton[4];
        answerOptions[0] = findViewById(R.id.option1RadioButton);
        answerOptions[1] = findViewById(R.id.option2RadioButton);
        answerOptions[2] = findViewById(R.id.option3RadioButton);
        answerOptions[3] = findViewById(R.id.option4RadioButton);
        submitButton = findViewById(R.id.submitButton);


       answerOptionsRadioGroup = findViewById(R.id.answerOptionsRadioGroup);



        // Carregar flashcards e iniciar quiz
        //loadFlashcards();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        // Configure o ouvinte de clique para o botão de submissão
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });
    }


    private int currentQuizIndex = 0;

    private void loadQuizzes() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userCollectionsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("collections");

            userCollectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    quizzes.clear();
                    long currentTime = System.currentTimeMillis();

                    for (DataSnapshot collectionSnapshot : dataSnapshot.getChildren()) {
                        String collectionId = collectionSnapshot.getKey(); // Obtenha o ID da coleção
                        DataSnapshot quizzesSnapshot = collectionSnapshot.child("quizzes");
                        for (DataSnapshot quizSnapshot : quizzesSnapshot.getChildren()) {
                            Quiz quiz = quizSnapshot.getValue(Quiz.class);
                            if (quiz != null) {
                                quiz.setCollectionId(collectionId); // Defina o collectionId no objeto quiz
                                long nextReviewTime = quiz.getLastReviewed() + quiz.getNextReviewInterval();
                                if (currentTime >= nextReviewTime) {
                                    quizzes.add(quiz);
                                    Log.d("QuizActivity", "Carregado Quiz: " + quiz.toString()); // Log detalhado de cada quiz carregado
                                }
                            }
                        }
                    }

                    if (!quizzes.isEmpty()) {
                        showQuiz();
                    } else {
                        Log.d("QuizActivity", "Nenhum quiz para revisar no momento."); // Log se não houver quizzes para revisar
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("QuizActivity", "Erro ao carregar quizzes: " + databaseError.getMessage()); // Log de erro
                }
            });
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showQuiz() {
        if (currentQuizIndex < quizzes.size()) {
            currentQuiz = quizzes.get(currentQuizIndex);
            questionTextView.setText(currentQuiz.getQuestion());

            List<String> allAnswers = new ArrayList<>();
            allAnswers.addAll(currentQuiz.getIncorrectAnswers());
            allAnswers.add(currentQuiz.getCorrectAnswer());
            Collections.shuffle(allAnswers);

            // Exibir todas as opções de resposta
            for (int i = 0; i < answerOptions.length; i++) {
                if (i < allAnswers.size()) {
                    answerOptions[i].setText(allAnswers.get(i));
                    answerOptions[i].setVisibility(View.VISIBLE);
                } else {
                    answerOptions[i].setVisibility(View.GONE);
                }
            }

            currentQuizIndex++;
        } else {
            finishQuiz();
        }
    }
    private interface Callback {
        void onResponse(List<String> fakeAnswers);
    }


   /* private void generateFakeAnswers(String question, String answer, Callback callback) {
        new Thread(() -> {
            ChatGPTConnector connector = new ChatGPTConnector();
            try {
                String modifiedPrompt =  "crie três opções para um quiz que sejam incorretas porem seja cabivel. as opções no modelo da resposta, ou seja sem colocar formatos que deixe diferente como numeros antes da resposta, informativo como 'questao x' 'opcao' e similares. Molde as opçoes ao mesmo formato da resposta se for 1 por exemplo gere em numero do mesmo jeito" + answer + "Para a pergunta '" + question + "'" + "Nao dê nenhuma resposta correta pois ja tenho a reposta. " + "Siga sempre a regra de nao colocar sinal nem simbolo em frente a questao pois somente reposta";
                String response = connector.getChatGPTResponse(modifiedPrompt);
                Log.d("ChatGPTResponse", "Resposta falsa gerada: " + response); // Log da resposta

                //List<String> fakeAnswers = parseResponse(response); // Extraia as respostas do JSON

                List<String> allAnswers = parseResponse(response); // Parse as respostas falsas
                allAnswers.add(answer); // Adicione a resposta correta à lista
                Collections.shuffle(allAnswers); // Embaralhe todas as respostas


                runOnUiThread(() -> callback.onResponse(allAnswers));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }*/

   /* private List<String> parseResponse(String response) {
        List<String> fakeAnswers = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray choicesArray = jsonResponse.getJSONArray("choices");
            if (choicesArray.length() > 0) {
                String content = choicesArray.getJSONObject(0).getJSONObject("message").getString("content");
                String[] answers = content.split("\n");
                for (String answer : answers) {
                    // Remover a numeração da resposta
                    //answer = answer.replaceAll("^[-\\d)\\s]+|Opção\\s[a-c]:\\s*|Opcao\\s[a-c]:\\s*|^[a-c]\\)\\s*|^\\.*\\s*|^[\\s\\.]*|[a-c]\\)\\s*", "");
                    fakeAnswers.add(answer);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fakeAnswers;
    }*/

   /* private void showFlashcard() {
        if (currentFlashcardIndex < flashcards.size()) {
            currentFlashcard = flashcards.get(currentFlashcardIndex);
            String question = currentFlashcard.getFront();
            String answer = currentFlashcard.getBack();
            questionTextView.setText(question);

            generateFakeAnswers(question, answer, fakeAnswers -> {
                int maxOptions = answerOptions.length; // Número de RadioButtons
                for (int i = 0; i < maxOptions; i++) {
                    if (i < fakeAnswers.size()) {
                        answerOptions[i].setText(fakeAnswers.get(i));
                    } else {
                        answerOptions[i].setVisibility(View.GONE); // Esconder o RadioButton se não houver resposta
                    }
                }
            });

            currentFlashcardIndex++;
        } else {
            finishQuiz();
        }
    }*/

    // Método para verificar a resposta e exibir um Toast
    private void checkAnswer() {
        String selectedAnswer = null;
        for (RadioButton option : answerOptions) {
            if (option.isChecked()) {
                selectedAnswer = option.getText().toString();
                break;
            }
        }

        if (selectedAnswer != null) {
            isCorrect = selectedAnswer.equals(currentQuiz.getCorrectAnswer());
            updateQuizAfterAnswer(isCorrect);
            Toast.makeText(this, isCorrect ? "Resposta correta!" : "Resposta incorreta!", Toast.LENGTH_SHORT).show();
            if(isCorrect){
                playSound(R.raw.certo);
            }else{
                playSound(R.raw.errado);
            }

            showNextQuiz(); // Avança para o próximo quiz
        } else {
            Toast.makeText(this, "Por favor, selecione uma resposta.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateQuizAfterAnswer(boolean isCorrect) {
        long currentTime = System.currentTimeMillis();
        long nextInterval = isCorrect ? currentQuiz.getNextReviewInterval() * 2 : currentQuiz.getNextReviewInterval() / 2;

        currentQuiz.setLastReviewed(currentTime);
        currentQuiz.setNextReviewInterval(nextInterval);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("QuizActivity", "UserId: " + userId);
            Log.d("QuizActivity", "QuizId: " + currentQuiz.getId());
            Log.d("QuizActivity", "CollectionId: " + currentQuiz.getCollectionId());
            DatabaseReference quizRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("collections").child(currentQuiz.getCollectionId()).child("quizzes").child(currentQuiz.getId());
            quizRef.setValue(currentQuiz);
            updateScore(userId);

        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void playSound(int soundResourceId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResourceId);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.start();
    }




    private void showNextQuiz() {
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


        if (!quizzes.isEmpty()) {
            quizzes.remove(0); // Remove o quiz que acabou de ser respondido

            if (!quizzes.isEmpty()) {
                currentQuiz = quizzes.get(0); // Atualiza o currentQuiz
                displayQuiz(currentQuiz); // Atualiza a tela com o novo quiz
            } else {
                finishQuiz(); // Termina o quiz se não houver mais quizzes
            }
        } else {
            finishQuiz(); // Termina o quiz se a lista estiver vazia
        }
    }

    private void updateScore(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("score").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double currentScore = dataSnapshot.exists() ? dataSnapshot.getValue(Double.class) : 0;
                if (isCorrect) {
                    currentScore += 2;
                }  else {
                    currentScore -= 1;

                }
                userRef.child("score").setValue(currentScore);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("UpdateScore", "Failed to read score value.", databaseError.toException());
            }
        });
    }



    private void displayQuiz(Quiz quiz) {
        questionTextView.setText(quiz.getQuestion());

        List<String> allAnswers = new ArrayList<>();
        allAnswers.addAll(quiz.getIncorrectAnswers());
        allAnswers.add(quiz.getCorrectAnswer());
        Collections.shuffle(allAnswers);

        for (int i = 0; i < answerOptions.length; i++) {
            if (i < allAnswers.size()) {
                answerOptions[i].setText(allAnswers.get(i));
                answerOptions[i].setVisibility(View.VISIBLE);
            } else {
                answerOptions[i].setVisibility(View.GONE);
            }
        }

        answerOptionsRadioGroup.clearCheck(); // Reseta a seleção do RadioButton
    }




   /* private void showNextQuestion() {
        if (currentQuestionIndex < flashcards.size()) {
            currentFlashcard = flashcards.get(currentQuestionIndex);

            String question = currentFlashcard.getFront(); // Obter a pergunta
            questionTextView.setText(question);

            // Chame generateFakeAnswers aqui
            generateFakeAnswers(question, new QuizActivity.Callback() {
                // Implementação do callback
            });

            currentQuestionIndex++;
        } else {
            finishQuiz();
        }
    }*/




    private void finishQuiz() {
        // Mostrar resultados ou terminar a atividade
        // ...
        finish();
    }
}