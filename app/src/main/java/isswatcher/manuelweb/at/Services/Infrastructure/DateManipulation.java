package isswatcher.manuelweb.at.Services.Infrastructure;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateManipulation {

    public static String getDateByMiliseconds(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

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
