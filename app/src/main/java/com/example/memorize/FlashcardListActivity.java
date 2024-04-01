package com.example.memorize;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class FlashcardListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FlashcardAdapter adapter; // Substitua pelo nome do seu adapter
    private List<Flashcard> flashcards = new ArrayList<>();
    private String collectionId, category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_list); // Substitua pelo seu layout

        recyclerView = findViewById(R.id.recyclerViewFlashcards); // Substitua pelo ID do seu RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicialize seu adapter aqui
        adapter = new FlashcardAdapter(flashcards); // Substitua pelo seu adapter
        recyclerView.setAdapter(adapter);

        collectionId = getIntent().getStringExtra("collectionId");
        category = getIntent().getStringExtra("category");

        loadFlashcards();
    }

    private void loadFlashcards() {
        DatabaseReference collectionRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("collections").child(collectionId);

        collectionRef.child("flashcards").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                flashcards.clear();
                for (DataSnapshot flashcardSnapshot : dataSnapshot.getChildren()) {
                    Flashcard flashcard = flashcardSnapshot.getValue(Flashcard.class);
                    if (flashcard != null && matchesCategory(flashcard)) {
                        flashcards.add(flashcard);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Tratamento de erro
            }
        });
    }

    private boolean matchesCategory(Flashcard flashcard) {
        // Implemente a lógica para verificar se o flashcard corresponde à categoria
        // Por exemplo, pode ser um simples verificador de tipo de revisão
        return true; // Retorna verdadeiro se corresponder à categoria, falso caso contrário
    }
}
