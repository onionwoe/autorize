package com.example.autorize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class ShareActivity extends AppCompatActivity {

    private Button shareBtn;//создание кнопки

    public String shareLinkText;//создаем переменную для хранения ссылки
    private String query_id = "";//переменная для запроса id
    private String query_guid = "";//переменная для запроса guid
    private FirebaseUser user; //создаем текущего пользовтателя
    public String uniqueID = UUID.randomUUID().toString();
    private String TAG = "SIGN  _ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        user = FirebaseAuth.getInstance().getCurrentUser();//здесь тоже инициализируем, чтобы проверить пользователя на существование (получаем текущего пользователя)
        shareBtn = findViewById(R.id.share_button);
        setupListener();
    }

    private void setupListener() {
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createLink();//вызов метода для создании ссылки
            }
        });
    }

//    public void getLink() {
//        FirebaseDynamicLinks.getInstance()
//                .getDynamicLink(getIntent())//получаем ссылку
//                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
//                    @Override
//                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
//                        Uri deepLink = null;
//                        if (pendingDynamicLinkData != null){//если ссылка получена
//                            deepLink = pendingDynamicLinkData.getLink();//сохраняем ссылку
//                            if (deepLink != null){//если ссылка сохранена
//                                relativeLayout.setVisibility(View.VISIBLE);//делаем окно видимым
//                                main_relativelayout.setVisibility(View.GONE);
//                                nameTV.setText(deepLink.getQueryParameter("user_name"));//выводим на экран данные через ключ
//                                idTV.setText(deepLink.getQueryParameter("user_id"));//выводим на экран данные через ключ
//                                guidTV.setText(deepLink.getQueryParameter("user_guid"));//выводим на экран данные через ключ
//
//
//
//                            }
//
//
//
//                            Log.e(TAG, "my referlink " + deepLink.toString());
//                        }
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                });
//    }

    private void createLink() {
        Log.e("main", "create link");

        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()//создаем диннамическую ссылку Firebase
                .setLink(Uri.parse("https://firebase.google.com/"))//создаем длинные ссылку на Firebase
                .setDomainUriPrefix("autorize.page.link")//Префикс URL-адреса динамической ссылки
                .buildDynamicLink();//вызываем метод чтобы создать ссылку

        Uri dynamicLinkUri = dynamicLink.getUri();//получаем uri ссылки
        Log.e("main", " Long refer " + dynamicLink.getUri());

        try {
            query_id = URLEncoder.encode(String.format("&%1s=%2s", "user_id", user.getUid()), "UTF-8");//кодируем URL - адрес, чтобы передать данные (id)
            query_guid = URLEncoder.encode(String.format("&%1s=%2s", "user_guid", uniqueID), "UTF-8");//кодируем URL - адрес, чтобы передать данные (guid)
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }

        shareLinkText = "https://autorize.page.link/?" +
                "link=http://autorize.page.link/?user_name=" + user.getDisplayName()  + query_id + query_guid +
                "&apn=" + getPackageName();//сохраняем ссылку в переменную и отправляем данные и само приложение
//                "&st=" + "Sender:" +
//                "&sd=" + user.getEmail() + "\n" + user.getDisplayName() + "\n" + uniqueID +
//                "&si=" + R.mipmap.ic_launcher;




        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(shareLinkText))//сокращаем ссылку
                .setDomainUriPrefix("autorize.page.link")//Префикс URL-адреса динамической ссылки
                .buildShortDynamicLink()//вызываем метод чтобы сократить ссылку
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {//если все успешно
                            // Короткая ссылка создана
                            Uri shortLink = task.getResult().getShortLink();//получаем короткую ссылку
                            Log.e("main", " short link " + shortLink);

                            Intent intent = new Intent();//создаем интент для отправки
                            intent.setAction(Intent.ACTION_SEND);//действие - отправка
                            intent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());//отправляем нашу ссылку
                            intent.setType("text/plain");//тип отправки - текст
                            startActivity(intent);//запуск активити

                        } else {
                            Log.e("main", "error " + task.getException());
                        }
                    }
                });
    }


}
