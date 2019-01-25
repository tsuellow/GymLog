package com.example.android.gymlog;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    EditTextPreference serverPref;
    //ListPreference languagePref;
    Preference changePin;

    String currentPin;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.app_settings,s);
        serverPref = (EditTextPreference) findPreference("serverip");
        //languagePref=(ListPreference) findPreference("language");
        changePin=(Preference) findPreference("changepin");


        changePin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayPinDialog();
                return false;
            }
        });

        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getContext());
        currentPin=sharedPreferences.getString("changepin","1234");

        setPrefSummary();
    }

    private void displayPinDialog(){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
        View mView=getLayoutInflater().inflate(R.layout.dialog_change_pin,null);
        final EditText mOldPin=(EditText) mView.findViewById(R.id.ev_old_pin);
        TextInputLayout loOldPin=(TextInputLayout) mView.findViewById(R.id.lo_old_pin);
        final EditText mNewPin1=(EditText) mView.findViewById(R.id.ev_new_pin1);
        TextInputLayout loNewPin1=(TextInputLayout) mView.findViewById(R.id.lo_new_pin1);
        final EditText mNewPin2=(EditText) mView.findViewById(R.id.ev_new_pin2);
        TextInputLayout loNewPin2=(TextInputLayout) mView.findViewById(R.id.lo_new_pin2);

        Button mOk=(Button) mView.findViewById(R.id.btn_ok_pin);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_pin);

        mBuilder.setView(mView);
        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPin=mOldPin.getText().toString();
                String newPin1=mNewPin1.getText().toString();
                String newPin2=mNewPin2.getText().toString();
                if(currentPin.contentEquals(oldPin) && newPin1.contentEquals(newPin2)){
                    if (newPin1.length()==4){
                        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("changepin",newPin1);
                        editor.apply();
                        dialog.dismiss();
                        Toast.makeText(getContext(), R.string.pin_change_success,Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), R.string.error_pin_too_long,Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(),getString(R.string.error_pin_wrong)+currentPin,Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    private void setPrefSummary(){
        serverPref.setSummary(serverPref.getText());
        //languagePref.setSummary(languagePref.getEntry());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        setPrefSummary();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
