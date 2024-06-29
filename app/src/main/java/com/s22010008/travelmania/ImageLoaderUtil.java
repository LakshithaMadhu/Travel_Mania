package com.s22010008.travelmania;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class ImageLoaderUtil {

    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        Log.d("ImageLoader", "Loading image from URL: " + imageUrl);
        Glide.with(context)
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("ImageLoader", "Image load failed for URL: " + imageUrl, e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("ImageLoader", "Image loaded successfully for URL: " + imageUrl);
                        return false;
                    }
                })
                .into(imageView);
    }
}
