package com.baiwanlu.android.network.base;

/**
 * Created by benren.fj on 6/12/16.
 */
public abstract class IRequestCallBack<T> {
    /**
     * 主线程执行
     * @param response
     */
    public abstract void onResponseSuccess(T response);
    public abstract void onResponseError(int errorCode);
}
