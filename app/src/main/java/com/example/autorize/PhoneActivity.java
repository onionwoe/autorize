package com.example.autorize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneActivity extends AppCompatActivity {

    private CountryCodePicker ccp;//создаем обьект для работы с странами
    private EditText phoneText, codeText;//наши View
    private Button continueBtn;//Кнопка
    private String checker = "", phoneNumber = "";//создали переменную чтобы хранить номер телефона и для проверки пришел ли нам код
    private RelativeLayout relativeLayout;//наш активити

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;//обратный вызов
    private FirebaseAuth mAuth;//создаем точку входа в Firebase Authentication
    private String mVerificationId;//Id нашего кода подтверждение
    private PhoneAuthProvider.ForceResendingToken mResendToken;//токен повторной отправки
    private ProgressDialog loadingBar;//загрузочное окно

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        setupViews();//вызов метода для инициализации view
        setupListener();//вызов метода для обработки нажатия
    }

    private void setupListener() {
        continueBtn.setOnClickListener(new View.OnClickListener() {//обработка нажатия кнопки
            @Override
            public void onClick(View view) {
                if (continueBtn.getText().equals("Submit") || checker.equals("Code Sent")){//проверяем состояние конпки
                    String verificationCode = codeText.getText().toString();//сохраняем в переменную verificationCode наш текст из EditText

                    if (verificationCode.equals("")){//проверяем пользователя вёл ли он код
                        Toast.makeText(PhoneActivity.this, "Please write verification code", Toast.LENGTH_SHORT).show();//если не вёл то предупреждаем его
                    }
                    else {//если он вёл код
                        //запускаем наш загрузочное окно
                        loadingBar.setTitle("Code Verification");
                        loadingBar.setMessage("Please wait, while we are verifying your code");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);//проверка Id и кода подтверждения
                        signInWithPhoneAuthCredential(credential);//вызов метода
                    }
                }
                else {
                    phoneNumber = ccp.getFullNumberWithPlus();//полученный номер телефона мы подключаем к кода страны
                    if (!phoneNumber.equals("")){//если пользователь вёл номер телефона
                        //запускаем загрузочное окно
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Please wait, while we are verifying your phone number");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(//отправляем провероный код
                                phoneNumber,//Номер телефона для подтверждения
                                60,//время рабочего кода (через 60 секунд код не будет работать)
                                TimeUnit.SECONDS,//секунда
                                PhoneActivity.this,//активити для привязки обратного вызова
                                callbacks//обратный вызов
                        );
                    }
                    else {//если не вёл номер телефона то попросим его ввести
                        Toast.makeText(PhoneActivity.this, "Please write valid phone number", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {//создание обратного вызова
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {//если все успешно
                signInWithPhoneAuthCredential(phoneAuthCredential);//вызов метода
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {//если какая то ошибка или неправильный номер
                Toast.makeText(PhoneActivity.this, "Invalid Phone Number...", Toast.LENGTH_SHORT).show();

                loadingBar.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);

                continueBtn.setText("Continue");
                codeText.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {//ожидание кода
                super.onCodeSent(s, forceResendingToken);

                //Код подтверждения по SMS был отправлен на указанный номер телефона, теперь нам нужно попросить пользователя ввести код, а затем создать учетные данные, объединив код с идентификатором подтверждения.


                //Сохраняем идентификатор подтверждения и токен повторной отправки, чтобы мы могли использовать их позже
                mVerificationId = s;
                mResendToken = forceResendingToken;

                relativeLayout.setVisibility(View.GONE);
                checker = "Code Sent";
                continueBtn.setText("Submit");
                codeText.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(PhoneActivity.this, "Code has been sent, please check", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void setupViews() {

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueBtn = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {//если все успеешно
                            loadingBar.dismiss();//остановка загрузочного окна
                            Toast.makeText(PhoneActivity.this, "Congratulation, you are logged in successfully", Toast.LENGTH_SHORT).show();
                            sendUser();//вызов метода
                        } else {//если неверный код
                            loadingBar.dismiss();//остановка загрузочного окна
                            String e = task.getException().toString();
                            Toast.makeText(PhoneActivity.this, "Error" + e, Toast.LENGTH_SHORT).show();//выведем ошшибку
                        }
                    }
                });
    }

    private void sendUser(){
        startActivity(new Intent(this, SettingsActivity.class));//перенаправление пользователя к ее данным
        finish();//закрытие активити
    }
}
