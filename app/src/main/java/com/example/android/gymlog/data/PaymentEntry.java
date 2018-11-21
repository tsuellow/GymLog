package com.example.android.gymlog.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;



@Entity(tableName = "payment", foreignKeys = @ForeignKey(entity = ClientEntry.class,
        parentColumns = "id",
        childColumns = "clientId",
        onDelete = ForeignKey.NO_ACTION),
        indices = {@Index(value = {"clientId"})})
public class PaymentEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int clientId;
    private String product;
    private float amountUsd;
    private Date paidFrom;
    private Date paidUntil;
    private Date timestamp;

    public PaymentEntry(int clientId, String product, float amountUsd, Date paidFrom, Date paidUntil, Date timestamp) {
        this.clientId = clientId;
        this.product = product;
        this.amountUsd = amountUsd;
        this.paidFrom = paidFrom;
        this.paidUntil = paidUntil;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public float getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(float amountUsd) {
        this.amountUsd = amountUsd;
    }

    public Date getPaidFrom() {
        return paidFrom;
    }

    public void setPaidFrom(Date paidFrom) {
        this.paidFrom = paidFrom;
    }

    public Date getPaidUntil() {
        return paidUntil;
    }

    public void setPaidUntil(Date paidUntil) {
        this.paidUntil = paidUntil;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}