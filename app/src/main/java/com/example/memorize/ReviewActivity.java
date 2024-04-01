package com.example.memorize;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {
    private List<Flashcard> flashcards;
    private int currentFlashcardIndex = 0;

    private TextView textViewFront;
    private TextView textViewBack;
    private TextView textViewNextReviewEasy;
    private TextView textViewNextReviewMedium;
    private TextView textViewNextReviewHard;
    private Button buttonShowAnswer, buttonEasy, buttonMedium, buttonHard;
    private boolean isSwipeEnabled = false;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        initializeUI();
        flashcards = new ArrayList<>();
        loadFlashcards();
        setupButtonListeners();
        setupCardSwipe();


    }







    @SuppressLint("ClickableViewAccessibility")
    private void initializeUI() {
        textViewFront = findViewById(R.id.textViewFront);
        textViewBack = findViewById(R.id.textViewBack);
        buttonShowAnswer = findViewById(R.id.buttonShowAnswer);
        buttonEasy = findViewById(R.id.buttonEasy);
        buttonMedium = findViewById(R.id.buttonMedium);
        buttonHard = findViewById(R.id.buttonHard);

        textViewNextReviewEasy = findViewById(R.id.textViewNextReviewEasy);
        textViewNextReviewMedium = findViewById(R.id.textViewNextReviewMedium);
        textViewNextReviewHard = findViewById(R.id.textViewNextReviewHard);

        CardView cardViewFlashcard = findViewById(R.id.cardViewFlashcard);
        /*cardViewFlashcard.setOnTouchListener(new OnSwipeTouchListener(ReviewActivity.this) {
          /* @Override
            public void onSwipeTop() {
                // Implementação opcional para deslizar para baixo
            }
            @Override
            public void onSwipeRight() {
                swipeAnimation(1000, 0, "Fácil");
            }
            @Override
            public void onSwipeLeft() {
                swipeAnimation(-1000, 0, "Difícil");
            }
            @Override
            public void onSwipeBottom() {
                swipeAnimation(0, -1000, "Médio");

            }*/
       /* });*/

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
                            if (flashcard != null) {
                                long nextReviewTime = flashcard.getLastReviewed() + (long) flashcard.getNextReviewInterval();
                                if (currentTime >= nextReviewTime) {
                                    flashcards.add(flashcard);
                                    int index = flashcards.indexOf(flashcard);
                                    Log.d("ReviewActivity", "Flashcard at index " + index + ": " + flashcard.toString());
                                }
                            }
                        }
                    }
                    showFlashcard(); // Mostrar o primeiro flashcard para revisão
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ReviewActivity.this, "Erro ao carregar flashcards: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }

    /*private void loadFlashcards() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference userFlashcardsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("flashcards");

            userFlashcardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    flashcards.clear();
                    long currentTime = System.currentTimeMillis();
                    for (DataSnapshot flashcardSnapshot : dataSnapshot.getChildren()) {
                        Flashcard flashcard = flashcardSnapshot.getValue(Flashcard.class);
                        if (flashcard != null) {
                            long nextReviewTime = flashcard.getLastReviewed() + (long) flashcard.getNextReviewInterval();
                            if (currentTime >= nextReviewTime) {
                                flashcards.add(flashcard);
                                // Log the index and details of the flashcard
                                int index = flashcards.indexOf(flashcard);
                                Log.d("ReviewActivity", "Flashcard at index " + index + ": " + flashcard.toString());
                            }
                        }
                    }
                    showFlashcard();
                   // Toast.makeText(ReviewActivity.this, "Total Flashcards: " + flashcards.size(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ReviewActivity.this, "Erro ao carregar flashcards: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }*/





    private void setupButtonListeners() {
        buttonShowAnswer.setOnClickListener(v -> {
            if (textViewFront.getVisibility() == View.VISIBLE) {
                // Esconde a frente e mostra o verso do flashcard
                textViewFront.setVisibility(View.GONE);
                textViewBack.setVisibility(View.VISIBLE);
                findViewById(R.id.instruction1).setVisibility(View.VISIBLE); // Errei
                findViewById(R.id.instruction2).setVisibility(View.VISIBLE); // Acertei
                findViewById(R.id.instruction3).setVisibility(View.VISIBLE); // Ainda aprendendo
                isSwipeEnabled = true;
            } else {
                // Se o verso estiver visível, isso irá redefinir a visibilidade para mostrar a frente novamente
                textViewFront.setVisibility(View.VISIBLE);
                textViewBack.setVisibility(View.GONE);
            }
        });

        buttonEasy.setOnClickListener(v -> handleReview("Fácil"));
        buttonMedium.setOnClickListener(v -> handleReview("Médio"));
        buttonHard.setOnClickListener(v -> handleReview("Difícil"));
    }

    /*private void swipeAnimation(float translationX, float translationY, final String difficulty) {
        // Desabilitar botões antes da animação

        CardView cardViewFlashcard = findViewById(R.id.cardViewFlashcard);
        cardViewFlashcard.animate()
                .translationX(translationX)
                .translationY(translationY)
                .setDuration(300)
                .withEndAction(() -> {
                    enableButtons(); // Habilitar botões após a animação
                    // Restante do código...
                })
                .start();
    }*/

    /*private void swipeAnimation(float translationX, float translationY, final String difficulty) {
        CardView cardViewFlashcard = findViewById(R.id.cardViewFlashcard);
        cardViewFlashcard.animate()
                .translationX(translationX)
                .translationY(translationY)
                .setDuration(300)
                .withEndAction(() -> {
                    // Retornar à posição original
                    cardViewFlashcard.setTranslationX(0);
                    cardViewFlashcard.setTranslationY(0);
                    // Processar a revisão
                    handleReview(difficulty);
                    // Carregar o próximo flashcard
                    showNextFlashcard();
                })
                .start();
    }*/

    private void setupCardSwipe() {
        final CardView cardViewFlashcard = findViewById(R.id.cardViewFlashcard);
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        final int screenHeight = getResources().getDisplayMetrics().heightPixels;

        cardViewFlashcard.setOnTouchListener(new View.OnTouchListener() {
            private float initialX, initialY;
            private boolean isDragging;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isSwipeEnabled) {
                    return false; // Ignorar eventos de toque se o arrastar e soltar não estiver habilitado
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getRawX();
                        initialY = event.getRawY();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - initialX;
                        float deltaY = event.getRawY() - initialY;
                        isDragging = true;

                        // Definindo um limite para mudança de cor
                        int colorChangeThreshold = screenWidth / 6;
                        int verticalThreshold = screenHeight / 6;

                        if (deltaX > colorChangeThreshold) {
                            cardViewFlashcard.setCardBackgroundColor(getResources().getColor(R.color.green)); // Fácil
                        } else if (deltaX < -colorChangeThreshold) {
                            cardViewFlashcard.setCardBackgroundColor(getResources().getColor(R.color.red)); // Difícil
                        } else if (Math.abs(deltaY) > verticalThreshold) {
                            cardViewFlashcard.setCardBackgroundColor(getResources().getColor(R.color.orange)); // Médio
                        } else {
                            // Cor original (branco) quando perto do centro
                            cardViewFlashcard.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                        }
                        v.setTranslationX(deltaX);
                        v.setTranslationY(deltaY);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (isDragging) {
                            float finalX = v.getTranslationX();
                            float finalY = v.getTranslationY();
                            String difficulty;

                            // Ajusta a distância de disparo para considerar um deslize significativo
                            int triggerDistance = screenWidth / 4;

                            if (Math.abs(finalX) > triggerDistance || Math.abs(finalY) > triggerDistance) {
                                difficulty = finalX > 0 ? "Fácil" : (Math.abs(finalY) > Math.abs(finalX) ? "Médio" : "Difícil");

                                // Anima o cartão para fora da tela
                                float endX = finalX > 0 ? screenWidth : -screenWidth;
                                v.animate().translationX(endX).setDuration(200).withEndAction(() -> {
                                    // Resetar a posição do cartão
                                    v.setTranslationX(0);
                                    v.setTranslationY(0);

                                    // Processar a revisão e mostrar o próximo flashcard
                                    handleReview(difficulty);
                                    isSwipeEnabled=false;
                                    textViewFront.setVisibility(View.VISIBLE);
                                    findViewById(R.id.instruction1).setVisibility(View.INVISIBLE); // Errei
                                    findViewById(R.id.instruction2).setVisibility(View.INVISIBLE); // Acertei
                                    findViewById(R.id.instruction3).setVisibility(View.INVISIBLE); // Ainda aprendendo
                                    cardViewFlashcard.setCardBackgroundColor(getResources().getColor(R.color.originalColor));
                                    showNextFlashcard();
                                }).start();
                            } else {
                                // Retornar o cartão para a posição original
                                v.animate().translationX(0).translationY(0).setDuration(200).start();
                            }
                        }
                        return true;
                }
                return false;
            }
        });
    }


    private void disableButtons() {
        buttonEasy.setEnabled(false);
        buttonMedium.setEnabled(false);
        buttonHard.setEnabled(false);
    }

    private void enableButtons() {
        buttonEasy.setEnabled(true);
        buttonMedium.setEnabled(true);
        buttonHard.setEnabled(true);
    }








    private void setReviewButtonsVisibility() {
        buttonEasy.setVisibility(View.INVISIBLE);
        buttonMedium.setVisibility(View.INVISIBLE);
        buttonHard.setVisibility(View.INVISIBLE);
    }
    private void setReviewIntervalsVisibility() {
        textViewNextReviewEasy.setVisibility(View.INVISIBLE);
        textViewNextReviewMedium.setVisibility(View.INVISIBLE);
        textViewNextReviewHard.setVisibility(View.INVISIBLE);
    }


    private void resetButtonVisibility() {
        textViewBack.setVisibility(View.INVISIBLE);
        setReviewButtonsVisibility();
        setReviewIntervalsVisibility();
        buttonShowAnswer.setVisibility(View.VISIBLE);
    }


    private void handleReview(String difficulty) {
        if (!flashcards.isEmpty()) {
            updateFlashcardAfterReview(flashcards.get(0), difficulty); // Sempre pega o primeiro flashcard
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
            // Exibir um Toast com o nível de dificuldade escolhido
            Toast.makeText(this, "Avaliado como: " + difficulty, Toast.LENGTH_SHORT).show();

            flashcards.remove(0); // Remove o flashcard revisado da lista
            showNextFlashcard();





        }
    }

    private void showNextFlashcard() {
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
        if (!flashcards.isEmpty()) {
            // Se ainda houver flashcards, mostra o próximo (agora o primeiro da lista)
            Flashcard currentFlashcard = flashcards.get(0);
            textViewFront.setText(currentFlashcard.getFront());
            textViewBack.setText(currentFlashcard.getBack());
            updateReviewIntervalsDisplay(currentFlashcard);
            resetButtonVisibility();
        } else {
            // Se não houver mais flashcards, termina a atividade
            textViewFront.setVisibility(View.GONE);
            textViewBack.setVisibility(View.GONE);
            finish();
        }
    }


    private void showFlashcard() {
        // Garantir que a frente do flashcard seja visível e o verso escondido

        if (currentFlashcardIndex < flashcards.size() ) {

            Flashcard currentFlashcard = flashcards.get(currentFlashcardIndex);
            textViewFront.setText(currentFlashcard.getFront());
            textViewBack.setText(currentFlashcard.getBack());


            // Mostra o índice atual em um Toast
           //Toast.makeText(this, "Flashcard Index: " + currentFlashcardIndex, Toast.LENGTH_SHORT).show();

            updateReviewIntervalsDisplay(currentFlashcard);
            updatePredictedIntervalsDisplay(currentFlashcard);
            resetButtonVisibility();
        } else {

            finish();
        }
    }

    private void updateReviewIntervalsDisplay(Flashcard flashcard) {
        // Se for a primeira revisão, usar o intervalo inicial baseado na dificuldade
        // Caso contrário, calcular com base no último intervalo e no multiplicador
        long intervalEasy = flashcard.getLastReviewed() == 0 ? getInitialInterval("Fácil") : getNextInterval(flashcard, 2.5);
        long intervalMedium = flashcard.getLastReviewed() == 0 ? getInitialInterval("Médio") : getNextInterval(flashcard, 1.5);
        long intervalHard = flashcard.getLastReviewed() == 0 ? getInitialInterval("Difícil") : getNextInterval(flashcard, 1.1);

        textViewNextReviewEasy.setText(formatInterval(intervalEasy));
        textViewNextReviewMedium.setText(formatInterval(intervalMedium));
        textViewNextReviewHard.setText(formatInterval(intervalHard));
    }

    private String formatInterval(long interval) {
        long intervalInSeconds = interval / 1000;
        long days = intervalInSeconds / (24 * 3600);
        long hours = (intervalInSeconds % (24 * 3600)) / 3600;
        long minutes = ((intervalInSeconds % (24 * 3600)) % 3600) / 60;

        if (days > 0) {
            return days + " dia(s)";
        } else if (hours > 0) {
            return hours + " hora(s)";
        } else if (minutes > 0) {
            return minutes + " min(s)";
        } else {
            return "Agora";
        }
    }

    private long getNextInterval(Flashcard flashcard, double multiplier) {
        // Multiplicar o intervalo anterior pelo fator de dificuldade
        return (long) (flashcard.getNextReviewInterval() * multiplier);
    }

    private void playSound(int soundResourceId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResourceId);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.start();
    }


    private long getInitialInterval(String difficulty) {
        // Definir intervalos iniciais diferentes para cada nível de dificuldade
        switch (difficulty) {
            case "Fácil":
                return 2 * 24 * 60 * 60 * 1000; // 2 dias
            // 1 dia
            case "Difícil":
                return (long) (0.5 * 24 * 60 * 60 * 1000); // 12 horas
            default:
                return 24 * 60 * 60 * 1000; // Padrão 1 dia
        }
    }

    private void updatePredictedIntervalsDisplay(Flashcard flashcard) {
        long intervalEasy = flashcard.getLastReviewed() == 0 ? getInitialInterval("Fácil") : getNextInterval(flashcard, 2.5);
        long intervalMedium = flashcard.getLastReviewed() == 0 ? getInitialInterval("Médio") : getNextInterval(flashcard, 1.5);
        long intervalHard = flashcard.getLastReviewed() == 0 ? getInitialInterval("Difícil") : getNextInterval(flashcard, 1.1);

        String textEasy = formatInterval(intervalEasy);
        String textMedium = formatInterval(intervalMedium);
        String textHard = formatInterval(intervalHard);

        TextView instruction1 = findViewById(R.id.instruction1);
        TextView instruction2 = findViewById(R.id.instruction2);
        TextView instruction3 = findViewById(R.id.instruction3);

        instruction1.setText("Errei: " + textHard);
        instruction2.setText("Acertei: " + textEasy);
        instruction3.setText("Quase lá: " + textMedium);
    }

    private void updateFlashcardAfterReview(Flashcard flashcard, String difficulty) {
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

        long currentTime = System.currentTimeMillis();
        double nextInterval = getNextInterval(flashcard, multiplier);
        flashcard.setDifficulty(difficulty);
        flashcard.setLastReviewed(currentTime);
        flashcard.setNextReviewInterval((long) nextInterval);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference flashcardRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("collections")
                    .child(flashcard.getCollectionId()) // Adicione o ID da coleção aqui
                    .child("flashcards")
                    .child(flashcard.getId());
            flashcardRef.setValue(flashcard);
            flashcardRef.setValue(flashcard);

            // Atualiza a pontuação do usuário
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
                        currentScore += 0.5;
                        break;
                    case "Médio":
                        currentScore += 0;
                        break;
                    case "Difícil":
                        currentScore -= 0.5;
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

    /*private void resetButtonVisibility() {
        textViewBack.setVisibility(View.INVISIBLE);
        buttonEasy.setVisibility(View.INVISIBLE);
        buttonMedium.setVisibility(View.INVISIBLE);
        buttonHard.setVisibility(View.INVISIBLE);
        buttonShowAnswer.setVisibility(View.VISIBLE);
        textViewNextReviewEasy.setVisibility(View.INVISIBLE);
        textViewNextReviewMedium.setVisibility(View.INVISIBLE);
        textViewNextReviewHard.setVisibility(View.INVISIBLE);
    }*/

    /*private void showNextFlashcard() {
        if (currentFlashcardIndex < flashcards.size() - 1) {
            currentFlashcardIndex++;
            showFlashcard();
        } else {
            finish();
        }
    }*/
}
