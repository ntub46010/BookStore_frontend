package com.xy.psn.async_helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.xy.psn.setting.SettingProfileActivity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class ImageUploadTask{
    private Context context;
    private String uploadURL = null;
    private String photoName = null;
    private int serverResponseCode = 0;

    public ImageUploadTask (Context context, String uploadURL) {
        this.context = context;
        this.uploadURL = uploadURL;
    }

    public int uploadFile(String sourceFileUri) {
        if (sourceFileUri == null ) {
            return -1 ;
        }

        //final String fileName = sourceFileUri;

        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            return 0;
        }else {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(uploadURL);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", sourceFileUri);

                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + sourceFileUri + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necessary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                //將上傳後的圖片檔名回傳至data
                conn.connect();
                inputStream = conn.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                photoName = bufferedReader.readLine();

                inputStream.close();
                outputStream.close();
                fileInputStream.close();
                dos.flush();
                dos.close();

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                //String serverResponseMessage = conn.getResponseMessage();

                if(serverResponseCode == 200) {}

            }catch (MalformedURLException e) {
                Toast.makeText(context, "MalformedURLException", Toast.LENGTH_SHORT).show();
            }catch (Exception e) {
                Toast.makeText(context, "Exception", Toast.LENGTH_SHORT).show();
            }
        }

        return serverResponseCode;
    }

    public int uploadFile(Bitmap bitmap) {
        if (bitmap == null ) {
            return -1 ;
        }

        final String fileName = "jpg";

        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        if (bitmap == null) {
            return 0;
        }else {
            try {
                // open a URL connection to the Servlet
                ByteArrayOutputStream bitmapOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bitmapOutputStream);
                byte[] bitmapOutputByteArray = bitmapOutputStream.toByteArray();
                ByteArrayInputStream fileInputStream = new ByteArrayInputStream(bitmapOutputByteArray);
                URL url = new URL(uploadURL);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necessary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                //將上傳後的圖片檔名回傳至data
                conn.connect();
                InputStream inputStream = conn.getInputStream();

                BufferedReader bufferedReader=new BufferedReader
                        (new InputStreamReader(inputStream, "utf-8"));
                photoName = bufferedReader.readLine();

                inputStream.close();
                fileInputStream.close();
                dos.flush();
                dos.close();

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                //String serverResponseMessage = conn.getResponseMessage();

                if(serverResponseCode == 200) {}

            }catch (MalformedURLException e) {

            }catch (Exception e) {

            }
        }

        return serverResponseCode;
    }

    public String getPhotoName() {
        return photoName;
    }

}
