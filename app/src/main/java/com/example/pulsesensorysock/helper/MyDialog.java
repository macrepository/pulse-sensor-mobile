package com.example.pulsesensorysock.helper;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.pulsesensorysock.R;

public class MyDialog extends Dialog {

    private Context context;
    private View rootView;
    private Button warnBtnYes;
    private Button warnBtnNo;

    public MyDialog(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        // Inflate the layout for the dialog
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.call_alert, null);

        // Set up buttons
        warnBtnYes = rootView.findViewById(R.id.warn_button_yes);
        warnBtnNo = rootView.findViewById(R.id.warn_button_no);

        warnBtnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:09394503849"));
                context.startActivity(intent);
            }
        });

        setContentView(rootView);
    }

    public View getView() {
        return rootView;
    }
}
