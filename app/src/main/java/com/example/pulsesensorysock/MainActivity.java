package com.example.pulsesensorysock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_main);

        LinearLayout connect, pulseRate, connectLabel, pulseRateLabel;
        ImageView userIcon;

        connect = findViewById(R.id.main_connect);
        pulseRate = findViewById(R.id.main_pulserate);

        connectLabel = findViewById(R.id.main_connect_label);
        pulseRateLabel = findViewById(R.id.main_pulserate_label);
        userIcon = findViewById(R.id.user_icon);

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
}