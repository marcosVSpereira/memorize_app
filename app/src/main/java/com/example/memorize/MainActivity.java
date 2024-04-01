package com.example.memorize;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        // Verifique se o usuário já está autenticado. Se sim, direcione para a próxima tela.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Se o usuário estiver logado, redirecione para Home
            startActivity(new Intent(MainActivity.this, Home.class));
        } else {
            // Se o usuário não estiver logado, redirecione para HomeActivity (ou para uma tela de login/registro)
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        }
        finish();
    }
}













/*package com.example.memorize;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Verifique se o usuário já está autenticado. Se sim, direcione para a próxima tela.
        if (currentUser != null) {
            // Redirecione para a próxima tela
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    }
}
*/