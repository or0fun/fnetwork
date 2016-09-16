package com.baiwanlu.android.network.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.baiwanlu.android.network.base.cookie.PersistentCookieStore;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * Created by benren.fj on 6/12/16.
 */
public class RequestManager {

    private static RequestManager mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context appContext;

    private static Interceptor sInterceptor;

    private int initialTimeoutMs = 30000;

    private int maxNumRetries = 3;

    private boolean enableCookie = true;

    private PersistentCookieStore cookieStore;

    private RequestManager() {
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });

        cookieStore = new PersistentCookieStore(appContext);
    }

    public static synchronized RequestManager getInstance() {
        if (mInstance == null) {
            mInstance = new RequestManager();
        }
        return mInstance;
    }

    /**
     * Please use application context
     * @param context
     */
    public static void init(Context context) {
        appContext = context;
    }

    public static Context getContext() {
        return appContext;
    }

    public static void addNetworkInterceptor(Interceptor interceptor) {
        sInterceptor = interceptor;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (null != sInterceptor) {
                builder.addNetworkInterceptor(sInterceptor)
                        .cookieJar(cookieJar)
                        .build();
            }
            if (enableCookie) {
                builder.cookieJar(cookieJar);
            }

            mRequestQueue = Volley.newRequestQueue(appContext, new OkHttp3Stack(builder.build()));
            mRequestQueue.start();
        }
        return mRequestQueue;
    }

    /**
     * 加入请求队列
     * @param req
     * @param <T>
     */
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(req.getUrl());
        getRequestQueue().add(req);
    }

    /**
     * 取消请求
     * @param tag
     */
    public void cancelRequest(Request tag) {
        getRequestQueue().cancelAll(tag.getTag());
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * 设置超时时间
     * @param timeoutMs
     */
    public void setInitialTimeoutMs(int timeoutMs) {
        initialTimeoutMs = timeoutMs;
    }

    /**
     * 获取超时时间
     * @param
     * @return
     */
    public int getInitialTimeoutMs() {
        return initialTimeoutMs;
    }

    /**
     * 设置重试次数
     * @param maxNumRetries
     */
    public void setMaxNumRetries(int maxNumRetries) {
        this.maxNumRetries = maxNumRetries;
    }

    /**
     * 获取重试次数
     * @param
     * @return
     */
    public int getMaxNumRetries() {
        return maxNumRetries;
    }

    public void setEnableCookie(boolean enableCookie) {
        this.enableCookie = enableCookie;
    }

    public void clearCookie() {
        cookieStore.removeAll();
    }

    public List<Cookie> getAllCookie() {
        return cookieStore.getCookies();
    }

    public List<Cookie> getCookie(String url) {
        return cookieStore.get(HttpUrl.parse(url));
    }

    public void setCookie(String url, List<Cookie> cookies) {
        if (cookies != null && cookies.size() > 0) {
            for (Cookie item : cookies) {
                cookieStore.add(HttpUrl.parse(url), item);
            }
        }
    }

    CookieJar cookieJar = new CookieJar() {

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            if (cookies != null && cookies.size() > 0) {
                for (Cookie item : cookies) {
                    cookieStore.add(url, item);
                }
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.getCookies();
            return cookies;
        }
    };
}
