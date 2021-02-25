package com.example.autorize;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import bolts.AppLink;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; //создаем точку входа в Firebase Authentication
    private FirebaseUser user; //создаем текущего пользовтателя
    private TextView id, name, email, phone;//создаем наши TextView
    private CircleImageView profileImage;//создаем CircleImageView
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");//доступ к базу данных для чтения данных пользователя

        initialize();//вызов метода для инициализации
        getData();//получаем данные пользователя
        getDataPhone();//получаем данные пользователя из Телефона
        getDataFacebook();//получаем данные пользователя из Facebook
    }

    private void getDataPhone() {
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())//получаем uid пользователя
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String uid = snapshot.child("uid").getValue().toString();//сохраням uid пользователя в переменную
                            String images  = snapshot.child("image").getValue().toString();//сохраням фотографию пользователя в переменную
                            String names  = snapshot.child("name").getValue().toString();//сохраням имя пользователя в переменную
                            String emails  = snapshot.child("email").getValue().toString();//сохраням email пользователя в переменную
                            String phones = user.getPhoneNumber();//получаем номер телефона

                            id.setText(uid);//получаем идентификатор пользователя
                            name.setText(names);//получаем имя пользователя
                            email.setText(emails);//получаем email пользователя
                            phone.setText(phones);//получаем email пользователя
                            Glide.with(ProfileActivity.this).load(images).placeholder(R.drawable.profile_image).into(profileImage);////получаем фото пользователя
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getDataFacebook() {
        if (user != null){
            String uid = user.getUid();
            String names = user.getDisplayName();
            String emails = user.getEmail();

            id.setText(uid);//получаем идентификатор пользователя
            name.setText(names);//получаем имя пользователя
            email.setText(emails);//получаем email пользователя
            Glide.with(this).load(user.getPhotoUrl()).into(profileImage);////получаем фото пользователя
        }
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
        phone = findViewById(R.id.phone_txt);
        profileImage = findViewById(R.id.profile_image);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    public void onClick(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
