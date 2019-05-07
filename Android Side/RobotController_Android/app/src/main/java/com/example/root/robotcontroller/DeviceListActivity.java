package com.example.root.robotcontroller;

import java.util.Set;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class DeviceListActivity extends AppCompatActivity
        implements MyRecyclerViewAdapter.ItemClickListener {

    private BluetoothAdapter bluetoothAdapter;
    private TextView showPairedDeviceTV;
    private RecyclerView deviceList;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        Button buttonExit, buttonPaired;


        buttonExit = findViewById(R.id.buttonExitID);
        buttonPaired = findViewById(R.id.buttonPairedID);
        showPairedDeviceTV = findViewById(R.id.showPairedDeviceID);
        deviceList = findViewById(R.id.recyclerViewID);
        deviceList.setLayoutManager(new LinearLayoutManager(this));

        String tmpStr = "Press button to see paired device(s)";
        showPairedDeviceTV.setText(tmpStr);

        bluetoothAdapter = null; // Initiate bluetooth adapter.


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!checkDeviceBTAdapter())
            finish();

        enableBtIfNot();

        buttonPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableBtIfNot();
                showPairedDevicesList();

            }
        });

        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastMsg("INFO", "Exiting...");
                finish();
            }
        });


    }


    void enableBtIfNot() { //Ask to the user turn the Bluetooth on
        if (!bluetoothAdapter.isEnabled()) { //If still not enabled.
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
            showToastMsg("ERROR", "Please turn on bluetooth and open app again.");
            finish();
        }

    }

    boolean checkDeviceBTAdapter() {
        if (bluetoothAdapter == null) {
            showToastMsg("ERROR", ": Bluetooth adapter is not available in your phone.");
            return false;
        }
        return true;
    }

    void showToastMsg(String tag, String msg) {
        String msgWithTag; // Temporary String value

        msgWithTag = tag + ": " + msg;
        Toast.makeText(getApplicationContext(),
                msgWithTag, Toast.LENGTH_SHORT).show();
    }


    private void showPairedDevicesList() {
        String msg;

        Set<BluetoothDevice> pairedDevices;

        pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            msg = pairedDevices.size() + " Paired Device(s) Found.";
            showPairedDeviceTV.setText(msg);
            blinkText(showPairedDeviceTV);

            for (BluetoothDevice aBtDevice : pairedDevices)
                list.add(aBtDevice.getName() + "\nADDRESS: " + aBtDevice.getAddress());
        } else {
            msg = "No Paired Devices Found.";
            showPairedDeviceTV.setText(msg);
            blinkText(showPairedDeviceTV);
        }


        myRecyclerViewAdapter = new MyRecyclerViewAdapter(this, list);
        myRecyclerViewAdapter.setClickListener(this);
        deviceList.setAdapter(myRecyclerViewAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {

        myRecyclerViewAdapter.getItem(position);

        String info = myRecyclerViewAdapter.getItem(position);
        String address = info.substring(info.length() - 17);
        showToastMsg("INFO", "Address:" + address);
        Intent i = new Intent(DeviceListActivity.this, ControlPanelActivity.class);
        i.putExtra("EXTRA_ADDRESS", address); //this will be received at ControlPanel Activity
        startActivity(i);

    }


    private void blinkText(TextView aText) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(50); //You can manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(3);
        aText.startAnimation(anim);
    }

}
