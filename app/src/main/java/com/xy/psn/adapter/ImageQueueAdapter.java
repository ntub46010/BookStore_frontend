package com.xy.psn.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.R;
import com.xy.psn.data.ImageObject;
import com.xy.psn.data.MyHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ImageQueueAdapter extends RecyclerView.Adapter<ImageQueueAdapter.DataViewHolder> {
    private Context context;
    private Activity activity;
    private ArrayList<ImageObject> pictures;
    private int pressedPosition = 0;

    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_ALBUM = 2;
    public static final int REQUEST_CROP = 3;

    private File mImageFile = null;
    private Bitmap mImageFileBitmap = null;

    public class DataViewHolder extends RecyclerView.ViewHolder {
        // 連結資料的顯示物件宣告
        private CardView cardView;
        private int position;
        private FrameLayout layBookPic;
        private ImageView imgBookPic;

        DataViewHolder(View itemView) {
            super(itemView);

            // 連結資料的顯示物件取得
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            layBookPic = (FrameLayout) itemView.findViewById(R.id.layBookImg);
            imgBookPic = (ImageView) itemView.findViewById(R.id.imgBook);

            // 當卡片被點擊時
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getEntityAmount() > 5) {
                        Toast.makeText(context, "最多新增五張圖片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pressedPosition = position;
                    //將寫入使用者對寫入的權限指定至permission
                    int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    //檢查是否開啟寫入權限
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        // 無權限，向使用者請求
                        //執行完後執行onRequestPermissionsResult
                        ActivityCompat.requestPermissions(
                                activity,
                                new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                0 //requestCode
                        );
                    }else{
                        //已有權限，準備選圖
                        pickImageDialog();
                    }
                }
            });

            cardView.setOnLongClickListener(new View.OnLongClickListener() { //長按跳出選項清單
                @Override
                public boolean onLongClick(View v) {
                    pressedPosition = position;
                    if (getItem(position).getBitmap() != null || getItem(position).getFileName() != null) {
                        prepareDialog();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    // 將連結的資料
    public ImageQueueAdapter(Context context, ArrayList<ImageObject> pictures, Activity activity) {
        this.context = context;
        this.pictures = pictures;
        this.activity = activity;
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    @Override
    public ImageQueueAdapter.DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_book_img, viewGroup, false);
        ImageQueueAdapter.DataViewHolder dataViewHolder = new ImageQueueAdapter.DataViewHolder(view);
        return dataViewHolder;
    }

    @Override
    public void onBindViewHolder(ImageQueueAdapter.DataViewHolder dataViewHolder, int i) {
        // 顯示資料物件及資料項目 的對應
        ImageObject picture =  pictures.get(i);
        dataViewHolder.position = i;

        if (picture.getBitmap() != null || picture.getFileName() != null) {
            dataViewHolder.layBookPic.setBackgroundColor(Color.parseColor("#FAFAFA"));
            dataViewHolder.imgBookPic.setScaleType(ImageView.ScaleType.FIT_CENTER);//
            dataViewHolder.imgBookPic.setImageBitmap(picture.getBitmap());
        }else { //加號圖片
            dataViewHolder.layBookPic.setBackgroundColor(Color.parseColor("#DDDDDD"));
            dataViewHolder.imgBookPic.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            dataViewHolder.imgBookPic.setImageResource(R.drawable.icon_add);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void pickImageDialog() {
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(albumIntent, REQUEST_ALBUM);
    }

    public void cropImage(Uri uri){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 3);
        intent.putExtra("aspectY", 4);
        intent.putExtra("outputX", 900);
        intent.putExtra("outputY", 1200);
        /*
        intent.putExtra("aspectX", 9);
        intent.putExtra("aspectY", 16);
        intent.putExtra("outputX", 720);
        intent.putExtra("outputY", 1280);
        */
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("return-date", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageFile));
        activity.startActivityForResult(intent, REQUEST_CROP);
    }

    public boolean createImageFile() {
        mImageFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        try {
            mImageFile.createNewFile();
            return mImageFile.exists();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "出錯啦", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean mImageFileToBitmap(ImageObject image) {
        mImageFileBitmap = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
        if (mImageFileBitmap != null) {
            image.setBitmap(mImageFileBitmap);
            if (mImageFile.delete()) {
                Log.w("Delete File", "deleted");
            }
            else {
                mImageFile.deleteOnExit();
                Log.w("Delete File", "delete failed and call deleteOnExit()");
            }
            return false;
        }
        else
            return true;
    }

    public int getPressedPosition() {
        return pressedPosition;
    }

    private void prepareDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dlg_gallery_options);
        dialog.setCancelable(true);

        String[] textGroup = {"左移", "右移", "移除"};
        int[] iconGroup = {
                R.drawable.icon_turn_left,
                R.drawable.icon_turn_right,
                R.drawable.icon_delete
        };

        ListView listView = (ListView) dialog.findViewById(R.id.lstGalleryOptions);
        listView.setAdapter(MyHelper.getSimpleAdapter(
                context,
                R.layout.lst_text_with_icon_black,
                R.id.imgIcon,
                R.id.txtTitle,
                iconGroup,
                textGroup
        ));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemName = ((TextView)view.findViewById(R.id.txtTitle)).getText().toString();
                switch (itemName) {
                    case "左移":
                        moveItem(pressedPosition, -1);
                        break;
                    case "右移":
                        moveItem(pressedPosition, 1);
                        break;
                    case "移除":
                        removeItem(pressedPosition);
                        break;
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public ImageObject getItem(int position) {
        return pictures.get(position);
    }

    public void setItem(int position, ImageObject item) {
        pictures.set(position, item);
        //notifyDataSetChanged(); //會出錯
        notifyItemChanged(position);
    }

    public void addItem(ImageObject image) {
        pictures.add(image);
        notifyDataSetChanged();
    }

    private boolean removeItem(int position) {
        int bmpAmount = 0;
        //第一條件，至少有兩張圖片(可能含一張空白)；第二條件，點選的不是空白圖
        if (getItemCount() > 1 && (getItem(position).getBitmap() != null /*|| getItem(position).getFileName() != null*/)) {
            for (int i=0; i<pictures.size(); i++) {
                if (getItem(i).getBitmap() != null)
                    bmpAmount++;
            }
            if (bmpAmount == 5/* && (getItem(getItemCount() - 1).isEntity() || getItem(getEntityAmount() - 1).getFileName() != null)*/) {
                //若移除前原先有5張真圖，要補一張空白圖
                Bitmap bitmap = null;
                addItem(new ImageObject(bitmap, false));
            }
            pictures.remove(position);
            notifyDataSetChanged();
            return true;
        }
        Toast.makeText(context, "移除失敗", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void moveItem(int position, int distance) {
        int destination;
        if (distance > 0)
            destination = (position + distance >= getItemCount()) ? getItemCount() : position + distance;
        else
            destination = (position + distance < 0) ? 0 : position + distance;

        if (getItem(destination).getBitmap() != null) {
            Collections.swap(pictures, position, destination);
            notifyDataSetChanged();
        }
    }

    public int getEntityAmount() {
        int count = 0;
        for (ImageObject image : pictures) {
            if (image.isEntity())
                count++;
        }
        return count;
    }
}