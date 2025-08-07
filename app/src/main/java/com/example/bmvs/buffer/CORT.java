package com.example.bmvs.buffer;

import android.util.Log;

import com.example.bmvs.dstruct.Image;
import com.example.bmvs.dstruct.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CORT (Current Operation Round Time) is a utility class that manages:
 * - A list of all online nodes participating in the current round.
 * - A list of all received image data during the current round.
 * - All VRF (Verifiable Random Function) values submitted by nodes in the current round.
 */
public class CORT {
    // List of currently online nodes
    private static List<Node> CORTOnlineNodeList;

    // List of images received from other nodes
    private static List<Image> CORTReceivedImageList;

    // Total number of currently online nodes
    private static int currentOnlineNodeNum;

    // Flag to indicate whether an image has been captured
    private static boolean isImageCaptured;

    public static synchronized void init() {
        CORTOnlineNodeList = new ArrayList<>();
        CORTReceivedImageList = new ArrayList<>();
        currentOnlineNodeNum = 0;
        isImageCaptured = false;
        Log.i("INIT", "CORT class initialize successfully.");
    }

    public static void addNewNode(Node node) {
        CORTOnlineNodeList.add(node);
        currentOnlineNodeNum++;
    }

    public static void addNewReceivedImage(Image image) {
        CORTReceivedImageList.add(image);
    }

    public static boolean updateNodeVRF(String nodeAddress, String nodeVRF) {
        return CORTOnlineNodeList.stream()
                .filter(node -> Objects.equals(node.getNodeAddress(), nodeAddress))
                .findFirst()
                .map(node -> {
                    node.setNodeVRF(nodeVRF);
                    return true;
                })
                .orElse(false);
    }

    public static String getCORTMinVRF() {
        return CORTOnlineNodeList.stream()
                .filter(node -> node.getNodeVRF() != null && node.getNodeAddress() != null)
                .min(Comparator.comparing(Node::getNodeVRF))
                .map(Node::getNodeAddress)
                .orElse(null);
    }

    public static List<String> getOnlineIPs() {
        return CORTOnlineNodeList.isEmpty() ? null :
                CORTOnlineNodeList.stream()
                        .map(Node::getNodeIP)
                        .collect(Collectors.toList());
    }

    public static void ClearCORTStatus() {
        CORTOnlineNodeList.clear();
        currentOnlineNodeNum = 0;
        isImageCaptured = false;
        Log.i("CORT", "Successful clear current ORT status.");
    }

    public static int getCurrentOnlineNodeNum() {
        return currentOnlineNodeNum;
    }

    public static String getNodeIPbyAddress(String nodeAddress) {
        return CORTOnlineNodeList.stream()
                .filter(node -> Objects.equals(node.getNodeAddress(), nodeAddress))
                .map(Node::getNodeIP)
                .findFirst()
                .orElse(null);
    }

    public static List<Image> getCORTReceivedImageList() {
        return CORTReceivedImageList;
    }

    public static List<String> getCORTStoreNodeAddress() {
        return CORTOnlineNodeList.isEmpty() ? null :
                CORTOnlineNodeList.stream()
                        .map(Node::getNodeAddress)
                        .collect(Collectors.toList());
    }

    public static boolean isImage() {
        return isImageCaptured;
    }

    public static void haveImage() {
        CORT.isImageCaptured = true;
    }
}
