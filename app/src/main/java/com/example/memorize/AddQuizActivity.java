package com.example.memorize;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Intent;

public class AddQuizActivity extends AppCompatActivity {

    private EditText editTextQuestion, editTextCorrectAnswer, editTextIncorrectAnswer1, editTextIncorrectAnswer2, editTextIncorrectAnswer3;
    private Spinner spinnerCollections;
    private Button buttonAddQuiz;
    boolean isQuizGenerated = false;

    String correctAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quiz);

        editTextQuestion = findViewById(R.id.editTextQuizQuestion);
        editTextCorrectAnswer = findViewById(R.id.editTextCorrectAnswer);
        editTextIncorrectAnswer1 = findViewById(R.id.editTextIncorrectAnswer1);
        editTextIncorrectAnswer2 = findViewById(R.id.editTextIncorrectAnswer2);
        editTextIncorrectAnswer3 = findViewById(R.id.editTextIncorrectAnswer3);
        spinnerCollections = findViewById(R.id.spinnerCollections);
        buttonAddQuiz = findViewById(R.id.buttonAddQuiz);

        // Inicialização das Views...
        buttonAddQuiz.setText("Gerar Quiz");


        buttonAddQuiz.setOnClickListener(v -> {
            String question = editTextQuestion.getText().toString();
            correctAnswer = editTextCorrectAnswer.getText().toString();

            if (!isQuizGenerated) {
                // Primeiro clique: Gerar respostas e mudar o estado e texto do botão
                if (!question.isEmpty()) {
                    if (correctAnswer.isEmpty()) {
                        // Gerar resposta correta automaticamente
                        generateCorrectAnswer(question);
                    } else {
                        // Usar a resposta correta fornecida pelo usuário
                        generateAndDisplayFakeAnswers(question, correctAnswer);
                    }
                    buttonAddQuiz.setText("Gerando...");
                    isQuizGenerated = true;
                } else {
                    Toast.makeText(this, "Por favor, preencha a pergunta.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Segundo clique: Salvar o quiz
                saveQuiz();
            }
        });

        /*
        buttonAddQuiz.setOnClickListener(v -> {
            String question = editTextQuestion.getText().toString();
            String correctAnswer = editTextCorrectAnswer.getText().toString();


            if (!isQuizGenerated) {
                // Primeiro clique: Gerar respostas falsas e mudar o estado e texto do botão
                if (!question.isEmpty() && !correctAnswer.isEmpty()) {
                    generateAndDisplayFakeAnswers(question, correctAnswer);
                    buttonAddQuiz.setText("Adicionar Quiz");
                    isQuizGenerated = true;
                } else {
                    Toast.makeText(this, "Por favor, preencha a pergunta e a resposta correta.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Segundo clique: Salvar o quiz
                saveQuiz();
            }
        });*/



        loadCollections();
    }

    private void generateAndDisplayFakeAnswers(String question, String answer) {
        // Simulando uma chamada para gerar respostas falsas
        // Na prática, você faria uma chamada de rede ou usaria um serviço
        new Thread(() -> {
            ChatGPTConnector connector = new ChatGPTConnector();
            try {
                String prompt = "crie três opções para um quiz que sejam incorretas porem seja cabivel. as opções no modelo da resposta, ou seja sem colocar formatos que deixe diferente como numeros antes da resposta, informativo como 'questao x' 'opcao' e similares. Molde as opçoes ao mesmo formato da resposta se for 1 por exemplo gere em numero do mesmo jeito" + answer + "Para a pergunta '" + question + "'" + "Nao dê nenhuma resposta correta pois ja tenho a reposta. " + "Siga sempre a regra de nao colocar sinal nem simbolo em frente a questao pois somente reposta";;
                String response = connector.getChatGPTResponse(prompt);
                List<String> fakeAnswers = parseResponse(response);

                runOnUiThread(() -> {
                    if (fakeAnswers.size() >= 1) {
                        // Definindo as respostas falsas nos campos EditText
                        editTextIncorrectAnswer1.setVisibility(View.VISIBLE);
                        editTextIncorrectAnswer2.setVisibility(View.VISIBLE);
                        editTextIncorrectAnswer3.setVisibility(View.VISIBLE);
                        editTextIncorrectAnswer1.setText(fakeAnswers.get(0));
                        editTextIncorrectAnswer2.setText(fakeAnswers.get(1));
                        editTextIncorrectAnswer3.setText(fakeAnswers.get(2));
                        saveQuiz();
                    }else {
                        // Lide com a situação onde não há respostas suficientes
                        Toast.makeText(AddQuizActivity.this, "Não foi possível gerar respostas falsas suficientes.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Lide com a exceção
            }
        }).start();
    }

    private void generateFakeAnswers(String question) {
        // Simulando uma chamada para gerar respostas falsas
        // Na prática, você faria uma chamada de rede ou usaria um serviço
        new Thread(() -> {
            ChatGPTConnector connector = new ChatGPTConnector();
            try {
                String prompt = "crie três opções diferentes desta '" + correctAnswer  + "' para um quiz que sejam incorretas porem seja cabivel. Fazer opções no modelo da resposta, ou seja sem colocar formatos que deixe diferente como numeros antes da resposta, informativo como 'questao x' 'opcao' e similares. Molde as opçoes no mesmo formato da resposta se for 1 por exemplo gere em numero do mesmo jeito" + "Para a pergunta '" + question + "' + Nunca enumere. Siga sempre a regra de nao colocar sinal nem simbolo em frente a questao pois somente reposta"  ;
                String response = connector.getChatGPTResponse(prompt);
                List<String> fakeAnswers = parseResponse(response);

                runOnUiThread(() -> {
                    if (fakeAnswers.size() >= 1) {
                        // Definindo as respostas falsas nos campos EditText
                        editTextIncorrectAnswer1.setText(fakeAnswers.get(0));
                        editTextIncorrectAnswer2.setText(fakeAnswers.get(1));
                        editTextIncorrectAnswer3.setText(fakeAnswers.get(2));
                        saveQuiz();
                    }else {
                        // Lide com a situação onde não há respostas suficientes
                        Toast.makeText(AddQuizActivity.this, "Não foi possível gerar respostas falsas suficientes.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Lide com a exceção
            }
        }).start();
    }

    private List<String> parseResponse(String response) {
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
    }

    private void generateCorrectAnswer(String question) {
        // Iniciar uma nova Thread para realizar a chamada de rede

        new Thread(() -> {
            ChatGPTConnector connector = new ChatGPTConnector();
            try {
                // Criar o prompt para enviar à API
                String prompt = "Crie uma resposta correta resumida gerada de no maximo 70 carcteres, mas seja sempre mais curto do q isso: '" + question + "'.";

                // Fazer a chamada para a API e obter a resposta
                String response = connector.getChatGPTResponse(prompt);

                // Extrair a resposta correta do retorno da API
                String correctAnswer = parseSingleResponse(response);
                Log.d("AddQuizActivity", "Resposta correta resumida gerada de no maximo 10 carcteres: " + response);

                // Atualizar a UI no thread principal
                runOnUiThread(() -> {
                    // Configurar a resposta correta no EditText correspondente
                    editTextCorrectAnswer.setText(correctAnswer);
                    editTextCorrectAnswer.setVisibility(View.INVISIBLE);

                    // Agora gerar respostas falsas com base na resposta correta
                    generateFakeAnswers(question);
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Tratar exceções, como problemas de rede ou de resposta da API
                runOnUiThread(() -> Toast.makeText(AddQuizActivity.this, "Erro ao gerar a resposta correta.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String parseSingleResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray choicesArray = jsonResponse.getJSONArray("choices");
            if (choicesArray.length() > 0) {
                JSONObject choice = choicesArray.getJSONObject(0);
                if (choice.has("message") && choice.getJSONObject("message").has("content")) {
                    return choice.getJSONObject("message").getString("content");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("AddQuizActivity", "Erro ao analisar a resposta: " + e.getMessage());
        }
        return "";
    }

    private void saveQuiz() {
        String question = editTextQuestion.getText().toString();
        String correctAnswer = editTextCorrectAnswer.getText().toString();
        String incorrectAnswer1 = editTextIncorrectAnswer1.getText().toString();
        String incorrectAnswer2 = editTextIncorrectAnswer2.getText().toString();
        String incorrectAnswer3 = editTextIncorrectAnswer3.getText().toString();

        if (question.isEmpty() || correctAnswer.isEmpty() || incorrectAnswer1.isEmpty() || incorrectAnswer2.isEmpty() || incorrectAnswer3.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obter o ID da coleção selecionada
        String selectedCollectionName = spinnerCollections.getSelectedItem().toString();
        String collectionId = collectionsMap.get(selectedCollectionName);

        // Caminho para salvar o quiz (ajuste conforme a estrutura do seu banco de dados)
        DatabaseReference quizzesRef = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("collections").child(collectionId).child("quizzes");

        String quizId = quizzesRef.push().getKey();

        Map<String, Object> quiz = new HashMap<>();
        quiz.put("id", quizId);
        quiz.put("question", question);
        quiz.put("correctAnswer", correctAnswer);

        List<String> incorrectAnswers = new ArrayList<>();
        incorrectAnswers.add(incorrectAnswer1);
        incorrectAnswers.add(incorrectAnswer2);
        incorrectAnswers.add(incorrectAnswer3);

        if (question.isEmpty() || correctAnswer.isEmpty() || incorrectAnswer1.isEmpty() || incorrectAnswer2.isEmpty() || incorrectAnswer3.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }



        // Criar objeto quiz
        quiz.put("id", quizId);
        quiz.put("question", question);
        quiz.put("correctAnswer", correctAnswer);
        quiz.put("incorrectAnswers", Arrays.asList(incorrectAnswer1, incorrectAnswer2, incorrectAnswer3));
        quiz.put("difficulty", "Não Avaliado");
        quiz.put("lastReviewed", 0L);
        quiz.put("nextReviewInterval",86400000L); // 1 dia em milissegundos

        // Salvar no banco de dados
        quizzesRef.child(quizId).setValue(quiz)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddQuizActivity.this, "Quiz adicionado com sucesso.", Toast.LENGTH_SHORT).show();
                    Intent homeIntent = new Intent(AddQuizActivity.this, Home.class);
                    startActivity(homeIntent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddQuizActivity.this, "Erro ao adicionar quiz.", Toast.LENGTH_SHORT).show());
    }


    private Map<String, String> collectionsMap = new HashMap<>();

    private void loadCollections() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference collectionsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("collections");
            collectionsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("collections");

            collectionsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> collectionNames = new ArrayList<>();
                    collectionsMap.clear();

                    for (DataSnapshot collectionSnapshot : dataSnapshot.getChildren()) {
                        Collection collection = collectionSnapshot.getValue(Collection.class);
                        if (collection != null) {
                            collectionNames.add(collection.getName());
                            collectionsMap.put(collection.getName(), collection.getId());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddQuizActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, collectionNames);
                    spinnerCollections.setAdapter(adapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("LoadCollections", "Failed to read collections.", databaseError.toException());
                }
            });
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }
}
