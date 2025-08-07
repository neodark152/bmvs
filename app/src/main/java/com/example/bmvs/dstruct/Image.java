package com.example.bmvs.dstruct;

/**
 * Represents an image entity, containing image metadata and verification information.
 */
public class Image {
    private final String imageHash;
    private final String timestamp;
    private final String photographer;
    private final String signature;

    public Image(String imageHash, String timestamp, String photographer, String signature) {
        this.imageHash = imageHash;
        this.timestamp = timestamp;
        this.photographer = photographer;
        this.signature = signature;
    }

    // Getters
    public String getImageHash() {
        return imageHash;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getPhotographer() {
        return photographer;
    }
    public String getSignature() {
        return signature;
    }
}
