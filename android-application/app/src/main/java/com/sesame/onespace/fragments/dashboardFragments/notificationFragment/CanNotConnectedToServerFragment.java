package com.sesame.onespace.fragments.dashboardFragments.notificationFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sesame.onespace.R;

/**
 * Created by Thian on 4/12/2559.
 */

public final class CanNotConnectedToServerFragment
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

        CanNotConnectedToServerFragment.this.view = inflater.inflate(R.layout.fragment_dashboard_can_not_connected_to_server, container, false);

        return CanNotConnectedToServerFragment.this.view;

    }

}
