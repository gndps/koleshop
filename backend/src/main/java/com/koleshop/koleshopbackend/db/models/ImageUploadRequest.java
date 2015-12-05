package com.koleshop.koleshopbackend.db.models;

/**
 * Created by Gundeep on 04/12/15.
 */
public class ImageUploadRequest {

    byte[] imageData;
    String fileName;
    String mimeType;

    public ImageUploadRequest(byte[] imageData, String fileName, String mimeTpye) {
        this.imageData = imageData;
        this.fileName = fileName;
        this.mimeType = mimeTpye;
    }

    public ImageUploadRequest() {
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
