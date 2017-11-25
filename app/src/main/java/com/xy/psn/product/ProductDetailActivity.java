package com.xy.psn.product;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.R;
import com.xy.psn.adapter.ImageGroupAdapter;
import com.xy.psn.async_helper.GetBitmapBatch;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.data.Book;
import com.xy.psn.data.MyHelper;
import com.xy.psn.member.MemberChatActivity;
import com.xy.psn.member.MemberProfileActivity;

import org.json.JSONArray;

import java.util.ArrayList;

import static com.xy.psn.data.MyHelper.Comma;
import static com.xy.psn.data.MyHelper.getNotFoundImg;
import static com.xy.psn.data.MyHelper.isProfileAlive;
import static com.xy.psn.data.MyHelper.isStockDisplayAlive;
import static com.xy.psn.data.MyHelper.loginUserId;
import static com.xy.psn.data.MyHelper.modifyJSON;

public class ProductDetailActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private String productId, productName;
    private static String sellerId, sellerName;
    private LinearLayout layDetail;
    private ImageView imgNotFound;
    private TextView txtNotFound;
    private TextView txtId, txtTitle, txtDep, txtStatus, txtNote, txtPrice, txtPS, txtSeller, txtPost, txtEdit;
    public static FloatingActionButton fabContact, fabLike;
    private ArrayList<ImageObj> books;
    public static ArrayList<Bitmap> pictures;
    public static int selectedIndex = 0;
    private MyAsyncTask[] tasks = new MyAsyncTask[2];
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        context = this;
        Bundle bundle = getIntent().getExtras();
        productId = bundle.getString("productId");
        productName = bundle.getString("productName");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(productName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        layDetail = (LinearLayout) findViewById(R.id.layDetail);
        layDetail.setVisibility(View.INVISIBLE);

        txtId = (TextView) findViewById(R.id.txtBookDetailId);
        txtTitle = (TextView) findViewById(R.id.txtBookDetailTitle);
        txtDep = (TextView) findViewById(R.id.txtBookDep);
        txtStatus = (TextView) findViewById(R.id.txtBookDetailStatus);
        txtNote = (TextView) findViewById(R.id.txtBookDetailNote);
        txtPrice = (TextView) findViewById(R.id.txtBookDetailPrice);
        txtPS = (TextView) findViewById(R.id.txtBookDetailPS);
        txtSeller = (TextView) findViewById(R.id.txtBookDetailSeller);
        txtPost = (TextView) findViewById(R.id.txtPostDate);
        txtEdit = (TextView) findViewById(R.id.txtEditDate);

        txtNotFound = (TextView) findViewById(R.id.txtNotFound);
        imgNotFound = (ImageView) findViewById(R.id.imgNotFound);
        txtNotFound.setVisibility(View.GONE);
        imgNotFound.setVisibility(View.GONE);

        setFab();
        fabContact.setVisibility(View.INVISIBLE);
        fabLike.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isShown) {
            loadProductData();
            loadFavoriteStatus();
        }
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
        MyHelper.bmpProductDetail = null;
        pictures = null;
        System.gc();
        super.onDestroy();
    }

    private void loadProductData() {
        //Toast.makeText(context, "載入商品詳情", Toast.LENGTH_SHORT).show();
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
                    JSONArray jsonArray = new JSONArray(modifyJSON(result));
                    for (int i=0; i<jsonArray.length(); i++) {
                        books.add(new Book(
                                productId,
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo"),
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo2"),
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo3"),
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo4"),
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo5"),
                                jsonArray.getJSONObject(i).getString("Title"),
                                jsonArray.getJSONObject(i).getString("Status"),
                                jsonArray.getJSONObject(i).getString("Note"),
                                Comma(jsonArray.getJSONObject(i).getString("Price")),
                                jsonArray.getJSONObject(i).getString("PS"),
                                jsonArray.getJSONObject(i).getString("Seller"),
                                jsonArray.getJSONObject(i).getString("SellerName"),
                                getString(R.string.avatar_link) + jsonArray.getJSONObject(i).getString("SellerAvatar"),
                                jsonArray.getJSONObject(i).getString("Department").substring(2),
                                jsonArray.getJSONObject(i).getString("PostDateTime"),
                                jsonArray.getJSONObject(i).getString("EditDateTime")));
                    }

                }catch (IllegalStateException ise) {
                    Toast.makeText(context, "IllegalStateException @ ", Toast.LENGTH_SHORT).show();
                }catch (StringIndexOutOfBoundsException sie) {
                    //Toast.makeText(context, "此商品不存在", Toast.LENGTH_SHORT).show();
                }catch (Exception e) {
                    Toast.makeText(context, "連線失敗! a", Toast.LENGTH_SHORT).show();
                }

                // 產生物件ArrayList資料後, 由圖片位址下載圖片, 完成後再顯示資料.
                GetBitmapBatch getBitmap = new GetBitmapBatch(context, getResources(), books, new GetBitmapBatch.TaskListener() {
                    // 下載圖片完成後執行的方法
                    @Override
                    public void onFinished() {
                        showData();
                    }
                });
                // 執行圖片下載
                getBitmap.execute();
            }
        });

        // (2)向主機網址發出取回資料請求
        tasks[0].execute(getString(R.string.product_detail_link, productId, String.valueOf(0)));
    }

    private void loadFavoriteStatus () {
        // (1)宣告一個處理資料取回後, 處理回傳JSON格式資料的物件.
        tasks[1] = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
            @Override
            public void onFinished(String result) {
                try{
                    if (result == null) {
                        Toast.makeText(context, "無資料!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //由主機取回的JSONArray內容
                    if (result.contains("[{"))
                        fabLike.setImageResource(R.drawable.ic_favorite_pink);
                    else
                        fabLike.setImageResource(R.drawable.ic_favorite_white);

                }catch (IllegalStateException ise) {
                    Toast.makeText(context, "IllegalStateException @ ", Toast.LENGTH_SHORT).show();
                }catch (StringIndexOutOfBoundsException sie) {
                    Toast.makeText(context, "StringIndexOutOfBoundsException @ ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // (2)向主機網址發出取回資料請求
        tasks[1].execute(getString(R.string.check_favorite_link, loginUserId, productId));
    }

    private void showData () {
        try {
            Book book = (Book) books.get(0);
            txtId.setText(productId);
            txtTitle.setText(book.getTitle());
            txtStatus.setText(book.getStatus());
            txtNote.setText(book.getNote());
            txtPrice.setText("$ " + book.getPrice());
            txtPS.setText(book.getPs());

            txtPost.setText(book.getPostDate() + "  刊登");
            txtEdit.setText(book.getEditDate() + "  編輯");
            if (book.getEditDate().equals("") || book.getEditDate().equals("null"))
                txtEdit.setVisibility(View.GONE);

            sellerId = book.getSeller();
            sellerName = book.getSellerName();

            //賣家若是自己，字型設灰色，不可點擊
            //SpannableString content = new SpannableString(sellerName + " (" + sellerId + ")");
            if (!sellerId.equals(loginUserId) && !isProfileAlive && !isStockDisplayAlive) { //若 MemberProfileActivity 已存在，也不可再點擊賣家
                //content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                txtSeller.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent it = new Intent(context, MemberProfileActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("memberId", sellerId);
                        it.putExtras(bundle);
                        startActivity(it);
                    }
                });
            }else {
                txtSeller.setTextColor(Color.parseColor("#555555"));
            }
            txtSeller.setText(sellerName + " (" + sellerId + ")");

            //顯示科系
            String bookDep = "";
            if (book.getDep().contains("01")) bookDep += "會資、";
            if (book.getDep().contains("02")) bookDep += "財金、";
            if (book.getDep().contains("03")) bookDep += "財稅、";
            if (book.getDep().contains("04")) bookDep += "國商、";
            if (book.getDep().contains("05")) bookDep += "企管、";
            if (book.getDep().contains("06")) bookDep += "資管、";
            if (book.getDep().contains("07")) bookDep += "應外、";
            if (book.getDep().contains("A")) bookDep += "商設、";
            if (book.getDep().contains("B")) bookDep += "商創、";
            if (book.getDep().contains("C")) bookDep += "數媒、";
            if (book.getDep().contains("00")) bookDep += "通識、";
            txtDep.setText(bookDep.substring(0, bookDep.length() - 1));

            if (sellerId.equals(loginUserId)) {
                fabLike.setVisibility(View.INVISIBLE);
                fabContact.setVisibility(View.INVISIBLE);
            }else {
                fabLike.setVisibility(View.VISIBLE);
                fabContact.setVisibility(View.VISIBLE);
                fabLike.show();
                fabContact.show();
            }
            showImages(book);

            layDetail.setVisibility(View.VISIBLE);
        }catch (NullPointerException npe) {
            Toast.makeText(context, "NullPointerException @ ", Toast.LENGTH_SHORT).show();
        }catch (IndexOutOfBoundsException ioe) {
            showFoundStatus();
            //Toast.makeText(context, "IndexOutOfBoundsException @ showData", Toast.LENGTH_SHORT).show();
        }
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.prgDetail);
        progressBar.setVisibility(View.GONE);
        isShown = true;
    }

    private void showImages (Book book) {
        try {
            MyHelper.bmpProductDetail = new Bitmap[5];
            pictures = new ArrayList<>();
            if (book.getImgURL().length() > getString(R.string.image_link).length() + 4) pictures.add(book.getImg());
            if (book.getImgURL2().length() > getString(R.string.image_link).length() + 4) pictures.add(book.getImg2());
            if (book.getImgURL3().length() > getString(R.string.image_link).length() + 4) pictures.add(book.getImg3());
            if (book.getImgURL4().length() > getString(R.string.image_link).length() + 4) pictures.add(book.getImg4());
            if (book.getImgURL5().length() > getString(R.string.image_link).length() + 4) pictures.add(book.getImg5());

            // 產生 RecyclerView
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recy_books);
            recyclerView.setHasFixedSize(true);

            // 設定 RecycleView的版型
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(linearLayoutManager);

            // 產生一個 MyAdapter物件, 連結將加入的資料
            ImageGroupAdapter myAdapter = new ImageGroupAdapter(context, pictures, true);

            // 將結合資料後的 stockAdapter 加入 RecyclerView物件中
            recyclerView.setAdapter(myAdapter);
        }catch (NullPointerException npe) {
            Toast.makeText(context, "NullPointerException @ ", Toast.LENGTH_SHORT).show();
        }

    }

    private void setFab () {
        fabContact = (FloatingActionButton) findViewById(R.id.fab_contact);
        fabContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Book book = (Book) books.get(0);
                Intent it = new Intent(context, MemberChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("memberId", sellerId);
                bundle.putString("memberName", sellerName);
                bundle.putString("memberAvatar", book.getSellerAvatar());
                //bundle.putString("memberToken", book.getSellerToken());
                bundle.putString("productId", productId);
                bundle.putSerializable("product", new Book(productId, book.getImgURL(), book.getTitle(), book.getPrice()));
                it.putExtras(bundle);
                startActivity(it);
            }
        });

        fabLike = (FloatingActionButton) findViewById(R.id.fab_favorite);
        fabLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // (1)宣告一個處理資料取回後, 處理回傳JSON格式資料的物件.
                MyAsyncTask myAsyncTask = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
                    @Override
                    public void onFinished(String result) {
                        try{
                            if (result == null) {
                                Toast.makeText(context, "操作失敗", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (result.contains("Add Success")) {
                                Toast.makeText(context, "加入我的最愛", Toast.LENGTH_SHORT).show();
                                fabLike.setImageResource(R.drawable.ic_favorite_pink);
                            }else if (result.contains("Remove Success")) {
                                Toast.makeText(context, "從我的最愛移除", Toast.LENGTH_SHORT).show();
                                fabLike.setImageResource(R.drawable.ic_favorite_white);
                            }

                        }catch (IllegalStateException ise) {
                            Toast.makeText(context, "IllegalStateException @ ", Toast.LENGTH_SHORT).show();
                        }catch (Exception e) {
                            Toast.makeText(context, "連線失敗! b", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                // (2)向主機網址發出資料請求
                myAsyncTask.execute(getString(R.string.add_favorite_link, loginUserId, productId));
            }
        });

    }

    private void showFoundStatus() {
        //若未找到書，則說明沒有找到
        if (books == null || books.isEmpty()) {
            txtNotFound.setText("此商品已被下架");
            txtNotFound.setVisibility(View.VISIBLE);
            imgNotFound.setImageResource(getNotFoundImg());
            imgNotFound.setVisibility(View.VISIBLE);
        }else {
            txtNotFound.setText("");
            txtNotFound.setVisibility(View.GONE);
            imgNotFound.setVisibility(View.GONE);
        }
    }
}
