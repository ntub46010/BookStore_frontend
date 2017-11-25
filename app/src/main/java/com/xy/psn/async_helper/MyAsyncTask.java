package com.xy.psn.async_helper;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

// 非同步工作, 用來取回網站回傳的資料
public class MyAsyncTask extends AsyncTask<String, Integer, String> {
    //private ProgressDialog loading;
    private Context context;

    // 宣告一個接收回傳結果的程式必須實作的介面
    public interface TaskListener {
        void onFinished(String result);
    }

    private TaskListener taskListener;

    // 建構元, 傳入(1)context, (2)取回資料後執行的程式
    public MyAsyncTask(Context context, TaskListener taskListener) {
        this.context = context;
        this.taskListener = taskListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //loading = ProgressDialog.show(context, "下載中", "請稍等...", false, false);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    // 由主程式呼叫.execute()方法時啟動,
    // 由主程式傳入:(1)主機網址, (2)傳給主機的參數
    @Override
    protected String doInBackground(String... params) {
        String data = null;
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // 第1個參數是網址
            URL url = new URL(params[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            String args = "";
            //String args = "userid=" + URLEncoder.encode(params[1], "UTF-8");

            writer.write(args);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();
            inputStream = conn.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            data = bufferedReader.readLine();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        return data;
    }

    // 完成資料取回後, 由主程式的taskListener.onFinished()處理取回資料
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //loading.dismiss();

        taskListener.onFinished(result);
    }

    @Override
    protected void onCancelled(String result) {
        super.onCancelled(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}