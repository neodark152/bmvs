package com.example.bmvs;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bmvs.buffer.CORT;
import com.example.bmvs.buffer.CapturedImageQueue;
import com.example.bmvs.buffer.Own;
import com.example.bmvs.dstruct.Block;
import com.example.bmvs.database.DBHandler;
import com.example.bmvs.photographing.TimeController;
import com.example.bmvs.tools.MainActivityRef;
import com.example.bmvs.tools.NetworkUtils;
import com.example.bmvs.tools.Millis;
import com.example.bmvs.tools.ULog;
import com.example.bmvs.nodemsg.Receiver;
import com.example.bmvs.verifying.ListenRequest;
import com.example.bmvs.verifying.SendRequest;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private Button takeButton;
    private Button verifyButton;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takeButton = findViewById(R.id.take_photo);
        verifyButton = findViewById(R.id.verify_photo);
        mainHandler = new Handler(Looper.getMainLooper());

        initialize();
    }

    private void initialize() {
        MainActivityRef.init(this);
        ULog.init();
        DBHandler.init();
        initOwnInfo();
        CORT.init();
        CapturedImageQueue.init();
        ListenRequest.init();
        SendRequest.init();

        ULog.add("==============================");

        ULog.add("Node IP Address:\n" + Own.getOwnIPAddress());
        ULog.add("Node Address:\n" + Own.getOwnAddress());
        ULog.add("All trusted node number: " + Own.getTrustedNodeNumber());

        ULog.add("==============================\n");

        fetchLatestBlock();


    }

    // Load the local private key file
    private void initOwnInfo() {
        try (InputStream inputStream = this.getAssets().open("own_info.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            JSONObject ownInfoJson = new JSONObject(jsonContent.toString());

            Log.i("INIT", "Local own_info file load successfully.");

            Own.init(ownInfoJson.getString("address"),
                    NetworkUtils.getCurrentIP(this),
                    ownInfoJson.getString("security_key"),
                    DBHandler.getTrustedNodeNumber()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize: " + e.getMessage());
        }
    }

    // Fetch the latest block from the network when the application starts
    private void fetchLatestBlock() {
        takeButton.setEnabled(false);
        verifyButton.setEnabled(false);

        ULog.add("Wait to get the latest block...");

        Receiver receiver = new Receiver(10016);

        receiver.receiveOnePacket(data -> {
            Block newBlock = new Block(data);
            if (newBlock.getBlockNum() > Integer.parseInt(Objects.requireNonNull(DBHandler.latestBlockInfo()).getThird())) {

                Log.i("INIT", "Received new block with block no: " + newBlock.getBlockNum());

                DBHandler.insertNewBlock(newBlock);

                ULog.add("New Block saved, with block no " + newBlock.getBlockNum());

                ULog.add("Initialization Successful! Please wait to next ORT to start.");

                mainHandler.postDelayed(() -> {
                    takeButton.setEnabled(true);
                    verifyButton.setEnabled(true);

                    TimeController.startTimeCheck();
                    ListenRequest.startListen();

                }, Millis.getMillisUntilNextMinute());
            }
        });
    }

    public void onActionTakePhoto(View view) {
        Own.getImageHandler().startCamera();
        ULog.add("Image captured, please wait for the next steps.");
    }

    public void onActionVerify(View view) {
        Own.getImageHandler().startFilePicker();
    }
}