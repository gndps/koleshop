package com.koleshop.koleshopbackend.gcm;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.gson.JsonObject;
import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.services.SessionService;
import com.koleshop.koleshopbackend.services.UserService;
import com.koleshop.koleshopbackend.utils.PropertiesCache;

public class GcmHelper {

    private static final Logger logger = Logger.getLogger(GcmHelper.class.getName());

    public static void notifyUser(Long userId, Message message, int numOfRetries) {

        List<String> deviceIds = UserService.getDeviceIdsForUserId(userId);

        logger.log(Level.INFO, "will notify user...data=" + message.getData());

        try {
            Sender sender = new Sender(PropertiesCache.getProp("GCM_API_KEY"));
            if(sender!=null) {
                MulticastResult result = sender.send(message, deviceIds, numOfRetries);

                //1. log the result status
                if(result==null || result.getFailure() == deviceIds.size()) {
                    logger.log(Level.SEVERE, "Gcm message sending failed for message type : " + message.getData().get("type"));
                    if(result!=null) {
                        logger.log(Level.SEVERE, result.getResults().toString());
                    }
                } else {
                    logger.log(Level.INFO, "gcm successfully sent!");
                }

                //2. update or delete the registration tokens if reqd.
                if(result!=null) {
                    try {
                        int index = 0;
                        for (Result singleResult : result.getResults()) {
                            String canonicalRegistrationId = singleResult.getCanonicalRegistrationId();
                            if (canonicalRegistrationId != null && !canonicalRegistrationId.isEmpty()) {
                                if (deviceIds.contains(canonicalRegistrationId)) {
                                    //delete deviceIds.get(index) from db
                                    new SessionService().deleteDeviceUser(userId, deviceIds.get(index));
                                } else {
                                    //update the device id with the new canonicalRegistrationId
                                    new SessionService().updateDeviceUser(userId, deviceIds.get(index), canonicalRegistrationId);
                                }
                            }
                            index++;
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "problem in updating canonical registration id", e);
                    }
                }

            } else {
                logger.log(Level.SEVERE, "Gcm sender is null...wtf");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "some problem in sending gcm message", e);
        }

    }

}
