# A Novel Private Blockchain Based Multimedia Verification Systems (BMVS)

This repository provides the source code and usage instructions for **ImageVerify**, an Android application leveraging blockchain technology to capture and verify the authenticity of digital images.

---

## 🔧 System Architecture

![System Architecture](docs/system_architecture.png)

The system comprises six primary modules:

- **Image**: Captures images via the device camera and packages the original image along with the corresponding blockchain data into a ZIP archive.
- **Main UI**: Provides two buttons for capturing and verifying images.
- **Photographing**: Invokes the device camera to capture an image and packages the original image together with the corresponding block data into a ZIP archive.
- **Verifying**: Processes incoming ZIP packages by extracting contents and verifying the authenticity of images through blockchain network.
- **NodeMessage**: Facilitates peer-to-peer communication between nodes, including the formatting, transmission, and parsing of messages for image data, blockchain updates, and verification requests.
- **Database**: Stores blockchain records, image metadata, and other essential system information.

---

## ✨ Features

- Capture images directly within the app, generating JPEG files.
- Package images and blockchain metadata into ZIP archives.
- Authenticate images through blockchain-based verification.
- Generate concise verification reports.

---

## 🖥 Requirements

- **Operating System**: Android 9.0 or later (tested on Android 10)
- **Programming Language**: Java (Android SDK)
- **Minimum SDK Version**: API Level 28

---

## 📦 Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/neodark152/bmvs.git
   
2. Ensure you have at least TWO devices. This project requires the "TrustedNodeList" to include at least three nodes, and at least two nodes must be online simultaneously to enable block generation.

3. Modify the code in the initOwnInfo method within MainActivity to load different private keys and addresses for each device. We provide two distinct owninfo files under app/src/main/assets for this purpose. The database tables store three key pairs, including those corresponding to these owninfo files.