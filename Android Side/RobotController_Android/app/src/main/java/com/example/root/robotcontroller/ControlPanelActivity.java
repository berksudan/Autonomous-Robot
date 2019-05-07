package com.example.root.robotcontroller;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;


/* *************** EKLENECEKLER: ***************
 * RFID okununca ekrana yeşil yazdır ve okunan RFID numarasını göster
 * Aracın hızını seekbar ile kontrol et.
 * Commandleri gerçekleştir.
 * Manual Mode - Autonomous Mode diye switch koy.
 * Düğmeden elini çektiğin anda ->> stop moving komutu gitsin: onRelease(){stopMoving()}
 *
 */

public class ControlPanelActivity extends AppCompatActivity {

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Button btnGoForward, btnGoBack, btnLeft, btnRight, btnDisc;
    ImageButton btnliftUp, btnliftDown;
    String deviceAddress;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth;
    BluetoothSocket btSocket;
    private boolean isBtConnected;

    private class Commands {
        static final String COMMAND_GO_FORWARD = "GF";
        static final String COMMAND_GO_BACK = "GB";
        static final String COMMAND_TURN_LEFT = "TL";
        static final String COMMAND_TURN_RIGHT = "TR";
        static final String COMMAND_STOP_MOVING = "SM";

        static final String COMMAND_LIFT_UP = "LU";
        static final String COMMAND_LIFT_DOWN = "LD";
    }

    protected void matchViewIDs() {
        btnGoForward = findViewById(R.id.btnUp);
        btnGoBack = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnDisc = findViewById(R.id.btnDisc);

        btnliftUp = findViewById(R.id.btnLiftUp);
        btnliftDown = findViewById(R.id.btnLiftDown);

    }

    @SuppressLint("ClickableViewAccessibility")
    protected void setContactListener() {
        btnGoForward.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    btnGoForward.setBackground(getResources().getDrawable(R.drawable.button_pressed));
                    sendMsgToDevice(Commands.COMMAND_GO_FORWARD);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    btnGoForward.setBackground(getResources().getDrawable(R.drawable.buttonshape));
                    sendMsgToDevice(Commands.COMMAND_STOP_MOVING);
                }

                return true;
            }
        });
        btnGoBack.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    btnGoBack.setBackground(getResources().getDrawable(R.drawable.button_pressed));

                    sendMsgToDevice(Commands.COMMAND_GO_BACK);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    btnGoBack.setBackground(getResources().getDrawable(R.drawable.buttonshape));
                    sendMsgToDevice(Commands.COMMAND_STOP_MOVING);
                }

                return true;
            }
        });


        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blinkButton(btnLeft);
                sendMsgToDevice(Commands.COMMAND_TURN_LEFT);
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blinkButton(btnRight);
                sendMsgToDevice(Commands.COMMAND_TURN_RIGHT);
            }
        });

        btnDisc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }

        });

        btnliftUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blinkImageButton(btnliftUp);
                sendMsgToDevice(Commands.COMMAND_LIFT_UP);
            }
        });
        btnliftDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blinkImageButton(btnliftDown);
                sendMsgToDevice(Commands.COMMAND_LIFT_DOWN);
            }
        });


    }


    @SuppressLint({"ClickableViewAccessibility", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);

        // Initiate variables
        deviceAddress = null;
        myBluetooth = null;
        btSocket = null;
        isBtConnected = false;

        //receive the deviceAddress of the bluetooth device
        Intent newIntent = getIntent();
        deviceAddress = newIntent.getStringExtra("EXTRA_ADDRESS");

        new ConnectBT().execute();

        matchViewIDs();
        setContactListener();


    }

    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                showToastMsg("ERROR", "IOException occurred.");
            }
        }
        finish(); //return to the first layout
    }

    private void sendMsgToDevice(String msg) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(msg.getBytes());
            } catch (IOException e) {
                showToastMsg("ERROR", "IOException occurred.");
            }
        }
    }


    private void showToastMsg(String tag, String msg) {
        String toastedMsg; // Temporary String value

        if (tag.equals("")) {
            toastedMsg = msg;
        } else {
            toastedMsg = tag + ": " + msg;
        }
        Toast.makeText(getApplicationContext(),
                toastedMsg, Toast.LENGTH_SHORT).show();
    }
    private void blinkButton(Button button) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(50);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(2);
        button.startAnimation(anim);
    }
    private void blinkImageButton(ImageButton imgButton){
        Animation animation = new AlphaAnimation(1, 0); //to change visibility from visible to invisible
        animation.setDuration(50); //1 second duration for each animation cycle
        animation.setStartOffset(20);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(2); //repeating indefinitely
        animation.setRepeatMode(Animation.REVERSE); //animation will start from end point once ended.
        imgButton.startAnimation(animation); //to start animation

    }


    @SuppressLint("StaticFieldLeak")
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ControlPanelActivity.this, "Connecting to the bluetooth device.",
                    "It can take for a while.");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(deviceAddress);//connects to the device's deviceAddress and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                showToastMsg("WARNING", "Connection Failed, try to reconnect! Device is not found.");
                finish();
            } else {
                showToastMsg("INFO", "Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }


}