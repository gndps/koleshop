package com.koleshop.koleshopbackend.utils;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;

import java.util.logging.Logger;

/**
 * Created by Gundeep on 28/04/16.
 */
public class ImageUtils {

    private static final Logger logger = Logger.getLogger(ImageUtils.class.getName());

    public static byte[] scale(byte[] oldImageData, int width, int height) {

        ImagesService imagesService = ImagesServiceFactory.getImagesService();

        Image oldImage = ImagesServiceFactory.makeImage(oldImageData);
        Transform resize = ImagesServiceFactory.makeResize(width, height);

        Image newImage = imagesService.applyTransform(resize, oldImage);

        byte[] newImageData = newImage.getImageData();
        return newImageData;
    }

}
