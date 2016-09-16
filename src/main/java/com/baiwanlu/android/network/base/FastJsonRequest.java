package com.baiwanlu.android.network.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.baiwanlu.android.network.IRequestInjector;
import com.baiwanlu.android.network.base.multipart.MultipartRequestParams;
import com.baiwanlu.android.network.utils.FNetworkLog;

import org.apache.http.HttpEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lufei on 3/26/16.
 */
public class FastJsonRequest<T> extends Request<T> {

    private String TAG = "FastJsonRequest";

    private TypeReference<T> mTypeReference;
    private Response.Listener<T> mListener;
    private Map<String, String> mParams;
    private MultipartRequestParams multipartRequestParams;

    private IRequestInjector requestInjector;

    HttpEntity httpEntity;

    public FastJsonRequest(int method, String url, TypeReference<T> typeReference,
                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(method, url, new HashMap<String, String>(), typeReference, listener, errorListener);
    }

    public FastJsonRequest(int method, String url, TypeReference<T> typeReference,
                           Response.Listener<T> listener, Response.ErrorListener errorListener, IRequestInjector requestInjector) {
        this(method, url, new HashMap<String, String>(), typeReference, listener, errorListener, null);
    }

    public FastJsonRequest(int method, String url, Map<String, String> params, TypeReference<T> typeReference,
                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mTypeReference = typeReference;
        this.mParams = params;
        this.mListener = listener;
    }

    public FastJsonRequest(int method, String url, Map<String, String> params, TypeReference<T> typeReference,
                           Response.Listener<T> listener, Response.ErrorListener errorListener, IRequestInjector requestInjector) {
        this(method, url, params, typeReference, listener, errorListener);
        this.requestInjector = requestInjector;
    }

    public FastJsonRequest(int method, String url, MultipartRequestParams multipartRequestParams, TypeReference<T> typeReference,
                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mTypeReference = typeReference;
        this.multipartRequestParams = multipartRequestParams;
        this.mListener = listener;
    }

    public FastJsonRequest(int method, String url, MultipartRequestParams multipartRequestParams, TypeReference<T> typeReference,
                           Response.Listener<T> listener, Response.ErrorListener errorListener, IRequestInjector requestInjector) {
        this(method, url, multipartRequestParams, typeReference, listener, errorListener);
        this.requestInjector = requestInjector;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams != null ? mParams : Collections.<String, String>emptyMap();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return super.getHeaders();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (null != multipartRequestParams) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            httpEntity = multipartRequestParams.getEntity();
            try {
                httpEntity.writeTo(baos);
            } catch (IOException e) {
                FNetworkLog.e(e);
            }
            String str = new String(baos.toByteArray());
            FNetworkLog.d(TAG, "bodyString is :" + str);
            return baos.toByteArray();
        }

        Map<String, String> params = getParams();
        if (null == params || 0 >= params.size()) {
            try {
                return "".getBytes(getParamsEncoding());
            } catch (UnsupportedEncodingException e) {
                FNetworkLog.e(e);
            }
        }

        return super.

                getBody();

    }

    @Override
    public String getBodyContentType() {
        if (null != multipartRequestParams) {
            if (null != httpEntity) {
                return httpEntity.getContentType().getValue();
            }
        }
        return super.getBodyContentType();
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
                try {
                    return Response.success(JSON.parseObject(json, mTypeReference),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (JSONException e) {
                    try {
                        FNetworkLog.e("[ur]", getUrl(), "[params]", mParams.toString(), "[response]", json);
                    }catch (Exception e1) {
                        FNetworkLog.e(e1);
                    }
                }
                return Response.error(new VolleyError(response));
            }
            return null;
        } catch (UnsupportedEncodingException e) {
            FNetworkLog.e(e);
            return Response.error(new ParseError(e));
        }
    }
}