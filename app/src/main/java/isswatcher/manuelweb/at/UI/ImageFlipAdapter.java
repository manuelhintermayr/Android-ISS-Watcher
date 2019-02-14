package isswatcher.manuelweb.at.UI;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;
import isswatcher.manuelweb.at.R;

public class ImageFlipAdapter extends PagerAdapter {
    //used tutorial https://codinginflow.com/tutorials/android/imageslider
    private Context activity;
    private int[] mImageIds = new int[]{R.drawable.iss_64, R.drawable.space, R.drawable.iss_64};

    ImageFlipAdapter(Context context) {
        activity = context;
    }

    @Override
    public int getCount() {
        return mImageIds.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(activity);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(mImageIds[position]);
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}