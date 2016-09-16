package com.baiwanlu.android.network;

import com.android.volley.NetworkResponse;

/**
 * Created by lufei on 7/26/16.
 */
public interface IRequestInjector {
    boolean isUnauthorized(NetworkResponse response);
}
