package com.example.android.gymlog;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.example.android.gymlog.data.ClientEntry;
import com.example.android.gymlog.data.GymDatabase;
import com.example.android.gymlog.data.PaymentEntry;
import com.example.android.gymlog.data.VisitEntry;

import org.json.JSONObject;

import java.util.List;

public class BackupBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent intent) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        final GymDatabase mDb = GymDatabase.getInstance(context);
        final DataBackup dataBackup=new DataBackup(context,pref);
        if (dataBackup.hasInternetConnectivity()){
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    if (dataBackup.hasHostAccess()){
                        final List<ClientEntry> clients=mDb.clientDao().getClientToBeSynced();
                        final List<PaymentEntry> payments=mDb.paymentDao().getPaymentToBeSynced();
                        final List<VisitEntry> visits=mDb.visitDao().getVisitToBeSynced();

                        JSONObject jsonObject=dataBackup.createClientJson(clients,payments,visits);
                        dataBackup.syncAllAutomatic(jsonObject,context);
                    }
                }
            });

        }
    }




}
