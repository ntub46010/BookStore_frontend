package com.xy.psn.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.xy.psn.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xy.psn.MainActivity.board;
import static com.xy.psn.MainActivity.txtBarTitle;

public class MyHelper {
    public static String loginUserId = "";
    public static String myAvatar = "";
    public static String myName = "";
    public static int gender = -1;
    public static String tmpToken = "token";

    public static boolean fromNotification = true;
    public static boolean fromClickDep = true;

    public static boolean canShowProduct = true;
    public static boolean canShowFavorite = true;
    public static boolean canShowShelf = true;
    public static boolean canShowChat = true;

    public static boolean isProductDisplayAlive = false;
    public static boolean isProfileAlive = false;
    public static boolean isStockDisplayAlive = false;
    public static boolean isChatroomAlive = false;

    public static boolean haveNewMsg = false;

    public static Bitmap[] bmpProductDetail = new Bitmap[5];

    public static String modifyJSON(String json) {
        int head = json.indexOf("[{");
        int tail = json.indexOf("]");
        return json.substring(head, tail + 1);
    }

    public static int getNotFoundImg() {
        int imgId;
        switch ((int) (Math.random()* 5) + 1) {
            case 1:
                imgId = R.drawable.sad_gray_1;
                break;
            case 2:
                imgId = R.drawable.sad_gray_2;
                break;
            case 3:
                imgId = R.drawable.sad_gray_3;
                break;
            case 4:
                imgId = R.drawable.sad_gray_4;
                break;
            case 5:
                imgId = R.drawable.sad_gray_5;
                break;
            default:
                imgId = R.drawable.sad_gray_1;
                break;
        }
        return imgId;
    }

    public static void setBoardTitle() {
        switch (board) {
            case "-1":
                txtBarTitle.setText("全部");
                break;
            case "00":
                txtBarTitle.setText("通識");
                break;
            case "01":
                txtBarTitle.setText("會計資訊／會計統計");
                break;
            case "02":
                txtBarTitle.setText("財務金融");
                break;
            case "03":
                txtBarTitle.setText("財政稅務");
                break;
            case "04":
                txtBarTitle.setText("國際商務／國際貿易");
                break;
            case "05":
                txtBarTitle.setText("企業管理");
                break;
            case "06":
                txtBarTitle.setText("資訊管理");
                break;
            case "07":
                txtBarTitle.setText("應用外語");
                break;
            case "A":
                txtBarTitle.setText("商業設計管理");
                break;
            case "B":
                txtBarTitle.setText("商品創意經營");
                break;
            case "C":
                txtBarTitle.setText("數位多媒體設計");
                break;
        }
    }

    public static String getBoardNickname() {
        String title = "";
        switch (board) {
            case "-1":
                title = "全部";
                break;
            case "00":
                title = "通識類";
                break;
            case "01":
                title = "會資系";
                break;
            case "02":
                title = "財金系";
                break;
            case "03":
                title = "財稅系";
                break;
            case "04":
                title = "國商系";
                break;
            case "05":
                title = "企管系";
                break;
            case "06":
                title = "資管系";
                break;
            case "07":
                title = "應外系";
                break;
            case "A":
                title = "商設系";
                break;
            case "B":
                title = "商創系";
                break;
            case "C":
                title = "數媒系";
                break;
        }
        return title;
    }

    public static String getSpnDepCode (int position) {
        String depCode = "";
        switch (position) {
            case 0:
                depCode = "51";
                break;
            case 1:
                depCode = "52";
                break;
            case 2:
                depCode = "53";
                break;
            case 3:
                depCode = "54";
                break;
            case 4:
                depCode = "55";
                break;
            case 5:
                depCode = "56";
                break;
            case 6:
                depCode = "57";
                break;
            case 7:
                depCode = "41";
                break;
            case 8:
                depCode = "42";
                break;
            case 9:
                depCode = "43";
                break;
            case 10:
                depCode = "44";
                break;
            case 11:
                depCode = "45";
                break;
            case 12:
                depCode = "46";
                break;
            case 13:
                depCode = "47";
                break;
            case 14:
                depCode = "4A";
                break;
            case 15:
                depCode = "4B";
                break;
            case 16:
                depCode = "4C";
                break;
            case 17:
                depCode = "31";
                break;
            case 18:
                depCode = "32";
                break;
            case 19:
                depCode = "33";
                break;
            case 20:
                depCode = "34";
                break;
            case 21:
                depCode = "35";
                break;
            case 22:
                depCode = "36";
                break;
            case 23:
                depCode = "37";
                break;
            case 24:
                depCode = "3A";
                break;
            case 25:
                depCode = "3B";
                break;
            case 26:
                depCode = "3C";
                break;
        }
        return depCode;
    }

    public static String Comma(String num) {
        boolean negative = num.contains("-");
        if (negative) num = num.substring(1);
        String[] numPart = num.split("\\.");
        String result;

        if (numPart[0].length() < 4)
            result = numPart[0];
        else {
            result = Comma(numPart[0].substring(0, numPart[0].length()-3))
                    + "," + numPart[0].substring(numPart[0].length()-3);
        }

        if (numPart.length == 2) result += "." + numPart[1];
        if (negative) result = "-" + result;

        return result;
    }

    public static SimpleAdapter getSimpleAdapter(Context context, int layoutId, int layoutIconId, int layoutTitleId, int[] icon, String[] title) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i< icon.length ; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("icon", icon[i]);
            item.put("title", title[i]);
            list.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                context,
                list,
                layoutId,
                new String[] {"icon", "title"},
                new int[] {layoutIconId, layoutTitleId}
        );

        return  adapter;
    }

    public static int getPixel(Resources res, int dp) {
        int px = 0;
        try {
            px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    res.getDisplayMetrics()
            );

        }catch (IllegalStateException ise) {}

        return px;
    }

    public static String convertToMD5(String s) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());
            BigInteger i = new BigInteger(1, m.digest());
            return String.format("%1$032x", i).toUpperCase();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] BitmapCompress2Byte(Bitmap bm, int compressRatio) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, compressRatio, baos);
        byte[] opt = baos.toByteArray();
        try {
            baos.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return opt;
    }

    public static Bitmap Bytes2Bitmap(byte[] b) {
        if(b.length != 0){
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }else {
            return null;
        }
    }
}
