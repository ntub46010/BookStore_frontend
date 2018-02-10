package com.xy.psn.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xy.psn.R;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.data.Chat;
import com.xy.psn.member.MemberChatActivity;

import java.util.ArrayList;

public class MailListAdapter extends BaseAdapter{
    private Context context;
    private Resources resources = null;
    private LayoutInflater layoutInflater;
    private ArrayList<ImageObj> mails;
    private int backgroundColor;

    public MailListAdapter(Context context, ArrayList<ImageObj> mails) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.mails = mails;
    }

    @Override
    public int getCount() {
        return mails.size();
    }

    @Override
    public Object getItem(int i) {
        return mails.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = layoutInflater.inflate(R.layout.lst_mail, viewGroup, false);

        //LinearLayout layMailCard = (LinearLayout) view.findViewById(R.id.layMail);
        ImageView imgAvatar = (ImageView) view.findViewById(R.id.imgAvatar);
        TextView txtName = (TextView) view.findViewById(R.id.txtName);
        TextView txtMsg = (TextView) view.findViewById(R.id.txtPreviewMsg);
        TextView txtDatetime = (TextView) view.findViewById(R.id.txtDatetime);

        Chat chat = (Chat) mails.get(i);
        txtName.setText(chat.getName());
        txtMsg.setText(chat.getMsg());
        txtDatetime.setText(chat.getDate() + "  " + chat.getTime());

        Bitmap bitmap = chat.getImg();
        if (bitmap != null)
            imgAvatar.setImageBitmap(bitmap);
        /*
        try {
            layMailCard.setBackgroundColor(resources.getColor(backgroundColor));
        }catch (NullPointerException e) {}
        */
        return view;
    }
}
