package es.goofyahead.mykeys.robo;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.inject.AbstractModule;

import es.goofyahead.mykeys.volley.Api;
import es.goofyahead.mykeys.volley.BitmapLruCache;

public class CustomConfigModule extends AbstractModule {

    @Override
    protected void configure() {
        Context mContext = MyKeysApplication.getContext();
        RequestQueue mRequestQueue = Volley.newRequestQueue(mContext);
        ImageLoader mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(50));
        Api api = new Api();

        bind(RequestQueue.class).toInstance(mRequestQueue);
        bind(ImageLoader.class).toInstance(mImageLoader);
        bind(Api.class).toInstance(api);
    }

}
