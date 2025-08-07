package com.example.bmvs.dstruct;

/**
 * Represents a block node in the system.
 */
public class Node {
    private final String nodeAddress;
    private final String nodeIP;
    private String nodeVRF;

    public Node(String nodeAddress, String nodeIP) {
        this.nodeAddress = nodeAddress;
        this.nodeIP = nodeIP;
        this.nodeVRF = null;
    }

    // Getters and Setters
    public String getNodeAddress() {
        return nodeAddress;
    }

    public String getNodeIP() {
        return nodeIP;
    }

    public String getNodeVRF() {
        return nodeVRF;
    }

    public void setNodeVRF(String nodeVRF) {
        this.nodeVRF = nodeVRF;
    }
}
