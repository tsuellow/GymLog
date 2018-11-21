package com.example.android.gymlog.data;

import android.arch.persistence.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {
//    @TypeConverter
//    public static Date toDate(Long timestamp) {
//        return timestamp == null ? null : new Date(timestamp);
//    }
//
//    @TypeConverter
//    public static Long toTimestamp(Date date) {
//        return date == null ? null : date.getTime();
//    }

    @TypeConverter
    public static String getDateString(Date date){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(date);
    }

    @TypeConverter
    public static Date String2Date(String datestr){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = null;
        try {
            date = sdf.parse(datestr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
