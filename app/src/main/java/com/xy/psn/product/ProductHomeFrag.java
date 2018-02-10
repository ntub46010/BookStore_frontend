package com.xy.psn.product;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.MainActivity;
import com.xy.psn.R;
import com.xy.psn.adapter.ProductDisplayAdapter;
import com.xy.psn.async_helper.GetBitmap;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.data.Book;

import static com.xy.psn.data.MyHelper.Comma;
import static com.xy.psn.data.MyHelper.canShowProduct;
import static com.xy.psn.data.MyHelper.fromClickDep;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.xy.psn.MainActivity.context;
import static com.xy.psn.MainActivity.board;
import static com.xy.psn.data.MyHelper.getNotFoundImg;
import static com.xy.psn.data.MyHelper.isProductDisplayAlive;
import static com.xy.psn.data.MyHelper.modifyJSON;
import static com.xy.psn.data.MyHelper.setBoardTitle;

public class ProductHomeFrag extends Fragment {
    public static ProgressBar prgBar;
    public static RecyclerView recyclerView;
    private ImageView imgNotFound;
    private TextView txtNotFound;
    public static FloatingActionButton fabTop, fabPost;
    private ArrayList<ImageObj> books;
    public static MyAsyncTask productTask;
    public static GetBitmap getBitmap;
    public static boolean isProductHomeShown = false;
    public static ProductDisplayAdapter myAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_product_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setBoardTitle();
        prgBar = (ProgressBar) getView().findViewById(R.id.prgBar);

        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myAdapter.setCanCheckLoop(false);
                myAdapter.initCheckThread(false);

                canShowProduct = true;
                fromClickDep = true;
                loadData("");
            }
        });

        txtNotFound = (TextView) getView().findViewById(R.id.txtNotFound);
        imgNotFound = (ImageView) getView().findViewById(R.id.imgNotFound);
        txtNotFound.setVisibility(View.GONE);
        imgNotFound.setVisibility(View.GONE);

        setFab();
        fabTop.setVisibility(View.VISIBLE);
        fabPost.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        isProductDisplayAlive = true;
        //不寫在onResume是因為切換Fragment後必執行onResume，則循環旗標又會被設為true，無法及時中止舊的執行緒
        try {
            myAdapter.setCanCheckLoop(true);
            myAdapter.initCheckThread(true);
        }catch (NullPointerException e) {
            //第一次開啟，adapter尚未準備好
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isProductHomeShown)
            loadData("");
    }

    @Override
    public void onPause() {
        isProductDisplayAlive = false;
        try {
            productTask.cancel(true); //太快去點選其他畫面會有NPE
        }catch (NullPointerException e) {}
        canShowProduct = true;
        prgBar.setVisibility(View.GONE);

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
        canShowProduct = true;
        fromClickDep = true;
        isProductHomeShown = false;
        super.onDestroy();
    }

    private void loadData(String keyword) {
        if (canShowProduct && fromClickDep) {
            canShowProduct = false;
            fromClickDep = false;

            swipeRefreshLayout.setEnabled(false);
            prgBar.setVisibility(View.VISIBLE);
            //recyclerView.setVisibility(View.INVISIBLE); //不能寫在這，會NPE

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
                    getBitmap = new GetBitmap(MainActivity.context, books, new GetBitmap.TaskListener() {
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
                Toast.makeText(context, "UnsupportedEncodingException @", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showData () {
        try {
            // 產生 RecyclerView
            recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);

            // 設定 RecycleView的版型
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);

            // 產生一個 MyAdapter物件, 連結將加入的資料
            myAdapter = new ProductDisplayAdapter(MainActivity.context, books);
            myAdapter.setBackgroundColor(getResources(), R.color.card_product);

            // 將結合資料後的 stockAdapter 加入 RecyclerView物件中
            recyclerView.setAdapter(myAdapter);
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setRefreshing(false);

            //若未找到最愛的書，則說明沒有找到
            showFoundStatus();
            books = null;

            prgBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            canShowProduct = true;
            isProductHomeShown = true;
        }catch (NullPointerException npe) {
            Toast.makeText(context, "NullPointerException", Toast.LENGTH_SHORT).show();
        }
    }

    private void setFab () {
        fabTop = (FloatingActionButton) getView().findViewById(R.id.fab_top);
        fabTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //myAdapter.showImgStatus();
                    recyclerView.scrollToPosition(0);
                }catch (Exception e) {
                    e.printStackTrace();Toast.makeText(context, "沒有商品，不能往上", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fabPost = (FloatingActionButton) getView().findViewById(R.id.fab_add);
        fabPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, ProductPostActivity.class));
            }
        });
    }

    private void showFoundStatus() {
        //若未找到書，則說明沒有找到
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
