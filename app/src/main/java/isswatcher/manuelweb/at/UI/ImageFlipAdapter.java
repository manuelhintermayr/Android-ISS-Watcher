package isswatcher.manuelweb.at.UI;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.List;

import androidx.viewpager.widget.PagerAdapter;
import isswatcher.manuelweb.at.Services.Models.Entities.Picture;

public class ImageFlipAdapter extends PagerAdapter {
    //used tutorial https://codinginflow.com/tutorials/android/imageslider
    private Context activity;
    private Uri[] imageUris;

    ImageFlipAdapter(Context context, List<Picture> pictureList) {
        activity = context;

        Picture[] totalPictureArray = new Picture[pictureList.size()];
        pictureList.toArray(totalPictureArray);
        Object[] result = Arrays.stream(totalPictureArray).map(o -> Uri.parse(o.picture_id)).toArray();

        imageUris = Arrays.copyOf(result, result.length, Uri[].class);
    }

    @Override
    public int getCount() {
        return imageUris.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(activity);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageURI(imageUris[position]);
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}