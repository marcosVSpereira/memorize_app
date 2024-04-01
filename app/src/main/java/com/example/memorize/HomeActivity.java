package com.example.memorize;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        // Botão para ir para a tela de Login
        Button loginButton = findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Iniciar a atividade de Login
                Intent loginIntent = new Intent(HomeActivity.this, Login.class);
                startActivity(loginIntent);
            }
        });

        // Botão para ir para a tela de Registro
       Button registerButton = findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Iniciar a atividade de Registro
                Intent registerIntent = new Intent(HomeActivity.this, Register.class);
                startActivity(registerIntent);
            }
        });
    }

    public static class CollectionsAdapter extends RecyclerView.Adapter<CollectionsAdapter.ViewHolder> {

        private List<Collection> collections;

        public CollectionsAdapter(List<Collection> collections) {
            this.collections = collections;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Collection collection = collections.get(position);
            holder.textViewCollectionName.setText(collection.getName());
            // Configurar mais elementos da CardView aqui, se necessário
        }

        @Override
        public int getItemCount() {
            return collections.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textViewCollectionName;

            public ViewHolder(View view) {
                super(view);
                textViewCollectionName = view.findViewById(R.id.textViewCollectionName);
            }
        }
    }
}
