package com.example.autorize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; //создаем точку входа в Firebase Authentication
    private FirebaseUser user; //создаем текущего пользовтателя
    private GoogleSignInAccount googleSignInAccount;
    public String uniqueID = UUID.randomUUID().toString();
    public String shareLinkText;
    private String query_id = "";
    private String query_guid = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        initialize();//вызов метода для инициализации обьектов

        checkUser();//вызов метода для проверки текущего пользователя

    }

    private void checkUser() {
        if (user == null){// Если пользователь еще не зарегистрировался
            Intent signInIntent = new Intent(this, SignInWithActivity.class);
            startActivity(signInIntent);//то перекидываем его в окно регистрации
            finish();//закрытия MainActivity
        }
    }

    private void initialize() {
        mAuth = FirebaseAuth.getInstance();//инициализируем нашу точку входа
        user = mAuth.getCurrentUser();//здесь тоже инициализируем, чтобы проверить пользователя на существование (получаем текущего пользователя)
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//переопределяем метод который создает меню
        MenuInflater inflater = getMenuInflater();//создаем обьект, который способен создавать меню из xml ресурсов
        inflater.inflate(R.menu.menu_item, menu);//находим xml ресурс по названиям
        return true;//возвращаем истину чтобы все это сработала
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {//переопределяем метод который вызывается при нажатии из списка меню

        switch (item.getItemId()){//находим нажатие по ID
            case R.id.settings_menu://если нажата Settings, то выполняется этот блок кода
                startActivity(new Intent(this, ProfileActivity.class));//при нажатии происходит перенаправление в ProfileActivity
                return true;//возвращаем истину потому что этот метод возвращает тип данных boolean

            case R.id.share_menu://если нажата Settings, то выполняется этот блок кода

                startActivity(new Intent(this, ShareActivity.class));//переход в другой активити
                return true;//возвращаем истину потому что этот метод возвращает тип данных boolean

            case R.id.logout_menu://если нажата Logout, то выполняется этот блок кода
                mAuth.signOut();//Выключает текущего вошедшего пользователя
                LoginManager.getInstance().logOut();
                startActivity(new Intent(this, SignInWithActivity.class));//при нажатии происходит перенаправление в SignInWithActivity
                finish();
                return true;//возвращаем истину потому что этот метод возвращает тип данных boolean
            default://если ничего не совпало простовыполняется по умолчанию
                return super.onOptionsItemSelected(item);
        }
    }
}