package com.example.autorize;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button saveBtn;//кнопка
    private EditText userName, userEmail;//Edit Text
    private CircleImageView profileImage;//ImageView
    private Uri ImageUri;//Uri фотографии
    private StorageReference userProfileImgRef;//для хранение фотографию пользователя в Firebase
    private String downloadUrl;//загрузкаUrl
    private DatabaseReference userRef;//для создания базы данных
    private ProgressDialog progressDialog;//загрузочное окно
    static int REQUEST_CODE = 1;//код для проверки обратного результата
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        userProfileImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");//создаем папку для хранения фотографии пользователей
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");//создаем базу данных Users

        setupViews();//инициализация
        setupListeners();//обработчик нажатия
    }

    private void setupListeners() {
        profileImage.setOnClickListener(new View.OnClickListener() {//когда собираемся загрузить фотографию
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();//создаем интент для открытия галерею телефона
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);//действие обьекта интент, позволяет пользователю выбрать определенный тип данных и вернуть их
                galleryIntent.setType("image/*");//указываем какой тип
                startActivityForResult(galleryIntent, REQUEST_CODE);//запускаем активити на результат
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {//нажатие кнопки
            @Override
            public void onClick(View view) {
                saveUserData();
            }
        });
    }

    private void saveUserData() {
        final String getUserName = userName.getText().toString();//сохранаем имя пользователя содержимое из EditText в переменную
        final String getEmailName = userEmail.getText().toString();//сохранаем email пользователя содержимое из EditText в переменную

        if (ImageUri == null){//если пользователь не загрузил свою фотографию
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //if (snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")){//получаем пользователя и проверяем его фотографию
                        saveInfoOnlyWithoutImage();//вызов метода без фотографии пользователя
                    //}
                    //else {
                        //Toast.makeText(SettingsActivity.this, "Please Select Image", Toast.LENGTH_SHORT).show();
                    //}
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
//        else if (getUserName.equals("")){//проверяем на пустоту Edit Text
//            Toast.makeText(this, "User Name is mandatory", Toast.LENGTH_SHORT).show();
//        }
//        else if (getEmailName.equals("")){//проверяем на пустоту Edit Text
//            Toast.makeText(this, "User Email is mandatory", Toast.LENGTH_SHORT).show();
//        }
        else {//если пользователь зарегался с фотографией

            //запускаем загрузочное окно
            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            final StorageReference filePath = userProfileImgRef
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());//создаем обьект который сохраняет фотографию в сохданнную папку
            final UploadTask uploadTask = filePath.putFile(ImageUri);//загружаем фотографию
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){//если не успешно
                        throw task.getException();
                    }

                    downloadUrl = filePath.getDownloadUrl().toString();//присваиваем к Url фотографии
                    return filePath.getDownloadUrl();//возвращаем Url
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){//если успеешно
                        downloadUrl = task.getResult().toString();//получаем результат

                        HashMap<String, Object> profileMap = new HashMap<>();//создаем поля
                        profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());//uid пользоваеля
                        profileMap.put("name", getUserName);//имя пользователя
                        profileMap.put("email", getEmailName);//email пользователя
                        profileMap.put("image", downloadUrl);//фотография пользователя

                        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {//загружаем сохданные поля к базу данных Users
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                                    finish();
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void saveInfoOnlyWithoutImage() {
        final String getUserName = userName.getText().toString();//сохранаем имя пользователя содержимое из EditText в переменную
        final String getEmailName = userEmail.getText().toString();//сохранаем email пользователя содержимое из EditText в переменную



//        if (getUserName.equals("")){//проверяем на пустоту Edit Text
//            Toast.makeText(this, "User Name is mandatory", Toast.LENGTH_SHORT).show();
//        }
//        else if (getEmailName.equals("")){//проверяем на пустоту Edit Text
//            Toast.makeText(this, "User Email is mandatory", Toast.LENGTH_SHORT).show();
//        }
//        else {//если пользователь зарегался без фотография

            //запускаем загрузочное окно
            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            HashMap<String, Object> profileMap = new HashMap<>();//создаем поля
            profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());//uid пользоваеля
            profileMap.put("name", getUserName);//имя пользователя
            profileMap.put("email", getEmailName);//фотография пользователя

            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                        finish();
                        progressDialog.dismiss();
                    }
                }
            });
    }

    private void setupViews() {
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        saveBtn = findViewById(R.id.save_btn);
        profileImage = findViewById(R.id.settings_profile_img);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null){//провряем возращенный результат
            ImageUri = data.getData();//получем фотографию
            profileImage.setImageURI(ImageUri);//загружаем фотографию
        }
    }
}
