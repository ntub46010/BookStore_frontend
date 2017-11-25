package com.xy.psn.member;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.R;
import com.xy.psn.adapter.ImageQueueAdapter;
import com.xy.psn.async_helper.GetBitmapBatch;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.async_helper.ImageUploadTask;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.data.Book;
import com.xy.psn.data.ImageObject;
import com.xy.psn.data.MyHelper;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.xy.psn.data.MyHelper.modifyJSON;
import static com.xy.psn.member.MemberStockActivity.isStockShown;
import static com.xy.psn.member.MemberStockActivity.stockAdapter;

public class ProductEditActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private LinearLayout layDetail;
    private ImageView btnPost;
    private TextView txtId, txtPhotoHint;
    private boolean isShown = false;

    private RecyclerView recyclerView;
    private ImageQueueAdapter myAdapter;
    private ArrayList<ImageObject> images = new ArrayList<>();
    private int itemIndex, itemAmount;

    private EditText edtTitle, edtStatus, edtPrice, edtPS;
    private CheckBox chkAI, chkFN, chkFT, chkIB, chkBM, chkIM, chkAF, chkCD, chkCC, chkDM, chkGN;
    private Spinner spnNote;
    private String bookId, title, status, price, ps, dep, note;

    private ArrayList<ImageObj> books;
    private MyAsyncTask[] tasks = new MyAsyncTask[2];
    private ProgressBar prgBar;

    private ImageUploadTask imageTask = null;
    private Thread trdWaitPhoto = null;
    private Dialog dialog = null;
    private TextView txtUploadHint;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);
        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView txtBarTitle = (TextView) toolbar.findViewById(R.id.txtToolbarTitle);
        txtBarTitle.setText("編輯商品");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle bundle = getIntent().getExtras();
        bookId = bundle.getString("productId");

        layDetail = (LinearLayout) findViewById(R.id.layDetail);
        layDetail.setVisibility(View.INVISIBLE);

        txtId = (TextView) findViewById(R.id.txtId);
        edtTitle = (EditText) findViewById(R.id.edtTitle);
        edtStatus = (EditText) findViewById(R.id.edtStatus);
        edtPrice = (EditText) findViewById(R.id.edtPrice);
        edtPS = (EditText) findViewById(R.id.edtPS);
        chkGN = (CheckBox) findViewById(R.id.chkGN);
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

        txtPhotoHint = (TextView) findViewById(R.id.txtPhotoHint);
        txtPhotoHint.setText(getString(R.string.edit_product_photo_hint));

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

        btnPost = (ImageView) toolbar.findViewById(R.id.btnPost);
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isShown)
                    return;
                if (!isInfoValid())
                    return;

                itemIndex = 0; //卡片陣列索引
                itemAmount = 0; //新圖片數量
                count = 0; //新圖片上傳計數
                ImageObject image;

                image = myAdapter.getItem(0);
                if (image.getBitmap() == null) { //檢查是否至少有一張圖片
                    Toast.makeText(context, "未選擇圖片", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (int i=0; i<myAdapter.getItemCount(); i++) { //計算新圖片數量
                    image = myAdapter.getItem(i);
                    if (image.isEntity())
                        itemAmount++;
                }
                //Toast.makeText(context, "判定有" + String.valueOf(itemAmount) + "張新圖", Toast.LENGTH_SHORT).show();

                if (itemAmount == 0) { //若無新圖片，直接上傳文字即可
                    postProduct();
                    return;
                }

                for (int i=0; i<myAdapter.getItemCount(); i++) { //尋找第一張新圖片在陣列的索引
                    final ImageObject newImage = myAdapter.getItem(i);
                    if (newImage.isEntity()) {
                        itemIndex = i;
                        //開始上傳
                        new Thread(new Runnable() {
                            public void run() {
                                imageTask = new ImageUploadTask(context, getString(R.string.upload_image_link));
                                imageTask.uploadFile(newImage.getBitmap());
                            }
                        }).start();
                        initTrdWaitPhoto(true);
                        count++;
                        txtUploadHint.setText("上傳中，長按取消...  (" + String.valueOf(count) + "/" + String.valueOf(itemAmount) + ")");
                        dialog.show();
                        break;
                    }
                }
            }
        });

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dlg_uploading);
        dialog.setCancelable(false);
        txtUploadHint = (TextView) dialog.findViewById(R.id.txtHint);
        LinearLayout layUpload = (LinearLayout) dialog.findViewById(R.id.layUpload);
        layUpload.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
                msgbox.setTitle("編輯商品")
                        .setMessage("確定取消上傳嗎？")
                        .setNegativeButton("否", null)
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    imageTask = null;
                                    tasks[1].cancel(true);
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

        prgBar = (ProgressBar) findViewById(R.id.prgDetail);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isShown)
            loadProductData();
    }

    @Override
    public void onDestroy() {
        for (MyAsyncTask task : tasks) {
            try {
                task.cancel(true);
            }catch (NullPointerException e) {
                //Toast.makeText(context, "NullPointerException @ onPause", Toast.LENGTH_SHORT).show();
            }
        }
        //--
        System.gc();
        super.onDestroy();
    }

    //挑選完圖片後執行此方法，將圖片放上cardView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {return;}

        Uri selectedImageUri = data.getData();

        switch (requestCode) {
            /*case ImageQueueAdapter.REQUEST_CAMERA:
                break;*/
            case ImageQueueAdapter.REQUEST_ALBUM:
                if (!myAdapter.createImageFile())
                    return;
                if (selectedImageUri != null)
                    myAdapter.cropImage(selectedImageUri);

                break;
            case ImageQueueAdapter.REQUEST_CROP:
                final int position = myAdapter.getPressedPosition();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ImageObject image = myAdapter.getItem(position);
                        boolean isEmptyCard = image.getBitmap() == null;
                        boolean bitmapIsNull = true;
                        do {
                            bitmapIsNull = myAdapter.mImageFileToBitmap(image);
                        } while (bitmapIsNull);

                        image.setEntity(true);
                        myAdapter.setItem(position, image);

                        if (isEmptyCard && myAdapter.getItemCount() < 5) {
                            Bitmap bitmap = null;
                            myAdapter.addItem(new ImageObject(bitmap, false)); //再新增一張空白圖
                        }

                    }
                }).start();
                recyclerView.scrollToPosition(position);

                break;
        }
    }

    private void loadProductData() {
        btnPost.setVisibility(View.GONE);
        books = new ArrayList<>();

        // (1)宣告一個處理資料取回後, 處理回傳JSON格式資料的物件.
        tasks[0] = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
            @Override
            public void onFinished(String result) {
                try{
                    if (result == null) {
                        Toast.makeText(context, "無資料!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 將由主機取回的JSONArray內容生成Book物件, 再加入ArrayList物件中
                    JSONArray jsonArray = new JSONArray(modifyJSON(result));
                    for(int i=0; i<jsonArray.length(); i++){
                        books.add(new Book(
                                bookId,
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo"),
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo2"),
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo3"),
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo4"),
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo5"),
                                jsonArray.getJSONObject(i).getString("Title"),
                                jsonArray.getJSONObject(i).getString("Status"),
                                jsonArray.getJSONObject(i).getString("Note"),
                                jsonArray.getJSONObject(i).getString("Price"),
                                jsonArray.getJSONObject(i).getString("PS"),
                                jsonArray.getJSONObject(i).getString("Department").substring(2)));//attribute is "seller"
                    }
                }catch (StringIndexOutOfBoundsException sie) {
                    //Toast.makeText(context, "此商品已被下架", Toast.LENGTH_SHORT).show();
                    return;
                }catch (Exception e) {
                    Toast.makeText(context, "連線失敗! ", Toast.LENGTH_SHORT).show();
                }

                // 產生物件ArrayList資料後, 由圖片位址下載圖片, 完成後再顯示資料.
                GetBitmapBatch getBitmap = new GetBitmapBatch(context, getResources(), books, new GetBitmapBatch.TaskListener() {
                    // 下載圖片完成後執行的方法
                    @Override
                    public void onFinished() {
                        showProductData();
                    }
                });

                // 執行圖片下載
                getBitmap.execute();
            }
        });

        // (2)向主機網址發出取回資料請求
        tasks[0].execute(getString(R.string.product_detail_link, bookId, String.valueOf(0)));
    }

    private void showProductData() {
        try {
            Book book = (Book) books.get(0);
            txtId.setText(bookId);
            edtTitle.setText(book.getTitle());
            edtStatus.setText(book.getStatus());
            edtPrice.setText(book.getPrice());
            edtPS.setText(book.getPs());

            String bookType = book.getSeller();
            if (bookType.contains("00")) chkGN.setChecked(true);
            if (bookType.contains("01")) chkAI.setChecked(true);
            if (bookType.contains("02")) chkFN.setChecked(true);
            if (bookType.contains("03")) chkFT.setChecked(true);
            if (bookType.contains("04")) chkIB.setChecked(true);
            if (bookType.contains("05")) chkBM.setChecked(true);
            if (bookType.contains("06")) chkIM.setChecked(true);
            if (bookType.contains("07")) chkAF.setChecked(true);
            if (bookType.contains("A")) chkCD.setChecked(true);
            if (bookType.contains("B")) chkCC.setChecked(true);
            if (bookType.contains("C")) chkDM.setChecked(true);

            String bookNote = book.getNote();
            switch (bookNote) {
                case "未附筆記":
                    spnNote.setSelection(1);
                    note = "未附筆記";
                    break;
                case "寫在書中":
                    spnNote.setSelection(2);
                    note = "寫在書中";
                    break;
                case "另附筆記本":
                    spnNote.setSelection(3);
                    note = "另附筆記本";
                    break;
                case "寫在書中及筆記本":
                    spnNote.setSelection(4);
                    note = "寫在書中及筆記本";
                    break;
            }
            showImages(book);

            books = null;
            prgBar.setVisibility(View.GONE);
            layDetail.setVisibility(View.VISIBLE);
            btnPost.setVisibility(View.VISIBLE);
            isShown = true;
        }catch (NullPointerException npe) {
            Toast.makeText(context, "NullPointerException @ ", Toast.LENGTH_SHORT).show();
        }catch (IndexOutOfBoundsException ioe) {
            Toast.makeText(context, "IndexOutOfBoundsException @ ", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImages(Book book) {
        ArrayList<ImageObject> pictures = new ArrayList<>();

        if (book.getImgURL().length() > getString(R.string.image_link).length() + 4) {
            Bitmap bitmap = book.getImg();
            ImageObject image = new ImageObject(bitmap, false); //此圖片來自網路，並非從手機追加的實體圖片
            image.setFileName(book.getImgURL().substring(getString(R.string.image_link).length()));
            pictures.add(image);
        }
        if (book.getImgURL2().length() > getString(R.string.image_link).length() + 4) {
            Bitmap bitmap = book.getImg2();
            ImageObject image = new ImageObject(bitmap, false);
            image.setFileName(book.getImgURL2().substring(getString(R.string.image_link).length()));
            pictures.add(image);
        }
        if (book.getImgURL3().length() > getString(R.string.image_link).length() + 4) {
            Bitmap bitmap = book.getImg3();
            ImageObject image = new ImageObject(bitmap, false);
            image.setFileName(book.getImgURL3().substring(getString(R.string.image_link).length()));
            pictures.add(image);
        }
        if (book.getImgURL4().length() > getString(R.string.image_link).length() + 4) {
            Bitmap bitmap = book.getImg4();
            ImageObject image = new ImageObject(bitmap, false);
            image.setFileName(book.getImgURL4().substring(getString(R.string.image_link).length()));
            pictures.add(image);
        }
        if (book.getImgURL5().length() > getString(R.string.image_link).length() + 4) {
            Bitmap bitmap = book.getImg5();
            ImageObject image = new ImageObject(bitmap, false);
            image.setFileName(book.getImgURL5().substring(getString(R.string.image_link).length()));
            pictures.add(image);
        }
        if (pictures.size() < 5) { //圖片不足5張時，添加一張空白圖
            Bitmap bitmap = null;
            pictures.add(new ImageObject(bitmap, false));
        }

        // 產生 RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recy_books);
        recyclerView.setHasFixedSize(true);

        // 設定 RecycleView的版型
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        // 產生一個 MyAdapter物件, 連結將加入的資料
        myAdapter = new ImageQueueAdapter(context, pictures, this);

        // 將結合資料後的 stockAdapter 加入 RecyclerView物件中
        recyclerView.setAdapter(myAdapter);
        pictures = null;
    }

    private boolean isInfoValid() {
        String errMsg = "";
        title = edtTitle.getText().toString();
        dep = "";
        status = edtStatus.getText().toString();
        price = edtPrice.getText().toString();
        ps = edtPS.getText().toString();

        if (title.equals("")) errMsg += "書名\n";
        if (chkGN.isChecked()) dep += "0#";
        if (chkAI.isChecked()) dep += "1#";
        if (chkFN.isChecked()) dep += "2#";
        if (chkFT.isChecked()) dep += "3#";
        if (chkIB.isChecked()) dep += "4#";
        if (chkBM.isChecked()) dep += "5#";
        if (chkIM.isChecked()) dep += "6#";
        if (chkAF.isChecked()) dep += "7#";
        if (chkCD.isChecked()) dep += "A#";
        if (chkCC.isChecked()) dep += "B#";
        if (chkDM.isChecked()) dep += "C#";
        if (dep.equals(""))
            errMsg += "科系\n";
        else {
            dep = "-1#" + dep;
            dep = dep.substring(0, dep.length() - 1);
        }

        if (status.equals("")) errMsg += "書況\n";
        if (note.equals("請選擇") || note.equals("")) errMsg += "筆記提供方式\n";
        if (price.equals(""))
            errMsg += "價格\n"; //EditText已限制只能輸入>=0的整數，就算複製其他文字過來也能過濾掉
        else
            price = String.valueOf(Integer.parseInt(price)); //避免有人開頭輸入一堆0

        if (!errMsg.equals("")) {
            errMsg = "以下資料未填寫：\n" + errMsg.substring(0, errMsg.length() - 1);
            AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
            msgbox.setTitle("編輯商品").setPositiveButton("確定", null);
            msgbox.setMessage(errMsg).show();
            return false;
        }else
            return true;
    }

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
                ImageObject image = myAdapter.getItem(itemIndex);
                image.setFileName(fileName);
                myAdapter.setItem(itemIndex, image);

                //上傳下一張
                itemIndex++;
                if (count >= itemAmount) { //圖片都上傳了
                    dialog.dismiss();
                    postProduct();
                    return;
                }
                for (int i=itemIndex; i<myAdapter.getItemCount(); i++) { //新增的圖片可能穿插，需要逐一確認
                    final ImageObject newImage = myAdapter.getItem(i);
                    if (newImage.getBitmap() != null) {
                        new Thread(new Runnable() {
                            public void run() {
                                imageTask = new ImageUploadTask(context, getString(R.string.upload_image_link));
                                imageTask.uploadFile(newImage.getBitmap());
                            }
                        }).start();
                        initTrdWaitPhoto(true);
                        count++;
                        txtUploadHint.setText("上傳中，長按取消...  (" + String.valueOf(count) + "/" + String.valueOf(itemAmount) + ")");
                        break;
                    }
                }
                //Toast.makeText(context, "B開始上傳第 " + String.valueOf(count), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void postProduct () {
        // (1)宣告一個處理資料取回後, 處理回傳JSON格式資料的物件.
        tasks[1] = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
            @Override
            public void onFinished(String result) {
                try{
                    if (result == null) {
                        Toast.makeText(context, "操作失敗", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (result.contains("Success")) {
                        Toast.makeText(context, "編輯成功", Toast.LENGTH_SHORT).show();
                        isStockShown = false;
                        stockAdapter.setCanCheckLoop(false);
                        finish();
                    }else if (result.contains("Fail")) {
                        Toast.makeText(context, "編輯失敗", Toast.LENGTH_SHORT).show();
                    }

                }catch (IllegalStateException ise) {
                    Toast.makeText(context, "IllegalStateException @ ", Toast.LENGTH_SHORT).show();
                }catch (Exception e) {
                    Toast.makeText(context, "連線失敗! ", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                MyHelper.canShowShelf = true;
            }
        });

        // (2)向主機網址發出資料請求
        try {
            String[] fileName = new String[5];
            for (int i=0; i<myAdapter.getItemCount(); i++)
                fileName[i] = myAdapter.getItem(i).getFileName();
            for (int i=0; i<5; i++) {
                if (fileName[i] == null)
                    fileName[i] = "";
            }

            tasks[1].execute(getString(R.string.update_product_link,
                    bookId,
                    URLEncoder.encode(title, "UTF-8"),
                    URLEncoder.encode(dep, "UTF-8"),
                    URLEncoder.encode(status, "UTF-8"),
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
            Toast.makeText(context, "UnsupportedEncodingException @", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MyHelper.canShowShelf = false;
    }

}
