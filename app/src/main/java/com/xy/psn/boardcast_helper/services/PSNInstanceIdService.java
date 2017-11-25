package com.xy.psn.boardcast_helper.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.xy.psn.boardcast_helper.managers.UserDataManager;
import com.xy.utils.Logger;

/**
 * Created by Xavier Yin on 10/5/16.
 */

public class PSNInstanceIdService extends FirebaseInstanceIdService {
    private static final Logger LOGGER = Logger.getInstance(PSNInstanceIdService.class);

    @Override
    public void onTokenRefresh() {
        String currentToken = FirebaseInstanceId.getInstance().getToken();

        LOGGER.d("Current Token: " + currentToken);

        UserDataManager.getInstance().setPushToken(currentToken);
    }
}
