package com.sesame.onespace.utils.date;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Thian on 2/12/2559.
 */

public abstract class DateConvert {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    public final static String DATE_FORMAT1 = "EEE, d MMM yyyy hh:mm aaa";  // Wed, 4 Jul 2001 12:08 PM

    //===========================================================================================================//
    //  METHOD                                                                                      METHOD
    //===========================================================================================================//

    public static String convertTimeStampToDate(String timeStampStr, String format){

        try{

            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date netDate = (new Date(Long.parseLong(String.valueOf(Long.parseLong(timeStampStr) * 1000))));

            return sdf.format(netDate);
        }
        catch(Exception ex){



        }

        return "";
    }

}
