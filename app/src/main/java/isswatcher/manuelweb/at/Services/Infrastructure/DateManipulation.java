package isswatcher.manuelweb.at.Services.Infrastructure;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateManipulation {

    public static String getDateByUnixTimestamp(long unixTimestamp, String dateFormat)
    {
        //to Test: https://www.unixtimestamp.com/index.php
        Date date = new java.util.Date(unixTimestamp*1000L);

        // the format of your date
        SimpleDateFormat sdf = new java.text.SimpleDateFormat(dateFormat);

        String formattedDate = sdf.format(date);
        return formattedDate;
    }
}
