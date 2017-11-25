package com.xy.psn;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xy.psn.data.MyHelper;
import com.xy.psn.data.ZoomableImageView;


public class ImageFrag extends Fragment {
    private int counter;
    private Bitmap image;

    public ImageFrag() {}

    @Override
    public  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            counter = getArguments().getInt("index");
            image = MyHelper.bmpProductDetail[counter];
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_image_zoom, container, false);
        ZoomableImageView imageView = (ZoomableImageView) view.findViewById(R.id.image);
        imageView.setImageBitmap(image);
        return view;
    }

    public static ImageFrag newInstance(Bitmap image, int counter) {
        ImageFrag frag = new ImageFrag();
        MyHelper.bmpProductDetail[counter] = image;
        Bundle bundle = new Bundle();
        bundle.putInt("index", counter);
        frag.setArguments(bundle);
        return frag;
    }
}
