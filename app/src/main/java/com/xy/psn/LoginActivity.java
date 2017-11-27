package com.xy.psn;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.boardcast_helper.managers.RequestManager;
import com.xy.psn.data.MyHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.xy.psn.MainActivity.lastPosition;
import static com.xy.psn.boardcast_helper.beans.custom.UserData.DATABASE_USERS;
//import static com.xy.psn.data.MyHelper.code;
import static com.xy.psn.data.MyHelper.convertToMD5;
import static com.xy.psn.data.MyHelper.gender;
import static com.xy.psn.data.MyHelper.getSpnDepCode;
import static com.xy.psn.data.MyHelper.loginUserId;
import static com.xy.psn.data.MyHelper.modifyJSON;
import static com.xy.psn.data.MyHelper.myAvatar;
import static com.xy.psn.data.MyHelper.myName;
import static com.xy.psn.data.MyHelper.tmpToken;

public class LoginActivity extends AppCompatActivity{
    private Context context;
    private EditText edtLogAcc, edtLogPwd;
    private Button btnLogin;
    private String userId = "", pwd = "";
    private LinearLayout layLoginField, layRegisterField;
    private TextView txtRegister;
    private Thread trdWaitDelete, trdWaitLogin, trdTimer;
    private ProgressBar prgLogin;
    private SharedPreferences sp;

    private EditText edtRegAcc, edtRegPwd, edtRegPwd2, edtRegName, edtRegEmail;
    private RadioGroup rgpRegGender;
    private Spinner spnRegDep;
    private Button btnRegister, btnCancel;
    private String department = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        sp = getSharedPreferences(getString(R.string.sp_fileName), MODE_PRIVATE);

