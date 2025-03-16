package com.bicycle.client.kite.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public interface Constant {

    NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();

    DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

    String HOME = System.getProperty("com.bicycle.client.kite.home", 
            System.getProperty("user.home") + File.separator + ".bicycle");
    
}
