package com.example.bc_final.bc_data_structure;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Block class represents a blockchain block with metadata and associated data lists.
 * It provides functionality to initialize, manipulate, and serialize block data.
 */
public class Block {
    private JSONArray imageList;        // List of image-related data
    private JSONArray nodeList;         // List of node-related data
    private String preHash;             // Hash of the previous block
    private String tailHash;            // Hash of the current block
    private int blockNum;               // Block number in the chain
    private String timestamp;           // Timestamp of the block creation
    private String vrfWinnerAddress;    // Address of the VRF (Verifiable Random Function) winner

    /**
     * Default constructor initializing an empty block with default values.
     */
    public Block() {
        this.imageList = new JSONArray();
        this.nodeList = new JSONArray();
        this.preHash = "";
        this.tailHash = "";
        this.blockNum = 0;
        this.timestamp = "";
        this.vrfWinnerAddress = "";
    }

    /**
     * Constructor that initializes the Block object from a JSON string.
     *
     * @param block JSON string representation of a block.
     * @throws IllegalArgumentException if the input string is not a valid JSON representation.
     */
    public Block(String block) {
        try {
            JSONObject blockJson = new JSONObject(block);
            this.preHash = blockJson.getString("pre_hash");
            this.tailHash = blockJson.getString("tail_hash");
            this.blockNum = blockJson.getInt("num");
            this.timestamp = blockJson.getString("timestamp");
            this.vrfWinnerAddress = blockJson.getString("vrf_address");
            this.imageList = blockJson.getJSONArray("image_list");
            this.nodeList = blockJson.getJSONArray("node_list");
        } catch (JSONException e) {
            Log.w("Block", "Invalid data, passed.");
        }
    }


    // Getters
    public JSONArray getImageList() {
        return imageList;
    }

    public String getPreHash() {
        return preHash;
    }

    public String getTailHash() {
        return tailHash;
    }

    public int getBlockNum() {
        return blockNum;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getVrfWinnerAddress() {
        return vrfWinnerAddress;
    }

    // Setters
    public void setPreHash(String preHash) {
        this.preHash = preHash;
    }

    public void setTailHash(String tailHash) {
        this.tailHash = tailHash;
    }

    public void setBlockNum(int blockNum) {
        this.blockNum = blockNum;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setVrfWinnerAddress(String vrfWinnerAddress) {
        this.vrfWinnerAddress = vrfWinnerAddress;
    }

    /**
     * Adds an image entry to the image list.
     *
     * @param imageHash   Hash of the image.
     * @param nodeAddress Address of the node that uploaded the image.
     * @param signature   Signature of the image data, pi.
     */
    public void addImage2ImageList(String nodeAddress, String imageHash, String signature) {
        try {
            JSONObject imageObject = new JSONObject();
            imageObject.put("address", nodeAddress);
            imageObject.put("im_hash", imageHash);
            imageObject.put("signature", signature);
            imageList.put(imageObject);
        } catch (JSONException e) {
            throw new RuntimeException("Error adding image to image list", e);
        }
    }

    /**
     * Adds a node entry to the node list.
     *
     * @param address Address of the node.
     */
    public void addNode2NodeList(String address) {
        try {
            JSONObject nodeObject = new JSONObject();
            nodeObject.put("address", address);
            nodeList.put(nodeObject);
        } catch (JSONException e) {
            throw new RuntimeException("Error adding node to node list", e);
        }
    }

    /**
     * Converts the node list (JSONArray) into a List of Strings.
     *
     * @return List of node addresses.
     */
    public List<String> getNodeAddresses() {
        List<String> nodeAddressList = new ArrayList<>();
        for (int i = 0; i < nodeList.length(); i++) {
            try {
                JSONObject nodeObject = nodeList.getJSONObject(i);
                String address = nodeObject.optString("address", "");
                if (!address.isEmpty()) {
                    nodeAddressList.add(address);
                }
            } catch (JSONException e) {
                throw new RuntimeException("Error parsing node list", e);
            }
        }
        return nodeAddressList;
    }


    /**
     * Serializes the block object into a JSON string.
     *
     * @return JSON string representation of the block.
     */
    public String getString() {
        try {
            JSONObject block = new JSONObject();
            block.put("pre_hash", preHash);
            block.put("tail_hash", tailHash);
            block.put("timestamp", timestamp);
            block.put("num", blockNum);
            block.put("vrf_address", vrfWinnerAddress);
            block.put("image_list", imageList);
            block.put("node_list", nodeList);
            return block.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Error serializing block to JSON string", e);
        }
    }


    /**
     * Serializes the block object into a JSON.
     *
     * @return JSON representation of the block.
     */
    public JSONObject getJson() {
        try {
            JSONObject block = new JSONObject();
            block.put("pre_hash", preHash);
            block.put("tail_hash", tailHash);
            block.put("timestamp", timestamp);
            block.put("num", blockNum);
            block.put("vrf_address", vrfWinnerAddress);
            block.put("image_list", imageList);
            block.put("node_list", nodeList);
            return block;
        } catch (JSONException e) {
            throw new RuntimeException("Error serializing block to JSON string", e);
        }
    }
}