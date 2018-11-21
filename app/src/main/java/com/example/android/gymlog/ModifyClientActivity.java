package com.example.android.gymlog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.TextInputLayout;

import com.example.android.gymlog.data.ClientEntry;
import com.example.android.gymlog.data.GymDatabase;
import com.example.android.gymlog.data.PaymentEntry;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ModifyClientActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_CODE = 10;
    EditText  mFirstName,mLastName,mPhone, mDob;
    AutoCompleteTextView mOccupation;
    TextInputLayout ilId,ilFirstName,ilLastName,ilPhone, ilOccupation, ilDob;
    TextView mId;
    RadioGroup mGender;
    RadioButton mMaleRb, mFemaleRb;

    ImageView mPhoto;
    Button mTakePic;
    Button mSubmit;
    DatePickerDialog.OnDateSetListener onDateSetListener;

    Date dateOfBirth;
    String mCurrentPhotoPath;

    int clientId;


    private GymDatabase mDb;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_client);




        mId=(TextView) findViewById(R.id.tv_id_mod);
        mFirstName =(EditText) findViewById(R.id.ev_first_name_mod);
        mLastName=(EditText) findViewById(R.id.ev_last_name_mod);
        mPhone=(EditText) findViewById(R.id.ev_phone_mod);

        mOccupation=(AutoCompleteTextView) findViewById(R.id.actv_occupation_mod);
        ArrayAdapter<String> occupationAdapter=new ArrayAdapter<String>(ModifyClientActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.occupation_array));
        mOccupation.setAdapter(occupationAdapter);
        mOccupation.setKeyListener(null);
        mOccupation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOccupation.setText(null);
                ((AutoCompleteTextView) view).showDropDown();
                return;
            }
        });


        /*        mOccupation.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                mOccupation.setText(null);
                ((AutoCompleteTextView) v).showDropDown();
                return false;
            }
        });*/

        mDob=(EditText) findViewById(R.id.ev_dob_mod);
        mDob.setInputType(InputType.TYPE_NULL);

        ilId=(TextInputLayout) findViewById(R.id.lo_id_mod);
        ilFirstName=(TextInputLayout) findViewById(R.id.lo_first_name_mod);
        ilLastName=(TextInputLayout) findViewById(R.id.lo_last_name_mod);
        ilPhone=(TextInputLayout) findViewById(R.id.lo_phone_mod);
        ilOccupation=(TextInputLayout) findViewById(R.id.lo_occupation_mod);
        ilDob=(TextInputLayout) findViewById(R.id.lo_dob_mod);

        mGender=(RadioGroup) findViewById(R.id.rg_gender_mod);
        mMaleRb=(RadioButton) findViewById(R.id.rb_male_mod);
        mFemaleRb=(RadioButton) findViewById(R.id.rb_female_mod);

        mPhoto=(ImageView) findViewById(R.id.iv_photo_mod);
        mTakePic=(Button) findViewById(R.id.bt_take_photo_mod);
        mSubmit=(Button) findViewById(R.id.bt_submit_mod);

        mDb=GymDatabase.getInstance(getApplicationContext());
        //get client id that started the intent
        Intent i=getIntent();
        clientId = i.getExtras().getInt("CLIENT_ID");

        mDb=GymDatabase.getInstance(getApplicationContext());
        retrieveData(clientId);



        mPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher("NI"));

        //date picker clicklistener
        onDateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month=1+month;
                String sDate=day+"/"+month+"/"+year;
                mDob.setText(sDate);
                try {
                    dateOfBirth = new SimpleDateFormat("dd/MM/yyyy").parse(sDate);
                }catch(ParseException e){
                    Log.d("belloxxx","date fail");
                }
            }
        };

        mDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Calendar cal=Calendar.getInstance();
