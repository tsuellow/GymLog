package com.example.android.gymlog.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.Nullable;

import java.util.List;

@Dao
public interface ClientDao {

    //test
    @Query("SELECT count(*) FROM client")
    int getCount();
    //test
    @Query("SELECT case when num>=1 then 0 else 1 end test FROM (select count(*) num from client where id=:id) ")
    int isIdNew(int id);

    @Query("SELECT * FROM client ORDER BY firstName")
    LiveData<List<ClientEntry>> getAllClients();

    @Query("SELECT * FROM client WHERE id=:id")
    LiveData<ClientEntry> getClientById(int id);

    @Query("SELECT * FROM client WHERE firstName LIKE :namePart ORDER BY firstName")
    LiveData<List<ClientEntry>> getClientByName(String namePart);

    @Insert
    void insertClient(ClientEntry clientEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateClient(ClientEntry clientEntry);

    @Delete
    void deleteClient(ClientEntry clientEntry);

}
