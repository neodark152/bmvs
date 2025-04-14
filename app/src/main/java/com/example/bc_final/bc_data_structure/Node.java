package com.example.bc_final.bc_data_structure;

/**
 * Represents a block node in the system with addressing and routing information.
 */
public class Node {
    private final String nodeAddress;
    private final String nodeIP;
    private String nodeVRF;

    /**
     * Constructs a Node instance with required addressing information.
     * VRF configuration is initialized as null and can be set later.
     *
     * @param nodeAddress the unique identifier for the node
     * @param nodeIP      the IP address for network communication (IPv4 format)
     */
    public Node(String nodeAddress, String nodeIP) {
        this.nodeAddress = nodeAddress;
        this.nodeIP = nodeIP;
        this.nodeVRF = null;
    }

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
