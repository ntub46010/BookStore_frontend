package com.xy.psn.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xy.psn.R;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.data.Book;

import java.util.ArrayList;

public class ProductSpinnerAdapter extends ArrayAdapter {
    private Context context;
    private ArrayList<ImageObj> products;

    public ProductSpinnerAdapter(Context context, int layoutResource, ArrayList<ImageObj> objects) {
        super(context, layoutResource, objects);
        this.context = context;
        this.products = objects;
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.spn_chat_product, parent, false);

        ImageView imgBookPic = (ImageView) layout.findViewById(R.id.imgBookSummaryPic);
        TextView txtBookTitle = (TextView) layout.findViewById(R.id.txtBookSummaryTitle);
        TextView txtBookPrice = (TextView) layout.findViewById(R.id.txtBookSummaryPrice);

        Book book = (Book) products.get(position);
        imgBookPic.setImageBitmap(book.getImg());
        txtBookTitle.setText(book.getTitle());
        txtBookPrice.setText("$ " + book.getPrice());

        return layout;
    }

    // It gets a View that displays in the drop down popup the data at the specified position
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // It gets a View that displays the data at the specified position
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }
}
