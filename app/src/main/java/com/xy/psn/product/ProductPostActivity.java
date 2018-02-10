package com.xy.psn.product;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.R;
import com.xy.psn.adapter.ImageQueueAdapter;
import com.xy.psn.async_helper.ImageUploadTask;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.data.ImageObject;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.xy.psn.adapter.ImageQueueAdapter.REQUEST_ALBUM;
import static com.xy.psn.adapter.ImageQueueAdapter.REQUEST_CROP;
import static com.xy.psn.data.MyHelper.loginUserId;
import static com.xy.psn.data.MyHelper.modifyJSON;

public class ProductPostActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private ImageView btnPost;

    private RecyclerView recyclerView;
    private ArrayList<ImageObject> images = new ArrayList<>();
    private ImageQueueAdapter queueAdapter;
    private int itemIndex, itemAmount;

    private EditText edtTitle, edtStatus, edtPrice, edtPS;
    private CheckBox chkAI, chkFN, chkFT, chkIB, chkBM, chkIM, chkAF, chkCD, chkCC, chkDM, chkGN;
    private Spinner spnNote;
    private String title, dep = "", condition, note, price, ps;

    private MyAsyncTask uploadTask = null;
    private ImageUploadTask imageTask = null;
    private Thread trdWaitPhoto = null;
    private Dialog dialog = null;
    private TextView txtPhotoHint, txtUploadHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_post);
        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView txtBarTitle = (TextView) toolbar.findViewById(R.id.txtToolbarTitle);
        txtBarTitle.setText("刊登商品");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        edtTitle = (EditText) findViewById(R.id.edtTitle);
        edtStatus = (EditText) findViewById(R.id.edtStatus);
        edtPrice = (EditText) findViewById(R.id.edtPrice);
        edtPS = (EditText) findViewById(R.id.edtPS);
        chkAI = (CheckBox) findViewById(R.id.chkAI);
        chkFN = (CheckBox) findViewById(R.id.chkFN);
        chkFT = (CheckBox) findViewById(R.id.chkFT);
        chkIB = (CheckBox) findViewById(R.id.chkIB);
        chkBM = (CheckBox) findViewById(R.id.chkBM);
        chkIM = (CheckBox) findViewById(R.id.chkIM);
        chkAF = (CheckBox) findViewById(R.id.chkAF);
        chkCD = (CheckBox) findViewById(R.id.chkCD);
        chkCC = (CheckBox) findViewById(R.id.chkCC);
        chkDM = (CheckBox) findViewById(R.id.chkDM);
        chkGN = (CheckBox) findViewById(R.id.chkGN);
        btnPost = (ImageView) toolbar.findViewById(R.id.btnPost);

        txtPhotoHint = (TextView) findViewById(R.id.txtPhotoHint);
        txtPhotoHint.setText(getString(R.string.post_product_photo_hint));

        spnNote = (Spinner) findViewById(R.id.spnNote);
        spnNote.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                note = adapterView.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isInfoValid())
                    return;

                itemIndex = 0;
                itemAmount = queueAdapter.getEntityAmount();

                if (itemAmount == 0) {
                    Toast.makeText(context, "未選擇圖片", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Toast.makeText(context, "判定有" + String.valueOf(itemAmount) + "張真圖", Toast.LENGTH_SHORT).show();

                for (int i=itemIndex; i<itemAmount; i++) {
                    final ImageObject newImage = queueAdapter.getItem(i);
                    if (newImage.getBitmap() != null) {
                        //開始上傳
                        new Thread(new Runnable() {
                            public void run() {
                                imageTask = new ImageUploadTask(context, getString(R.string.upload_image_link));
                                imageTask.uploadFile(newImage.getBitmap());
                            }
                        }).start();
                        initTrdWaitPhoto(true);
                        txtUploadHint.setText("上傳中，長按取消...  (" + String.valueOf(itemIndex + 1) + "/" + String.valueOf(itemAmount) + ")");
                        dialog.show();
                        break;
                    }

                    if (itemIndex == itemAmount - 1)
                        Toast.makeText(context, "未選擇圖片", Toast.LENGTH_SHORT).show();
                }

            }
        });

        Bitmap bitmap = null;
        images.add(new ImageObject(bitmap)); //此為空白圖

        recyclerView = (RecyclerView) findViewById(R.id.recy_books);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        queueAdapter = new ImageQueueAdapter(context, images, this);
        recyclerView.setAdapter(queueAdapter);
        images = null;

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dlg_uploading);
        dialog.setCancelable(false);
        txtUploadHint = (TextView) dialog.findViewById(R.id.txtHint);
        LinearLayout layUpload = (LinearLayout) dialog.findViewById(R.id.layUpload);
        layUpload.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
                msgbox.setTitle("刊登商品")
                        .setMessage("確定取消上傳嗎？")
                        .setNegativeButton("否", null)
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    imageTask = null;
                                    uploadTask.cancel(true);
                                    dialog.dismiss();
                                    Toast.makeText(context, "上傳已取消", Toast.LENGTH_SHORT).show();
                                }catch (NullPointerException e) {
                                    Toast.makeText(context, "NullPointerException @ cancel", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //詢問是否開啟權限
        switch(requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得權限，進行檔案存取
                    queueAdapter.pickImageDialog();
                }else {
                    //使用者拒絕權限，停用檔案存取功能，並顯示訊息
                    Toast.makeText(context, "權限不足，無法選擇圖片", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //挑選完圖片後執行此方法，將圖片放上cardView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        Uri selectedImageUri = data.getData();
        switch (requestCode) {
            case REQUEST_ALBUM:
                if (!queueAdapter.createImageFile())
                    return;
                if (selectedImageUri != null)
                    queueAdapter.cropImage(selectedImageUri);

                break;
            case REQUEST_CROP:
                final int position = queueAdapter.getPressedPosition();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ImageObject image = queueAdapter.getItem(position);
                        boolean isEmptyCard = image.getBitmap() == null;
                        boolean bitmapIsNull = true;
                        do {
                            bitmapIsNull = queueAdapter.mImageFileToBitmap(image);
                        } while (bitmapIsNull);

                        image.setEntity(true);
                        queueAdapter.setItem(position, image);

                        if (isEmptyCard && queueAdapter.getItemCount() < 5) {
                            Bitmap bitmap = null;
                            queueAdapter.addItem(new ImageObject(bitmap, false)); //再新增一張空白圖
                        }
                    }
                }).start();
                recyclerView.scrollToPosition(position);

                break;
        }
    }

    private boolean isInfoValid() {
        title = edtTitle.getText().toString();
        condition = edtStatus.getText().toString();
        price = edtPrice.getText().toString();
        ps = edtPS.getText().toString();
        dep = "";

        String errMsg = "";
        if (title.equals("")) errMsg += "書名\n";

        if (chkGN.isChecked()) dep += "00#";
        if (chkAI.isChecked()) dep += "01#";
        if (chkFN.isChecked()) dep += "02#";
        if (chkFT.isChecked()) dep += "03#";
        if (chkIB.isChecked()) dep += "04#";
        if (chkBM.isChecked()) dep += "05#";
        if (chkIM.isChecked()) dep += "06#";
        if (chkAF.isChecked()) dep += "07#";
        if (chkCD.isChecked()) dep += "A#";
        if (chkCC.isChecked()) dep += "B#";
        if (chkDM.isChecked()) dep += "C#";
        if (dep.equals(""))
            errMsg += "科系\n";
        else {
            dep = "-1#" + dep;
            dep = dep.substring(0, dep.length() - 1);
        }

        if (condition.equals("")) errMsg += "書況\n";
        if (note.equals("請選擇") || note.equals("")) errMsg += "筆記提供方式\n";
        if (price.equals(""))
            errMsg += "價格\n"; //EditText已限制只能輸入>=0的整數，就算複製其他文字過來也能過濾掉
        else
            price = String.valueOf(Integer.parseInt(price)); //避免有人開頭輸入一堆0

        if (!errMsg.equals("")) {
            errMsg = "以下資料未填寫：\n" + errMsg.substring(0, errMsg.length() - 1);
            AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
            msgbox.setTitle("刊登商品").setPositiveButton("確定", null);
            msgbox.setMessage(errMsg).show();
            return false;
        }else
            return true;
    }

    private Handler hdrWaitPhoto = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (imageTask == null)
                return;

            String fileName = imageTask.getPhotoName();
            if (fileName == null) {
                initTrdWaitPhoto(true); //還沒收到檔名，繼續監聽
            }else {
                initTrdWaitPhoto(false);
                //寫入檔名
                ImageObject image = queueAdapter.getItem(itemIndex);
                image.setFileName(fileName);
                queueAdapter.setItem(itemIndex, image);

                //上傳下一張
                itemIndex++;
                if (itemIndex >= itemAmount) { //圖片都上傳完
                    postProduct();
                    return;
                }
                final ImageObject newImage = queueAdapter.getItem(itemIndex);
                new Thread(new Runnable() {
                    public void run() {
                        imageTask = new ImageUploadTask(context, getString(R.string.upload_image_link));
                        imageTask.uploadFile(newImage.getBitmap());
                    }
                }).start();
                initTrdWaitPhoto(true);
                txtUploadHint.setText("上傳中，長按取消...  (" + String.valueOf(itemIndex + 1) + "/" + String.valueOf(itemAmount) + ")");
            }
        }
    };

    private void initTrdWaitPhoto(boolean restart) {
        trdWaitPhoto = new Thread(new Runnable() {
            @Override
            public void run() {
                hdrWaitPhoto.sendMessage(hdrWaitPhoto.obtainMessage());
            }
        });
        if (restart)
            trdWaitPhoto.start();
    }

    private void postProduct() {
        uploadTask = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
            @Override
            public void onFinished(String result) {
                try{
                    if (result == null) {
                        Toast.makeText(context, "操作失敗", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }
                    JSONArray jsonArray = new JSONArray(modifyJSON(result));
                    if (!jsonArray.getJSONObject(0).getString("Id").equals("Fail")) {
                        dialog.dismiss();
                        Intent it = new Intent(context, ProductDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("productId", jsonArray.getJSONObject(0).getString("Id"));
                        bundle.putString("productName", jsonArray.getJSONObject(0).getString("Title"));
                        it.putExtras(bundle);
                        startActivity(it);
                        finish();
                    }else
                        Toast.makeText(context, "刊登失敗", Toast.LENGTH_SHORT).show();

                }catch (IllegalStateException ise) {
                    Toast.makeText(context, "IllegalStateException @ postProduct", Toast.LENGTH_SHORT).show();
                }catch (Exception e) {
                    Toast.makeText(context, "連線失敗! ", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        try {
            String[] fileName = new String[5];
            for (int i=0; i<itemAmount; i++)
                fileName[i] = queueAdapter.getItem(i).getFileName();
            for (int i=0; i<5; i++) {
                if (fileName[i] == null)
                    fileName[i] = "";
            }
            uploadTask.execute(getString(R.string.post_product_link,
                    loginUserId,
                    URLEncoder.encode(title, "UTF-8"),
                    URLEncoder.encode(dep, "UTF-8"),
                    URLEncoder.encode(condition, "UTF-8"),
                    URLEncoder.encode(note, "UTF-8"),
                    price,
                    URLEncoder.encode(ps, "UTF-8"),
                    URLEncoder.encode(fileName[0], "UTF-8"),
                    URLEncoder.encode(fileName[1], "UTF-8"),
                    URLEncoder.encode(fileName[2], "UTF-8"),
                    URLEncoder.encode(fileName[3], "UTF-8"),
                    URLEncoder.encode(fileName[4], "UTF-8")
            ));
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }catch (IllegalStateException e2) {
            e2.printStackTrace();
            Toast.makeText(context, "IllegalStateException @ hdrWaitPhoto", Toast.LENGTH_SHORT).show();
        }
    }

}
