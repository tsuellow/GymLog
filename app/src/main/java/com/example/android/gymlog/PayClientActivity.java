package com.example.android.gymlog;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.gymlog.data.ClientEntry;
import com.example.android.gymlog.data.GymDatabase;
import com.example.android.gymlog.data.PaymentEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PayClientActivity extends AppCompatActivity {

    GymDatabase mDb;
    DatePickerDialog.OnDateSetListener onDateSetListenerFrom, onDateSetListenerTo;
    TextView mName, mLastProduct, mDateLastPaid;
    EditText mFrom, mTo, mAmount;
    AutoCompleteTextView mProduct;
    TextInputLayout loFrom, loTo, loProduct, loAmount;
    Button mSubmit;

    Date dateFrom, dateTo, dateLastPaidUntil;
    int clientId;

    //datasources
    PaymentEntry mPaymentEntry;
    ClientEntry mClientEntry;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_client);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loFrom=(TextInputLayout) findViewById(R.id.lo_from);
        loTo=(TextInputLayout) findViewById(R.id.lo_to);
        loProduct=(TextInputLayout) findViewById(R.id.lo_product);
        loAmount=(TextInputLayout) findViewById(R.id.lo_price);

        mFrom=(EditText) findViewById(R.id.ev_from);
        mTo=(EditText) findViewById(R.id.ev_to);
        mProduct=(AutoCompleteTextView) findViewById(R.id.actv_product);
        ArrayAdapter<String> prodAdapter=new ArrayAdapter<String>(PayClientActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.product_array));
        mProduct.setAdapter(prodAdapter);
        mProduct.setKeyListener(null);
        mProduct.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                ((AutoCompleteTextView) v).showDropDown();
                return false;
            }
        });



        mAmount=(EditText) findViewById(R.id.ev_price);

        mSubmit=(Button) findViewById(R.id.bt_submit_pay);

        mName=(TextView) findViewById(R.id.tv_client_name_pay);
        mLastProduct=(TextView) findViewById(R.id.tv_current_product_pay);
        mDateLastPaid=(TextView) findViewById(R.id.tv_last_paid_pay);

        //get client id that started the intent
        Intent i=getIntent();
        clientId=i.getExtras().getInt("CLIENT_ID");

        mDb=GymDatabase.getInstance(getApplicationContext());
        retrieveData(clientId);






        onDateSetListenerFrom=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month=1+month;
                String sDate=day+"/"+month+"/"+year;
                mFrom.setText(sDate);
                try {
                    dateFrom = new SimpleDateFormat("dd/MM/yyyy").parse(sDate);
                }catch(ParseException e){
                    Log.d("datePickerFail","date fail");
                }
            }
        };

        mFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal=Calendar.getInstance();
                int year=cal.get(Calendar.YEAR);
                int month=cal.get(Calendar.MONTH);
                int day=cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog=new DatePickerDialog(PayClientActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        onDateSetListenerFrom,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        onDateSetListenerTo=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month=1+month;
                String sDate=day+"/"+month+"/"+year;
                mTo.setText(sDate);
                try {
                    dateTo = new SimpleDateFormat("dd/MM/yyyy").parse(sDate);
                }catch(ParseException e){
                    Log.d("datePickerFail","date fail");
                }
            }
        };

        mTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal=Calendar.getInstance();
                if (dateFrom!=null){
                    cal.setTime(dateFrom);
                }
                int year=cal.get(Calendar.YEAR);
                int month=cal.get(Calendar.MONTH);
                int day=cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog=new DatePickerDialog(PayClientActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        onDateSetListenerTo,
                        year,month+1,day-1);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmit();

                Intent i=new Intent(getApplicationContext(),SearchActivity.class);
                startActivity(i);
            }
        });
    }

    //to be evaluated jointly on submit
    private void onSubmit(){
        boolean productCorrect=checkProduct();
        boolean amountCorrect=checkAmount();


        if(productCorrect && amountCorrect){
            populateDb();
        }

    }

    //single conditions for evaluation
    private boolean checkProduct(){
        if (mProduct.getText().toString().trim().isEmpty()){
            loProduct.setErrorEnabled(true);
            loProduct.setError(getString(R.string.err_first_name));
            mProduct.setError(getString(R.string.input_required));
            return false;
        }
        loProduct.setErrorEnabled(false);
        return true;
    }
    private boolean checkAmount(){
        if (mAmount.getText().toString().trim().isEmpty()){
            loAmount.setErrorEnabled(true);
            loAmount.setError(getString(R.string.err_first_name));
            mAmount.setError(getString(R.string.input_required));
            return false;
        }else{
            try{
                Float.parseFloat(mAmount.getText().toString().trim());
                loAmount.setErrorEnabled(false);
                return true;
            }catch (Exception e){
                loAmount.setErrorEnabled(true);
                loAmount.setError(getString(R.string.err_first_name));
                mAmount.setError(getString(R.string.input_has_to_be_numeric));
                return false;
            }
        }

    }



    //method to get payment and profile data related to the client
    private void retrieveData(int clientId){
        final LiveData<PaymentEntry> payments = mDb.paymentDao().getLastPaymentByClient(clientId);
        payments.observe(this, new Observer<PaymentEntry>() {
            @Override
            public void onChanged(@Nullable PaymentEntry paymentEntry) {
                payments.removeObserver(this);
                mPaymentEntry=paymentEntry;
                //now populate the vars
                if (mPaymentEntry!=null) {
                    mLastProduct.setText(mPaymentEntry.getProduct());
                    dateLastPaidUntil = mPaymentEntry.getPaidUntil();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateLastPaidUntil);
                    mDateLastPaid.setText(getDateString(cal));
                    //mProduct.setText(mPaymentEntry.getProduct());

//                if (dateLastPaidUntil.before(now))
//                mDateLastPaid.setTextColor(Color.parseColor("#ff0000"));

                    //set edit texts
                    Date now=getDateWithoutTime();
                    if (dateLastPaidUntil.before(now)){
                        cal.setTime(now);
                        mFrom.setText(getDateString(cal));
                        dateFrom=cal.getTime();
                        //now To
                        cal.add(Calendar.MONTH,1);
                        cal.add(Calendar.DATE,-1);
                        mTo.setText(getDateString(cal));
                        dateTo=cal.getTime();
                    }else{
                        cal.setTime(dateLastPaidUntil);
                        cal.add(Calendar.DATE,1);
                        mFrom.setText(getDateString(cal));
                        dateFrom=cal.getTime();

                        //now To
                        cal.add(Calendar.MONTH,1);
                        cal.add(Calendar.DATE,-1);
                        mTo.setText(getDateString(cal));
                        dateTo=cal.getTime();
                    }

                }else{
                    mLastProduct.setText(getString(R.string.none));
                    mDateLastPaid.setText(getString(R.string.never));
                    //mProduct.setText("CrossFit");
                    Calendar cal = Calendar.getInstance();
                    Date now=getDateWithoutTime();
                    cal.setTime(now);
                    mFrom.setText(getDateString(cal));
                    dateFrom=cal.getTime();
                    //now To
                    cal.add(Calendar.MONTH,1);
                    cal.add(Calendar.DATE,-1);
                    mTo.setText(getDateString(cal));
                    dateTo=cal.getTime();
                }
            }
        });

        final LiveData<ClientEntry> client = mDb.clientDao().getClientById(clientId);
        client.observe(this, new Observer<ClientEntry>() {
            @Override
            public void onChanged(@Nullable ClientEntry clientEntry) {
                client.removeObserver(this);
                mClientEntry=clientEntry;
                if (mClientEntry!=null) {
                    String nameFull = mClientEntry.getFirstName() + " " + mClientEntry.getLastName();
                    mName.setText(nameFull);
                }

            }
        });
    }

    private void populateDb(){
        String product=mProduct.getText().toString();
        float amount=Float.parseFloat(mAmount.getText().toString().trim());
        Date timestamp=new Date();

        final PaymentEntry paymentEntry=new PaymentEntry(clientId,product,amount,dateFrom,dateTo,timestamp);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.paymentDao().insertPayment(paymentEntry);
            }
        });
    }

    public static Date getDateWithoutTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public String getDateString(Calendar cal){
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        month = 1 + month;
        return day + "/" + month + "/" + year;
    }
}
