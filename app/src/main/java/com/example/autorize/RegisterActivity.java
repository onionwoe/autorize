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

public class RegisterActivity extends AppCompatActivity {

    private EditText mEmail, mPassword;
    private Button mRegisterButton;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setupViews();
        setupListener();

    }

    private void setupListener() {
        mRegisterButton.setOnClickListener(new View.OnClickListener() {//обработчик нажатия
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();//сохраняем email пользователя в переменную
                String password = mPassword.getText().toString().trim();//сохраняем пароль пользователя в переменную

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

                //создание аккаунта
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){//если задача выполнена успешно

                            //отправляем письмо на почту текущего пользователя
                            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){//если письмо успешно отправлено
                                        Toast.makeText(RegisterActivity.this, "Registered successfully. Please check your email for verification", Toast.LENGTH_LONG).show();//выводим сообщение что аккаунт создался
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                    else {//если произошла ошибка
                                        Toast.makeText(RegisterActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();//сообщение об ошибке
                                    }
                                }
                            });


                        }
                        else {//если произошла ошибка
                            Toast.makeText(RegisterActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();//сообщение об ошибке
                        }
                    }
                });
            }
        });
    }

    private void setupViews() {
        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);
        mRegisterButton = findViewById(R.id.register_button);

        mAuth = FirebaseAuth.getInstance();
    }

    public void onClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }
}
