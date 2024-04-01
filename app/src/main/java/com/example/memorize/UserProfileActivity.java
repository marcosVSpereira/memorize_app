package com.example.memorize;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView userProfileImage;
    private TextView userNickname, userFirstName, userBirthDate;
    private Button deleteAccountButton, changePasswordButton, changeDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile); // Substitua 'your_layout_name' pelo nome do seu arquivo XML

        // Inicializando as Views
        userProfileImage = findViewById(R.id.userProfileImage);
        userNickname = findViewById(R.id.userNickname);
        userFirstName = findViewById(R.id.userFirstName);
        userBirthDate = findViewById(R.id.userBirthDate);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        changeDataButton = findViewById(R.id.changeData);

        // Configurando os Listeners para os botões
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserAccount();
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adicione aqui o código para lidar com a mudança de senha
                showChangePasswordDialog();
            }
        });

        changeDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeDataDialog();
            }
        });


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        } else {
            // Usuário não está logado. Lidar com este caso.
        }






        // Aqui você pode carregar os dados do usuário e definir nas Views
        // Exemplo:
        // userNickname.setText("Nickname do usuário");
        // userFirstName.setText("Nome do usuário");
        // userBirthDate.setText("Data de nascimento do usuário");
        // userProfileImage.setImageDrawable(...); // Configurar imagem do perfil do usuário
    }

    private void loadUserData(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Supondo que você tenha os campos firstName, nickname e birthDate no seu banco de dados
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String nickname = dataSnapshot.child("nickname").getValue(String.class);
                    String birthDate = dataSnapshot.child("birthDate").getValue(String.class);

                    // Configurar os valores nas Views
                    userFirstName.setText("Nome - " + firstName);
                    userNickname.setText("@" + nickname); // Adiciona @ antes do nickname
                    userBirthDate.setText("Nascimento - " + birthDate);

                    // Configurar a imagem do perfil do usuário se necessário
                    // Por exemplo, se você armazenar a URL da imagem no banco de dados
                    // String profileImageUrl = dataSnapshot.child("profileImage").getValue(String.class);
                    // Use uma biblioteca como Glide ou Picasso para carregar a imagem na ImageView
                    // Glide.with(UserProfileActivity.this).load(profileImageUrl).into(userProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("UserProfileActivity", "loadUserData:onCancelled", databaseError.toException());
                // Tratar o erro
            }
        });
    }

    private void deleteUserAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Excluir Conta")
                    .setMessage("Tem certeza que deseja excluir sua conta? Esta ação não pode ser desfeita.")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        // Lógica para excluir a conta
                        performAccountDeletion(user);
                    })
                    .setNegativeButton("Não", (dialog, which) -> {
                        // Fecha o dialogo sem fazer nada
                        dialog.dismiss();
                    })
                    .show();
        } else {
            Toast.makeText(this, "Nenhum usuário conectado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void performAccountDeletion(FirebaseUser user) {
        String userId = user.getUid();

        // Primeiro, exclua todos os dados do usuário do Firebase Database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Se os dados foram excluídos com sucesso, agora exclua o usuário do Firebase Authentication
                user.delete().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(UserProfileActivity.this, "Conta excluída com sucesso.", Toast.LENGTH_SHORT).show();

                        // Redirecione para a tela de login ou qualquer outra tela inicial
                        android.content.Intent intent = new android.content.Intent(UserProfileActivity.this, HomeActivity.class);
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Encerra a Activity atual
                    } else {
                        Toast.makeText(UserProfileActivity.this, "Erro ao excluir a conta do usuário.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(UserProfileActivity.this, "Erro ao excluir dados do usuário.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mudar Senha");

        // Layout para conter os dois EditText
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Define um EditText para o usuário inserir a nova senha
        final EditText newPasswordInput = new EditText(this);
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswordInput.setHint("Nova Senha");
        layout.addView(newPasswordInput);

        // Define um EditText para confirmar a nova senha
        final EditText confirmPasswordInput = new EditText(this);
        confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordInput.setHint("Confirmar Senha");
        layout.addView(confirmPasswordInput);

        builder.setView(layout);

        // Define os botões do diálogo
        builder.setPositiveButton("Mudar", (dialog, which) -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            if (newPassword.equals(confirmPassword)) {
                Log.d("UserProfileActivity", "Confirm Password: " + confirmPassword);
                changeUserPassword(newPassword);
            } else {
                Toast.makeText(UserProfileActivity.this, "As senhas não correspondem.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void changeUserPassword(String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && !newPassword.isEmpty()) {
            user.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UserProfileActivity.this, "Senha atualizada com sucesso.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserProfileActivity.this, "Erro ao atualizar a senha, saia da conta, entre e tente novamente.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(UserProfileActivity.this, "Senha não pode ser vazia.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangeDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alterar Dados");

        // Layout para conter os EditTexts
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // EditText para o nickname
        final EditText nicknameInput = new EditText(this);
        nicknameInput.setHint("Nickname");
        layout.addView(nicknameInput);

        // EditText para o primeiro nome
        final EditText firstNameInput = new EditText(this);
        firstNameInput.setHint("Primeiro Nome");
        layout.addView(firstNameInput);

        // EditText para a data de nascimento
        final EditText birthDateInput = new EditText(this);
        birthDateInput.setHint("Data de Nascimento");
        layout.addView(birthDateInput);

        builder.setView(layout);

        // Define os botões do diálogo
        builder.setPositiveButton("Alterar", (dialog, which) -> {
            String newNickname = nicknameInput.getText().toString().trim();
            String newFirstName = firstNameInput.getText().toString().trim();
            String newBirthDate = birthDateInput.getText().toString().trim();

            updateUserData(newNickname, newFirstName, newBirthDate);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void updateUserData(String newNickname, String newFirstName, String newBirthDate) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

            // Verifica se o nickname já existe
            Query query = usersRef.orderByChild("nickname").equalTo(newNickname);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists() || newNickname.isEmpty()) {
                        // Nickname não existe ou não foi fornecido, pode atualizar os outros campos
                        DatabaseReference userRef = usersRef.child(user.getUid());
                        Map<String, Object> updates = new HashMap<>();

                        if (!newNickname.isEmpty()) {
                            updates.put("nickname", newNickname);
                        }
                        if (!newFirstName.isEmpty()) {
                            updates.put("firstName", newFirstName);
                        }
                        if (!newBirthDate.isEmpty()) {
                            updates.put("birthDate", newBirthDate);
                        }

                        userRef.updateChildren(updates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(UserProfileActivity.this, "Dados atualizados com sucesso.", Toast.LENGTH_SHORT).show();
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                loadUserData(currentUser.getUid());
                            } else {
                                Toast.makeText(UserProfileActivity.this, "Erro ao atualizar dados.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Nickname já existe
                        Toast.makeText(UserProfileActivity.this, "Nickname já está em uso. Escolha outro.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(UserProfileActivity.this, "Erro ao verificar nickname.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(UserProfileActivity.this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }



}
