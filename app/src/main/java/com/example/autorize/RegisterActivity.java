package com.example.autorize;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class RegisterActivity extends AppCompatActivity {

    CallbackManager mCallbackManager;
    private LoginButton loginButton;

    static final int RC_SIGN_IN = 1;//request код который проверяем откуда пришел результат
    private FirebaseAuth mAuth;////создаем точку входа в Firebase Authentication
    private GoogleSignInClient googleSignInClient;//создаем обьект, чтобы использовать клиент для входа в Google
    private Button signInBtn;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Инициализация Facebook SDK
        FacebookSdk.sdkInitialize(RegisterActivity.this);
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.facebook_btn);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });



        mAuth = FirebaseAuth.getInstance();

        createRequest();
        setupViews();
        setupListener();

    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Sorry", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupListener() {
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//обработка нажатия
                signIn();//вызов метода для входа в аккаунт
            }
        });
    }

    private void setupViews() {
        signInBtn = findViewById(R.id.signIn_btn);
    }

    private void createRequest() {
        //Настройка входа для запроса идентификатора пользователя, адреса электронной почты. ID и базовый профиль включены в DEFAULT_SIGN_IN
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))//
                .requestEmail()//показывает электронные почты
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);//чтобы инпортировать настройки, инициализируем наш клиент входа в Google
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();//получаем Intent запуска процесса входа в Google путем вызова startActivityForResult()
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //Результат, возвращенный при запуске Intent из GoogleSignInClient.getSignInIntent (...);
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {//проверка возвращаемого результата
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()){//если все успешно
                try {
                    // Вход в Google выполнен успешно, аутентифицируйтесь с помощью Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);//возвращаем информацию о пользователе, выполнившем вход в это приложение;
                    firebaseGoogleAuth(account);
                } catch (ApiException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    firebaseGoogleAuth(null);
                }
            }

        }
    }

    private void firebaseGoogleAuth(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){//Успешный вход, обновите пользовательский интерфейс, указав информацию о зарегистрированном пользователе

                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(RegisterActivity.this, "Sorry", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
