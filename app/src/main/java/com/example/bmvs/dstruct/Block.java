package com.example.bmvs.dstruct;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Block class represents a blockchain's block data structure with metadata and associated data lists.
 */
public class Block {
    private JSONArray imageList;
    private JSONArray nodeList;
    private String preHash;
    private String tailHash;
    private int blockNum;
    private String timestamp;
    private String vrfWinnerAddress;

    public Block() {
        this.imageList = new JSONArray();
        this.nodeList = new JSONArray();
        this.preHash = "";
        this.tailHash = "";
        this.blockNum = 0;
        this.timestamp = "";
        this.vrfWinnerAddress = "";
    }

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

    public void addNode2NodeList(String address) {
        try {
            JSONObject nodeObject = new JSONObject();
            nodeObject.put("address", address);
            nodeList.put(nodeObject);
        } catch (JSONException e) {
            throw new RuntimeException("Error adding node to node list", e);
        }
    }

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