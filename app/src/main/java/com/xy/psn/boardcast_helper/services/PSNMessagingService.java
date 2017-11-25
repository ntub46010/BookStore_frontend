package com.xy.psn.boardcast_helper.services;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.xy.psn.boardcast_helper.PSNApplication;
import com.xy.psn.boardcast_helper.constants.KeyData;
import com.xy.psn.boardcast_helper.managers.NotificationManager;
import com.xy.psn.data.MyHelper;
import com.xy.utils.Logger;

import java.util.Map;

public class PSNMessagingService extends FirebaseMessagingService {
    private Logger LOGGER = Logger.getInstance(this.getClass());

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            LOGGER.d(remoteMessage.getData().toString());

            Map<String, String> map = remoteMessage.getData();
            final String title = map.get(KeyData.PRODUCT_NAME);
            final String message = map.get(KeyData.MESSAGE);
            final String userPhoto = map.get(KeyData.PRODUCT_PHOTO);

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MyHelper.haveNewMsg = true;
                    SharedPreferences sp = getSharedPreferences("BookStore", MODE_PRIVATE);
                    if (sp.getBoolean("showNotification", true)) {
                        if (!MyHelper.isChatroomAlive) {
                            Glide.with(PSNApplication.getAPPLICATION())
                                    .load(userPhoto)
                                    .asBitmap()
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                            LOGGER.d("FCM Received, and now Loaded Image!");
                                            //NotificationManager.getInstance().generateNotification(PSNApplication.getAPPLICATION(), title, message);

                                            NotificationManager.getInstance().generateNotification(PSNApplication.getAPPLICATION(), resource, title, message);
                                            /*try {
                                                NotificationManager.getInstance().generateNotification(PSNApplication.getAPPLICATION(), resource, title, URLDecoder.decode(message, "UTF-8"));
                                            }catch (UnsupportedEncodingException e){
                                                Toast.makeText(MemberChatActivity.context, "UnsupportedEncodingException", Toast.LENGTH_SHORT).show();
                                            }*/
                                        }

                                        @Override
                                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                            super.onLoadFailed(e, errorDrawable);
                                            //NotificationManager.getInstance().generateNotification(PSNApplication.getAPPLICATION(), title, message);
                                        }
                                    });
                        }else {
                            //Toast.makeText(MemberChatActivity.context, "有新訊息", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });
        }
    }
}
