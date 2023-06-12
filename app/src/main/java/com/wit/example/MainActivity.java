package com.wit.example;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wit.witsdk.modular.sensor.device.exceptions.OpenDeviceException;
import com.wit.witsdk.modular.sensor.example.ble5.Bwt901ble;
import com.wit.witsdk.modular.sensor.example.ble5.interfaces.IBwt901bleRecordObserver;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.BluetoothBLE;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.BluetoothSPP;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.WitBluetoothManager;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.exceptions.BluetoothBLEException;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.interfaces.IBluetoothFoundObserver;
import com.wit.witsdk.modular.sensor.modular.processor.constant.WitSensorKey;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.*;
public class MainActivity extends AppCompatActivity implements IBluetoothFoundObserver, IBwt901bleRecordObserver {
    private static final String TAG = MainActivity.class.getSimpleName();

    private List<Bwt901ble> bwt901bleList = new ArrayList<>();
    private Thread writeThread = null;

    private boolean destroyed = true;
    private boolean writeOnSensorData = false;

    private Button writeSensorDataButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WitBluetoothManager.initInstance(this);

        Button startSearchButton = findViewById(R.id.startSearchButton);
        startSearchButton.setOnClickListener((v) -> {
            startDiscovery();
        });

        Button stopSearchButton = findViewById(R.id.stopSearchButton);
        stopSearchButton.setOnClickListener((v) -> {
            stopDiscovery();
        });

        Button appliedCalibrationButton = findViewById(R.id.appliedCalibrationButton);
        appliedCalibrationButton.setOnClickListener((v) -> {
            handleAppliedCalibration();
        });

        Button startFieldCalibrationButton = findViewById(R.id.startFieldCalibrationButton);
        startFieldCalibrationButton.setOnClickListener((v) -> {
            handleStartFieldCalibration();
        });

        Button endFieldCalibrationButton = findViewById(R.id.endFieldCalibrationButton);
        endFieldCalibrationButton.setOnClickListener((v) -> {
            handleEndFieldCalibration();
        });

        Button readReg03Button = findViewById(R.id.readReg03Button);
        readReg03Button.setOnClickListener((v) -> {
            handleReadReg03();
        });

        writeOnSensorData = false;
        writeSensorDataButton = findViewById(R.id.writeSensorDateButton);
        writeSensorDataButton.setOnClickListener((v) -> {
            handleWriteSensorDataButton();
        });

        Thread thread = new Thread(this::refreshDataTh);
        destroyed = false;
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void startDiscovery() {
        for (int i = 0; i < bwt901bleList.size(); i++) {
            Bwt901ble bwt901ble = bwt901bleList.get(i);
            bwt901ble.removeRecordObserver(this);
            bwt901ble.close();
        }

        bwt901bleList.clear();

        try {
            WitBluetoothManager bluetoothManager = WitBluetoothManager.getInstance();
            bluetoothManager.registerObserver(this);
            bluetoothManager.startDiscovery();
        } catch (BluetoothBLEException e) {
            e.printStackTrace();
        }
    }

