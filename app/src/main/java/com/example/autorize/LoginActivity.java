package com.example.autorize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail, mPassword;
    private Button mLoginButton;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupViews();
        setupListeners();

    }

    private void setupListeners() {
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)){//проверяем на пустоту
                    mEmail.setError("Email is Required");//если не введена выводим ошибку
                    return;//возвращаем ошибку
                }
                if (TextUtils.isEmpty(password)){//проверяем на пустоту
                    mPassword.setError("Password is Required");//если не введена выводим ошибку
                    return;//возвращаем ошибку
                }
                if (password.length() < 3){//если пароль пользователя меньше 3-х символов
                    mPassword.setError("Password Must be >= 3 Characters");//предупреждаем пользователя что пароль дожен содержать больше трех символов
                    return;//возвращаем ошибку
                }

                //вход в аккаунт
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){//проверяем на успешшностью входа
                            if (mAuth.getCurrentUser().isEmailVerified()){//проверяе пользователя подтвердил ли он почту
                                Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();//выводим сообщение что вошел в аккаунт
                                startActivity(new Intent(LoginActivity.this, SettingsActivity.class));
                                finish();
                            }
                            else {//если все еще не подтвердил почту
                                Toast.makeText(LoginActivity.this, "Please verify your email address", Toast.LENGTH_SHORT).show();//сообщение об ошибке
                            }

                        }
                        else {//если неверный логин или какая то ошибка
                            Toast.makeText(LoginActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();//сообщение об ошибке
                        }
                    }
                });
            }
        });
    }

    private void setupViews() {
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mLoginButton = findViewById(R.id.login_button);

        mAuth = FirebaseAuth.getInstance();
    }

    public void onClick(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
