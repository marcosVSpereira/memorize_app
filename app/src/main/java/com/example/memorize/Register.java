package com.example.memorize;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText editTextRegisterEmail, editTextRegisterPassword;
    private EditText editTextFirstName, editTextLastName, editTextNickname, editTextBirthDate;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializa os componentes da UI
        editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        editTextFirstName = findViewById(R.id.editTextName);
        editTextNickname = findViewById(R.id.editTextNickname);
        editTextBirthDate = findViewById(R.id.editTextBirthDate);

        firebaseAuth = FirebaseAuth.getInstance();

        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextRegisterEmail.getText().toString();
                String password = editTextRegisterPassword.getText().toString();
                String firstName = editTextFirstName.getText().toString();

                String nickname = editTextNickname.getText().toString();
                String birthDate = editTextBirthDate.getText().toString();

                if (!email.isEmpty() && !password.isEmpty() && !firstName.isEmpty() &&  !nickname.isEmpty() && !birthDate.isEmpty()) {
                    registerUser(email, password, firstName, nickname, birthDate);
                } else {
                    Toast.makeText(Register.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView textViewLogin = findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }

    private void registerUser(String email, String password, String firstName, String nickname, String birthDate) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Autenticação bem-sucedida, usuário criado
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

                                Map<String, Object> userData = new HashMap<>();
                                userData.put("firstName", firstName);
                                userData.put("nickname", nickname);
                                userData.put("birthDate", birthDate);
                                userData.put("email", email);

                                usersRef.child(userId).setValue(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(Register.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(Register.this, Home.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(Register.this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });

                                createDefaultCollection(userId);
                            }
                        } else {
                            // Tratamento de falha no registro
                            Toast.makeText(Register.this, "Falha no registro. Tente novamente.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createDefaultCollection(String userId) {
        // Obtenha uma referência ao FirebaseDatabase
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        // Crie um novo objeto Collection
        Collection defaultCollection = new Collection("default", "Padrão");

        // Defina a coleção padrão no caminho adequado
        mDatabase.child("users").child(userId).child("collections").child("default").setValue(defaultCollection)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Coleção padrão criada com sucesso."))
                .addOnFailureListener(e -> Log.d("Firebase", "Falha ao criar coleção padrão."));
    }
}
