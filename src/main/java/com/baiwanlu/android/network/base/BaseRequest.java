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
import com.baiwanlu.android.network.utils.FNetworkLog;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by benren.fj on 6/12/16.
 */
public abstract class BaseRequest<T> {

    protected final static int GET = Request.Method.GET;
    protected final static int POST = Request.Method.POST;

    protected Request httpRequest;
    protected boolean stopped;
    protected IRequestCallBack<T> callBack;
    TypeReference<T> typeReference;
    Class<T> clazz;

    Map<String, String> params = new HashMap<String, String>();

    long startTime;
    long endTime;

    public BaseRequest(IRequestCallBack<T> callBack) {
        startTime = System.currentTimeMillis();
        this.callBack = callBack;
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
        FNetworkLog.d("RealUrl:", realUrl);
        return realUrl;
    }

    public void stop() {
        stopped = true;
        if (null != httpRequest) {
            RequestManager.getInstance().cancelRequest(httpRequest);
        }
    }

    public void start() {
        if (stopped) {
            return;
        }

        try {
            httpRequest = createRequest();
            if (null == httpRequest) {
                return;
            }
            httpRequest.setRetryPolicy(new DefaultRetryPolicy(
                    RequestManager.getInstance().getInitialTimeoutMs(),
                    RequestManager.getInstance().getMaxNumRetries(),
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestManager.getInstance().addToRequestQueue(httpRequest);
        }catch (Throwable throwable) {
            FNetworkLog.e(throwable);
        }
    }

    private Request createRequest() {
        String url = getRequestUrl();
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        Map<String, String> params = getParams();
        if (null == params) {
            params = new HashMap<String, String>();
        }

        int method = getRequestMethod();

        Response.Listener<T> listener = new Response.Listener<T>() {
            @Override
            public void onResponse(T response) {
                if (!stopped && null != callBack) {
                    if (null == response) {
                        FNetworkLog.e(BaseRequest.this.getRequestUrl(), "parse error！！");
                    } else {
                        FNetworkLog.d("response", response.toString());
                        callBack.onResponseSuccess(parseResponse(response));
                        endTime = System.currentTimeMillis();
                        FNetworkLog.d("BaseRequest", String.valueOf(endTime - startTime));

                    }
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                FNetworkLog.e("BaseRequest", error.toString());

                try {
                    if (null != error.networkResponse) {
                        String json = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                        FNetworkLog.e("BaseRequest", json);
                    }
                } catch (UnsupportedEncodingException e) {
                    FNetworkLog.e("BaseRequest", e);
                }
                if (!stopped && null != callBack) {
                    if (error instanceof ParseError) {
                        callBack.onResponseError(FNetworkError.ERROR_PARSE_FAILED);
                    }else if (error instanceof TimeoutError) {
                        callBack.onResponseError(FNetworkError.ERROR_TIMEOUT);
                    }else {
                        callBack.onResponseError(FNetworkError.ERROR_UNKNOWN);
                    }
                }
            }
        };

        FNetworkLog.d("request:", url);
        FNetworkLog.d("request params:", params.toString());

        typeReference = getTypeReference();
        if (GET == method) {
            if (null != typeReference) {
                return new FastJsonRequest(method, getRealUrl(url, params), typeReference, listener, errorListener);
            } else {
                return new FastJsonRequest(method, getRealUrl(url, params), clazz, listener, errorListener);
            }
        }
        if (null != typeReference) {
            return new FastJsonRequest<T>(method, url, params, typeReference, listener, errorListener);
        } else {
            return new FastJsonRequest<T>(method, url, params, clazz, listener, errorListener);
        }
    }

    public BaseRequest addParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public BaseRequest addParam(String key, int value) {
        params.put(key, String.valueOf(value));
        return this;
    }

    public BaseRequest addParam(String key, long value) {
        params.put(key, String.valueOf(value));
        return this;
    }

    public BaseRequest addParam(String key, double value) {
        params.put(key, String.valueOf(value));
        return this;
    }

    public BaseRequest addParam(String key, boolean value) {
        params.put(key, String.valueOf(value));
        return this;
    }

    public BaseRequest addParams(Map<String, String> params) {
        if (null != params) {
            this.params.putAll(params);
        }
        return this;
    }

    protected Map<String, String> getParams() {
        return params;
    }
    /**
     * 从Request.Method 选择值
     * @return
     */
    protected abstract int getRequestMethod();
    @NonNull
    protected abstract String getRequestUrl();

    protected abstract TypeReference<T> getTypeReference();

    protected T parseResponse(T response) {
        return response;
    }
}
