package com.example.pulsesensorysock.helper;

import android.content.Context;
import android.widget.Toast;

public class Helper {

    Context context;

    public Helper(Context context) {
        this.context = context;
    }

    public void showToast(String message) {
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
    }
}
