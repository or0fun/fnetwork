package com.baiwanlu.android.network.base;

/**
 * Created by lufei on 3/28/16.
 */
public abstract class BaseGetRequest<T> extends BaseRequest<T> {

    @Override
    protected int getRequestMethod() {
        return GET;
    }
}
