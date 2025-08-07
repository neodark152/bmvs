package com.example.bmvs.photographing;

import android.util.Log;

import com.example.bmvs.buffer.CORT;
import com.example.bmvs.buffer.Own;
import com.example.bmvs.dstruct.Block;
import com.example.bmvs.dstruct.Image;
import com.example.bmvs.database.DBHandler;
import com.example.bmvs.tools.Hash;
import com.example.bmvs.tools.NetworkUtils;
import com.example.bmvs.tools.ULog;
import com.example.bmvs.nodemsg.Receiver;
import com.example.bmvs.nodemsg.Sender;

import java.util.List;
import java.util.Objects;

import kotlin.Triple;

/**
 * GenerateBlock is the final process in photographing.
 * It performs:
 * - Verifying whether the current node is the VRF-selected block creator
 * - Checking if the number of online nodes meets the majority threshold
 * - Generating a new block using received images and broadcasting it to the network
 * - Receiving and storing newer blocks from other nodes
 */
public class GenerateBlock {
    private final Receiver receiver;
    private static GenerateBlock instance;
    private static final String TAG = "Generate New Block Process.";
    private boolean isRunning;

    private GenerateBlock() {
        this.receiver = Own.getReceiver(2);
        this.isRunning = false;
    }

    public static synchronized GenerateBlock getInstance() {
        if (instance == null) {
            instance = new GenerateBlock();
        }
        return instance;
    }

    // Check if the current node is the block creator
    private boolean isBlockCreator() {
        return Objects.equals(Own.getOwnAddress(), CORT.getCORTMinVRF());
    }

    private boolean isEnoughNodeNumber() {
        List<String> onlineIPs = CORT.getOnlineIPs();
        if (onlineIPs == null) {
            return false;
        }

        long onlineCount = onlineIPs.stream().filter(NetworkUtils::isHostReachable).count();

        return onlineCount > (Own.getTrustedNodeNumber() / 2);
    }


    // Generate and broadcast a new block
    private void generateAndBroadcastBlock() {
        try {
            // Get the latest block info
            Triple<String, String, String> latestBlockInfo = DBHandler.latestBlockInfo();
            if (latestBlockInfo == null) {
                Log.e(TAG, "Empty Latest Block Info!");
                return;
            }
            String tailHash = latestBlockInfo.getSecond();
            int blockNum = Integer.parseInt(latestBlockInfo.getThird()) + 1;  // next block number

            Block newBlock = new Block();
            newBlock.setBlockNum(blockNum);
            newBlock.setPreHash(tailHash);
            newBlock.setTimestamp(String.valueOf(System.currentTimeMillis()));
            newBlock.setVrfWinnerAddress(Own.getOwnAddress());  // VRF winner address

            // Add images to the block
            List<Image> imageList = CORT.getCORTReceivedImageList();
            for (Image image : imageList) {
                newBlock.addImage2ImageList(image.getPhotographer(), image.getImageHash(), image.getSignature());
            }

            // Add store nodes to the block
            for (String node : Objects.requireNonNull(CORT.getCORTStoreNodeAddress())) {
                newBlock.addNode2NodeList(node);
            }

            newBlock.setTailHash(Hash.calculateHash(newBlock.getString()));

            // Broadcast the block
            new Sender(10013).broadcastData(newBlock.getJson());
        } catch (Exception e) {
            Log.e(TAG, "Error generate and broadcasting block", e);
        }
    }

    // Receive and store the incoming block if it's newer than the latest one
    private void receiveAndStoreBlock() {
        receiver.receiveData(data -> {
            Block newBlock = new Block(data);
            if (newBlock.getBlockNum() > Integer.parseInt(Objects.requireNonNull(DBHandler.latestBlockInfo()).getThird())) {
                Log.i(TAG, "Received new block with block no: " + newBlock.getBlockNum());
                DBHandler.insertNewBlock(newBlock);
                ULog.add("New Block saved, with block no " + newBlock.getBlockNum());
                if (CORT.isImage()) {
                    Own.getImageHandler().packageImage(newBlock);
                    ULog.add("New V-Image Saved.");
                }
            }
        });
    }

    // Start the block generation process
    public void start() {
        if (!isRunning) {
            if (isBlockCreator() && isEnoughNodeNumber()) generateAndBroadcastBlock();
            receiveAndStoreBlock();
            isRunning = true;
        } else {
            Log.i(TAG, "Generate New Block Process is Running.");
        }
    }

    public void stop() {
        if (isRunning) {
            receiver.stopReceiving();
            Log.i(TAG, "Generate New Block Process Stopped.");
            isRunning = false;
        }
    }
}