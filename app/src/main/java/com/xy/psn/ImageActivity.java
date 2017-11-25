package com.xy.psn;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.xy.psn.product.ProductDetailActivity;

import java.util.ArrayList;

import static com.xy.psn.product.ProductDetailActivity.selectedIndex;

public class ImageActivity extends AppCompatActivity {
    private ArrayList<Bitmap> pictures;
    private ViewPager viewPager;
    private int counter = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        this.pictures = ProductDetailActivity.pictures;
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new ImageAdapter(getSupportFragmentManager(), pictures));
        viewPager.setCurrentItem(selectedIndex);
    }

    private class ImageAdapter extends FragmentPagerAdapter {
        ArrayList<Bitmap> pictures;

        public ImageAdapter (FragmentManager fm, ArrayList<Bitmap> pictures) {
            super(fm);
            this.pictures = pictures;
        }

        @Override
        public Fragment getItem(int position) {
            counter++;
            return ImageFrag.newInstance(pictures.get(position), counter);
        }

        @Override
        public int getCount() {
            return pictures.size();
        }
    }
}
