package com.sesame.onespace.fragments.dashboardFragments.notificationFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sesame.onespace.R;

/**
 * Created by Thian on 19/12/2559.
 */

public final class DoNotHaveLocationFragment
        extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private View view;

    //===========================================================================================================//
    //  FRAGMENT LIFECYCLE (MAIN BLOCK)                                                             FRAGMENT LIFECYCLE (MAIN BLOCK)
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        DoNotHaveLocationFragment.this.view = inflater.inflate(R.layout.fragment_dashboard_do_not_have_location, container, false);

        return DoNotHaveLocationFragment.this.view;

    }

}
