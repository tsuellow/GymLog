package com.example.android.gymlog;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.gymlog.data.ClientDao;
import com.example.android.gymlog.data.ClientEntry;
import com.example.android.gymlog.data.GymDatabase;
import com.example.android.gymlog.data.PaymentDao;
import com.example.android.gymlog.data.PaymentEntry;
import com.example.android.gymlog.data.VisitDao;
import com.example.android.gymlog.data.VisitEntry;
import com.example.android.gymlog.utils.DateUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import android.view.Menu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity{


    private Button mSearch;
    private Context mContext;


    private GymDatabase mDb;

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;

    private String lastText;
    private ClientEntry mClientData;
    private PaymentEntry mPaymentData;
    private MediaPlayer mSoundPass;
    private MediaPlayer mSoundFail;


    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null
                    || result.getText().equals(lastText)
                    ) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();
            //barcodeScannerView.setStatusText(result.getText());
            try {
                JSONObject jObj = new JSONObject(lastText);
                int ufId = jObj.getInt("ufid");
                retrieveClientData(ufId);
            }catch(JSONException e){
                Toast.makeText(getApplicationContext(), lastText, Toast.LENGTH_LONG).show();
            }


        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearch=(Button) findViewById(R.id.bt_search);

        mContext=getApplicationContext();

        mDb=GymDatabase.getInstance(mContext);

        barcodeScannerView = (DecoratedBarcodeView)findViewById(R.id.zxing_barcode_scanner);

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeScannerView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeScannerView.initializeFromIntent(getIntent());
        barcodeScannerView.setStatusText("Focus barcode");
        barcodeScannerView.decodeContinuous(callback);

        mSoundPass=MediaPlayer.create(MainActivity.this,R.raw.correct_sound);
        mSoundFail=MediaPlayer.create(MainActivity.this,R.raw.error_sound);

