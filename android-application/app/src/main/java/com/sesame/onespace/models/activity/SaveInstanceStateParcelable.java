package com.sesame.onespace.models.activity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Thian on 14/12/2559.
 */

public class SaveInstanceStateParcelable
        implements Parcelable {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    public static final Creator<SaveInstanceStateParcelable> CREATOR = new Creator<SaveInstanceStateParcelable>() {
        @Override
        public SaveInstanceStateParcelable createFromParcel(Parcel in) {
            return new SaveInstanceStateParcelable(in);
        }

        @Override
        public SaveInstanceStateParcelable[] newArray(int size) {
            return new SaveInstanceStateParcelable[size];
        }
    };

    //===========================================================================================================//
    //  CONSTRUCTOR                                                                                 CONSTRUCTOR
    //===========================================================================================================//

    public SaveInstanceStateParcelable(){



    }

    protected SaveInstanceStateParcelable(Parcel in) {



    }

    //===========================================================================================================//
    //  OTHER METHOD                                                                                OTHER METHOD
    //===========================================================================================================//

    @Override
    public void writeToParcel(Parcel dest, int flags) {



    }

    @Override
    public int describeContents() {

        return 0;
    }

}
