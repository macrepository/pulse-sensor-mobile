package com.example.pulsesensorysock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pulsesensorysock.helper.Helper;

import java.util.ArrayList;
import java.util.Calendar;

public class User extends AppCompatActivity {

    private ImageView userPhoto;
    private EditText userName, userAge, userBday, userContact, userAddress, userEmergName1, userEmergContact1, userEmergName2, userEmergContact2;
    private Spinner sexField;
    private ArrayList<String> user;
    private String userId;

    com.example.pulsesensorysock.model.User db;

    //Helper
    static Helper helper;

    static String phoneNumber;
    static boolean isActive;
    static Context thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        helper = new Helper(getApplicationContext());
        db = new com.example.pulsesensorysock.model.User(this);

        userPhoto = findViewById(R.id.user_photo);

        userName = findViewById(R.id.user_name);
        userAge = findViewById(R.id.user_age);
        userBday = findViewById(R.id.user_bday);
        userContact = findViewById(R.id.user_contact);
        userAddress = findViewById(R.id.user_address);
        userEmergName1 = findViewById(R.id.user_emerg_name1);
        userEmergContact1 = findViewById(R.id.user_emerg_contact1);
        userEmergName2 = findViewById(R.id.user_emerg_name2);
        userEmergContact2 = findViewById(R.id.user_emerg_contact2);

        sexField = findViewById(R.id.user_sex);
        ArrayAdapter<CharSequence>sexAdapter = ArrayAdapter.createFromResource(this, R.array.sex, android.R.layout.simple_spinner_dropdown_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexField.setAdapter(sexAdapter);

        //Check and Retrieve user data
        user = db.getUser();

        if (user.size() > 0) {
            userId = user.get(0);
            userName.setText(user.get(1));
            userAge.setText(user.get(2));
            userBday.setText(user.get(3));

            String userSex = user.get(4);
            ArrayAdapter myAdapter = (ArrayAdapter) sexField.getAdapter();
            int spinnerPosition = myAdapter.getPosition(userSex);
            sexField.setSelection(spinnerPosition);

            userContact.setText(user.get(5));
            userAddress.setText(user.get(6));
            userEmergName1.setText(user.get(7));
            userEmergContact1.setText(user.get(8));
            userEmergName2.setText(user.get(9));
            userEmergContact2.setText(user.get(10));

            String contactEmerg1 = user.get(8);
            this.phoneNumber = user.get(10);

            if (contactEmerg1 != null && !contactEmerg1.isEmpty()) {
                this.phoneNumber = contactEmerg1;
            }
        }

        userBday.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "mmddyyyy";
            private Calendar cal = Calendar.getInstance();


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]", "");
                    String cleanC = current.replaceAll("[^\\d.]", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    //Fix for pressing delete next to a forward slash
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    }else{
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        int mon  = Integer.parseInt(clean.substring(0,2));
                        int day  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        if(mon > 12) mon = 12;
                        cal.set(Calendar.MONTH, mon-1);

                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d", mon, day, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    userBday.setText(current);
                    userBday.setSelection(sel < current.length() ? sel : current.length());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        thisActivity = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main_menu. this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.save_button:
                saveUserForm();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void saveUserForm() {
        String userPhoto = "";
        String userNameD = userName.getText().toString().trim();
        String userAgeD = userAge.getText().toString().trim();
        String userBdayD = userBday.getText().toString().trim();
        String userContactD = userContact.getText().toString().trim();
        String userAddressD = userAddress.getText().toString().trim();
        String userEmergName1D = userEmergName1.getText().toString().trim();
        String userEmergContact1D = userEmergContact1.getText().toString().trim();
        String userEmergName2D = userEmergName2.getText().toString().trim();
        String userEmergContact2D = userEmergContact2.getText().toString().trim();
        String userSexD = sexField.getSelectedItem().toString();

        if (!isValidUserNumber(userContactD)) {
            helper.showToast("Invalid user contact number");
            userContact.requestFocus();
        } else if (!isValidUserNumber(userEmergContact1D)) {
            helper.showToast("Invalid user contact number");
            userEmergContact1.requestFocus();
        } else if (!isValidUserNumber(userEmergContact2D)) {
            helper.showToast("Invalid user contact number");
            userEmergContact2.requestFocus();
        } else {
            String[] userData = {userNameD, userAgeD, userBdayD, userSexD, userContactD, userAddressD, userEmergName1D, userEmergContact1D, userEmergName2D, userEmergContact2D, userPhoto};

            long saveOrUpdateUser = (userId != null && !userId.isEmpty()) ? db.editUSer(userData, userId) : db.addUSer(userData);

            if (saveOrUpdateUser > 0) {
                Toast.makeText(this, "User details were successfully updated.", Toast.LENGTH_SHORT).show();
                Intent intentCon = new Intent(getApplication(), MainActivity.class);
                startActivity(intentCon);
            } else {
                Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isValidUserNumber(String number) {
        if (number == null || number.isEmpty()) {
            return true;
        }

        boolean flag = true;
        String[] num = number.split("");
        number = number.replaceAll(" ","s");

        if ((num.length-1 == 11 && num[1].equals("0") && num[2].equals("9")) || (num.length-1 == 10 && num[1].equals("9"))) {
            try {
                long n = Long.parseLong(number);
            } catch (Exception e) {
                flag = false;
            }
        }
        else flag = false;

        return flag;
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