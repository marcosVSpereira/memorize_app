package com.example.memorize;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class Home extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CollectionsAdapter adapter;

    private TextView textViewFirstPlace, textViewSecondPlace, textViewThirdPlace, textViewUserPosition;

    private int currentUserPosition = 0;
    private ImageView imageViewProfile;
    private BottomNavigationView bottomNav;




    @Override
    protected void onStart() {
        super.onStart();
        updateScoreDisplay(); // Atualizar a pontuação quando a atividade começa
        updateReviewButton();
    }

    @Override
    public void onBackPressed() {
        // Não chame super.onBackPressed() para evitar fechar a atividade
        // Você pode deixar vazio para não fazer nada ou adicionar outra lógica se necessário

    }






    private DatabaseReference databaseReference;

    @Override
   protected void onResume() {
        super.onResume();
        updateScoreDisplay(); // Garante que a pontuação seja atualizada quando a Home Activity for retomada
        updateReviewButton();
        updateCoinDisplay();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        loadCollections();
        updateCoinDisplay();



        imageViewProfile = findViewById(R.id.imageViewProfile);

        //BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // Código para "Início"
                } else if (itemId == R.id.nav_search) {
                    // Código para "Pesquisar"
                } else if (itemId == R.id.nav_add) {
                    // Código para "Adicionar"
                    showAddOptionsDialog();
                } else if (itemId == R.id.nav_market) {
                    // Código para "Mercado"
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(Home.this, UserProfileActivity.class);
                    startActivity(intent);
                }

                return true;
            }
        });



        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Criação do PopupMenu
                PopupMenu popup = new PopupMenu(Home.this, imageViewProfile);

                // Adicionando itens manualmente
                popup.getMenu().add(Menu.NONE, 1, 1, "Perfil");
                popup.getMenu().add(Menu.NONE, 2, 2, "Configurações");
                popup.getMenu().add(Menu.NONE, 3, 3, "Sair");

                // Adicionando evento de clique para os itens do menu
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 1: // ID para "Perfil"
                                // Ação para "Perfil"
                                // startActivity(new Intent(Home.this, ProfileActivity.class));
                                break;
                            case 2: // ID para "Configurações"
                                // Ação para "Configurações"
                                // startActivity(new Intent(Home.this, SettingsActivity.class));
                                break;
                            case 3: // ID para "Sair"
                                // Ação para "Sair"
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(Home.this, HomeActivity.class));
                                finish();
                                break;
                        }
                        return true;
                    }
                });

                popup.show(); // Mostrando o popup
            }
        });

        // Configuração do OnClickListener para o LinearLayout do ranking
        LinearLayout linearLayoutRanking = findViewById(R.id.linearlayoutRanking);
        linearLayoutRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inicie a RankingActivity
                Intent intent = new Intent(Home.this, RankingActivity.class);
                startActivity(intent);
            }
        });



        textViewUserPosition = findViewById(R.id.textViewUserPosition);




        // Inicializar RecyclerView e Adapter
        recyclerView = findViewById(R.id.recyclerViewCollections);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CollectionsAdapter(collectionsList);
        recyclerView.setAdapter(adapter);

        // Inicialização do FirebaseUser e DatabaseReference para flashcards
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Atualizar a referência do banco de dados para apontar para os flashcards do usuário
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("flashcards");

            // Adicione aqui o novo código para buscar o nickname
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        String currentUserNickname = user.getNickname();
                        loadRanking(currentUserNickname); // Chame loadRanking aqui com o nickname
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("DatabaseError", "loadUserData:onCancelled", databaseError.toException());
                }
            });

        } else {
            // Tratar o caso em que o usuário não está autenticado
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }


       /*  Obter o usuário autenticado atualmente
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Atualizar a referência do banco de dados para apontar para os flashcards do usuário
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("flashcards");
        } else {
            // Tratar o caso em que o usuário não está autenticado
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }*/

        //ImageButton buttonAddFlashcards = (ImageButton) findViewById(R.id.buttonAddFlashcards);
        Button buttonReviewFlashcards = findViewById(R.id.buttonReviewFlashcards);



        /*buttonAddFlashcards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddOptionsDialog();
            }
        });*/




        buttonReviewFlashcards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReviewOptionsDialog();
            }
        });

    }

   /* private void testChatGPTAPI() {
        ChatGPTConnector connector = new ChatGPTConnector();
        try {
            String prompt = "Responda com apenas um caracter s, para verdadeiro, e n para falso sobre a seguinte pergunta 'Como você descreveria uma tarde tranquila e relaxante?' as respostas a e b tem o mesmo sentido a) Eu diria que é um momento sereno e calmo, perfeito para descansar e descontrair. B) Eu a consideraria uma ocasião pacífica e sossegada, ideal para relaxamento e repouso.";
            new Thread(() -> {
                try {
                    String response = connector.getChatGPTResponse(prompt);
                    Log.d("ChatGPTResponse", "Resposta: " + response); // Adiciona o log aqui
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/



   private void updateReviewButton() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userCollectionsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("collections");


            userCollectionsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int count = 0;
                    int countQuizzes = 0;
                    long currentTime = System.currentTimeMillis();

                    for (DataSnapshot collectionSnapshot : dataSnapshot.getChildren()) {
                        // Conta flashcards normais e de revisão aberta
                        DataSnapshot flashcardsSnapshot = collectionSnapshot.child("flashcards");
                        for (DataSnapshot flashcardSnapshot : flashcardsSnapshot.getChildren()) {
                            Flashcard flashcard = flashcardSnapshot.getValue(Flashcard.class);
                            if (flashcard != null) {
                                long nextReviewTime = flashcard.getLastReviewed() + (long) flashcard.getNextReviewInterval();
                                if (currentTime >= nextReviewTime) {
                                    count++;
                                }
                            }
                        }

                        // Conta quizzes que devem ser revisados
                        DataSnapshot quizzesSnapshot = collectionSnapshot.child("quizzes");
                        for (DataSnapshot quizSnapshot : quizzesSnapshot.getChildren()) {
                            Quiz quiz = quizSnapshot.getValue(Quiz.class);
                            if (quiz != null && currentTime >= quiz.getLastReviewed() + quiz.getNextReviewInterval()) {
                                countQuizzes++;
                            }
                        }




                        // Aqui você pode adicionar lógica similar para contar flashcards de quiz, se necessário
                        // Por exemplo, se estiverem armazenados em um nó separado dentro de cada coleção
                    }

                    Button buttonReviewFlashcards = findViewById(R.id.buttonReviewFlashcards);
                    if (buttonReviewFlashcards != null) {
                        String buttonText = "Revisar (" + (count+countQuizzes)+ ")";
                        buttonReviewFlashcards.setText(buttonText);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("HomeActivity", "updateReviewButton:onCancelled", databaseError.toException());
                }
            });
        }
   }







    private void showReviewOptionsDialog() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference userCollectionsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("collections");

        userCollectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int countStandardReview = 0;
                int countQuizReview = 0;
                int countOpenReview = 0;
                long currentTime = System.currentTimeMillis();

                for (DataSnapshot collectionSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot flashcardSnapshot : collectionSnapshot.child("flashcards").getChildren()) {
                        Flashcard flashcard = flashcardSnapshot.getValue(Flashcard.class);
                        if (flashcard != null && currentTime >= flashcard.getLastReviewed() + (long) flashcard.getNextReviewInterval()) {
                            if (flashcard.getIsTypingRevision()) {
                                countOpenReview++;
                            } else {
                                countStandardReview++;
                            }
                        }
                    }

                    for (DataSnapshot quizSnapshot : collectionSnapshot.child("quizzes").getChildren()) {
                        Quiz quiz = quizSnapshot.getValue(Quiz.class);
                        if (quiz != null && currentTime >= quiz.getLastReviewed() + quiz.getNextReviewInterval()) {
                            countQuizReview++;
                        }
                    }
                }

                // Construir e mostrar o diálogo
                new AlertDialog.Builder(Home.this,R.style.DialogTheme)
                        .setTitle("Escolher Modo de Revisão")
                        .setItems(new String[]{
                                "Revisão Padrão (" + countStandardReview + ")",
                                "Revisão em Quiz (" + countQuizReview + ")",
                                "Revisão Aberta (" + countOpenReview + ")"
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        startActivity(new Intent(Home.this, ReviewActivity.class));
                                        break;
                                    case 1:
                                        startActivity(new Intent(Home.this, QuizActivity.class));
                                        break;
                                    case 2:
                                        startActivity(new Intent(Home.this, OpenRevisionActivity.class));
                                        break;
                                }
                            }
                        })
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("HomeActivity", "Erro ao carregar dados: " + databaseError.getMessage());
            }
        });
    }

    private void updateCoinDisplay() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.child("coins").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        int coins = dataSnapshot.getValue(Integer.class);
                        TextView textViewCoin = findViewById(R.id.textViewCoin);
                        textViewCoin.setText(String.valueOf(coins));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("UpdateCoinDisplay", "Failed to read coin value.", databaseError.toException());
                }
            });
        }
    }

    private void showAddOptionsDialog() {
        // Adicionar a nova opção no array
        String[] options = {"Criar Coleção", "Criar Flashcard", "Criar Flashcard de Revisão Aberta", "Criar Quiz"};

        new AlertDialog.Builder(this,  R.style.DialogTheme)
                .setTitle("Escolha uma opção")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showCreateCollectionDialog();
                            break;
                        case 1:
                            showAddFlashcardDialog();
                            break;
                        case 2:
                            showAddOpenRevisionFlashcardDialog(); // Método para criar flashcard de revisão aberta
                            break;
                        case 3:
                            startActivity(new Intent(Home.this, AddQuizActivity.class));
                            break;
                    }
                })
                .setNegativeButton("Cancelar", null).setOnDismissListener(dialog -> {
                    // Aqui, resetamos a seleção para o botão "Início" quando o diálogo é fechado
                    bottomNav.setSelectedItemId(R.id.nav_home);
                })
                .show();
    }

    private void showAddOpenRevisionFlashcardDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_typing_flashcard, null);
        EditText editTextFront = dialogView.findViewById(R.id.editTextFront);
        EditText editTextBack = dialogView.findViewById(R.id.editTextBack);
        Spinner spinnerCollections = dialogView.findViewById(R.id.spinnerCollections); // Supondo que você adicionou este Spinner no layout

        // Configurar o spinner com os nomes das coleções
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(collectionsMap.keySet()));
        spinnerCollections.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Adicionar Flashcard de Revisão Aberta")
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String front = editTextFront.getText().toString();
                    String back = editTextBack.getText().toString();
                    String selectedCollection = spinnerCollections.getSelectedItem().toString();
                    if (!front.isEmpty() && !back.isEmpty()) {
                        addOpenRevisionFlashcard(front, back, collectionsMap.get(selectedCollection));
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void addOpenRevisionFlashcard(String front, String back, String collectionId) {
        String flashcardId = databaseReference.push().getKey();
        Map<String, Object> flashcard = new HashMap<>();
        flashcard.put("id", flashcardId);
        flashcard.put("front", front);
        flashcard.put("back", back);
        flashcard.put("isTypingRevision", true);
        flashcard.put("lastReviewed", 0L);
        flashcard.put("nextReviewInterval", 86400000L); // 1 dia em milissegundos
        flashcard.put("collectionId", collectionId); // Adiciona o ID da coleção

        // Salva o flashcard na coleção selecionada
        DatabaseReference collectionRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("collections").child(collectionId).child("flashcards");

        collectionRef.child(flashcardId).setValue(flashcard)
                .addOnSuccessListener(aVoid -> Toast.makeText(Home.this, "Flashcard de Revisão Aberta adicionado.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Home.this, "Erro ao adicionar flashcard de Revisão Aberta.", Toast.LENGTH_SHORT).show());
    }


    private void addOpenRevisionFlashcard(String front, String back) {
        String flashcardId = databaseReference.push().getKey();
        Map<String, Object> flashcard = new HashMap<>();
        flashcard.put("id", flashcardId);
        flashcard.put("front", front);
        flashcard.put("back", back);
        flashcard.put("isTypingRevision", true); // Aqui, assumimos que é um Flashcard de Revisão Aberta
        flashcard.put("lastReviewed", 0L);
        flashcard.put("nextReviewInterval", 86400000L); // 1 dia em milissegundos

        databaseReference.child(flashcardId).setValue(flashcard)
                .addOnSuccessListener(aVoid -> Toast.makeText(Home.this, "Flashcard de Revisão Aberta adicionado.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Home.this, "Erro ao adicionar flashcard de Revisão Aberta.", Toast.LENGTH_SHORT).show());
    }



    /*private void addTypingFlashcard(String front, String back, boolean isTyping) {
        String flashcardId = databaseReference.push().getKey();
        Map<String, Object> flashcard = new HashMap<>();
        flashcard.put("id", flashcardId);
        flashcard.put("front", front);
        flashcard.put("back", back);
        flashcard.put("isTypingFlashcard", isTyping); // Adiciona a indicação se é um flashcard de revisão aberta
        flashcard.put("difficulty", "Não Avaliado");
        flashcard.put("lastReviewed", 0L);
        flashcard.put("nextReviewInterval", 86400000L);

        databaseReference.child(flashcardId).setValue(flashcard)
                .addOnSuccessListener(aVoid -> Toast.makeText(Home.this, "Flashcard adicionado.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Home.this, "Erro ao adicionar flashcard.", Toast.LENGTH_SHORT).show());
    }*/
    private void showCreateCollectionDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_collection, null);
        EditText editTextCollectionName = dialogView.findViewById(R.id.editTextCollectionName);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Criar Nova Coleção")
                .setPositiveButton("Criar", (dialog, which) -> {
                    String collectionName = editTextCollectionName.getText().toString();
                    if (!collectionName.isEmpty()) {
                        createNewCollection(collectionName);
                    } else {
                        Toast.makeText(Home.this, "Nome da coleção é obrigatório.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void createNewCollection(String collectionName) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference collectionsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("collections");

            String collectionId = collectionsRef.push().getKey(); // Gera um ID único
            Collection newCollection = new Collection(collectionId, collectionName);

            collectionsRef.child(collectionId).setValue(newCollection)
                    .addOnSuccessListener(aVoid -> Toast.makeText(Home.this, "Coleção criada com sucesso.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(Home.this, "Erro ao criar coleção.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }

    /*private void showAddTypingFlashcardDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_typing_flashcard, null);
        EditText editTextFront = dialogView.findViewById(R.id.editTextFront);
        EditText editTextBack = dialogView.findViewById(R.id.editTextBack);
        CheckBox checkBoxTyping = dialogView.findViewById(R.id.checkBoxTyping);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Adicionar Flashcard de Revisão Aberta")
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String front = editTextFront.getText().toString();
                    String back = editTextBack.getText().toString();
                    if (!front.isEmpty() && !back.isEmpty()) {
                        addTypingFlashcard(front, back, checkBoxTyping.isChecked());
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }*/


    private void showAddFlashcardDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_flashcard_with_collection, null);
        EditText editTextFront = dialogView.findViewById(R.id.editTextFront);
        EditText editTextBack = dialogView.findViewById(R.id.editTextBack);
        Spinner spinnerCollections = dialogView.findViewById(R.id.spinnerCollections);

        // Configurar o spinner com os nomes das coleções
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(collectionsMap.keySet()));
        spinnerCollections.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Adicionar Flashcard")
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String front = editTextFront.getText().toString();
                    String back = editTextBack.getText().toString();
                    String selectedCollection = spinnerCollections.getSelectedItem().toString();
                    if (!front.isEmpty() && !back.isEmpty()) {
                        addFlashcardToCollection(front, back, collectionsMap.get(selectedCollection));
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void addFlashcardToCollection(String front, String back, String collectionId) {
        String flashcardId = databaseReference.push().getKey();
        Map<String, Object> flashcard = new HashMap<>();
        flashcard.put("id", flashcardId);
        flashcard.put("front", front);
        flashcard.put("back", back);
        flashcard.put("collectionId", collectionId); // Adiciona o ID da coleção
        flashcard.put("difficulty", "Não Avaliado");
        flashcard.put("lastReviewed", 0L);
        flashcard.put("nextReviewInterval", 86400000L);

        // Aqui é onde modificamos o caminho para incluir o collectionId
        DatabaseReference collectionFlashcardsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("collections")
                .child(collectionId)
                .child("flashcards");

        collectionFlashcardsRef.child(flashcardId).setValue(flashcard)
                .addOnSuccessListener(aVoid -> Toast.makeText(Home.this, "Flashcard adicionado.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Home.this, "Erro ao adicionar flashcard.", Toast.LENGTH_SHORT).show());

        /*databaseReference.child(flashcardId).setValue(flashcard)
                .addOnSuccessListener(aVoid -> Toast.makeText(Home.this, "Flashcard adicionado.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Home.this, "Erro ao adicionar flashcard.", Toast.LENGTH_SHORT).show());
        */

    }




    private void updateScoreDisplay() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            // Adicionando um ouvinte contínuo
            userRef.child("score").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    double score = dataSnapshot.exists() ? dataSnapshot.getValue(Double.class) : 0;
                    TextView scoreView = findViewById(R.id.textViewScore);
                    scoreView.setText(String.format(Locale.US, "%.1f", score));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("UpdateScoreDisplay", "Failed to read score value.", databaseError.toException());
                }
            });
        }
    }


    private void addFlashcard(String front, String back) {
        String flashcardId = databaseReference.push().getKey();
        Map<String, Object> flashcard = new HashMap<>();
        flashcard.put("id", flashcardId);
        flashcard.put("front", front);
        flashcard.put("back", back);
        flashcard.put("difficulty", "Não Avaliado");
        flashcard.put("lastReviewed", 0L);
        flashcard.put("nextReviewInterval", 86400000L); // Exemplo: 1 dia em milissegundos

        databaseReference.child(flashcardId).setValue(flashcard)
                .addOnSuccessListener(aVoid -> Toast.makeText(Home.this, "Flashcard adicionado.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Home.this, "Erro ao adicionar flashcard.", Toast.LENGTH_SHORT).show());
    }

    private List<Collection> collectionsList = new ArrayList<>();
    private Map<String, String> collectionsMap = new HashMap<>();

    private void loadCollections() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference collectionsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("collections");

            collectionsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    collectionsList.clear();
                    collectionsMap.clear();
                    /*// Adicionar a coleção padrão
                    collectionsList.add(new Collection("default", "Padrão"));
                    collectionsMap.put("Padrão", "default");*/

                    for (DataSnapshot collectionSnapshot : dataSnapshot.getChildren()) {
                        Collection collection = collectionSnapshot.getValue(Collection.class);
                        if (collection != null) {
                            collectionsList.add(collection);
                            collectionsMap.put(collection.getName(), collection.getId());
                        }
                    }
                    // Notificar o adaptador de que os dados foram atualizados
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("HomeActivity", "loadCollections:onCancelled", databaseError.toException());
                }
            });
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRanking(String currentUserNickname) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("score").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> allUsers = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getScore() != 0.0) {
                        allUsers.add(user);
                    }
                }

                Collections.sort(allUsers, (user1, user2) -> Double.compare(user2.getScore(), user1.getScore()));

                List<User> topThreeUsers = allUsers.size() > 3 ? allUsers.subList(0, 3) : new ArrayList<>(allUsers);
               // updateRankingUI(topThreeUsers);

                currentUserPosition = 0; // Reset posição
                for (int i = 0; i < allUsers.size(); i++) {
                    if (allUsers.get(i).getNickname().equals(currentUserNickname)) {
                        currentUserPosition = i + 1;
                        Log.d("Ranking", "User position found: " + currentUserPosition);
                        break;
                    }
                }

                if (currentUserPosition != 0) {
                    textViewUserPosition.setText(String.format(Locale.getDefault(), "Sua posição: %dº Lugar", currentUserPosition));
                } else {
                    textViewUserPosition.setText("Sua posição: Fora do ranking");
                    Log.d("Ranking", "User position not found, out of ranking");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("loadRanking", "loadRanking:onCancelled", databaseError.toException());
            }
        });
    }




    private void updateRankingUI(List<User> rankedUsers) {
        // Atualiza as TextViews para os três primeiros usuários
        if (rankedUsers.size() > 0) {
            textViewFirstPlace.setText(String.format(Locale.getDefault(), "1°. %s - %.1f Pontos", rankedUsers.get(0).getNickname(), rankedUsers.get(0).getScore()));
        } else {
            textViewFirstPlace.setText("1. -");
        }

        if (rankedUsers.size() > 1) {
            textViewSecondPlace.setText(String.format(Locale.getDefault(), "2°. %s - %.1f Pontos", rankedUsers.get(1).getNickname(), rankedUsers.get(1).getScore()));
        } else {
            textViewSecondPlace.setText("2. -");
        }

        if (rankedUsers.size() > 2) {
            textViewThirdPlace.setText(String.format(Locale.getDefault(), "3°. %s - %.1f Pontos", rankedUsers.get(2).getNickname(), rankedUsers.get(2).getScore()));
        } else {
            textViewThirdPlace.setText("3. -");
        }

        // Determinar a posição e a pontuação do usuário atual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            boolean isUserInTopThree = false;

            for (int i = 0; i < rankedUsers.size(); i++) {
                User user = rankedUsers.get(i);
                if (user != null && user.getUid() != null && user.getUid().equals(currentUserId)) {
                    int userPosition = i + 1;
                    double userScore = user.getScore(); // Certifique-se de que getScore não retorne null
                    textViewUserPosition.setText(String.format(Locale.getDefault(), "Sua posição: %dº Lugar - %.1fpts", userPosition, userScore));
                   // isUserInTopThree = true;
                    break;
                }
            }

           /* if (!isUserInTopThree) {
                textViewUserPosition.setText("Sua posição: Fora do Top 3");
            }*/
        } else {
            textViewUserPosition.setText("Usuário não autenticado");
        }
    }




}
