package com.xy.psn.member;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.MainActivity;
import com.xy.psn.R;
import com.xy.psn.adapter.MailLIstAdapter;
import com.xy.psn.async_helper.GetBitmap;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.data.Chat;
import com.xy.psn.data.MyHelper;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import static com.xy.psn.data.MyHelper.fromNotification;
import static com.xy.psn.data.MyHelper.gender;
import static com.xy.psn.data.MyHelper.loginUserId;
import static com.xy.psn.data.MyHelper.getNotFoundImg;
import static com.xy.psn.data.MyHelper.modifyJSON;
import static com.xy.psn.data.MyHelper.myAvatar;
import static com.xy.psn.data.MyHelper.tmpToken;

public class MemberMailboxActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private boolean canShowMail = true;
    private ImageView imgNotFound;
    private TextView txtNotFound;
    private ArrayList<ImageObj> chats;
    private MyAsyncTask mailTask;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_mailbox);
        context = this;

        if (fromNotification) {
            SharedPreferences sp = getSharedPreferences(getString(R.string.sp_fileName), MODE_PRIVATE);
            loginUserId = sp.getString(getString(R.string.sp_myLoginUserId), "");
            myAvatar = sp.getString(getString(R.string.sp_myAvatar), "");
            MyHelper.myName = sp.getString(getString(R.string.sp_myName), "");
            gender = sp.getInt(getString(R.string.sp_myGender), -1);
            //tmpToken = sp.getString("tmpToken", "");
            tmpToken = "token";
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("信箱");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(canShowMail) {
                    loadData();
                }
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.prgMailbox);
        progressBar.setVisibility(View.VISIBLE);
        txtNotFound = (TextView) findViewById(R.id.txtNotFound);
        imgNotFound = (ImageView) findViewById(R.id.imgNotFound);
        imgNotFound.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onDestroy() {
        if (fromNotification)
            startActivity(new Intent(context, MainActivity.class));
        mailTask.cancel(true);
        System.gc();
        super.onDestroy();
    }

    private void loadData() {
        if (canShowMail) {
            //Toast.makeText(context, "載入信箱", Toast.LENGTH_SHORT).show();
            canShowMail = false;
            swipeRefreshLayout.setEnabled(false);

            // 產生將顯示的資料
            chats = new ArrayList<>();

            // (1)宣告一個處理資料取回後, 處理回傳JSON格式資料的物件.
            mailTask = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
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
                            chats.add(new Chat(
                                    getString(R.string.avatar_link) + jsonArray.getJSONObject(i).getString("Avatar"),
                                    jsonArray.getJSONObject(i).getString("Name"),
                                    URLDecoder.decode(jsonArray.getJSONObject(i).getString("Message"), "UTF-8"),
                                    jsonArray.getJSONObject(i).getString("SendDate"),
                                    jsonArray.getJSONObject(i).getString("SendTime"),
                                    jsonArray.getJSONObject(i).getString("Product"),
                                    jsonArray.getJSONObject(i).getString("MemberId")
                            ));
                        }
                    }catch (StringIndexOutOfBoundsException sie) {
                        //Toast.makeText(context, "沒有訊息", Toast.LENGTH_SHORT).show();
                    }catch (UnsupportedEncodingException e) {

                    }catch (Exception e) {
                        Toast.makeText(context, "連線失敗! ", Toast.LENGTH_SHORT).show();
                    }

                    // 產生物件ArrayList資料後, 由圖片位址下載圖片, 完成後再顯示資料.
                    GetBitmap getBitmap = new GetBitmap(context, chats, new GetBitmap.TaskListener() {
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
            mailTask.execute(getString(R.string.mailbox_link, loginUserId));
        }
    }

    private void showData () {
        try {
            ListView listView = (ListView) findViewById(R.id.lstMails);
            final MailLIstAdapter adapter = new MailLIstAdapter(context, chats);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Chat chat = (Chat) adapter.getItem(i);
                    Intent it = new Intent(context, MemberChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("memberId", chat.getMember());
                    bundle.putString("memberName", chat.getName());
                    bundle.putString("memberAvatar", chat.getImgURL());
                    bundle.putString("productId", chat.getProduct());
                    it.putExtras(bundle);
                    startActivity(it);
                }
            });

            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setRefreshing(false);

            //若未找到信，則說明沒有找到
            if (chats.isEmpty()) {
                txtNotFound.setText("沒有找到訊息");
                imgNotFound.setImageResource(getNotFoundImg());
                txtNotFound.setVisibility(View.VISIBLE);
                imgNotFound.setVisibility(View.VISIBLE);
            }else {
                txtNotFound.setText("");
                imgNotFound.setVisibility(View.GONE);
            }
            chats = null;

            progressBar.setVisibility(View.GONE);
            canShowMail = true;
            //isProductHomeShown = true;
        }catch (NullPointerException npe) {
            Toast.makeText(context, "NullPointerException ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (fromNotification)
            startActivity(new Intent(context, MainActivity.class));
        super.onBackPressed();
    }
}
