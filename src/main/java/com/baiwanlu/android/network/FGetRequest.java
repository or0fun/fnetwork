package com.baiwanlu.android.network;

import com.baiwanlu.android.network.base.BaseRequest;
import com.baiwanlu.android.network.base.IRequestCallBack;

/**
 * Created by benren.fj on 6/12/16.
 */
public abstract class FGetRequest<T> extends BaseRequest<T> {

    public FGetRequest(IRequestCallBack<T> callBack) {
        super(callBack);
    }

    @Override
    protected int getRequestMethod() {
        return GET;
    }
}
