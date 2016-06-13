package com.baiwanlu.android.network;

import android.content.Context;

import com.baiwanlu.android.network.base.RequestManager;
import com.baiwanlu.android.network.utils.FNetworkLog;

/**
 * Created by benren.fj on 6/12/16.
 */
public class FNetworkManager {

    public static void init(Context context, boolean logDebug) {
        RequestManager.init(context);
        FNetworkLog.setDebug(logDebug);
    }
}
