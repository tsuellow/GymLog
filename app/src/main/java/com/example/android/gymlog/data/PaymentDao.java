package com.example.android.gymlog.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PaymentDao {

    @Query("SELECT * FROM payment WHERE clientId=:clientId AND paidUntil=(SELECT MAX(paidUntil) FROM payment WHERE clientId=:clientId)")
    LiveData<PaymentEntry> getLastPaymentByClient(int clientId);

    @Query("SELECT * FROM payment ORDER BY paidUntil DESC")
    List<PaymentEntry> getAllPayments();

    @Delete
    void deletePayment(PaymentEntry paymentEntry);

    @Insert
    void insertPayment(PaymentEntry paymentEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePayment(PaymentEntry paymentEntry);


}
