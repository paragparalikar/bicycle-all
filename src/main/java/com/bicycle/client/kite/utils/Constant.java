package com.bicycle.client.kite.utils;

import java.io.File;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public final class Constant {

    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

    public static final String HOME = System.getProperty("com.bicycle.client.kite.home",
            System.getProperty("user.home") + File.separator + ".bicycle");

    static {
        NUMBER_FORMAT.setRoundingMode(RoundingMode.CEILING);
        NUMBER_FORMAT.setMaximumFractionDigits(2);
        NUMBER_FORMAT.setGroupingUsed(false);
        NUMBER_FORMAT.setMinimumFractionDigits(2);
        NUMBER_FORMAT.setMinimumIntegerDigits(1);
    }


}
