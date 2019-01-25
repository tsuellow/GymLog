package com.example.android.gymlog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.android.gymlog.data.ClientEntry;
import com.example.android.gymlog.data.DateConverter;
import com.example.android.gymlog.data.GymDatabase;
import com.example.android.gymlog.data.PaymentEntry;
import com.example.android.gymlog.data.VisitEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DataBackup {
    Context mContext;


    public DataBackup(Context context) {
        mContext=context;
    }

    SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(SearchActivity.getAppCont());

    String SERVER_IP = sharedPreferences.getString("serverip", "192.168.1.6");

    //functions and logic for data backup

    //ckeck internet connectivity
    public  boolean hasInternetConnectivity(){
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return ((netInfo != null) && (netInfo.isConnectedOrConnecting()));
    }

    //ckeck host availability
    public  boolean hasHostAccess(){
        try {
            InetAddress ip= InetAddress.getByName(SERVER_IP.trim());
            int port=80;
            SocketAddress socketAddress = new InetSocketAddress(ip,port);
            Socket socket = new Socket();
            int timeoutMs = 5000;   // 3 seconds
            socket.connect(socketAddress, timeoutMs);
            return true;
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String SERVER_URL="http://"+SERVER_IP.trim()+"/gymlog/";
            //"http://"+SERVER_IP+"/gymlog/";

    //set counters
    //for clients
    private int COUNTER_SYNCED_CLIENT;
    private int SYNC_CLIENT_VOLUME;
    //for payments
    private int COUNTER_SYNCED_PAYMENT;
    private int SYNC_PAYMENT_VOLUME;
    //for visits
    private int COUNTER_SYNCED_VISIT;
    private int SYNC_VISIT_VOLUME;

    //backup entire client synclist
    public void backupClientTable(List<ClientEntry> clients, List<PaymentEntry> payments, List<VisitEntry>  visits){
        COUNTER_SYNCED_CLIENT =0;
        SYNC_CLIENT_VOLUME=clients.size();
        COUNTER_SYNCED_PAYMENT =0;
        SYNC_PAYMENT_VOLUME=payments.size();
        COUNTER_SYNCED_VISIT =0;
        SYNC_VISIT_VOLUME=visits.size();
        CountDownLatch requestCountDown = new CountDownLatch(SYNC_CLIENT_VOLUME+SYNC_PAYMENT_VOLUME+SYNC_VISIT_VOLUME);
        for (int i=0;i<SYNC_CLIENT_VOLUME;i++){
            syncSingleClient(clients.get(i),mContext,requestCountDown);
        }
        for (int i=0;i<SYNC_PAYMENT_VOLUME;i++){
            syncSinglePayment(payments.get(i),mContext,requestCountDown);
        }
        for (int i=0;i<SYNC_VISIT_VOLUME;i++){
            syncSingleVisit(visits.get(i),mContext,requestCountDown);
        }
        new Thread(new ThreadToBeHeld(requestCountDown)).start();
    }


    class ThreadToBeHeld implements Runnable {
        CountDownLatch requestCountDown;
        Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        public ThreadToBeHeld(CountDownLatch requestCountDown) {
            this.requestCountDown = requestCountDown;
        }
        public void run() {
            try {
                //requestCountDown.countDown();
                requestCountDown.await();

            }catch(InterruptedException ex){}
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(getApplicationContext(),"clients "+COUNTER_SYNCED_CLIENT+
//                            "payments "+COUNTER_SYNCED_PAYMENT+"" +
//                            "visits "+COUNTER_SYNCED_VISIT,Toast.LENGTH_LONG).show();
                    showPositiveDialog();
                }
            });
        }
    };

    class CountdownExecuted implements Runnable {
        CountDownLatch requestCountDown;
        public CountdownExecuted(CountDownLatch requestCountDown) {
            this.requestCountDown = requestCountDown;
        }
        public void run() {
            requestCountDown.countDown();
        }};


    //sync single client record to server
    private void syncSingleClient(final ClientEntry client, final Context context, final CountDownLatch countDownLatch){

        JSONObject params=new JSONObject();
        try {
            params.put("id", client.getId());
            params.put("firstname", null2String(client.getFirstName()));
            params.put("lastname", null2String(client.getLastName()));
            params.put("dob", null2String(DateConverter.getDateString(client.getDob())));
            params.put("gender", null2String(client.getGender()));
            params.put("occupation", null2String(client.getOccupation()));
            params.put("phone", null2String(client.getPhone()));
            params.put("photo", null2String(client.getPhoto()));
            params.put("qrcode", null2String(client.getQrCode()));
            params.put("lastupdated", null2String(DateConverter.getDateString(client.getLastUpdated())));

        }catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest;

            jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, SERVER_URL + "client_insert.php", params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String res = response.getString("response");
                                if (res.equals("OK")) {
                                    COUNTER_SYNCED_CLIENT++;
                                    //update sqlite db
                                    client.setSyncStatus(1);
                                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            GymDatabase.getInstance(context).clientDao().updateClient(client);
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            new Thread(new CountdownExecuted(countDownLatch)).start();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            new Thread(new CountdownExecuted(countDownLatch)).start();
                        }
                    });
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    //sync single payment record to server
    private void syncSinglePayment(final PaymentEntry payment, final Context context, final CountDownLatch countDownLatch){

        JSONObject params=new JSONObject();
        try {
            params.put("id", payment.getId());
            params.put("clientid", payment.getClientId());
            params.put("product", null2String(payment.getProduct()));
            params.put("amountusd", payment.getAmountUsd());
            params.put("paidfrom", null2String(DateConverter.getDateString(payment.getPaidFrom())));
            params.put("paiduntil", null2String(DateConverter.getDateString(payment.getPaidUntil())));
            params.put("timestamp", null2String(DateConverter.getDateString(payment.getTimestamp())));
            params.put("isvalid", payment.getIsValid());

        }catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest;

            jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, SERVER_URL + "payment_insert.php", params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String res = response.getString("response");
                                if (res.equals("OK")) {
                                    COUNTER_SYNCED_PAYMENT++;
                                    //update sqlite db
                                    payment.setSyncStatus(1);
                                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            GymDatabase.getInstance(context).paymentDao().updatePayment(payment);
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            new Thread(new CountdownExecuted(countDownLatch)).start();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            new Thread(new CountdownExecuted(countDownLatch)).start();
                        }
                    });

        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    //sync single client record to server
    private void syncSingleVisit(final VisitEntry visit, final Context context, final CountDownLatch countDownLatch){

        JSONObject params=new JSONObject();
        try {
            params.put("id", visit.getId());
            params.put("clientid", visit.getClientId());
            params.put("timestamp", null2String(DateConverter.getDateString(visit.getTimestamp())));
            params.put("access", null2String(visit.getAccess()));
        }catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest;
        jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, SERVER_URL + "visit_insert.php", params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String res = response.getString("response");
                            if (res.equals("OK")) {
                                COUNTER_SYNCED_VISIT++;
                                //update sqlite db
                                visit.setSyncStatus(1);
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        GymDatabase.getInstance(context).visitDao().updateVisit(visit);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new Thread(new CountdownExecuted(countDownLatch)).start();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        new Thread(new CountdownExecuted(countDownLatch)).start();
                    }
                });
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    //convert null to ""
    private String null2String(String textField){
        if (textField==null){
            textField="";
        }
        return textField;
    }

    private void showPositiveDialog(){
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(mContext.getString(R.string.backup_finished));
        alertDialog.setMessage(""+COUNTER_SYNCED_CLIENT+"/"+SYNC_CLIENT_VOLUME+" "+mContext.getString(R.string.clients)+ " \n"+
                COUNTER_SYNCED_PAYMENT+"/"+SYNC_PAYMENT_VOLUME+" "+mContext.getString(R.string.payments)+ " \n"+
                COUNTER_SYNCED_VISIT+"/"+SYNC_VISIT_VOLUME+" "+mContext.getString(R.string.visits)+ " \n"+
                mContext.getString(R.string.were_synced));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void showNegativeDialog(){
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(mContext.getString(R.string.backup_failed));
        alertDialog.setMessage(mContext.getString(R.string.backup_failed_explained));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

}
