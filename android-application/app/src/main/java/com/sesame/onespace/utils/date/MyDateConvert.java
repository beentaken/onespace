package com.sesame.onespace.utils.date;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Thian on 2/12/2559.
 */

public abstract class MyDateConvert {

    public static String convertTimeStampToDate(String timeStampStr, String pattern){

        try{

            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Date netDate = (new Date(Long.parseLong(String.valueOf(Long.parseLong(timeStampStr) * 1000))));

            return sdf.format(netDate);
        }
        catch(Exception ex){



        }

        return "";
    }

}
