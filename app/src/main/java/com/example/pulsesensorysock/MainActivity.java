package com.example.pulsesensorysock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pulsesensorysock.helper.Helper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> user;

    com.example.pulsesensorysock.model.User db;

    //Helper
    static Helper helper;

    static String phoneNumber;
    static boolean isActive;
    static Context thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_main);

        TextView greeting;
        LinearLayout connect, pulseRate, connectLabel, pulseRateLabel;
        ImageView userIcon;

        connect = findViewById(R.id.main_connect);
        pulseRate = findViewById(R.id.main_pulserate);

        connectLabel = findViewById(R.id.main_connect_label);
        pulseRateLabel = findViewById(R.id.main_pulserate_label);
        userIcon = findViewById(R.id.user_icon);
        greeting = findViewById(R.id.greeting);

        db = new com.example.pulsesensorysock.model.User(this);

        helper = new Helper(getApplicationContext());

        //Check and Retrieve user data
        user = db.getUser();

        if (user.size() > 0) {
            greeting.setText("Good Day, " + user.get(1) + "!");
            String contactEmerg1 = user.get(8);
            this.phoneNumber = user.get(10);

            if (contactEmerg1 != null && !contactEmerg1.isEmpty()) {
                this.phoneNumber = contactEmerg1;
            }
        }

        thisActivity = this;

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCon = new Intent(getApplication(), Connect.class);
                startActivity(intentCon);
            }
        });

        connectLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCon = new Intent(getApplication(), Connect.class);
                startActivity(intentCon);
            }
        });

        pulseRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentPulseRate = new Intent(getApplication(), PulseRate.class);
                startActivity(intentPulseRate);
            }
        });

        pulseRateLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentPulseRate = new Intent(getApplication(), PulseRate.class);
                startActivity(intentPulseRate);
            }
        });

        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentPulseRate = new Intent(getApplication(), User.class);
                startActivity(intentPulseRate);
            }
        });
    }

    public static void checkHeartRate(String rate) {
        String cleanStrRate = rate.replaceAll("[^\\d.]", "");

        if (isActive && helper.isNumeric(cleanStrRate)) {
            int minimumHeartRateToAlert = 130;
            int heartRateCount = Integer.parseInt(cleanStrRate);

            if (heartRateCount >= minimumHeartRateToAlert) {
                Log.d("Main activity", "Heart Rate: " + heartRateCount);
                helper.showAlertDialog(thisActivity, phoneNumber);
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        isActive = true;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        isActive = false;
    }
}