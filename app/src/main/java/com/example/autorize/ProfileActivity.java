package com.example.autorize;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; //создаем точку входа в Firebase Authentication
    private FirebaseUser user; //создаем текущего пользовтателя
    private TextView id, name, email;//создаем наши TextView
    private CircleImageView profileImage;//создаем CircleImageView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initialize();//вызов метода для инициализации
        getData();//получаем данные пользователя
    }

    private void getData() {

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null){
            id.setText(user.getUid());//получаем идентификатор пользователя
            name.setText(user.getDisplayName());//получаем имя пользователя
            email.setText(user.getEmail());//получаем email пользователя
            Glide.with(this).load(user.getPhotoUrl()).into(profileImage);////получаем фото пользователя
        }


    }

    private void initialize() {//инициализируем наши созданные обьекты
        id = findViewById(R.id.id_txt);
        name = findViewById(R.id.name_txt);
        email = findViewById(R.id.email_txt);
        profileImage = findViewById(R.id.profile_image);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }
}
