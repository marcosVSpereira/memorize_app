package com.example.memorize;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        mAuth = FirebaseAuth.getInstance();

        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                // Verificar se os campos de e-mail e senha estão preenchidos
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Autenticar o usuário
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Login bem-sucedido, redirecione para a
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String userId = user.getUid();
                                    startActivity(new Intent(Login.this, Home.class));
                                    finish();
                                } else {
                                    // Se ocorrer um erro no login, exiba uma mensagem de erro
                                    Toast.makeText(Login.this, "Credenciais inválidas. Tente novamente.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        TextView textViewRegister = findViewById(R.id.textViewRegister);
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navegar para a tela de registro
                startActivity(new Intent(Login.this, Register.class));
            }
        });
    }
}