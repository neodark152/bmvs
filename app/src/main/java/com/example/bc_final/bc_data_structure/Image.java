package com.example.bc_final.bc_data_structure;

/**
 * Represents an image entity in the system, containing image metadata and verification information.
 */
public class Image {
    private final String imageHash;
    private final String timestamp;
    private final String photographer;
    private final String signature;

    /**
     * Constructs an Image instance with the specified metadata.
     *
     * @param imageHash     the cryptographic hash of the image content (SHA-256)
     * @param timestamp     the creation timestamp of the image
     * @param photographer  the address of the image photographer
     * @param signature     the digital signature verifying the image authenticity
     */
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
