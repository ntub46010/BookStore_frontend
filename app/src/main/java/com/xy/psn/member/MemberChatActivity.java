package com.xy.psn.member;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.R;
import com.xy.psn.adapter.ChatAdapter;
import com.xy.psn.adapter.ImageGroupAdapter;
import com.xy.psn.adapter.ProductSpinnerAdapter;
import com.xy.psn.async_helper.GetBitmap;
import com.xy.psn.async_helper.GetBitmapBatch;
import com.xy.psn.async_helper.GetBitmapPair;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.boardcast_helper.managers.RequestManager;
import com.xy.psn.data.Book;
import com.xy.psn.data.Chat;
import com.xy.psn.data.MyHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.xy.psn.data.MyHelper.Comma;
import static com.xy.psn.data.MyHelper.getNotFoundImg;
import static com.xy.psn.data.MyHelper.haveNewMsg;
import static com.xy.psn.data.MyHelper.isChatroomAlive;
import static com.xy.psn.data.MyHelper.isProfileAlive;
import static com.xy.psn.data.MyHelper.loginUserId;
import static com.xy.psn.data.MyHelper.canShowChat;
import static com.xy.psn.data.MyHelper.modifyJSON;
import static com.xy.psn.data.MyHelper.tmpToken;
import static com.xy.psn.product.ProductDetailActivity.pictures;

