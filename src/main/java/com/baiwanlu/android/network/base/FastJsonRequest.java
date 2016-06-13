package com.baiwanlu.android.network.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.baiwanlu.android.network.utils.FNetworkLog;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by benren.fj on 6/12/16.
 */
public class FastJsonRequest <T> extends Request<T> {
    private Class<T> mClazz;
    private TypeReference<T> mTypeReference;
    private final Response.Listener<T> mListener;
    private final Map<String, String> mHeaders;
    private final Map<String, String> mParams;

    public FastJsonRequest(int method, String url, Class<T> clazz,
                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(method, url, null, clazz, listener, errorListener);
    }

    public FastJsonRequest(int method, String url, Map<String, String> params, Class<T> clazz,
                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(method, url, params, null, clazz, listener, errorListener);
    }

    public FastJsonRequest(int method, String url, Map<String, String> params, Map<String, String> headers, Class<T> clazz,
                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mClazz = clazz;
        this.mParams = params;
        this.mHeaders = headers;
        this.mListener = listener;
    }

    public FastJsonRequest(int method, String url, TypeReference<T> typeReference,
                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(method, url, null, typeReference, listener, errorListener);
    }

    public FastJsonRequest(int method, String url, Map<String, String> params, TypeReference<T> typeReference,
                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(method, url, params, null, typeReference, listener, errorListener);
    }

    public FastJsonRequest(int method, String url, Map<String, String> params, Map<String, String> headers, TypeReference<T> typeReference,
                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mTypeReference = typeReference;
        this.mParams = params;
        this.mHeaders = headers;
        this.mListener = listener;
    }
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams != null ? mParams : Collections.<String, String>emptyMap();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders != null ? mHeaders : super.getHeaders();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        Map<String, String> params = getParams();
        if (null == params || 0 >= params.size()) {
            try {
                return "".getBytes(getParamsEncoding());
            }catch (UnsupportedEncodingException e) {
                FNetworkLog.e(e);
            }
        }
        return super.getBody();
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            FNetworkLog.d("[ur]", getUrl(), "[params]", mParams, "[response]", json);

            if (null != mTypeReference) {
                return Response.success(JSON.parseObject(json, mTypeReference),
                        HttpHeaderParser.parseCacheHeaders(response));
            }

            return Response.success(JSON.parseObject(json, mClazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            FNetworkLog.e(e);
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            FNetworkLog.e(e);
            return Response.error(new ParseError(e));
        }
    }
}
