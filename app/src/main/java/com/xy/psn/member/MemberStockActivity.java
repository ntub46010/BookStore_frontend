package com.xy.psn.member;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.R;
import com.xy.psn.adapter.StockListAdapter;
import com.xy.psn.async_helper.GetBitmap;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.data.Book;
import com.xy.psn.data.MyHelper;
import com.xy.psn.product.ProductDetailActivity;

import org.json.JSONArray;

import java.util.ArrayList;

import static com.xy.psn.data.MyHelper.Comma;
import static com.xy.psn.data.MyHelper.isStockDisplayAlive;
import static com.xy.psn.data.MyHelper.loginUserId;
import static com.xy.psn.data.MyHelper.canShowShelf;
import static com.xy.psn.data.MyHelper.getNotFoundImg;
import static com.xy.psn.data.MyHelper.modifyJSON;

public class MemberStockActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private ListView lstProduct;
    private ImageView imgNotFound;
    private TextView txtNotFound;
    private ArrayList<ImageObj> products;
    public static StockListAdapter stockAdapter = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String bookId, bookTitle;
    private MyAsyncTask[] tasks = new MyAsyncTask[2];
    private ProgressBar prgBar;
    public static boolean isStockShown = false;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_stock);
        context = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("商品管理");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        prgBar = (ProgressBar) findViewById(R.id.prgBar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        txtNotFound = (TextView) findViewById(R.id.txtNotFound);
        imgNotFound = (ImageView) findViewById(R.id.imgNotFound);
        imgNotFound.setVisibility(View.GONE);

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(canShowShelf) {
                    loadStockData();
                }
            }
        });

        prepareDialog();
        lstProduct = (ListView) findViewById(R.id.lstProduct);
        lstProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Book book = (Book) stockAdapter.getItem(position);
                bookId = book.getId();
                bookTitle = book.getTitle();
                TextView textView = (TextView) dialog.findViewById(R.id.txtBookTitle);
                textView.setText(bookTitle);
                dialog.show();
            }
        });

        isStockDisplayAlive = true;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        MyHelper.isStockDisplayAlive = true;
        try {
            //stockAdapter.setCanCheckLoop(true);
            stockAdapter.initCheckThread(true);
        }catch (NullPointerException e) {
            //第一次開啟，adapter尚未準備好
        }
        if (!isStockShown)
            loadStockData();
    }

    @Override
    public void onPause() {
        MyHelper.isStockDisplayAlive = false;

        try { //注意：回到桌面也會執行這裡，而停止執行緒
            stockAdapter.setCanCheckLoop(false);
            stockAdapter.initCheckThread(false);
        }catch (NullPointerException e) {
            //第一次開啟，adapter尚未準備好
        }
        super.onPause();
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
        isStockDisplayAlive = false;
        isStockShown = false;
        canShowShelf = true;
        System.gc();
        super.onDestroy();
    }

    private void loadStockData() {
        //Toast.makeText(context, "載入庫存", Toast.LENGTH_SHORT).show();
        if (canShowShelf) {
            canShowShelf = false;
            swipeRefreshLayout.setEnabled(false);
            prgBar.setVisibility(View.VISIBLE);
            lstProduct.setVisibility(View.GONE);

            products = new ArrayList<>();

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
                        for (int i=0; i<jsonArray.length(); i++) {
                            products.add(new Book(
                                    jsonArray.getJSONObject(i).getString("Id"),
                                    getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo"),
                                    jsonArray.getJSONObject(i).getString("Title"),
                                    Comma(jsonArray.getJSONObject(i).getString("Price"))));
                        }

                    }catch (StringIndexOutOfBoundsException sie) {
                        //Toast.makeText(context, "沒有上架商品", Toast.LENGTH_SHORT).show();
                    }catch (Exception e) {
                        Toast.makeText(context, "連線失敗! " + "chat_product", Toast.LENGTH_SHORT).show();
                    }

                    // 產生物件ArrayList資料後, 由圖片位址下載圖片, 完成後再顯示資料.
                    GetBitmap getBitmap = new GetBitmap(context, products, new GetBitmap.TaskListener() {
                        // 下載圖片完成後執行的方法
                        @Override
                        public void onFinished() {
                            showStockData();
                        }
                    });

                    // 執行圖片下載
                    getBitmap.setPreLoadAmount(12);
                    getBitmap.execute();
                }
            });

            // (2)向主機網址發出取回資料請求
            tasks[0].execute(getString(R.string.profile_shelf_link, loginUserId));
        }
    }

    private void showStockData() {
        try {
            prgBar.setVisibility(View.GONE);
            lstProduct.setVisibility(View.VISIBLE);
            stockAdapter = new StockListAdapter(context, products, R.layout.spn_chat_product);
            stockAdapter.setBackgroundColor(getResources(), R.color.lst_stock);
            lstProduct.setAdapter(stockAdapter);
            registerForContextMenu(lstProduct);

            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setRefreshing(false);

            if (products.isEmpty()) {
                txtNotFound.setText("沒有上架商品");
                txtNotFound.setVisibility(View.VISIBLE);
                imgNotFound.setImageResource(getNotFoundImg());
                imgNotFound.setVisibility(View.VISIBLE);
            }else {
                txtNotFound.setText("");
                imgNotFound.setVisibility(View.GONE);
            }
            products = null;
            canShowShelf = true;
            isStockShown = true;

        }catch (NullPointerException npe) {
            Toast.makeText(this, "NullPointerException @", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct (String productId) {
        // (1)宣告一個處理資料取回後, 處理回傳JSON格式資料的物件.
        tasks[1] = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
            @Override
            public void onFinished(String result) {
                try{
                    if (result == null) {
                        Toast.makeText(context, "已被交談過，還不能刪", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (result.contains("Success")) {
                        //下架成功
                        Toast.makeText(context, "商品已下架", Toast.LENGTH_SHORT).show();
                        loadStockData();
                    }else
                        Toast.makeText(context, "下架失敗", Toast.LENGTH_SHORT).show();
                }catch (StringIndexOutOfBoundsException sie) {
                    Toast.makeText(context, "沒有找到該書籍", Toast.LENGTH_SHORT).show();
                }catch (IllegalStateException ise) {
                    Toast.makeText(context, "IllegalStateException @ ", Toast.LENGTH_SHORT).show();
                }catch (Exception e) {
                    Toast.makeText(context, "連線失敗! ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // (2)向主機網址發出取回資料請求
        tasks[1].execute("http://140.131.115.73/delete_product?bookid=" + productId);
    }

    private void prepareDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dlg_stock_mgt);
        dialog.setCancelable(true);

        String[] textGroup = {"查看", "編輯", "下架"};
        int[] iconGroup = {
                R.drawable.icon_see,
                R.drawable.icon_edit,
                R.drawable.icon_delete
        };

        ListView listView = (ListView) dialog.findViewById(R.id.lstStockMgt);
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
                showActivity(itemName);
            }
        });
    }

    private void showActivity (String itemName) {
        Intent it = null;
        Bundle bundle = new Bundle();
        switch (itemName) {
            case "查看":
                it = new Intent(context, ProductDetailActivity.class);
                bundle.putString("productId", bookId);
                bundle.putString("productName", bookTitle);
                it.putExtras(bundle);
                startActivity(it);
                break;
            case "編輯":
                it = new Intent(context, ProductEditActivity.class);
                bundle.putString("productId", bookId);
                it.putExtras(bundle);
                startActivity(it);
                break;
            case "下架":
                AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
                msgbox.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProduct(bookId);
                    }
                });
                msgbox.setNegativeButton("取消", null);
                msgbox.setTitle("下架商品").setMessage("確定要下架「" + bookTitle + "」嗎？").show();
                break;
            case "返回":
                break;
        }
        dialog.dismiss();
    }
}