        //登入元件
        layLoginField = (LinearLayout) findViewById(R.id.layLoginField);
        edtLogAcc = (EditText) findViewById(R.id.edtAccount);
        edtLogPwd = (EditText) findViewById(R.id.edtPassword);
        txtRegister = (TextView) findViewById(R.id.txtRegister);
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layLoginField.setVisibility(View.GONE);
                layRegisterField.setVisibility(View.VISIBLE);
            }
        });

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userId = edtLogAcc.getText().toString();
                pwd = edtLogPwd.getText().toString();
                if (!userId.equals("") && !pwd.equals(""))
                    registerDevice(userId, pwd);
            }
        });

        prgLogin = (ProgressBar) findViewById(R.id.prgLogin);
        prgLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, MyHelper.tmpToken, Toast.LENGTH_SHORT).show();
            }
        });

        //註冊元件
        layRegisterField = (LinearLayout) findViewById(R.id.layRegisterField);
        edtRegAcc = (EditText) findViewById(R.id.edtRegAccount);
        edtRegPwd = (EditText) findViewById(R.id.edtRegPassword);
        edtRegPwd2 = (EditText) findViewById(R.id.edtRegPasswordAgain);
        edtRegName = (EditText) findViewById(R.id.edtRegName);
        edtRegEmail = (EditText) findViewById(R.id.edtRegEmail);
        rgpRegGender = (RadioGroup) findViewById(R.id.rgpRegGender);
        spnRegDep = (Spinner) findViewById(R.id.spnRegDepartment);
        btnRegister = (Button) findViewById(R.id.btnRegConfirm);
        btnCancel = (Button) findViewById(R.id.btnRegCancel);

        spnRegDep.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                department = getSpnDepCode(i);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String acc = edtRegAcc.getText().toString();
                String pwd = edtRegPwd.getText().toString();
                String pwd2 = edtRegPwd2.getText().toString();
                String name = edtRegName.getText().toString();
                String email = edtRegEmail.getText().toString();

                String gender;
                switch (rgpRegGender.getCheckedRadioButtonId()) {
                    case R.id.rdoRegMale:
                        gender = "1";
                        break;
                    case R.id.rdoRegFemale:
                        gender = "0";
                        break;
                    default:
                        gender = "";
                }

                if (isInfoValid(acc, pwd, pwd2, name, email, gender)) {
                    registerMember(acc, pwd, name, email, gender);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layRegisterField.setVisibility(View.GONE);
                layLoginField.setVisibility(View.VISIBLE);
            }
        });
    }

    //登入成功後一律向Firebase重新註冊裝置Token，再取回
    private void registerDevice(final String userId, final String pwd) {
        //準備顯示物件並計時
        layLoginField.setVisibility(View.GONE);
        prgLogin.setVisibility(View.VISIBLE);
        initTrdTimer(true);

        //開始連線
        MyAsyncTask myAsyncTask = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
            @Override
            public void onFinished(String result) {
                try{
                    if (result == null) {
                        Toast.makeText(context, "沒有網路連線", Toast.LENGTH_SHORT).show();
                        layLoginField.setVisibility(View.VISIBLE);
                        prgLogin.setVisibility(View.GONE);
                        return;
                    }
                    //由主機取回的JSONArray內容
                    if (result.contains("[{")) {
                        //Toast.makeText(context, "帳號正確", Toast.LENGTH_SHORT).show();
                        //帳號正確
                        JSONArray jsonArray = new JSONArray(modifyJSON(result));
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        MyHelper.myAvatar = getString(R.string.avatar_link) + jsonObject.getString("Avatar");
                        MyHelper.myName = jsonObject.getString("Name");
                        if (jsonObject.getBoolean("Gender"))
                            MyHelper.gender = 1;
                        else
                            MyHelper.gender = 0;

                        deleteOriginalToken(userId); //刪除原本token，重新註冊裝置
                    }else {
                        Toast.makeText(context, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                        loginUserId = "x";
                        layLoginField.setVisibility(View.VISIBLE);
                        prgLogin.setVisibility(View.GONE);
                        initTrdTimer(false);
                    }

                }catch (IllegalStateException ise) {
                    Toast.makeText(context, "IllegalStateException @ ", Toast.LENGTH_SHORT).show();
                }catch (StringIndexOutOfBoundsException sie) {
                    Toast.makeText(context, "StringIndexOutOfBoundsException @ ", Toast.LENGTH_SHORT).show();
                }catch (Exception je) {
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                }
            }
        });
        myAsyncTask.execute(getString(R.string.login_link,
                userId,
                convertToMD5(pwd)
        ));
    }

    private void writeLoginRecord () {
        sp.edit()
                .putString(getString(R.string.sp_myLoginUserId), loginUserId)
                .putString(getString(R.string.sp_myAvatar), myAvatar)
                .putString(getString(R.string.sp_myName), myName)
                .putInt(getString(R.string.sp_myGender), gender)
                .apply();
        tmpToken = "token";
    }

    private Handler hdrWaitLogin = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!MyHelper.tmpToken.equals("token")) {
                MyHelper.tmpToken = "token";
                trdTimer = null;
                prgLogin.setVisibility(View.GONE);
                loginUserId = userId;
                lastPosition = 1;
                writeLoginRecord();

                initTrdWaitDelete(false);
                initTrdWaitLogin(false);
                initTrdTimer(false);
                startActivity(new Intent(context, MainActivity.class));
                finish();
            }else
                initTrdWaitLogin(true);
        }
    };

    private Handler hdrTimer = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            initTrdWaitDelete(false);
            initTrdWaitLogin(false);
            initTrdTimer(false);

            layLoginField.setVisibility(View.VISIBLE);
            prgLogin.setVisibility(View.GONE);
            if (loginUserId.equals(""))
                Toast.makeText(context, "登入連線逾時", Toast.LENGTH_SHORT).show();
        }
    };

    private  Handler hdrWaitDelete = new Handler() { //等待token值從預設文字被改為空
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (MyHelper.tmpToken.equals("")) {
                initTrdWaitDelete(false);
                RequestManager.getInstance().insertUserPushData(userId); //註冊Token (Firebase)
                RequestManager.getInstance().getToken(userId); //取得Token，存到MyHelper.tmpToken
                initTrdWaitLogin(true);
            }else {
                initTrdWaitDelete(true);
            }
        }
    };

    private void initTrdWaitLogin(boolean restart) {
        trdWaitLogin = new Thread(new Runnable() {
            @Override
            public void run() {
                hdrWaitLogin.sendMessage(hdrWaitLogin.obtainMessage());
            }
        });
        if (restart)
            trdWaitLogin.start();
    }

    private void initTrdWaitDelete(boolean restart) {
        trdWaitDelete = new Thread(new Runnable() {
            @Override
            public void run() {
                hdrWaitDelete.sendMessage(hdrWaitDelete.obtainMessage());
            }
        });
        if (restart)
            trdWaitDelete.start();
    };

    private void initTrdTimer(boolean restart) {
        trdTimer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                hdrTimer.sendMessage(hdrTimer.obtainMessage());
            }
        });
        if (restart)
            trdTimer.start();
    }

    private void deleteOriginalToken(String userId) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(DATABASE_USERS).child(userId).removeValue();
        RequestManager.getInstance().getToken(userId); //取得Token，存到MyHelper.tmpToken
        initTrdWaitDelete(true);
    }

    private boolean isInfoValid (String acc, String pwd, String pwd2, String name, String email, String gender) {
        //還不能阻擋在帳密輸入中文
        String errMsg = "";
        if (acc.length() < 8 || acc.length() > 10)
            errMsg += "帳號長度錯誤\n";

        if (pwd.length() < 6 || pwd.length() > 15)
            errMsg += "密碼長度錯誤\n";
        else if (!pwd.equals(pwd2))
            errMsg += "確認密碼錯誤\n";

        if (name.length() < 1)
            errMsg += "姓名未輸入\n";

        if (!email.contains("@") || email.indexOf("@") == 0 || email.indexOf("@") == email.length() - 1)
            errMsg += "信箱格式錯誤\n";

        if (gender.equals(""))
            errMsg += "性別未選擇\n";

        if (!errMsg.equals("")){
            errMsg = "註冊資料不正確：\n" + errMsg.substring(0, errMsg.length() - 1);
            AlertDialog.Builder msgbox = new AlertDialog.Builder(this);
            msgbox.setTitle("註冊帳號")
                    .setMessage(errMsg)
                    .setPositiveButton("確定", null)
                    .show();
            return false;
        }else {
            this.userId = acc;
            this.pwd = pwd;
            return true;
        }
    }

    private void registerMember(final String acc, final String pwd, String name, String email, String gender) {
        layRegisterField.setVisibility(View.GONE);
        prgLogin.setVisibility(View.VISIBLE);
        MyAsyncTask myAsyncTask = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
            @Override
            public void onFinished (String result) {
                try {
                    if (result == null) {
                        Toast.makeText(context, "無傳回資料", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONArray jsonArray = new JSONArray(modifyJSON(result));
                    if (jsonArray.getJSONObject(0).getString("Result").equals("Success")) {
                        Toast.makeText(context, "註冊成功", Toast.LENGTH_SHORT).show();
                        registerDevice(acc, pwd);
                    }else if (jsonArray.getJSONObject(0).getString("Result").equals("Already")) {
                        Toast.makeText(context, "該帳號已被註冊", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "註冊失敗", Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e) {
                    Toast.makeText(context, "JSONException", Toast.LENGTH_SHORT).show();
                }
                layRegisterField.setVisibility(View.VISIBLE);
            }
        });

        try {
            myAsyncTask.execute(getString(R.string.register_link,
                acc,
                convertToMD5(pwd),
                URLEncoder.encode(name, "UTF-8"),
                email,
                gender,
                department
            ));
        }catch (UnsupportedEncodingException e) {}

    }

    @Override
    public void onBackPressed() {
        if (layRegisterField.getVisibility() == View.VISIBLE) {
            layRegisterField.setVisibility(View.GONE);
            layLoginField.setVisibility(View.VISIBLE);
        }else
            super.onBackPressed();
    }

}