//                int year=cal.get(Calendar.YEAR);
//                int month=cal.get(Calendar.MONTH);
//                int day=cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog=new DatePickerDialog(ModifyClientActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        onDateSetListener,
                        2000,00,01);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        //take photo click listener
        mTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) {
                        dispatchTakePictureIntent();

                    }else{
                        String[] permissionRequested={Manifest.permission.CAMERA};
                        requestPermissions(permissionRequested, REQUEST_CODE);
                    }

                }


            }
        });

        //submit click listener
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmit();
            }
        });

    }

    //to be evaluated jointly on submit
    private void onSubmit(){
        boolean firstNameCorrect=checkFirstName();
        boolean lastNameCorrect=checkLastName();
        boolean phoneCorrect=checkPhoneNumber();
        //boolean idCorrect=checkId();

        if(firstNameCorrect && lastNameCorrect && phoneCorrect){
            populateDb();
        }

    }

    //single conditions for evaluation
    private boolean checkFirstName(){
        if (mFirstName.getText().toString().trim().isEmpty()){
            ilFirstName.setErrorEnabled(true);
            ilFirstName.setError(getString(R.string.err_first_name));
            mFirstName.setError("Input required");
            return false;
        }
        ilFirstName.setErrorEnabled(false);
        return true;
    }
    private boolean checkLastName(){
        if (mLastName.getText().toString().trim().isEmpty()){
            ilLastName.setErrorEnabled(true);
            ilLastName.setError(getString(R.string.err_last_name));
            mLastName.setError("Input required");
            return false;
        }
        ilLastName.setErrorEnabled(false);
        return true;
    }
    private boolean checkPhoneNumber(){
        String phoneText=mPhone.getText().toString().replace(" ","")
                .replace("+","00").replace("-","");
        if (phoneText.length()==0){
            ilPhone.setErrorEnabled(false);
            return true;
        }
        try{
            Integer.parseInt(phoneText);
            if (phoneText.length()>=8){
                ilPhone.setErrorEnabled(false);
                return true;
            }else{
                ilPhone.setErrorEnabled(true);
                ilPhone.setError(getString(R.string.err_phone));
                mPhone.setError("valid phone required");
                return false;
            }
        }catch(Exception e){
            ilPhone.setErrorEnabled(true);
            ilPhone.setError(getString(R.string.err_phone));
            mPhone.setError("valid phone required");
            return  false;
        }
    }

    /*//check id validity with two different error messages
    int isNew;
    void setIsNewId(int result){
        isNew=result;
    }
    private boolean checkId(){
        String id=mId.getText().toString().trim();


        try{
            final int parseId=Integer.parseInt(id);

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    final int result=mDb.clientDao().isIdNew(parseId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setIsNewId(result);
                        }
                    });
                }
            });

        }catch(Exception e){
            ilId.setErrorEnabled(true);
            ilId.setError("Id has to be numeric");
            mId.setError("Id has to be numeric");
            Toast.makeText(this,"Id needs to be numeric", Toast.LENGTH_LONG).show();
            return false;
        }
        if (isNew==1 && !id.isEmpty()){
            ilId.setErrorEnabled(false);
            return  true;
        }else{
            ilId.setErrorEnabled(true);
            ilId.setError("Id has to be new");
            mId.setError("Id needs to be new");
            Toast.makeText(this,"Id needs to be new", Toast.LENGTH_LONG).show();
            return false;
        }
    }*/





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_CODE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else{
                Toast.makeText(this,"you need to grant permission for this to work",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==REQUEST_TAKE_PHOTO && resultCode==RESULT_OK){
            setPic();
        }
    }

    private void populateDb() {
        int genderId=mGender.getCheckedRadioButtonId();
        String gender;
        if (genderId==R.id.rb_male_mod){
            gender="m";
        }else{
            gender="f";
        }
        String firstName= mFirstName.getText().toString();
        String lastName=mLastName.getText().toString();
        String phone=mPhone.getText().toString();
        String occupation=mOccupation.getText().toString();
        Date date=new Date();

        final ClientEntry clientEntry=new ClientEntry(clientId,firstName,lastName,dateOfBirth,gender,occupation,phone, mCurrentPhotoPath, null, date);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.clientDao().updateClient(clientEntry);
            }
        });


        Intent i=new Intent(getApplicationContext(),SearchActivity.class);
        startActivity(i);

    }

    //take a photo functionality


    private File createImageFile() throws IOException {
        // Create an image file name
        String idPart = mId.getText().toString();//new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PHOTO_ID_" + idPart ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("bellox",  mCurrentPhotoPath);
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("bello2",  "error creating picture");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Log.d("bello",  photoURI.toString());

            }

        }

    }


    //code to frame and display pic
    private void setPic() {
        // Get the dimensions of the View
        int targetW = mPhoto.getWidth();
        int targetH = mPhoto.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,bmOptions);
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
        mPhoto.setImageDrawable(roundedBitmapDrawable);
    }


    //method to get payment and profile data related to the client
    private void retrieveData(int clientId){
        final LiveData<ClientEntry> client = mDb.clientDao().getClientById(clientId);
        client.observe(this, new Observer<ClientEntry>() {
            @Override
            public void onChanged(@Nullable ClientEntry clientEntry) {
                client.removeObserver(this);
                //now set all vars
                mId.setText("ID: "+clientEntry.getId());
                mFirstName.setText(clientEntry.getFirstName());
                mLastName.setText(clientEntry.getLastName());
                if (clientEntry.getOccupation()!=null) mOccupation.setText(clientEntry.getOccupation());
                if (clientEntry.getPhone()!=null) mPhone.setText(clientEntry.getPhone());
                //dob setting
                dateOfBirth=clientEntry.getDob();
                if (dateOfBirth!=null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateOfBirth);
                    mDob.setText(getDateString(cal));
                }
                //gender setting
                if (clientEntry.getGender()=="m"){
                    mMaleRb.setChecked(true);
                }else{
                    mFemaleRb.setChecked(true);
                }
                //set pick
                mCurrentPhotoPath=clientEntry.getPhoto();
                if (mCurrentPhotoPath!=null) {
                    setPic();
                }
            }
        });

    }

    public String getDateString(Calendar cal){
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        month = 1 + month;
        return day + "/" + month + "/" + year;
    }


}

