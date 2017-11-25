package com.xy.psn.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.R;
import com.xy.psn.async_helper.GetBitmap;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.data.Book;

import java.util.ArrayList;

import static com.xy.psn.R.layout.grd_member_shelf;
import static com.xy.psn.R.layout.spn_chat_product;
import static com.xy.psn.data.MyHelper.isStockDisplayAlive;

public class StockListAdapter extends BaseAdapter {
    private Context context;
    private Resources resources = null;
    private LayoutInflater layoutInflater;
    private int layout, backgroundColor;
    private ArrayList<ImageObj> products;
    private ArrayList<GetBitmap> bitTasks;
    private boolean isFirstToHead = true, canCheckLoop = true;
    private Thread trdCheckImg = null;
    private int head = 0, tail = 9, section = 8;
    private int lastPosition = 0, lastDirection = 1;
    private boolean[] loadLock = null;

    public StockListAdapter(Context context, ArrayList<ImageObj> products, int layout) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.products = products;
        this.layout = layout;

        loadLock = new boolean[products.size()];
        for (int i=0; i<loadLock.length; i++)
            loadLock[i] = true;

        initTask();
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = layoutInflater.inflate(layout, parent, false);

        setContent(position);

        final Book book = (Book) products.get(position);
        ImageView imgBookPic = (ImageView) convertView.findViewById(R.id.imgBookSummaryPic);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.txtBookSummaryTitle);
        imgBookPic.setImageBitmap(book.getImg());
        txtTitle.setText(book.getTitle());

        if (layout == spn_chat_product) { //商品管理：追加價格、選單顯示、選單內點擊事件
            LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.layProductSummaryList);
            linearLayout.setBackgroundColor(resources.getColor(backgroundColor));
            TextView txtPrice = (TextView) convertView.findViewById(R.id.txtBookSummaryPrice);
            txtPrice.setText("$ " + book.getPrice());
        }else if (layout == grd_member_shelf) { //個人檔案上架：

        }

        return convertView;
    }

    private void setContent(int position) {
        head = (position - section >= 0) ? position - section : 0;
        tail = (position + section <= products.size() - 1) ? position + section : products.size() - 1;
        initTask();
        if (isFirstToHead && position == 0) {
            isFirstToHead = false; //避免重新回到第一個項目，又啟動第二個執行緒
            initCheckThread(true);
        }
    }

    private void prepareImgs() {
        //下載應出現的照片
        try {
            for (int i = head; i <= tail; i++) {
                if (loadLock[i] && products.get(i).getImg() == null) {
                    loadLock[i] = false;
                    bitTasks.get(i).execute();
                }
            }
        }catch (ArrayIndexOutOfBoundsException e) {}

        //清除應消失的照片
        for (int i = 0; i < head; i++) {
            if (!loadLock[i] || products.get(i).getImg() != null) {
                bitTasks.get(i).cancel(true);
                products.get(i).setImg(null);
            }
        }
        for (int i = tail+1; i<products.size(); i++) {
            if (!loadLock[i] || products.get(i).getImg() != null) {
                bitTasks.get(i).cancel(true);
                products.get(i).setImg(null);
            }
        }
    }

    public void initCheckThread (boolean restart) {
        trdCheckImg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    hdrCheckImg.sendMessage(hdrCheckImg.obtainMessage());
                }catch (Exception e) {}
            }
        });

        if (restart) {
            trdCheckImg.start();
        }
    }

    private Handler hdrCheckImg = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (canCheckLoop && isStockDisplayAlive) {
                prepareImgs();
                initCheckThread(true);
            }else {
                canCheckLoop = true;
                initCheckThread(false);
            }
        }
    };

    private void initTask() {
        bitTasks = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            final int i2 = i;
            bitTasks.add(
                    new GetBitmap(context, products.get(i), new GetBitmap.TaskListener() {
                        @Override
                        public void onFinished() {
                            loadLock[i2] = true;
                            notifyDataSetChanged();
                        }
                    })
            );
        }
    }

    public void setCanCheckLoop(boolean loop) {
        this.canCheckLoop = loop;
    }

    public void setBackgroundColor(Resources r, int color) {
        this.resources = r;
        this.backgroundColor = color;
    }
}
