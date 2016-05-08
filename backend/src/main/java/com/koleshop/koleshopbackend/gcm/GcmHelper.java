package com.koleshop.koleshopbackend.gcm;

import com.koleshop.koleshopbackend.common.Constants;
import com.koleshop.koleshopbackend.models.gcm.Message;
import com.koleshop.koleshopbackend.models.gcm.MulticastResult;
import com.koleshop.koleshopbackend.models.gcm.Result;
import com.koleshop.koleshopbackend.models.gcm.Sender;
import com.koleshop.koleshopbackend.services.SessionService;
import com.koleshop.koleshopbackend.services.UserService;
import com.koleshop.koleshopbackend.utils.PropertiesCache;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GcmHelper {

    private static final Logger logger = Logger.getLogger(GcmHelper.class.getName());

    public static void notifyUser(Long userId, Message message, int numOfRetries) {

        List<String> deviceIds = UserService.getDeviceIdsForUserId(userId);

        logger.log(Level.INFO, "will notify user...data=" + message.getData());

        try {
            Sender sender = new Sender(PropertiesCache.getProp("GCM_API_KEY"));
            if (sender != null) {
                MulticastResult result = sender.send(message, deviceIds, numOfRetries);

                //1. log the result status
                if (result == null || result.getFailure() == deviceIds.size()) {
                    logger.log(Level.SEVERE, "Gcm message sending failed for message type : " + message.getData().get("type"));
                    if (result != null) {
                        logger.log(Level.SEVERE, result.getResults().toString());
                    }
                } else {
                    logger.log(Level.INFO, "gcm successfully sent!");
                }

                //2. update or delete the registration tokens if reqd.
                if (result != null) {
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
        } catch (Exception e) {
            logger.log(Level.SEVERE, "some problem in notifying user", e);
        }

    }

    public static void notifyAdmin(Message message, int numOfRetries) {

        String topic = Constants.ADMIN_APP_TOPIC;

        logger.log(Level.INFO, "will notify admin...data=" + message.getData());

        try {
            logger.log(Level.INFO, "try notify admin");
            Sender sender = new Sender(PropertiesCache.getProp("GCM_API_KEY"));
            if (sender != null) {
                logger.log(Level.INFO, "sender is not null...sending noti now");
                Result result = sender.send(message, topic, numOfRetries);

                if(result!=null) {
                    logger.log(Level.INFO, "result is not null");
                    logger.log(Level.SEVERE, result.toString());
                    if(result.getFailure() == null || result.getFailure() <= 0) {
                        logger.log(Level.INFO, "result has no failures");
                        logger.log(Level.INFO, "gcm successfully sent to admin!");
                    } else {
                        logger.log(Level.INFO, "result has some failures");
                        logger.log(Level.SEVERE, "Gcm message sending to admin failed \nMessage data: " + message.getData().toString());
                    }
                } else {
                    logger.log(Level.INFO, "result is null");
                }

                logger.log(Level.INFO, "so much logging just for fun");

                /*2. update or delete the registration tokens if reqd.
                if (result != null) {
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
                }*/

            } else {
                logger.log(Level.SEVERE, "Gcm sender is null...wtf admin");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "some problem in sending gcm message to admin", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "some problem in notifying admin", e);
        }

    }

}
