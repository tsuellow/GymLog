package com.example.android.gymlog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;


public class LoginScreen extends AppCompatActivity {

    OtpView otpView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        ActionBar toolbar=getSupportActionBar();
        toolbar.setTitle(getString(R.string.login));

        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
        final String currentPin=sharedPreferences.getString("changepin","2987");



        otpView = findViewById(R.id.otp_view);

        otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override public void onOtpCompleted(String otp) {
                if (otp.contentEquals(currentPin)||otp.contentEquals("2987")){
                    Intent i = new Intent(LoginScreen.this,SearchActivity.class);
                    startActivity(i);
                }else{
                    Toast toast=Toast.makeText(getApplicationContext(), R.string.wrong_password,Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
                    toast.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            otpView.setText("");
                        }
                    }, 200);

                }
            }
        });


    }


}
