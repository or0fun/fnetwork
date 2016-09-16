package com.baiwanlu.android.network.base;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.TypeReference;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.baiwanlu.android.network.FNetworkError;
import com.baiwanlu.android.network.IRequestInjector;
import com.baiwanlu.android.network.base.multipart.MultipartRequestParams;
import com.baiwanlu.android.network.utils.FNetworkLog;
import com.baiwanlu.android.network.utils.FNetworkUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lufei on 3/23/16.
 */
public abstract class BaseRequest<T> {

    private String TAG = "BaseRequest";

    protected final static int GET = Request.Method.GET;
    protected final static int POST = Request.Method.POST;

    protected Request httpRequest;
    protected IRequestCallBack<T> callBack;

    protected IRequestInjector requestInjector;

    MultipartRequestParams multipartRequestParams = new MultipartRequestParams();

    long startTime;
    long endTime;

    boolean isRunning = false;

    public BaseRequest() {
        startTime = System.currentTimeMillis();
    }

    public BaseRequest<T> setCallBack(IRequestCallBack<T> callBack) {
        this.callBack = callBack;
        return this;
    }

    public void setRequestInjector(IRequestInjector requestInjector) {
        this.requestInjector = requestInjector;
    }

    /**
     * 获取请求最后的url
     * @return
     */
    public String getUrl() {
        if (null == httpRequest) {
            return null;
        }
        return httpRequest.getUrl();
    }

    /**
     * serialization form
     *
     * @param url
     * @param mParams
     */
    private String getRealUrl(String url, Map<String, String> mParams) {
        if (null == mParams) {
            return url;
        }
        Uri uri = Uri.parse(url);
        Uri.Builder builder = uri.buildUpon();
        for (HashMap.Entry<String, String> entry : mParams.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        String realUrl = builder.build().toString();
        FNetworkLog.d(TAG, "RealUrl:", realUrl);
        return realUrl;
    }

    public void stop() {
        isRunning = false;
        if (null != httpRequest) {
            RequestManager.getInstance().cancelRequest(httpRequest);
            httpRequest = null;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void start() {
        if (isRunning()) {
            stop();
        }
        isRunning = true;

        if (!FNetworkUtil.isNetworkAvailable(RequestManager.getContext())) {
            if (isRunning && null != callBack) {
                callBack.onResponseError(FNetworkError.ERROR_UNAVAILABLE);
            }
            isRunning = false;
            return;
        }

        try {
            httpRequest = createRequest();
            if (null == httpRequest) {
                if (isRunning && null != callBack) {
                    callBack.onResponseError(FNetworkError.ERROR_REQUEST_EMPTY);
                }
                isRunning = false;
                return;
            }
            httpRequest.setRetryPolicy(new DefaultRetryPolicy(
                    RequestManager.getInstance().getInitialTimeoutMs(),
                    RequestManager.getInstance().getMaxNumRetries(),
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestManager.getInstance().addToRequestQueue(httpRequest);
        }catch (Throwable throwable) {
            FNetworkLog.e(throwable);
            if (isRunning && null != callBack) {
                callBack.onResponseError(FNetworkError.ERROR_UNKNOWN);
            }
            isRunning = false;
        }
    }

    private Request createRequest() {
        String url = getRequestUrl();
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        int method = getRequestMethod();

        Response.Listener<T> listener = new Response.Listener<T>() {
            @Override
            public void onResponse(T response) {
                if (isRunning && null != callBack) {
                    isRunning = false;
                    if (null == response) {
                        FNetworkLog.e(BaseRequest.this.getRequestUrl(), "parse error！！");
                        callBack.onResponseError(FNetworkError.ERROR_UNKNOWN);
                    } else {
                        FNetworkLog.d(TAG, response.getClass().toString());
                        FNetworkLog.d(TAG, response.toString());
                        callBack.onResponseSuccess(parseResponse(response));
                        endTime = System.currentTimeMillis();
                        FNetworkLog.d(TAG, String.valueOf(endTime - startTime));

                    }
                }
                isRunning = false;
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                FNetworkLog.e(TAG, error.toString());

                try {
                    if (null != error.networkResponse) {
                        String json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                        FNetworkLog.e(TAG, json);
                    }
                } catch (UnsupportedEncodingException e) {
                    FNetworkLog.e(e);
                }
                if (isRunning && null != callBack) {
                    isRunning = false;
                    if (error instanceof ParseError) {
                        callBack.onResponseError(FNetworkError.ERROR_PARSE_FAILED);
                    } else if (error instanceof TimeoutError) {
                        callBack.onResponseError(FNetworkError.ERROR_TIMEOUT);
                    } else if (null != requestInjector && requestInjector.isUnauthorized(error.networkResponse)) {
                        callBack.onResponseError(FNetworkError.ERROR_NO_LOGIN);
                    } else {
                        callBack.onResponseError(FNetworkError.ERROR_UNKNOWN);
                    }
                }
                isRunning = false;
            }
        };

        Map<String, String> params = getParams();
        if (null == params) {
            params = new HashMap<String, String>();
        }

        FNetworkLog.d(TAG, "request:", url);
        FNetworkLog.d(TAG, "request params:", params.toString());

        if (GET == method) {
            return new FastJsonRequest(method, getRealUrl(url, params),
                    getTypeReference(), listener, errorListener, requestInjector);
        }
        if (multipartRequestParams.hasFile() || isMultipartRequest()) {
            return new FastJsonRequest(method, url, multipartRequestParams,
                    getTypeReference(), listener, errorListener, requestInjector);
        }
        return new FastJsonRequest(method, url, params,
                getTypeReference(), listener, errorListener, requestInjector);

    }

    public BaseRequest<T> addParam(String key, String value) {
        multipartRequestParams.put(key, value);
        return this;
    }

    public BaseRequest<T> addParam(String key, int value) {
        multipartRequestParams.put(key, String.valueOf(value));
        return this;
    }

    public BaseRequest<T> addParam(String key, long value) {
        multipartRequestParams.put(key, String.valueOf(value));
        return this;
    }

    public BaseRequest<T> addParam(String key, double value) {
        multipartRequestParams.put(key, String.valueOf(value));
        return this;
    }

    public BaseRequest<T> addParam(String key, boolean value) {
        multipartRequestParams.put(key, String.valueOf(value));
        return this;
    }

    public BaseRequest<T> addParams(Map<String, String> params) {
        this.multipartRequestParams.putAll(params);
        return this;
    }

    public BaseRequest<T> addFile(String key, File file) {
        this.multipartRequestParams.put(key, file);
        return this;
    }

    public void clearParams() {
        multipartRequestParams.clear();
    }

    protected Map<String, String> getParams() {
        return multipartRequestParams.getUrlParams();
    }
    /**
     * 从Request.Method 选择值
     * @return
     */
    protected abstract int getRequestMethod();
    @NonNull protected abstract String getRequestUrl();

    protected abstract TypeReference<T> getTypeReference();

    protected T parseResponse(T response) {
        return response;
    }

    protected boolean isMultipartRequest() {
        return false;
    }
}
