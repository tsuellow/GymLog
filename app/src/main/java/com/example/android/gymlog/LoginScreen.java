package com.example.android.gymlog;

import android.content.Intent;
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
        toolbar.setTitle("Login");



        otpView = findViewById(R.id.otp_view);

        otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override public void onOtpCompleted(String otp) {
                if (otp.contentEquals("1234")){
                    Intent i = new Intent(LoginScreen.this,SearchActivity.class);
                    startActivity(i);
                }else{
                    Toast toast=Toast.makeText(getApplicationContext(),"wrong password",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });


    }


}
