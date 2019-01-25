package com.example.android.gymlog;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.SearchView;
import android.widget.Toast;
import com.example.android.gymlog.data.ClientEntry;
import com.example.android.gymlog.data.GymDatabase;
import com.example.android.gymlog.data.PaymentEntry;
import com.example.android.gymlog.data.VisitEntry;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    public static final String SEARCH_STRING = "SEARCH_STRING";
    RecyclerView rvClients;
    GymDatabase mDb;
    SearchAdapter mAdapter;
    Context mContext;
    SearchView searchView;
    Toolbar mToolbar;
    String searchString;
    SharedPreferences sharedPreferences;
    static Context appContext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //normal variable instantiation
        setContentView(R.layout.activity_search);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);


        mContext = getApplicationContext();
        rvClients = (RecyclerView) findViewById(R.id.rv_client_search);
        mAdapter = new SearchAdapter(this);
        mDb = GymDatabase.getInstance(getApplicationContext());

        rvClients.setAdapter(mAdapter);
        rvClients.setLayoutManager(new LinearLayoutManager(this));
        if (savedInstanceState==null){
            searchString = "";
        }else{
            searchString=savedInstanceState.getString(SEARCH_STRING);
        }

        populateDataSource(searchString);

        //searchView.setQuery("a",false);


        //floating + button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), NewClientActivity.class);
                startActivity(i);
            }
        });
        appContext=getApplicationContext();

    }

    public static Context getAppCont(){
        return appContext;
    }

    private void populateDataSource(String s) {
        String str = s + "%";

        final LiveData<List<ClientEntry>> clients = mDb.clientDao().getClientByName(str);
        clients.observe(this, new Observer<List<ClientEntry>>() {
            @Override
            public void onChanged(@Nullable List<ClientEntry> clientEntries) {
                mAdapter.setClients(clientEntries);
                mToolbar.setSubtitle(mAdapter.getItemCount()+" "+getString(R.string.clients));

            }
        });
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_plus, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchView.setQuery(searchString,true);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.opt_setting:{
                Intent intent=new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.opt_reminder:{
                //Toast.makeText(getApplicationContext(),"open reminder list",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(getApplicationContext(),SendReminderActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.opt_backup:{
                Toast.makeText(getApplicationContext(), R.string.backup_in_process,Toast.LENGTH_SHORT).show();
                final DataBackup dataBackup=new DataBackup(SearchActivity.this);
                if (dataBackup.hasInternetConnectivity()){
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (dataBackup.hasHostAccess()){
                                final List<ClientEntry> clients=mDb.clientDao().getClientToBeSynced();
                                final List<PaymentEntry> payments=mDb.paymentDao().getPaymentToBeSynced();
                                final List<VisitEntry> visits=mDb.visitDao().getVisitToBeSynced();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dataBackup.backupClientTable(clients,payments,visits);

                                    }
                                });
                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dataBackup.showNegativeDialog();
                                    }
                                });
                            }
                        }
                    });

                }else {
                    Toast.makeText(getApplicationContext(), R.string.no_internet,Toast.LENGTH_LONG).show();

                }
                break;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private SearchView.OnQueryTextListener onQueryTextListener =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    populateDataSource(query);
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    populateDataSource(newText);
                    searchString=newText;

                    return true;
                }
            };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString(SEARCH_STRING, searchString);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }





}

