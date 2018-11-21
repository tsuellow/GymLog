package com.example.android.gymlog.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface VisitDao {

    @Query("SELECT * FROM visit WHERE clientId=:clientId AND timestamp=(SELECT MAX(timestamp) FROM visit WHERE clientId=:clientId)")
    VisitEntry getLatestVisit(int clientId);

    @Query("SELECT * FROM visit WHERE timestamp>=:lastHourMinus30")
    List<VisitEntry> getCurrentClass(Date lastHourMinus30);

    @Insert
    void insertVisit(VisitEntry visitEntry);

    @Delete
    void deleteVisit(VisitEntry visitEntry);

}
