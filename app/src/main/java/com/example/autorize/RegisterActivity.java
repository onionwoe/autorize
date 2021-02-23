package com.example.autorize;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class RegisterActivity extends AppCompatActivity {

    static final int RC_SIGN_IN = 1;//request код который проверяем откуда пришел результат
    private FirebaseAuth mAuth;////создаем точку входа в Firebase Authentication
    private GoogleSignInClient googleSignInClient;//создаем обьект, чтобы использовать клиент для входа в Google

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Настройка входа для запроса идентификатора пользователя, адреса электронной почты. ID и базовый профиль включены в DEFAULT_SIGN_IN
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))//
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);//чтобы инпортировать настройки, инициализируем наш клиент входа в Google
        mAuth = FirebaseAuth.getInstance();//инициализируем нашу точку входа
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();//получаем Intent запуска процесса входа в Google путем вызова startActivityForResult()
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onClick(View view) {//обработка нажатия
        signIn();//вызов метода для входа в аккаунт
    }


    //Результат, возвращенный при запуске Intent из GoogleSignInClient.getSignInIntent (...);
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {//проверка возвращаемого результата
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()){//если все успешно
                try {
                    // Вход в Google выполнен успешно, аутентифицируйтесь с помощью Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);//возвращаем информацию о пользователе, выполнившем вход в это приложение
                    firebaseAuthWithGoogle(account.getIdToken());//получаем токен аккаунта
                } catch (ApiException e) {

                }
            }

        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){//Успешный вход, обновите пользовательский интерфейс, указав информацию о зарегистрированном пользователе
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
}
