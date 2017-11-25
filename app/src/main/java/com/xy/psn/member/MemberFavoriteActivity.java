package com.xy.psn.member;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.R;
import com.xy.psn.adapter.ProductDisplayAdapter;
import com.xy.psn.async_helper.GetBitmap;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.data.Book;

import org.json.JSONArray;

import java.util.ArrayList;

import static com.xy.psn.data.MyHelper.Comma;
import static com.xy.psn.data.MyHelper.canShowFavorite;
import static com.xy.psn.data.MyHelper.isProductDisplayAlive;
import static com.xy.psn.data.MyHelper.loginUserId;
import static com.xy.psn.data.MyHelper.getNotFoundImg;
import static com.xy.psn.data.MyHelper.modifyJSON;

public class MemberFavoriteActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private ImageView imgNotFound;
    private TextView txtNotFound;
    private ArrayList<ImageObj> books;
    private ProductDisplayAdapter myAdapter;
    private MyAsyncTask productTask;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_favorite);
        context = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("我的最愛");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(canShowFavorite) {
                    loadData();
                }
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.prgBar);
        txtNotFound = (TextView) findViewById(R.id.txtNotFound);
        imgNotFound = (ImageView) findViewById(R.id.imgNotFound);
        txtNotFound.setVisibility(View.GONE);
        imgNotFound.setVisibility(View.GONE);

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        isProductDisplayAlive = true;
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
        productTask.cancel(true);
        System.gc();
        super.onDestroy();
    }

    // 產生資料
    private void loadData() {
        if (canShowFavorite) {
            //Toast.makeText(context, "載入最愛", Toast.LENGTH_SHORT).show();
            canShowFavorite = false;
            swipeRefreshLayout.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
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
                                    jsonArray.getJSONObject(i).getString("Seller")
                            ));
                        }

                    }catch (StringIndexOutOfBoundsException sie) {
                        //Toast.makeText(context, "沒有你最愛的書籍", Toast.LENGTH_SHORT).show();
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
                    getBitmap.execute();
                }
            });

            // (2)向主機網址發出取回資料請求
            productTask.execute(getString(R.string.favorite_link, loginUserId));
        }
    }

    private void showData () {
        try {
            // 產生 RecyclerView
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);

            // 設定 RecycleView的版型
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);

            // 產生一個 MyAdapter物件, 連結將加入的資料
            myAdapter = new ProductDisplayAdapter(context, books);
            myAdapter.setBackgroundColor(getResources(), R.color.card_favorite);

            // 將結合資料後的 stockAdapter 加入 RecyclerView物件中
            recyclerView.setAdapter(myAdapter);
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setRefreshing(false);

            //若未找到最愛的書，則說明沒有找到
            showFoundStatus();
            books = null;

            progressBar.setVisibility(View.GONE);
            canShowFavorite = true;

        }catch (NullPointerException npe) {
            Toast.makeText(context, "NullPointerException", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFoundStatus() {
        //若未找到最愛的書，則說明沒有找到
        if (books == null || books.isEmpty()) {
            txtNotFound.setText("沒有你最愛的商品");
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
