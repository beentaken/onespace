package com.sesame.onespace.utils.database;

import java.util.ArrayList;

/**
 * Created by Thian on 21/12/2559.
 */

public abstract class DatabaseConvert {

    public static String convertArrayListToString(ArrayList<String> arrayList){

        String result = "";

        for (String s : arrayList){

            result = result + s + ",";

        }

        return result;

    }

    public static ArrayList<String> convertStringToArrayList(String s){

        ArrayList<String> arrayList = new ArrayList<String>();

        int index = 0;

        char[] charArray = s.toCharArray();

        String part = "";

        while(index < charArray.length){

            if (charArray[index] == ','){

                arrayList.add(part);
                part = "";

            }
            else{

                part = part + charArray[index];

            }

            index = index + 1;

        }

        return arrayList;

    }

}
