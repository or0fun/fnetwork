package com.baiwanlu.android.network;

import com.baiwanlu.android.network.base.BaseRequest;
import com.baiwanlu.android.network.base.IRequestCallBack;

/**
 * Created by benren.fj on 6/12/16.
 */
public abstract class FPostRequest<T> extends BaseRequest<T> {

    public FPostRequest(IRequestCallBack<T> callBack) {
        super(callBack);
    }

    @Override
    protected int getRequestMethod() {
        return POST;
    }
}
