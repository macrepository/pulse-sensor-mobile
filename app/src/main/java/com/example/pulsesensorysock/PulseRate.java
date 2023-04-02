package com.example.pulsesensorysock;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pulsesensorysock.helper.Helper;

import java.util.ArrayList;

public class PulseRate extends AppCompatActivity {

    static TextView heartRate;
    static TextView heartRateIndicator;

    //Helper
    static Helper helper;

    //Database
    com.example.pulsesensorysock.model.User db;

    static boolean isActive;
    static Context thisActivity;
    static String phoneNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulserate);

        helper = new Helper(getApplicationContext());

        db = new com.example.pulsesensorysock.model.User(this);

        //Check and Retrieve user data
        ArrayList<String> user = db.getUser();

        if (user.size() > 0) {
            String contactEmerg1 = user.get(8);
            this.phoneNumber = user.get(10);

            if (contactEmerg1 != null && !contactEmerg1.isEmpty()) {
                this.phoneNumber = contactEmerg1;
            }
        }

        thisActivity = this;

        heartRate = findViewById(R.id.heart_rate);
        heartRateIndicator = findViewById(R.id.heart_rate_indicator);
        isActive = true;
    }

    public static void displayHeartRate(String rate) {
        try {
            String cleanStrRate = rate.replaceAll("[^\\d.]", "");

            if (isActive && helper.isNumeric(cleanStrRate)) {
                int heartRateCount = Integer.parseInt(cleanStrRate);
                heartRate.setText("" + heartRateCount);

                if (heartRateCount <= 60) {
                    heartRateIndicator.setText("BELOW AVERAGE");
                    heartRateIndicator.setTextColor(Color.parseColor("#2b5f74"));
                } else if (heartRateCount <= 102) {
                    heartRateIndicator.setText("AVERAGE");
                    heartRateIndicator.setTextColor(Color.parseColor("#ffde59"));
                } else if (heartRateCount <= 142) {
                    heartRateIndicator.setText("HEALTHY");
                    heartRateIndicator.setTextColor(Color.parseColor("#ff914d"));
                } else if (heartRateCount <= 183) {
                    heartRateIndicator.setText("MAXIMUM");
                    heartRateIndicator.setTextColor(Color.parseColor("#ff0000"));
                } else {
                    heartRateIndicator.setText("DANGER");
                    heartRateIndicator.setTextColor(Color.parseColor("#920000"));
                }

                int minimumHeartRateToAlert = 130;
                if (heartRateCount >= minimumHeartRateToAlert) {
                    Log.d("Pulse rate activity", "Heart Rate: " + heartRateCount);
                    helper.showAlertDialog(thisActivity, phoneNumber);
                }
            }
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
        } catch (Throwable t){
            t.printStackTrace();
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
