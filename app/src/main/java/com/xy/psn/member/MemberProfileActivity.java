package com.xy.psn.member;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.R;
import com.xy.psn.adapter.StockListAdapter;
import com.xy.psn.async_helper.GetBitmap;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.data.Book;
import com.xy.psn.data.Member;
import com.xy.psn.data.MyHelper;
import com.xy.psn.product.ProductDetailActivity;

import org.json.JSONArray;

import java.util.ArrayList;

import static com.xy.psn.data.MyHelper.isProfileAlive;
import static com.xy.psn.data.MyHelper.loginUserId;
import static com.xy.psn.data.MyHelper.modifyJSON;

public class MemberProfileActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private String memberId, email, originValue, talkerId = "";
    private LinearLayout layInfo;
    private ImageView imgAvatar;
    private TextView txtName, txtDepartment, txtPositive, txtNegative;
    private LinearLayout layLike, layDislike;
    private ImageButton btnPositive, btnNegative;
    private GridView grdShelf;
    private ArrayList<ImageObj> members, books;
    private StockListAdapter myAdapter;
    private MyAsyncTask[] tasks = new MyAsyncTask[4];
    private ProgressBar prgBar;
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);
        context = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        Bundle bundle = getIntent().getExtras();
        memberId = bundle.getString("memberId");
        talkerId = bundle.getString("talkerId", "");
        if (talkerId.equals("")) {
            if (memberId.equals(loginUserId))
                toolbar.setTitle("我的個人檔案");
            else
                toolbar.setTitle("賣家的個人檔案");
        }else {
            if (talkerId.equals(loginUserId))
                toolbar.setTitle("賣家的個人檔案");
            else
                toolbar.setTitle("買家的個人檔案");
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        layInfo = (LinearLayout) findViewById(R.id.layInfo);
        layInfo.setVisibility(View.INVISIBLE);

        imgAvatar = (ImageView) findViewById(R.id.imgAvatar);
        txtName = (TextView) findViewById(R.id.txtName);
        txtDepartment = (TextView) findViewById(R.id.txtDepartment);
        layLike = (LinearLayout) findViewById(R.id.layPositive);
        layDislike = (LinearLayout) findViewById(R.id.layNegative);
        btnPositive = (ImageButton) findViewById(R.id.btnPositive);
        btnNegative = (ImageButton) findViewById(R.id.btnNegative);
        txtPositive = (TextView) findViewById(R.id.txtPositive);
        txtNegative = (TextView) findViewById(R.id.txtNegative);
        grdShelf = (GridView) findViewById(R.id.grdShelf);
        grdShelf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Book book = (Book) myAdapter.getItem(i);
                Intent it = new Intent(context, ProductDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("productId", book.getId());
                bundle.putString("productName", book.getTitle());
                it.putExtras(bundle);
                startActivity(it);
            }
        });
        prgBar = (ProgressBar) findViewById(R.id.prgBar);
        prgBar.setVisibility(View.VISIBLE);
        isProfileAlive = true;

        Button btnEmail = (Button) findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Intent intent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + email));
                    //intent.putExtra(Intent.EXTRA_SUBJECT, "【北商二手書交易】想購買...");
                    //intent.putExtra(Intent.EXTRA_TEXT, "信件內文");
                    startActivity(intent);
                }catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        MyHelper.isStockDisplayAlive = true;
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
        if (!isShown) {
            loadInfoData();
            //loadShelfData();
        }
    }

    @Override
    public void onPause() {
        MyHelper.isStockDisplayAlive = false;
        try { //注意：回到桌面也會執行這裡，而停止執行緒
            myAdapter.setCanCheckLoop(false);
            myAdapter.initCheckThread(false);
        }catch (NullPointerException e) {
            //第一次開啟，adapter尚未準備好
        }
        super.onPause();
    }

    @Override
    public void onDestroy () {
        for (MyAsyncTask task : tasks) {
            try {
                task.cancel(true);
            }catch (NullPointerException e) {}
        }
        isProfileAlive = false;
        System.gc();
        super.onDestroy();
    }

    private void loadInfoData () {
        members = new ArrayList<>();

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
                        members.add(new Member(
                                getString(R.string.avatar_link) + jsonArray.getJSONObject(i).getString("Avatar"),
                                jsonArray.getJSONObject(i).getString("Name"),
                                jsonArray.getJSONObject(i).getString("Department"),
                                jsonArray.getJSONObject(i).getString("Positive"),
                                jsonArray.getJSONObject(i).getString("Negative"),
                                jsonArray.getJSONObject(i).getString("Email")
                        ));
                    }
                }catch (StringIndexOutOfBoundsException sie) {
                    Toast.makeText(context, "沒有找到會員", Toast.LENGTH_SHORT).show();
                }catch (IllegalStateException ise) {
                    Toast.makeText(context, "IllegalStateException @ ", Toast.LENGTH_SHORT).show();
                }catch (Exception e) {
                    Toast.makeText(context, "連線失敗! ", Toast.LENGTH_SHORT).show();
                }

                // 產生物件ArrayList資料後, 由圖片位址下載圖片, 完成後再顯示資料.
                GetBitmap getBitmap = new GetBitmap(context, members, new GetBitmap.TaskListener() {
                    // 下載圖片完成後執行的方法
                    @Override
                    public void onFinished() {
                        showInfoData();
                        loadShelfData();
                    }
                });

                // 執行圖片下載
                getBitmap.execute();
            }
        });

        // (2)向主機網址發出取回資料請求
        tasks[0].execute(getString(R.string.profile_info_link, memberId));
        loadEvaluationStatus();
    }

    private void loadEvaluationStatus () {
        // (1)宣告一個處理資料取回後, 處理回傳JSON格式資料的物件.
        tasks[1] = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
            @Override
            public void onFinished(String result) {
                try{
                    if (result == null) {
                        Toast.makeText(context, "操作失敗", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONArray jsonArray = new JSONArray(modifyJSON(result));
                    txtPositive.setText(jsonArray.getJSONObject(0).getString("Positive"));
                    txtNegative.setText(jsonArray.getJSONObject(0).getString("Negative"));
                    if (jsonArray.getJSONObject(0).getString("MyEvaluation").equals("1")) {
                        originValue = "1";
                        txtPositive.setTextColor(Color.parseColor("#00B050"));
                        txtNegative.setTextColor(Color.parseColor("#555555"));
                    }else if (jsonArray.getJSONObject(0).getString("MyEvaluation").equals("-1")){
                        originValue = "-1";
                        txtNegative.setTextColor(Color.parseColor("#FF0000"));
                        txtPositive.setTextColor(Color.parseColor("#555555"));
                    }else if (jsonArray.getJSONObject(0).getString("MyEvaluation").equals("0")){
                        originValue = "0";
                        txtPositive.setTextColor(Color.parseColor("#555555"));
                        txtNegative.setTextColor(Color.parseColor("#555555"));
                    }

                }catch (IllegalStateException ise) {
                    Toast.makeText(context, "IllegalStateException @ ", Toast.LENGTH_SHORT).show();
                }catch (Exception e) {
                    Toast.makeText(context, "連線失敗! ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // (2)向主機網址發出資料請求
        tasks[1].execute(getString(R.string.check_evaluation_link, loginUserId, memberId));
    }

    private void loadShelfData () {
        books = new ArrayList<>();

        // (1)宣告一個處理資料取回後, 處理回傳JSON格式資料的物件.
        tasks[2] = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
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
                        books.add(new Book(
                                jsonArray.getJSONObject(i).getString("Id"),
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo"),
                                jsonArray.getJSONObject(i).getString("Title")
                        ));
                    }
                }catch (StringIndexOutOfBoundsException sie) {
                    //Toast.makeText(context, "沒有找到上架書籍", Toast.LENGTH_SHORT).show();
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
                        showShelfData();
                    }
                });

                // 執行圖片下載
                getBitmap.setPreLoadAmount(12);
                getBitmap.execute();
            }
        });

        // (2)向主機網址發出取回資料請求
        tasks[2].execute(getString(R.string.profile_shelf_link, memberId));
    }

    private void showInfoData () {
        try {
            Member member = (Member) members.get(0);
            txtName.setText(member.getName());
            txtDepartment.setText(member.getDepartment());
            txtPositive.setText(member.getPositive());
            txtNegative.setText(member.getNegative());
            email = member.getEmail();

            Bitmap bitmap = member.getImg();
            if (bitmap != null)
                imgAvatar.setImageBitmap(bitmap);

            if (!memberId.equals(loginUserId)) {
                layLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (originValue.equals("1"))
                            giveEvaluation("0");
                        else
                            giveEvaluation("1");
                    }
                });
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (originValue.equals("1"))
                            giveEvaluation("0");
                        else
                            giveEvaluation("1");
                    }
                });
                layDislike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (originValue.equals("-1"))
                            giveEvaluation("0");
                        else
                            giveEvaluation("-1");
                    }
                });
                btnNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (originValue.equals("-1"))
                            giveEvaluation("0");
                        else
                            giveEvaluation("-1");
                    }
                });
            }

            members = null;
        }catch (NullPointerException npe) {
            Toast.makeText(context, "NullPointerException @ ", Toast.LENGTH_SHORT).show();
        }catch (IndexOutOfBoundsException ioe) {
            Toast.makeText(context, "IndexOutOfBoundsException @ ", Toast.LENGTH_SHORT).show();
        }
    }

    private void showShelfData () {
        try {
            myAdapter = new StockListAdapter(context, books, R.layout.grd_member_shelf);
            grdShelf.setAdapter(myAdapter);
            prgBar.setVisibility(View.GONE);
            layInfo.setVisibility(View.VISIBLE);
            isShown = true;
            books = null;
        }catch (NullPointerException npe) {
            Toast.makeText(context, "NullPointerException @ showShelfData", Toast.LENGTH_SHORT).show();
        }catch (IndexOutOfBoundsException ioe) {
            Toast.makeText(context, "IndexOutOfBoundsException @ showShelfData", Toast.LENGTH_SHORT).show();
        }
    }

    private void giveEvaluation (String value) {
        // (1)宣告一個處理資料取回後, 處理回傳JSON格式資料的物件.
        tasks[3] = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
            @Override
            public void onFinished(String result) {
                try{
                    if (result == null) {
                        Toast.makeText(context, "操作失敗", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (result.contains("Success")) {
                        loadEvaluationStatus();
                    }else {
                        Toast.makeText(context, "評價失敗", Toast.LENGTH_SHORT).show();
                        loadEvaluationStatus();
                    }

                }catch (IllegalStateException ise) {
                    Toast.makeText(context, "IllegalStateException @ ", Toast.LENGTH_SHORT).show();
                }catch (Exception e) {
                    Toast.makeText(context, "連線失敗! ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // (2)向主機網址發出資料請求
        tasks[3].execute(getString(R.string.evaluate_member_link, loginUserId, memberId, value));
    }

}