public class MemberChatActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private Bundle bundle;
    private RelativeLayout layChatroom;
    private FrameLayout layGoodsDetail;
    private LinearLayout layInnerDetail;
    private Spinner spnProduct;
    private ImageView btnProductInfo, btnMemberProfile;
    private ImageView imgNotFound;
    private TextView txtNotFound;
    private boolean isFirstIn = true, isReturnToBuy = false;
    private RecyclerView recyChat;
    private Button btnSubmit;
    private EditText edtMessage;
    private ArrayList<ImageObj> avatars, products, chats, details;
    private String memberName, memberId, productId, memberToken, memberAvatarURL, talkerId;
    private Thread trdWaitToken, trdWaitMsg;
    private GetBitmapPair avatarTask;
    private MyAsyncTask[] tasks = new MyAsyncTask[4];
    private ProgressBar prgList, prgChat, prgDetail;
    private ProductSpinnerAdapter productAdapter;
    private boolean isChatroomShown = false;

    public static int selectedIndex = 0;
    private TextView txtId, txtTitle, txtDep, txtStatus, txtNote, txtPrice, txtPS, txtSeller, txtPost, txtEdit;
    private String id = "", title, dep, status, note, price, ps, seller, postDate, editDate;

    //使用表情符號，應注意
    //聊天室下載訊息的decode、傳送訊息的encode
    //信箱的decode
    //推播的decode
    //資料庫若有中文訊息應全部清除
    //訊息原文的百分比一律改成全形

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_chat);
        context = this;

        bundle = getIntent().getExtras();
        memberName = bundle.getString("memberName");
        memberId = bundle.getString("memberId");
        productId = bundle.getString("productId");
        memberAvatarURL = bundle.getString("memberAvatar");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView txtBarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        txtBarTitle.setText(memberName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        edtMessage = (EditText) findViewById(R.id.edtMsg);

        spnProduct = (Spinner) findViewById(R.id.spnProduct);
        spnProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isFirstIn) {
                    Book book = (Book) productAdapter.getItem(i);
                    productId = book.getId();
                    id = "";
                    loadChatData(true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = edtMessage.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(msg);
                    RequestManager.getInstance().boardcastMsg(MyHelper.myName, msg, memberToken, MyHelper.myAvatar); //發送推播
                }
            }
        });

        btnProductInfo = (ImageView) findViewById(R.id.btnGoodsInfo);
        btnProductInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layGoodsDetail.setVisibility(View.VISIBLE);
                edtMessage.setEnabled(false);
                if (id.equals(""))
                    loadDetailData(true);
                else
                    loadDetailData(false);
            }
        });
        btnProductInfo.setVisibility(View.GONE);

        btnMemberProfile = (ImageView) toolbar.findViewById(R.id.btnMemberProfile);
        btnMemberProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isProfileAlive) {
                    Intent it = new Intent(context, MemberProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("talkerId", talkerId); //第一個說話的必為買家ID
                    bundle.putString("memberId", memberId);
                    it.putExtras(bundle);
                    startActivity(it);
                }else
                    Toast.makeText(context, "你已開啟賣家個人檔案，請返回查看", Toast.LENGTH_SHORT).show();
            }
        });
        btnMemberProfile.setVisibility(View.GONE);

        layChatroom = (RelativeLayout) findViewById(R.id.layChatroom);
        layChatroom.setEnabled(true);
        recyChat = (RecyclerView) findViewById(R.id.recy_chats);

        layGoodsDetail = (FrameLayout) findViewById(R.id.layGoodsDetail);
        layGoodsDetail.setVisibility(View.GONE);
        layInnerDetail = (LinearLayout) findViewById(R.id.layDetail);
        layInnerDetail.setVisibility(View.INVISIBLE);

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

        prgList = (ProgressBar) findViewById(R.id.prgBar);
        prgChat = (ProgressBar) findViewById(R.id.prgChat);
        prgDetail = (ProgressBar) findViewById(R.id.prgDetail);
        recyChat.setVisibility(View.INVISIBLE);

        txtNotFound = (TextView) findViewById(R.id.txtNotFound);
        imgNotFound = (ImageView) findViewById(R.id.imgNotFound);
        txtNotFound.setVisibility(View.GONE);
        imgNotFound.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        isChatroomAlive = true;
        if (!isChatroomShown) {
            loadProductData();
            loadAvatar();
        }
    }

    @Override
    public void onPause() {
        isChatroomAlive = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            for (MyAsyncTask task : tasks) {
                try {
                    task.cancel(true);
                }catch (NullPointerException e) {}
            }
            avatarTask.cancel(true);
        }catch (NullPointerException e) {}
        //--
        initMsgThread(false);
        MyHelper.bmpProductDetail = null;
        pictures = null;
        tmpToken = "token";
        canShowChat = true;
        haveNewMsg = false;
        System.gc();
        super.onDestroy();
    }

    private void loadAvatar() {
        avatars = new ArrayList<>();
        final ImageObj imageObj = new ImageObj();
        imageObj.setImgURL(MyHelper.myAvatar);
        imageObj.setImgURL2(memberAvatarURL);
        avatars.add(imageObj);
        avatarTask = new GetBitmapPair(context, avatars, new GetBitmapPair.TaskListener() {
            @Override
            public void onFinished() {
                loadChatData(true);
            }
        });
        avatarTask.execute();
    }

    private void loadProductData () {
        products = new ArrayList<>();
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
                        products.add(new Book(
                                jsonArray.getJSONObject(i).getString("Id"),
                                getString(R.string.image_link) + jsonArray.getJSONObject(i).getString("Photo"),
                                jsonArray.getJSONObject(i).getString("Title"),
                                Comma(jsonArray.getJSONObject(i).getString("Price"))));
                    }
                    boolean isSameProduct = false; //檢查是否從商品詳情進入已經談過商品的聊天室
                    for (int i=0; i<products.size(); i++) {
                        Book book = (Book) products.get(i);
                        if (book.getId().equals(productId)) {
                            isSameProduct = true;
                            break;
                        }
                    }
                    if (!isSameProduct) {
                        products.add((Book)bundle.getSerializable("product"));
                        isReturnToBuy = true;
                    }

                }catch (StringIndexOutOfBoundsException sie) {
                    //Toast.makeText(context, "與此賣家從未對話", Toast.LENGTH_SHORT).show();
                    products.add((Book)bundle.getSerializable("product"));
                }catch (Exception e) {
                    Toast.makeText(context, "連線失敗! " + "chat_product", Toast.LENGTH_SHORT).show();
                }

                // 產生物件ArrayList資料後, 由圖片位址下載圖片, 完成後再顯示資料.
                GetBitmap getBitmap = new GetBitmap(context, products, new GetBitmap.TaskListener() {
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
        tasks[0].execute(getString(R.string.chat_product_link, loginUserId, memberId));
    }

    public void loadChatData (boolean showPrgBar) {
        if (!canShowChat)
            return;

        //Toast.makeText(context, "載入對話", Toast.LENGTH_SHORT).show();
        canShowChat = false;
        isChatroomShown = false;
        if (showPrgBar)
            prgChat.setVisibility(View.VISIBLE);

        chats = new ArrayList<>();
        tasks[1] = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
            @Override
            public void onFinished(String result) {
                try{
                    if (result == null) {
                        Toast.makeText(context, "連線逾時", Toast.LENGTH_SHORT).show();
                        isChatroomAlive = true;
                        return;
                    }
                    // 將由主機取回的JSONArray內容生成Book物件, 再加入ArrayList物件中
                    Bitmap avatar = null;
                    JSONArray jsonArray = new JSONArray(modifyJSON(result));
                    for(int i=0; i<jsonArray.length(); i++){
                        Chat chat = new Chat(
                                jsonArray.getJSONObject(i).getString("Sender"),
                                avatar, //圖片在顯示時才會賦予，先以null代替
                                URLDecoder.decode(jsonArray.getJSONObject(i).getString("Message"), "UTF-8"),
                                //jsonArray.getJSONObject(i).getString("Message"),
                                jsonArray.getJSONObject(i).getString("SendDate") + "  ", //用來顯示的日期
                                jsonArray.getJSONObject(i).getString("SendDate"), //實際日期(不會被改)
                                jsonArray.getJSONObject(i).getString("SendTime")
                        );
                        chats.add(chat);
                    }

                    //調整外在顯示日期
                    for (int i=chats.size()-2; i>=0; i--) {
                        Chat c1 = (Chat) chats.get(i); //新訊息
                        Chat c0 = (Chat) chats.get(i+1); //舊訊息(上一則)
                        if (c1.getInnerDate().equals(c0.getInnerDate()))
                            c1.setDate("");
                    }
                }catch (StringIndexOutOfBoundsException sie) {
                    //Toast.makeText(context, "沒有訊息", Toast.LENGTH_SHORT).show();
                }catch (JSONException e) {
                    Toast.makeText(context, "連線失敗! " + "chat_conversation 2", Toast.LENGTH_SHORT).show();
                }catch (UnsupportedEncodingException e){}
                try {
                    RequestManager.getInstance().getToken(memberId); //到Firebase取得對方Token，並以執行緒不斷檢查是否取得，而後顯示聊天訊息
                    initChatThread(true); //隨後showChatData
                }catch (IllegalThreadStateException e){
                    Toast.makeText(context, "IllegalThreadStateException", Toast.LENGTH_SHORT).show();
                }
            }
        });
        tasks[1].execute(getString(R.string.chat_conversation_link, loginUserId, memberId, productId));
    }

    private void loadDetailData (final boolean isFirstSee) {
        if (isFirstSee) {
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.prgDetail);
            progressBar.setVisibility(View.VISIBLE);
            layInnerDetail.setVisibility(View.INVISIBLE);
        }

        details = new ArrayList<>();
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
                        details.add(new Book(
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
                                jsonArray.getJSONObject(i).getString("SellerAvatar"),
                                jsonArray.getJSONObject(i).getString("Department").substring(2),
                                jsonArray.getJSONObject(i).getString("PostDateTime"),
                                jsonArray.getJSONObject(i).getString("EditDateTime")));
                    }
                }catch (StringIndexOutOfBoundsException sie) {
                    //Toast.makeText(context, "商品已下架", Toast.LENGTH_SHORT).show();
                    showFoundStatus();
                    return;
                }catch (Exception e) {
                    Toast.makeText(context, "連線失敗! " + "chat_product", Toast.LENGTH_SHORT).show();
                }

                GetBitmapBatch getBitmap = new GetBitmapBatch(context, getResources(), details, new GetBitmapBatch.TaskListener() {
                    // 下載圖片完成後執行的方法
                    @Override
                    public void onFinished() {
                        Book product = (Book) details.get(0);
                        id = product.getId();
                        title = product.getTitle();
                        dep = product.getDep();
                        status = product.getStatus();
                        note = product.getNote();
                        price = Comma(product.getPrice());
                        ps = product.getPs();
                        postDate = product.getPostDate();
                        editDate = product.getEditDate();
                        seller = product.getSellerName() + " (" + product.getSeller() + ")";
                        showDetailData();
                    }
                });
                // 執行圖片下載
                getBitmap.execute();
            }
        });
        tasks[2].execute(getString(R.string.product_detail_link, productId, String.valueOf(1)));
    }

    private void showProductData () {
        try {
            prgList.setVisibility(View.GONE);

            productAdapter = new ProductSpinnerAdapter(context, R.layout.spn_chat_product, products);
            spnProduct.setAdapter(productAdapter);
            if (isReturnToBuy) spnProduct.setSelection(products.size() - 1);
            products = null;

        }catch (NullPointerException npe) {
            Toast.makeText(this, "NullPointerException @ showProductData", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChatData () {
        try {
            // 產生 RecyclerView
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recy_chats);
            recyclerView.setHasFixedSize(true);

            // 設定 RecycleView的版型
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);

            // 產生一個 MyAdapter物件, 連結將加入的資料
            ChatAdapter myAdapter = new ChatAdapter(chats, avatars);
            try {
                Chat chat = (Chat) chats.get(chats.size()-1);
                talkerId = chat.getSender(); //第一個說話的必為買家ID
            }catch (ArrayIndexOutOfBoundsException aie) {
                //Toast.makeText(context, "沒有談過這個商品", Toast.LENGTH_SHORT).show();
            }
            chats = null;
            //avatars不清除，之後傳送訊息就不必再下載了;

            // 將結合資料後的 stockAdapter 加入 RecyclerView物件中
            recyclerView.setAdapter(myAdapter);

            prgChat.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnProductInfo.setVisibility(View.VISIBLE);
            btnMemberProfile.setVisibility(View.VISIBLE);
            isFirstIn = false;
            canShowChat = true;
            isChatroomShown = true;

            haveNewMsg = false;
            initMsgThread(isChatroomAlive);;
            myAdapter.notifyDataSetChanged();
            //Toast.makeText(context, "對話顯示完成", Toast.LENGTH_SHORT).show();
        }catch (NullPointerException npe) {
            Toast.makeText(this, "NullPointerException @ showChatData", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDetailData() {
        prgDetail.setVisibility(View.GONE);
        layInnerDetail.setVisibility(View.VISIBLE);

        try {
            txtId.setText(id);
            txtTitle.setText(title);
            txtStatus.setText(status);
            txtNote.setText(note);
            txtPrice.setText("$ " + price);
            txtPS.setText(ps);

            txtPost.setText(postDate + "  刊登");
            txtEdit.setText(editDate + "  編輯");
            if (editDate.equals("") || editDate.equals("null"))
                txtEdit.setVisibility(View.GONE);

            txtSeller.setText(seller);
            txtSeller.setTextColor(Color.parseColor("#555555"));

            //顯示科系
            String bookDep = "";
            if (dep.contains("01")) bookDep += "會資、";
            if (dep.contains("02")) bookDep += "財金、";
            if (dep.contains("03")) bookDep += "財稅、";
            if (dep.contains("04")) bookDep += "國商、";
            if (dep.contains("05")) bookDep += "企管、";
            if (dep.contains("06")) bookDep += "資管、";
            if (dep.contains("07")) bookDep += "應外、";
            if (dep.contains("A")) bookDep += "商設、";
            if (dep.contains("B")) bookDep += "商創、";
            if (dep.contains("C")) bookDep += "數媒、";
            if (dep.contains("00")) bookDep += "通識、";
            txtDep.setText(bookDep.substring(0, bookDep.length() - 1));

            showImages((Book) details.get(0));
        }catch (IndexOutOfBoundsException ioe) {
            //Toast.makeText(context, "IndexOutOfBoundsException @ " + "showDetailData", Toast.LENGTH_SHORT).show();
        }
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
            Toast.makeText(context, "NullPointerException @ showImages", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage (final String msg) {
        tasks[3] = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
            @Override
            public void onFinished(String result) {
                try{
                    if (result == null) {
                        Toast.makeText(context, "傳送失敗", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    prgChat.setVisibility(View.VISIBLE);
                    loadChatData(true);
                    edtMessage.setText("");
                }catch (IllegalStateException ise) {
                    Toast.makeText(context, "IllegalStateException", Toast.LENGTH_SHORT).show();
                }
            }
        });
        try {
            //可以正常使用emoji表情，但資料庫文字會被編碼
            tasks[3].execute(getString(R.string.send_message_link,
                    loginUserId,
                    memberId,
                    productId,
                    URLEncoder.encode(msg.replace("%", "％").replace("&", "＆"), "UTF-8").replace("%", "%25")
            ));
        }catch (UnsupportedEncodingException uee) {
            Toast.makeText(context, "UnsupportedEncodingException", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFoundStatus() {
        //若未找到書，則說明沒有找到
        if (details == null || details.isEmpty()) {
            txtNotFound.setText("此商品已被下架");
            txtNotFound.setVisibility(View.VISIBLE);
            imgNotFound.setImageResource(getNotFoundImg());
            imgNotFound.setVisibility(View.VISIBLE);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.prgDetail);
            progressBar.setVisibility(View.GONE);
        }else {
            txtNotFound.setText("");
            txtNotFound.setVisibility(View.GONE);
            imgNotFound.setVisibility(View.GONE);
        }
    }

    private Handler hdrWaitToken = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!tmpToken.equals("token")) {
                memberToken = tmpToken;
                showChatData();
                initChatThread(false);
            }else {
                initChatThread(true);
            }
        }
    };

    private Handler hdrWaitMsg = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isChatroomAlive) {
                if (haveNewMsg) {
                    initMsgThread(false);
                    //tasks[1].cancel(true);
                    loadChatData(false);
                }else {
                    initMsgThread(true);
                }
            }
        }
    };

    private void initChatThread(boolean restart) {
        trdWaitToken = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                }catch (Exception e) {}
                hdrWaitToken.sendMessage(hdrWaitToken.obtainMessage());
            }
        });
        if (restart)
            trdWaitToken.start();
    }

    private void initMsgThread(boolean restart) {
        trdWaitMsg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                }catch (Exception e) {}
                hdrWaitMsg.sendMessage(hdrWaitMsg.obtainMessage());
            }
        });
        if (restart)
            trdWaitMsg.start();
    }

    @Override
    public void onBackPressed() {
        if (layGoodsDetail.getVisibility() == View.VISIBLE) {
            layGoodsDetail.setVisibility(View.GONE);
            edtMessage.setEnabled(true);
            try {
                tasks[2].cancel(true);
            }catch (NullPointerException e) {}
        }else
            super.onBackPressed();
    }
}
