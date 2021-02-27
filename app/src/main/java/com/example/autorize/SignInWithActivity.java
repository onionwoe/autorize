package com.example.autorize;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.facebook.FacebookSdk;

import java.util.Arrays;

public class SignInWithActivity extends AppCompatActivity {

    CallbackManager mCallbackManager;//создаем диспетчер обратного вызова

    static final int RC_SIGN_IN = 1;//request код который проверяем откуда пришел результат
    private FirebaseAuth mAuth;////создаем точку входа в Firebase Authentication
    private GoogleSignInClient googleSignInClient;//создаем обьект, чтобы использовать клиент для входа в Google
    private Button signInBtn, customFButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signinwith);


        mAuth = FirebaseAuth.getInstance();

        initializeFacebook();
        createRequest();
        setupViews();
        setupListener();

    }

    private void initializeFacebook() {

        FacebookSdk.sdkInitialize(SignInWithActivity.this);//Инициализация Facebook SDK
        mCallbackManager = CallbackManager.Factory.create();//вызовим этот метод для обработки токена для входа
        customFButton = findViewById(R.id.customFButton);//инициализируем кнопку
        customFButton.setOnClickListener(new View.OnClickListener() {//обработчик нажатия
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(SignInWithActivity.this,
                        Arrays.asList("email", "public_profile"));//получаем доступ к данным пользователя из Facebook
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {//чтобы отреагировать на результат входа, нужно зарегистрировать обравтный вызов
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
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {//если все успешно, то открывается MainActivity
                            Intent intent = new Intent(SignInWithActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignInWithActivity.this, "Sorry", Toast.LENGTH_SHORT).show();
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
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
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

        mCallbackManager.onActivityResult(requestCode, resultCode, data);//чтобы передать результаты входа в LoginManager через callbackManager.

        if (requestCode == RC_SIGN_IN) {//проверка возвращаемого результата
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);//возвращаем информацию о пользователе, выполнившем вход в это приложение;
            firebaseGoogleAuth(account);
        }
        catch (ApiException e){
            Toast.makeText(this, "" + e.getMessage() + e.getStatusCode(), Toast.LENGTH_SHORT).show();//если возникнет какая то ошибка выведем его на экран
        }
    }

    private void firebaseGoogleAuth(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){//Успешный вход, обновите пользовательский интерфейс, указав информацию о зарегистрированном пользователе

                            Intent intent = new Intent(SignInWithActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(SignInWithActivity.this, "Sorry", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void onClick(View view) {
        startActivity(new Intent(this, PhoneActivity.class));
        finish();
    }


    public void onClickEmail(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
