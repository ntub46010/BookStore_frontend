package com.xy.psn.product;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.R;
import com.xy.psn.adapter.ProductDisplayAdapter;
import com.xy.psn.async_helper.GetBitmap;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.data.Book;
import com.xy.psn.data.MyHelper;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.xy.psn.MainActivity.board;
import static com.xy.psn.data.MyHelper.Comma;
import static com.xy.psn.data.MyHelper.getBoardNickname;
import static com.xy.psn.data.MyHelper.getNotFoundImg;
import static com.xy.psn.data.MyHelper.isProductDisplayAlive;
import static com.xy.psn.data.MyHelper.modifyJSON;

public class ProductSearchActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private SearchView searchView;
    private ProgressBar prgBar;
    private RecyclerView recyclerView;
    private ImageView imgNotFound;
    private TextView txtNotFound;
    private ArrayList<ImageObj> books;
    private ProductDisplayAdapter myAdapter;
    private MyAsyncTask productTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_search);
        context = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        prgBar = (ProgressBar) findViewById(R.id.prgBar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        txtNotFound = (TextView) findViewById(R.id.txtNotFound);
        imgNotFound = (ImageView) findViewById(R.id.imgNotFound);
        txtNotFound.setVisibility(View.GONE);
        imgNotFound.setVisibility(View.GONE);

        searchView = (SearchView) toolbar.findViewById(R.id.searchview);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!s.equals("")) {
                    loadData(s);
                }
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        searchView.setQueryHint("搜尋" + getBoardNickname() + "商品");

        try {
            int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView textView = (TextView) searchView.findViewById(id);
            textView.setTextColor(Color.parseColor("#FFFFFF"));
            textView.setHintTextColor(Color.parseColor("#80FFFFFF"));
        }catch (Exception e) {
            Toast.makeText(context, "Exception", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isProductDisplayAlive = true;

        try { //注意：回到桌面也會執行這裡，而停止執行緒
            myAdapter.setCanCheckLoop(true);
            myAdapter.initCheckThread(true);
        }catch (NullPointerException e) {
            //第一次開啟，adapter尚未準備好
        }
    }

    @Override
    public void onPause() {
        isProductDisplayAlive = false;
        try { //注意：回到桌面也會執行這裡，而停止執行緒
            myAdapter.setCanCheckLoop(false);
            myAdapter.initCheckThread(false);
        }catch (NullPointerException e) {
            //第一次開啟，adapter尚未準備好
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            productTask.cancel(true);
        }catch (NullPointerException e) {}
        System.gc();
        super.onDestroy();
    }

    private void loadData(String keyword) {
        if (MyHelper.canShowProduct) {
            MyHelper.canShowProduct = false;
            //Toast.makeText(context, "搜尋商品", Toast.LENGTH_SHORT).show();
            prgBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            // 產生將顯示的資料
            books = new ArrayList<>();

            // (1)宣告一個處理資料取回後, 處理回傳JSON格式資料的物件.
            productTask = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
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
                                    jsonArray.getJSONObject(i).getString("Id"),
                                    getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo"),
                                    jsonArray.getJSONObject(i).getString("Title"),
                                    Comma(jsonArray.getJSONObject(i).getString("Price")),
                                    jsonArray.getJSONObject(i).getString("SellerName")
                            ));
                        }
                    }catch (StringIndexOutOfBoundsException sie) {
                        Toast.makeText(context, "沒有找到商品", Toast.LENGTH_SHORT).show();
                    }catch (IllegalStateException ise) {
                        Toast.makeText(context, "IllegalStateException @ ", Toast.LENGTH_SHORT).show();
                    }catch (Exception e) {
                        Toast.makeText(context, "連線失敗! ", Toast.LENGTH_SHORT).show();
                    }

                    // 產生物件ArrayList資料後, 由圖片位址下載圖片, 完成後再顯示資料.
                    GetBitmap getBitmap = new GetBitmap(context, books, new GetBitmap.TaskListener() {
                        // 下載圖片完成後執行的方法
                        @Override
                        public void onFinished() {
                            showData();
                        }
                    });
                    // 執行圖片下載
                    getBitmap.setPreLoadAmount(12);
                    getBitmap.execute();
                }
            });

            // (2)向主機網址發出取回資料請求
            try {
                productTask.execute(getString(R.string.product_home_link, board, URLEncoder.encode(keyword, "UTF-8")));
            }catch (UnsupportedEncodingException uee) {
                //Toast.makeText(context, "UnsupportedEncodingException @ loadData", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showData () {
        try {
            // 產生 RecyclerView
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);

            // 設定 RecycleView的版型
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);

            // 產生一個 MyAdapter物件, 連結將加入的資料
            myAdapter = new ProductDisplayAdapter(context, books);
            myAdapter.setBackgroundColor(getResources(), R.color.card_product);

            // 將結合資料後的 stockAdapter 加入 RecyclerView物件中
            recyclerView.setAdapter(myAdapter);

            showFoundStatus();
            books = null;

            prgBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            MyHelper.canShowProduct = true;

        }catch (NullPointerException npe) {
            Toast.makeText(context, "NullPointerException", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFoundStatus() {
        //若未找到最愛的書，則說明沒有找到
        if (books == null || books.isEmpty()) {
            txtNotFound.setText("沒有找到商品");
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
