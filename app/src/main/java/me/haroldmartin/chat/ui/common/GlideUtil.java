package me.haroldmartin.chat.ui.common;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import me.haroldmartin.chat.R;
import com.stfalcon.chatkit.commons.ImageLoader;

public class GlideUtil implements ImageLoader {
    public void loadImage(ImageView imageView, String url) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(context, R.color.colorPrimary));
        Glide.with(context)
                .load(url)
                .placeholder(cd)
                .crossFade()
                .centerCrop()
                .into(imageView);
    }

    public static void loadProfileIcon(String url, ImageView imageView) {
        Context context = imageView.getContext();
        Glide.with(context)
                .load(url)
//                .placeholder(R.drawable.ic_person_outline_black_24dp)
                .dontAnimate()
                .fitCenter()
                .into(imageView);
    }
}