    public void stopDiscovery() {
        try {
            WitBluetoothManager bluetoothManager = WitBluetoothManager.getInstance();
            bluetoothManager.removeObserver(this);
            bluetoothManager.stopDiscovery();
        } catch (BluetoothBLEException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFoundBle(BluetoothBLE bluetoothBLE) {
        Bwt901ble bwt901ble = new Bwt901ble(bluetoothBLE);
        bwt901bleList.add(bwt901ble);
        bwt901ble.registerRecordObserver(this);

        try {
            bwt901ble.open();
        } catch (OpenDeviceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFoundSPP(BluetoothSPP bluetoothSPP) {
    }

    @Override
    public void onRecord(Bwt901ble bwt901ble) {
        String deviceData = getDeviceData(bwt901ble);
        Log.d(TAG, "device data [ " + bwt901ble.getDeviceName() + "] = " + deviceData);
    }

    private void refreshDataTh() {

        while (!destroyed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            StringBuilder text = new StringBuilder();
            for (int i = 0; i < bwt901bleList.size(); i++) {
                // Make all devices accelerometer calibrated
                Bwt901ble bwt901ble = bwt901bleList.get(i);
                String deviceData = getDeviceData(bwt901ble);
                text.append(deviceData);
            }

            TextView deviceDataTextView = findViewById(R.id.deviceDataTextView);
            runOnUiThread(() -> {
                deviceDataTextView.setText(text.toString());
            });
        }
    }

    private String getDeviceData(Bwt901ble bwt901ble) {
        String content;
        File file = new File(getExternalFilesDir(null), "dados_wit.txt");
        FileOutputStream outputStream;
        
        StringBuilder builder = new StringBuilder();
        Date currentTime = Calendar.getInstance().getTime();
        builder.append(currentTime).append("\n");
        builder.append(bwt901ble.getDeviceName()).append("\n");
        builder.append(getString(R.string.accX)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AccX)).append("g \t");
        builder.append(getString(R.string.accY)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AccY)).append("g \t");
        builder.append(getString(R.string.accZ)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AccZ)).append("g \n");
        builder.append(getString(R.string.asX)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AsX)).append("°/s \t");
        builder.append(getString(R.string.asY)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AsY)).append("°/s \t");
        builder.append(getString(R.string.asZ)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AsZ)).append("°/s \n");
        builder.append(getString(R.string.angleX)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AngleX)).append("° \t");
        builder.append(getString(R.string.angleY)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AngleY)).append("° \t");
        builder.append(getString(R.string.angleZ)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AngleZ)).append("° \n");
        builder.append(getString(R.string.hX)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.HX)).append("\t");
        builder.append(getString(R.string.hY)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.HY)).append("\t");
        builder.append(getString(R.string.hZ)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.HZ)).append("\n");
        builder.append(getString(R.string.t)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.T)).append("\n");
        builder.append(getString(R.string.electricQuantityPercentage)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.ElectricQuantityPercentage)).append("\n");
        builder.append(getString(R.string.versionNumber)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.VersionNumber)).append("\n");
        content= builder.toString();

        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Escreva o conteúdo no arquivo
            bufferedWriter.write(content);

            // Lembre-se de fechar o BufferedWriter
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        builder.append("deu merda\n");
        return builder.toString();
    }

    private String buildSensorData(Bwt901ble bwt901ble) {
        StringBuilder builder = new StringBuilder();

        Date currentTime = Calendar.getInstance().getTime();

        builder.append(currentTime).append("\n");
        builder.append(bwt901ble.getDeviceName()).append("\n");
        builder.append(getString(R.string.accX)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AccX)).append("g \t");
        builder.append(getString(R.string.accY)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AccY)).append("g \t");
        builder.append(getString(R.string.accZ)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AccZ)).append("g \n");
        builder.append(getString(R.string.asX)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AsX)).append("°/s \t");
        builder.append(getString(R.string.asY)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AsY)).append("°/s \t");
        builder.append(getString(R.string.asZ)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AsZ)).append("°/s \n");
        builder.append(getString(R.string.angleX)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AngleX)).append("° \t");
        builder.append(getString(R.string.angleY)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AngleY)).append("° \t");
        builder.append(getString(R.string.angleZ)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.AngleZ)).append("° \n");
        builder.append(getString(R.string.hX)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.HX)).append("\t");
        builder.append(getString(R.string.hY)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.HY)).append("\t");
        builder.append(getString(R.string.hZ)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.HZ)).append("\n");
        builder.append(getString(R.string.t)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.T)).append("\n");
        builder.append(getString(R.string.electricQuantityPercentage)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.ElectricQuantityPercentage)).append("\n");
        builder.append(getString(R.string.versionNumber)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.VersionNumber)).append("\n");

        return builder.toString();
    }

    private void writeSensorData() {
        OutputStreamWriter outputStreamWriter = null;

        String fileName = "test.txt";

        try {
            outputStreamWriter =
                    new OutputStreamWriter(
                            getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE | Context.MODE_APPEND));
        } catch (IOException e) {
            Log.e(TAG, "Error while handling the file: " + e);
            return;
        }

        try {
            while (writeOnSensorData) {
                Bwt901ble bwt901ble = null;
                for (int i = 0; i < bwt901bleList.size(); i++) {
                    bwt901ble = bwt901bleList.get(i);
                }
                outputStreamWriter.write(buildSensorData(bwt901ble));
                outputStreamWriter.flush();
            }
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while writing to the file: " + e);
        }

    }



    public void handleWriteSensorDataButton() {
        if (writeSensorDataButton == null) {
            return;
        }

        if (!writeOnSensorData) {
            writeSensorDataButton.setText(getString(R.string.parar_escrita));
            writeOnSensorData = true;

            writeThread = new Thread(this::writeSensorData);
            writeThread.start();
        } else {
            writeOnSensorData = false;
            writeSensorDataButton.setText(getString(R.string.escrever_dados));
        }
    }

    private void handleAppliedCalibration() {
        for (int i = 0; i < bwt901bleList.size(); i++) {
            Bwt901ble bwt901ble = bwt901bleList.get(i);
            // unlock register
            bwt901ble.unlockReg();
            // send command
            bwt901ble.appliedCalibration();
        }
        Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
    }

    private void handleStartFieldCalibration() {
        for (int i = 0; i < bwt901bleList.size(); i++) {
            Bwt901ble bwt901ble = bwt901bleList.get(i);
            // unlock register
            bwt901ble.unlockReg();
            // send command
            bwt901ble.startFieldCalibration();
        }
        Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
    }

    private void handleEndFieldCalibration() {
        for (int i = 0; i < bwt901bleList.size(); i++) {
            Bwt901ble bwt901ble = bwt901bleList.get(i);
            // unlock register
            bwt901ble.unlockReg();
            // send command
            bwt901ble.endFieldCalibration();
        }
        Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
    }

    private void handleReadReg03() {
        for (int i = 0; i < bwt901bleList.size(); i++) {
            Bwt901ble bwt901ble = bwt901bleList.get(i);

            int waitTime = 200;

            bwt901ble.sendProtocolData(new byte[]{(byte) 0xff, (byte) 0xAA, (byte) 0x27, (byte) 0x03, (byte) 0x00}, waitTime);
            String reg03Value = bwt901ble.getDeviceData("03");
            
            Toast.makeText(this, bwt901ble.getDeviceName() + " reg03Value: " + reg03Value, Toast.LENGTH_LONG).show();
        }
    }
}