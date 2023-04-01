package com.example.pulsesensorysock.helper;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.pulsesensorysock.R;

public class Helper {

    Context context;
    private Button warnBtnYes;
    private Button warnBtnNo;

    public Helper(Context context) {
        this.context = context;
    }

    public void showToast(String message) {
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
    }

    public void showAlertDialog(Context context, String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.call_alert);

            // Set up buttons
            warnBtnYes = dialog.findViewById(R.id.warn_button_yes);
            warnBtnNo = dialog.findViewById(R.id.warn_button_no);

            warnBtnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    context.startActivity(intent);
                }
            });

            warnBtnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }
}
