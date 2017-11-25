package com.xy.psn.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xy.psn.R;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.data.Chat;

import java.util.ArrayList;

import static com.xy.psn.data.MyHelper.loginUserId;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.DataViewHolder> {
    private ArrayList<ImageObj> chats;
    private ArrayList<ImageObj> avatars;

    public class DataViewHolder extends RecyclerView.ViewHolder {
        // 連結資料的顯示物件宣告
        private CardView cardView;
        private int position;
        private ImageView imgAvatar;
        private TextView txtMsg, txtTime;

        DataViewHolder(View itemView) {
            super(itemView);

            // 連結資料的顯示物件取得
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            imgAvatar = (ImageView) itemView.findViewById(R.id.imgAvatar);
            txtMsg = (TextView) itemView.findViewById(R.id.txtMsg);
            txtTime = (TextView) itemView.findViewById(R.id.txtDatetime);

            // 當卡片被點擊時
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(context, String.valueOf(position), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public ChatAdapter(ArrayList<ImageObj> chats, ArrayList<ImageObj> avatars) {
        this.chats = chats;
        this.avatars = avatars;
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    @Override
    public int getItemViewType(int position) {
        //藉由此方法可做出依某種規則來顯示不同樣式的卡片
        Chat chat = (Chat) chats.get(position);
        if (chat.getSender().equals(loginUserId))
            return 1;
        else
            return 0;
    }

    @Override
    public ChatAdapter.DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // 顯示資料物件來自 R.layout.card_chat_xxx 中
        View view = null;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_chat_left, viewGroup, false);
                break;
            case 1:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_chat_right, viewGroup, false);
                break;
        }
        ChatAdapter.DataViewHolder dataViewHolder = new ChatAdapter.DataViewHolder(view);
        return dataViewHolder;
    }

    @Override
    public void onBindViewHolder(ChatAdapter.DataViewHolder dataViewHolder, int i) {
        // 顯示資料物件及資料項目的對應
        Chat chat = (Chat) chats.get(i);
        dataViewHolder.position = i;
        dataViewHolder.txtMsg.setText(chat.getMsg());
        dataViewHolder.txtTime.setText(chat.getDate() + chat.getTime());

        //根據傳送者ID給予對應大頭貼
        if (chat.getSender().equals(loginUserId)) { //自身訊息
            Bitmap bitmap = avatars.get(0).getImg();
            if (bitmap != null)
                dataViewHolder.imgAvatar.setImageBitmap(bitmap); //Img是自身照片
            /*else
                dataViewHolder.imgAvatar.setImageResource(R.drawable.user);
            */
        }else { //對方訊息
            Bitmap bitmap = avatars.get(0).getImg2();
            if (bitmap != null)
                dataViewHolder.imgAvatar.setImageBitmap(bitmap); //Img2是對方照片
            /*else
                dataViewHolder.imgAvatar.setImageResource(R.drawable.user);
            */
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
