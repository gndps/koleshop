package com.koleshop.koleshopbackend.utils;

import com.google.appengine.api.utils.SystemProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 29/03/16.
 */
public class PropertiesCache {

    private static final Logger logger = Logger.getLogger(PropertiesCache.class.getName());

    private final static String DEVELOPMENT_PROJECT_ID = "koleshop-1";
    private final static String PRODUCTION_PROJECT_ID = "koleshop-green";

    private final Properties configProp = new Properties();

    private PropertiesCache() {
        try {
            //Private constructor to restrict new instances
            InputStream in;
            String appId = SystemProperty.applicationId.get();
            logger.log(Level.INFO, "app id = " + appId);
            switch (appId) {
                case DEVELOPMENT_PROJECT_ID:
                    logger.log(Level.INFO, "this is development environment");
                    in = getInputStreamFromConfigFile("config-development.properties");
                    break;
                case PRODUCTION_PROJECT_ID:
                    logger.log(Level.INFO, "this is production environment");
                    in = getInputStreamFromConfigFile("config-production.properties");
                    break;
                default:
                    logger.log(Level.INFO, "this is default environment");
                    in = getInputStreamFromConfigFile("config-development.properties");
                    break;
            }
            logger.info("Read all properties from file");
            try {
                configProp.load(in);
                in.close();
                printTheProperties();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "problem while reading properties", e);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "problem while initializing properties", e);
        }
    }

    //Bill Pugh Solution for singleton pattern
    private static class LazyHolder {
        private static final PropertiesCache INSTANCE = new PropertiesCache();
    }

    public static PropertiesCache getInstance() {
        return LazyHolder.INSTANCE;
    }

    public String getProperty(String key) {
        return configProp.getProperty(key);
    }

    public Set<String> getAllPropertyNames() {
        return configProp.stringPropertyNames();
    }

    public boolean containsKey(String key) {
        return configProp.containsKey(key);
    }

    private void printTheProperties() {
        Enumeration<?> e = configProp.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = configProp.getProperty(key);
            logger.log(Level.INFO, "Printing properties");
            logger.log(Level.INFO, "Key : " + key + ", Value : " + value);
        }
    }

    private InputStream getInputStreamFromConfigFile(String fileName) {
        String filePath = new File("").getAbsolutePath();
        filePath = filePath.concat("/" + fileName);
        return getInputStreamFromPath(filePath);
    }

    private InputStream getInputStreamFromPath(String path) {
        InputStream is = null;
        try {
            is = new FileInputStream(path);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "some prob", e);
        }
        return is;
    }

    public static String getProp(String key) {
        return PropertiesCache.getInstance().getProperty(key);
    }

}