//        mScan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
//                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
//                intentIntegrator.setPrompt("Scan");
//                intentIntegrator.setCameraId(1);
//                intentIntegrator.setBeepEnabled(false);
//                intentIntegrator.setBarcodeImageEnabled(false);
//                intentIntegrator.initiateScan();
//            }
//        });

        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getApplicationContext(),ClientsSearchActivity.class);
                startActivity(i);
            }
        });


    }
    //test
    @Override
    protected void onResume() {
        super.onResume();

        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeScannerView.pause();
    }
    //test

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.opt_admin:{
                Intent login =new Intent(getApplicationContext(),LoginScreen.class);
                startActivity(login);
                break;
            }
            case R.id.opt_class:{
                Toast.makeText(MainActivity.this,"class",Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.opt_settings:{
                Toast.makeText(MainActivity.this,"settings",Toast.LENGTH_LONG).show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void retrieveClientData(int clientId){
        counter=0;
        final LiveData<ClientEntry> client = mDb.clientDao().getClientById(clientId);
        client.observe(this, new Observer<ClientEntry>() {
            @Override
            public void onChanged(@Nullable ClientEntry clientEntry) {
                client.removeObserver(this);
                mClientData=clientEntry;
                //Toast.makeText(getApplicationContext(),mClientData.getFirstName(),Toast.LENGTH_LONG).show();
                taskCompleted();
            }
        });
        final LiveData<PaymentEntry> payment = mDb.paymentDao().getLastPaymentByClient(clientId);
        payment.observe(this, new Observer<PaymentEntry>() {
            @Override
            public void onChanged(@Nullable PaymentEntry paymentEntry) {
                payment.removeObserver(this);
                mPaymentData=paymentEntry;
                //Toast.makeText(getApplicationContext(),mPaymentData.getProduct(),Toast.LENGTH_LONG).show();
                taskCompleted();
            }
        });



    }
    //sync db response
    private  final int NUMBER_DB_CALLS = 2;
    private  int counter = 0;
    public synchronized void taskCompleted(){
        counter++;
        if(counter == NUMBER_DB_CALLS){

            Handler forgetLastQR  = new Handler();
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    lastText=null;
                }
            };

            if (mClientData==null){
                Toast.makeText(getApplicationContext(),"This QR code has not been assigned",Toast.LENGTH_LONG).show();
                forgetLastQR.postDelayed(run, 5000);
            }else if (mPaymentData==null){
                Toast.makeText(getApplicationContext(),mClientData.getFirstName()+" has not yet paid",Toast.LENGTH_LONG).show();
                forgetLastQR.postDelayed(run, 5000);
            }else{
                //check if is paying client
                Date today=DateUtils.getRoundDate(new Date());
                boolean isPayingClient=true;
                String access="G";
                if (today.after(mPaymentData.getPaidUntil()) || mPaymentData.getPaidFrom().before(today)){
                    isPayingClient=false;
                    access="D";
                }
                //make DB insert
                final VisitEntry visitEntry= new VisitEntry(mClientData.getId(),new Date(),access);
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.visitDao().insertVisit(visitEntry);
                    }
                });

                //display dialog box
                displayDialog(isPayingClient);
            }
        }
    }

    //make dialog pop up
    private void displayDialog(final boolean isPayingClient){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(MainActivity.this);
        View mView=getLayoutInflater().inflate(R.layout.dialog_welcome,null);
        Date today=DateUtils.getRoundDate(new Date());
        long daysLeft=TimeUnit.DAYS.convert(mPaymentData.getPaidUntil().getTime()-today.getTime(), TimeUnit.MILLISECONDS);

        TextView mFirstLineTop=(TextView) mView.findViewById(R.id.tv_welcome);
        TextView mSecondLineTop=(TextView) mView.findViewById(R.id.tv_welcome_name);
        TextView mFirstLineBottom=(TextView) mView.findViewById(R.id.tv_payment_info1);
        TextView mSecondLineBottom=(TextView) mView.findViewById(R.id.tv_payment_info2);
        ImageView mPhoto=(ImageView) mView.findViewById(R.id.iv_welcome_image);
        View mTopStrip=(View) mView.findViewById(R.id.v_top_view);
        View mBottomStrip=(View) mView.findViewById(R.id.v_bottom_view);

        if (mClientData.getPhoto()!=null){
            setPic(mPhoto,mClientData.getPhoto());
        }else{
            mPhoto.setImageResource(android.R.drawable.ic_menu_camera);
        }

        Button mDismiss=(Button) mView.findViewById(R.id.bt_back);

        //conditional on passing
        if (isPayingClient){
            mTopStrip.setBackgroundColor(Color.parseColor("#14dc02"));
            mBottomStrip.setBackgroundColor(Color.parseColor("#14dc02"));
            mFirstLineTop.setText("Welcome");
            mSecondLineTop.setText(mClientData.getFirstName()+",");
            mFirstLineBottom.setText(""+daysLeft);
            mSecondLineBottom.setText("days of access remaining");
            if (daysLeft<4){
                mFirstLineBottom.setTextColor(Color.parseColor("#ed0400"));
            }else{
                mFirstLineBottom.setTextColor(getResources().getColor(android.R.color.tertiary_text_dark));
            }
        }else{
            mTopStrip.setBackgroundColor(Color.parseColor("#ed0400"));
            mBottomStrip.setBackgroundColor(Color.parseColor("#ed0400"));
            mFirstLineTop.setText("Sorry "+mClientData.getFirstName()+",");
            mSecondLineTop.setText("it seems that your access to the gym has expired.");
            mFirstLineBottom.setText("Please proceed to the counter to pay for access");
            mSecondLineBottom.setText("Thanks for prefering us");
            //Format
            mFirstLineTop.setTextSize(28);
            mFirstLineTop.setTypeface(Typeface.DEFAULT_BOLD);
            mSecondLineTop.setTextSize(12);
            mSecondLineTop.setTypeface(Typeface.DEFAULT);
            mFirstLineBottom.setTextSize(12);
            mFirstLineBottom.setTypeface(Typeface.DEFAULT);
            mSecondLineBottom.setTypeface(Typeface.DEFAULT);
        }

        mBuilder.setView(mView);
        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // Handler to
        Handler handler  = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        };
        handler.postDelayed(runnable, 9000);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                if (isPayingClient){
                    mSoundPass.start();
                }else{
                    mSoundFail.start();
                }

            }
        });
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Toast.makeText(getApplicationContext(),"dialog just closed drop al vars",Toast.LENGTH_LONG).show();
                mClientData=null;
                mPaymentData=null;
                lastText=null;
            }
        });
    }

    private void setPic(ImageView imageView, String path) {
        // Get the dimensions of the View
        int targetW = 180;
        int targetH = 180;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path,bmOptions);
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
        RoundedBitmapDrawable roundedBitmapDrawable=RoundedBitmapDrawableFactory.create(getResources(),cropImg);
        roundedBitmapDrawable.setCircular(true);
        imageView.setImageDrawable(roundedBitmapDrawable);
    }



//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        IntentResult intentResult=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
//        if (intentResult!=null){
//            if (intentResult.getContents()==null){
//                Toast.makeText(this,"Scan unsuccessful", Toast.LENGTH_LONG).show();
//            }else{
//                Toast.makeText(this, intentResult.getContents(), Toast.LENGTH_LONG).show();
//            }
//        }else{
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
}
