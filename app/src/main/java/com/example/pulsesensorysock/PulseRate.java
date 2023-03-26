package com.example.pulsesensorysock;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PulseRate extends AppCompatActivity {

    static TextView heartRate;
    static TextView heartRateIndicator;

    static boolean isActive;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulserate);

        heartRate = findViewById(R.id.heart_rate);
        heartRateIndicator = findViewById(R.id.heart_rate_indicator);
        isActive = true;
    }

    public static void displayHeartRate(String rate) {
        try {
            String cleanStrRate = rate.replaceAll("[^\\d.]", "");

            if (isActive && isNumeric(cleanStrRate)) {
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
            }
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
        } catch (Throwable t){
            t.printStackTrace();
        }
    }

    private static boolean isNumeric(String str){
        return str != null && str.matches("[0-9.]+");
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
