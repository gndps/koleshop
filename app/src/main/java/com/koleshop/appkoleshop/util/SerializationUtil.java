package com.koleshop.appkoleshop.util;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Gundeep on 04/11/15.
 */
public class SerializationUtil {

    private static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();

    public static byte[] getSerializableFromGenericJson(Object value) throws Exception {
        byte[] outputbytes = null;
        if (value instanceof GenericJson) {
            outputbytes = JSON_FACTORY.toByteArray(value);
        } else {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream objectstream = new ObjectOutputStream(output);
            objectstream.writeObject(value);
            objectstream.close();
            outputbytes = output.toByteArray();
        }
        return outputbytes;
    }

    public static <T> T getGenericJsonFromSerializable(byte[] serializableObject, Class<T> outclass) throws Exception {

        if (serializableObject[0] == '{' && serializableObject[1] == '"' && serializableObject[serializableObject.length-1] == '}') {
            // Looks like JSON...
            return JSON_FACTORY.fromString(new String(serializableObject, "UTF-8"), outclass);
        } else {
            ByteArrayInputStream input = new ByteArrayInputStream(serializableObject);
            ObjectInputStream objectstream = new ObjectInputStream(input);
            Object value = objectstream.readObject();
            objectstream.close();
            return outclass.cast(value);
        }
    }

}
