package com.kolshop.kolshopmaterial.receiver;

/**
 * Created by Gundeep on 01/10/15.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.kolshop.kolshopmaterial.common.constant.Constants;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = SmsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (Object aPdusObj : pdusObj) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String senderAddress = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();
                    Log.e(TAG, "Received SMS: " + message + ", Sender: " + senderAddress);
                    if(message.startsWith("Code : ")) {
                        String verificationCode = getVerificationCode(message);
                        if(verificationCode!=null) {
                            Log.e(TAG, "OTP received: " + verificationCode);
                            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                            Intent intentBroadcast = new Intent(Constants.ACTION_OTP_RECEIVED);
                            intent.putExtra("code", verificationCode);
                            localBroadcastManager.sendBroadcast(intentBroadcast);
                        }
                    } else {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Getting the OTP from sms message body
     * ':' is the separator of OTP from the message
     *
     * @param message
     * @return
     */
    private String getVerificationCode(String message) {
        String code = null;
        int index = message.indexOf(":");

        if (index != -1) {
            int start = index + 2;
            int length = 4;
            code = message.substring(start, start + length);
            return code;
        }

        return code;
    }
}