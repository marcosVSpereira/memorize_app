package com.example.memorize;

import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CollectionDetailsActivity extends AppCompatActivity {

    private TextView textViewStandardCount, textViewOpenCount, textViewQuizCount, textViewCollectionTitle;
    private String collectionId;

    @Override
    protected void onStart() {
        super.onStart();
        loadCollectionData(); // Recarrega os dados da coleção sempre que a atividade começa
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_details);

        textViewStandardCount = findViewById(R.id.textViewStandardCount);
        textViewOpenCount = findViewById(R.id.textViewOpenCount);
        textViewQuizCount = findViewById(R.id.textViewQuizCount);
        textViewCollectionTitle = findViewById(R.id.textViewCollectionTitle);






        // Receber os dados da coleção
        collectionId = getIntent().getStringExtra("collectionId");
        String collectionName = getIntent().getStringExtra("collectionName");

        // Configurar a UI com os dados da coleção
        textViewCollectionTitle.setText(collectionName);  // Exemplo



        loadCollectionData();
        setupButtonListeners();

    }



    private void setupButtonListeners() {
        Button buttonOpenRevision = findViewById(R.id.textViewOpenCount); // Certifique-se de usar os IDs corretos aqui
        Button buttonQuizzes = findViewById(R.id.textViewQuizCount);
        Button buttonStandardFlashcards = findViewById(R.id.textViewStandardCount);

        buttonOpenRevision.setOnClickListener(v -> showOptionsDialog("Flashcards de Revisão Aberta"));
        buttonQuizzes.setOnClickListener(v -> showOptionsDialog("Quizzes"));
        buttonStandardFlashcards.setOnClickListener(v -> showOptionsDialog("Flashcards Padrão"));
    }

    private void showOptionsDialog(String type) {
        CharSequence[] options = {"Revisar", "Ver Tudo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha uma opção para " + type);
        builder.setItems(options, (dialog, which) -> {
            Intent intent = null;
            if (which == 0) { // Opção Revisar
                switch (type) {
                    case "Flashcards de Revisão Aberta":
                        intent = new Intent(CollectionDetailsActivity.this, OpenRevisionActivity.class);
                        break;
                    case "Quizzes":
                        intent = new Intent(CollectionDetailsActivity.this, QuizActivity.class);
                        break;
                    case "Flashcards Padrão":
                        intent = new Intent(CollectionDetailsActivity.this, ReviewActivity.class);
                        break;
                }
                if (intent != null) {
                    intent.putExtra("collectionId", collectionId); // Passa o ID da coleção se necessário
                    startActivity(intent);
                }
            } else {
                // Opção Ver Tudo
                // Aqui você pode adicionar lógica para "Ver Tudo"
            }
        });
        builder.show();
    }


    private void loadCollectionData() {
        DatabaseReference collectionRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("collections").child(collectionId);
        long currentTime = System.currentTimeMillis();

        collectionRef.child("flashcards").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int totalFlashcards = 0;
                int flashcardsToReview = 0;
                int openToReview = 0;
                int openTotal = 0;

                for (DataSnapshot flashcardSnapshot : dataSnapshot.getChildren()) {
                    Flashcard flashcard = flashcardSnapshot.getValue(Flashcard.class);
                    if (flashcard != null) {
                        long nextReviewTime = flashcard.getLastReviewed() + flashcard.getNextReviewInterval();
                        if (Boolean.TRUE.equals(flashcard.getIsTypingRevision())) {
                            openTotal++;
                            if (currentTime >= nextReviewTime) {
                                openToReview++;
                            }
                        } else {
                            totalFlashcards++;
                            if (currentTime >= nextReviewTime) {
                                flashcardsToReview++;
                            }
                        }
                    }
                }
                textViewStandardCount.setText("Flashcards Padrão: " + flashcardsToReview + "/" + totalFlashcards);
                textViewOpenCount.setText("Flashcards de Revisão Aberta: " + openToReview + "/" + openTotal);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("CollectionDetails", "loadFlashcards:onCancelled", databaseError.toException());
            }
        });

        collectionRef.child("quizzes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int totalQuizzes = 0;
                int quizzesToReview = 0;
                // Você precisará adicionar sua lógica para determinar se um quiz precisa ser revisado.

                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    // Adicione sua lógica para determinar se um quiz precisa ser revisado
                    totalQuizzes++;
                    // if (quiz precisa ser revisado) { quizzesToReview++; }
                }
                textViewQuizCount.setText("Quizzes: " + quizzesToReview + "/" + totalQuizzes);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("CollectionDetails", "loadQuizzes:onCancelled", databaseError.toException());
            }
        });
    }
}

