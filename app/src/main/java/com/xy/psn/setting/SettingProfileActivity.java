package com.xy.psn.setting;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.psn.R;
import com.xy.psn.async_helper.GetBitmap;
import com.xy.psn.async_helper.ImageObj;
import com.xy.psn.async_helper.ImageUploadTask;
import com.xy.psn.async_helper.MyAsyncTask;
import com.xy.psn.data.ImageObject;
import com.xy.psn.data.Member;
import com.xy.psn.member.MemberProfileActivity;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.xy.psn.data.MyHelper.convertToMD5;
import static com.xy.psn.data.MyHelper.getSpnDepCode;
import static com.xy.psn.data.MyHelper.loginUserId;
import static com.xy.psn.data.MyHelper.modifyJSON;

public class SettingProfileActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private LinearLayout layInfo;
    private EditText edtName, edtEmail, edtOldPwd, edtNewPwd, edtNewPwd2;
    private Spinner spnDepartment;
    private String pwdMD5, name, department, email, avatar, newPwd;
    private ImageButton btnSelectAvatar;
    private ImageView btnUpdateInfo;
    private ArrayList<ImageObj> members;
    private MyAsyncTask[] tasks = new MyAsyncTask[2];
    private ProgressBar prgBar;
    private boolean isShown = false;

    private final int REQUEST_ALBUM = 2;
    private final int REQUEST_CROP = 3;
    private File mImageFile;
    private Thread trdCreateImage;
    private ImageObject profileImg = null;

    private ImageUploadTask imageTask = null;
    private boolean isImageChanged = false;
    private Dialog dialog;
    private Thread trdWaitPhoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);
        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView txtBarTitle = (TextView) toolbar.findViewById(R.id.txtToolbarTitle);
        txtBarTitle.setText("設定個人檔案");
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

        edtName = (EditText) findViewById(R.id.edtName);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtOldPwd = (EditText) findViewById(R.id.edtOldPwd);
        edtNewPwd = (EditText) findViewById(R.id.edtNewPwd);
        edtNewPwd2 = (EditText) findViewById(R.id.edtNewPwd2);

        spnDepartment = (Spinner) findViewById(R.id.spnDepartment);
        spnDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                department = getSpnDepCode(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnSelectAvatar = (ImageButton) findViewById(R.id.btnSelectAvatar);
        btnSelectAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //將寫入使用者對寫入的權限指定至permission
                int permission = ActivityCompat.checkSelfPermission(SettingProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                //檢查是否開啟寫入權限
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // 無權限，向使用者請求
                    //執行完後執行onRequestPermissionsResult
                    ActivityCompat.requestPermissions(
                            SettingProfileActivity.this,
                            new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            0 //requestCode
                    );
                }else{
                    //已有權限，準備選圖
                    pickImageDialog();
                }
            }
        });

        btnUpdateInfo = (ImageView) toolbar.findViewById(R.id.btnUpdateInfo);
        btnUpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShown)
                    updateProfile();
            }
        });
        prgBar = (ProgressBar) findViewById(R.id.prgBar);

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dlg_uploading);
        dialog.setCancelable(false);
        LinearLayout layUpload = (LinearLayout) dialog.findViewById(R.id.layUpload);
        layUpload.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
                msgbox.setTitle("更新個人檔案")
                        .setMessage("確定取消更新嗎？")
                        .setNegativeButton("否", null)
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    trdWaitPhoto = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hdrWaitPhoto.sendMessage(hdrWaitPhoto.obtainMessage());
                                        }
                                    });
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
    }

    @Override
    public void onResume(){
        super.onResume();
        if (!isShown)
            loadProfileData();
    }

    @Override
    public void onDestroy() {
        try {
            tasks[0].cancel(true);
        }catch (NullPointerException e) {}
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //詢問是否開啟權限
        switch(requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得權限，進行檔案存取
                    pickImageDialog();
                }else {
                    //使用者拒絕權限，停用檔案存取功能，並顯示訊息
                    Toast.makeText(context, "權限不足，無法選擇圖片", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void pickImageDialog() {
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(albumIntent, REQUEST_ALBUM);
    }

    @Override
    //挑選完圖片後執行此方法，將圖片放上ImageButton
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        Uri selectedImageUri = data.getData();
        switch (requestCode) {
            case REQUEST_ALBUM:
                if (!createImageFile())
                    return;
                if (selectedImageUri != null)
                    cropImage(selectedImageUri);
                break;
            case REQUEST_CROP:
                Bitmap bitmap = null;
                final ImageObject tmpImage = new ImageObject(bitmap);
                trdCreateImage = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean bitmapIsNull = true;
                        do {
                            bitmapIsNull = mImageFileToBitmap(tmpImage);
                        } while (bitmapIsNull);
                        profileImg = tmpImage;
                        hdrCreateImage.sendMessage(hdrCreateImage.obtainMessage());
                    }
                });
                trdCreateImage.start();
                break;
        }
    }

    private void loadProfileData() {
        btnUpdateInfo.setVisibility(View.GONE);
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
                                jsonArray.getJSONObject(i).getString("Email")
                        ));
                        avatar = jsonArray.getJSONObject(i).getString("Avatar");
                        pwdMD5 = jsonArray.getJSONObject(i).getString("Password");
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
                        showProfileData();
                    }
                });

                // 執行圖片下載
                getBitmap.execute();
            }
        });

        // (2)向主機網址發出取回資料請求
        tasks[0].execute(getString(R.string.profile_info_link, loginUserId));
    }

    private void showProfileData() {
        try {
            Member member = (Member) members.get(0);
            edtName.setText(member.getName());
            edtEmail.setText(member.getEmail());

            Bitmap bitmap = member.getImg();
            if (bitmap != null)
                btnSelectAvatar.setImageBitmap(member.getImg());

            String department = member.getDepartment();
            switch (department) {
                case "五專會計統計科":
                    spnDepartment.setSelection(0);
                    break;
                case "五專財務金融科":
                    spnDepartment.setSelection(1);
                    break;
                case "五專財政稅務科":
                    spnDepartment.setSelection(2);
                    break;
                case "五專國際貿易科":
                    spnDepartment.setSelection(3);
                    break;
                case "五專企業管理科":
                    spnDepartment.setSelection(4);
                    break;
                case "五專資訊管理科":
                    spnDepartment.setSelection(5);
                    break;
                case "五專應用外語科":
                    spnDepartment.setSelection(6);
                    break;
                case "四技會計資訊系":
                    spnDepartment.setSelection(7);
                    break;
                case "四技財務金融系":
                    spnDepartment.setSelection(8);
                    break;
                case "四技財政稅務系":
                    spnDepartment.setSelection(9);
                    break;
                case "四技國際商務系":
                    spnDepartment.setSelection(10);
                    break;
                case "四技企業管理系":
                    spnDepartment.setSelection(11);
                    break;
                case "四技資訊管理系":
                    spnDepartment.setSelection(12);
                    break;
                case "四技應用外語系":
                    spnDepartment.setSelection(13);
                    break;
                case "四技商業設計管理系":
                    spnDepartment.setSelection(14);
                    break;
                case "四技商品創意經營系":
                    spnDepartment.setSelection(15);
                    break;
                case "四技數位多媒體設計系":
                    spnDepartment.setSelection(16);
                    break;
                case "二技會計資訊系":
                    spnDepartment.setSelection(17);
                    break;
                case "二技財務金融系":
                    spnDepartment.setSelection(18);
                    break;
                case "二技財政稅務系":
                    spnDepartment.setSelection(19);
                    break;
                case "二技國際商務系":
                    spnDepartment.setSelection(20);
                    break;
                case "二技企業管理系":
                    spnDepartment.setSelection(21);
                    break;
                case "二技資訊管理系":
                    spnDepartment.setSelection(22);
                    break;
                case "二技應用外語系":
                    spnDepartment.setSelection(23);
                    break;
            }
            members = null;
            prgBar.setVisibility(View.GONE);
            layInfo.setVisibility(View.VISIBLE);
            btnUpdateInfo.setVisibility(View.VISIBLE);
            isShown = true;

        }catch (NullPointerException npe) {
            Toast.makeText(context, "NullPointerException @ ", Toast.LENGTH_SHORT).show();
        }catch (IndexOutOfBoundsException ioe) {
            Toast.makeText(context, "IndexOutOfBoundsException @ ", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfile() {
        if (isInfoValid(edtName.getText().toString(), edtEmail.getText().toString(), edtOldPwd.getText().toString(), edtNewPwd.getText().toString(), edtNewPwd2.getText().toString())) {
            // (1)宣告一個處理資料取回後, 處理回傳JSON格式資料的物件.
            tasks[1] = new MyAsyncTask(context, new MyAsyncTask.TaskListener() {
                @Override
                public void onFinished(String result) {
                    try{
                        if (result == null) {
                            Toast.makeText(context, "操作失敗", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        JSONArray jsonArray = new JSONArray(modifyJSON(result));
                        if (!jsonArray.getJSONObject(0).getString("MemberId").equals("Fail")) {
                            Toast.makeText(context, "編輯成功", Toast.LENGTH_SHORT).show();
                            Intent it = new Intent(context, MemberProfileActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("memberId", jsonArray.getJSONObject(0).getString("MemberId"));
                            it.putExtras(bundle);
                            startActivity(it);
                            finish();
                        }else
                            Toast.makeText(context, "編輯失敗", Toast.LENGTH_SHORT).show();

                    }catch (IllegalStateException ise) {
                        Toast.makeText(context, "IllegalStateException @ ", Toast.LENGTH_SHORT).show();
                    }catch (Exception e) {
                        Toast.makeText(context, "連線失敗! ", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            });

            // (2)向主機網址發出資料請求
            dialog.show();
            if (!isImageChanged) {
                try {
                    tasks[1].execute(getString(R.string.update_profile_info_link,
                            loginUserId,
                            URLEncoder.encode(name, "UTF-8"),
                            department,
                            URLEncoder.encode(avatar, "UTF-8"),
                            URLEncoder.encode(email, "UTF-8"),
                            !newPwd.equals("") ? URLEncoder.encode(convertToMD5(newPwd), "UTF-8") : URLEncoder.encode(pwdMD5, "UTF-8")
                    ));
                }catch (UnsupportedEncodingException uee) {
                    Toast.makeText(context, "UnsupportedEncodingException @", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(context, "正在更新大頭貼，可能會多花點時間", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    public void run() {
                        imageTask = new ImageUploadTask(context, getString(R.string.upload_avatar_link, loginUserId));
                        imageTask.uploadFile(profileImg.getBitmap());
                    }
                }).start();

                trdWaitPhoto = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        hdrWaitPhoto.sendMessage(hdrWaitPhoto.obtainMessage());
                    }
                });
                trdWaitPhoto.start();
            }
        }
    }

    private boolean isInfoValid(String name, String email, String oldPwd, String newPwd, String newPwd2) {
        String errMsg = "";
        if (name.length() < 1)
            errMsg += "姓名未輸入\n";

        if (!email.contains("@") || email.indexOf("@") == 0 || email.indexOf("@") == email.length() - 1)
            errMsg += "信箱格式錯誤\n";

        if (!oldPwd.equals("") || !newPwd.equals("") || !newPwd2.equals("")) {
            if (!pwdMD5.equals(convertToMD5(oldPwd)))
                errMsg += "原密碼錯誤\n";
            else {
                if (newPwd.length() < 6 || newPwd.length() > 15)
                    errMsg += "新密碼長度錯誤\n";
                else {
                    if (!newPwd.equals(newPwd2))
                        errMsg += "確認密碼錯誤\n";
                }
            }
        }

        if (!errMsg.equals("")){
            errMsg = errMsg.substring(0, errMsg.length() - 1);
            AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
            msgbox.setTitle("編輯個人檔案")
                    .setPositiveButton("確定", null)
                    .setMessage(errMsg)
                    .show();
            return false;
        }else {
            this.name = name;
            this.email = email;
            this.newPwd = newPwd;
            return true;
        }
    }

    private Handler hdrCreateImage = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            btnSelectAvatar.setImageBitmap(profileImg.getBitmap());
            isImageChanged = true;
        }
    };

    private Handler hdrWaitPhoto = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String photoName = imageTask.getPhotoName();
            if (photoName != null) {
                try {
                    tasks[1].execute(getString(R.string.update_profile_info_link,
                            loginUserId,
                            URLEncoder.encode(name, "UTF-8"),
                            URLEncoder.encode(department, "UTF-8"),
                            URLEncoder.encode(photoName, "UTF-8"),
                            URLEncoder.encode(email, "UTF-8"),
                            !newPwd.equals("") ? URLEncoder.encode(convertToMD5(newPwd), "UTF-8") : URLEncoder.encode(pwdMD5, "UTF-8")
                    ));
                }catch (UnsupportedEncodingException e) {
                }catch (IllegalStateException e) {}
            }else {
                trdWaitPhoto = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(200);
                        }catch (InterruptedException e) {}
                        hdrWaitPhoto.sendMessage(hdrWaitPhoto.obtainMessage());
                    }
                });
                trdWaitPhoto.start();
            }
        }
    };

    private boolean createImageFile() {
        mImageFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        try {
            mImageFile.createNewFile();
            return mImageFile.exists();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "出錯啦", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void cropImage(Uri uri){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 5);
        intent.putExtra("aspectY", 6);
        intent.putExtra("outputX", 350);
        intent.putExtra("outputY", 420);
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("return-date", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageFile));
        startActivityForResult(intent, REQUEST_CROP);
    }

    private boolean mImageFileToBitmap(ImageObject image) {
        Bitmap mImageFileBitmap = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
        if (mImageFileBitmap != null) {
            image.setBitmap(mImageFileBitmap);
            if (mImageFile.delete())
                Log.w("Delete File", "deleted");
            else
                mImageFile.deleteOnExit();
            return false;
        }else
            return true;
    }
}
