package com.wit.example;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
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

    private boolean destroyed = true;
    private boolean writeOnSensorData = false;

    private boolean isCalibrating = false;
    private Button startCalibrationButton = null;
    private Button writeSensorDataButton = null;
    private Button rotulateButton = null;
    private String writeContent = "";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss:SSS");
    private SimpleDateFormat dateFormatFile = new SimpleDateFormat("dd-MM-YYYY_(HH.mm.ss.SSS)");


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

        startCalibrationButton = findViewById(R.id.startFieldCalibrationButton);
        startCalibrationButton.setOnClickListener((v) -> {
            if (!isCalibrating)
                handleStartFieldCalibration();
            else
                handleEndFieldCalibration();
        });

        Button readReg03Button = findViewById(R.id.readReg03Button);
        readReg03Button.setOnClickListener((v) -> {
            handleReadReg03();
        });

        writeSensorDataButton = findViewById(R.id.writeSensorDateButton);
        writeSensorDataButton.setOnClickListener((v) -> {
            handleWriteSensorDataButton();
        });

        rotulateButton = findViewById(R.id.rotulateButton);

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
        //Log.d(TAG, "device data [ " + bwt901ble.getDeviceName() + "] = " + deviceData);
    }

    private void refreshDataTh() {

        while (!destroyed) {
            try {
                sleep(100);
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
        //builder.append(getString(R.string.electricQuantityPercentage)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.ElectricQuantityPercentage)).append("\n");
        builder.append(getString(R.string.versionNumber)).append(":").append(bwt901ble.getDeviceData(WitSensorKey.VersionNumber)).append("\n");

        return builder.toString();
    }

    private String buildSensorDataTable() {
        StringBuilder builder = new StringBuilder();

        builder.append("time,");
        builder.append("accX,");
        builder.append("accY,");
        builder.append("accZ,");
        builder.append("asX,");
        builder.append("asY,");
        builder.append("asZ,");
        builder.append("angleX,");
        builder.append("angleY,");
        builder.append("angleZ,");
        builder.append("hX,");
        builder.append("hY,");
        builder.append("hZ,");
        builder.append("tag\n");

        return builder.toString();
    }

    private String buildSensorData(Bwt901ble bwt901ble) {
        StringBuilder builder = new StringBuilder();

        Date date = new Date();
        String stringDate = dateFormat.format(date);

        builder.append(stringDate).append(",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.AccX)).append("\",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.AccY)).append("\",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.AccZ)).append("\",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.AsX)).append("\",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.AsY)).append("\",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.AsZ)).append("\",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.AngleX)).append("\",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.AngleY)).append("\",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.AngleZ)).append("\",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.HX)).append("\",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.HY)).append("\",\"");
        builder.append(bwt901ble.getDeviceData(WitSensorKey.HZ)).append("\",");
        builder.append(rotulateButton.isPressed() ? "1" : "0").append("\n");

        return builder.toString();
    }

    private void writeSensorDataString(String fileName, boolean deleteOld) {
        File file = new File(getExternalFilesDir(null), fileName);
        boolean fileExists = file.exists();

        if (fileExists && deleteOld) {
            file.delete();
            fileExists = false;
        }

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (IOException e) {
            Log.e(TAG, "Error while handling the file: " + e);
            return;
        }

        try {
            if (!fileExists)
                bufferedWriter.write(buildSensorDataTable());
            bufferedWriter.write(writeContent);

            bufferedWriter.close();
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

            final Thread thread = new Thread(() -> {
                writeContent = "";
                Bwt901ble bwt901ble = null;
                for (int i = 0; i < bwt901bleList.size(); i++) {
                    bwt901ble = bwt901bleList.get(i);
                }

                while (writeOnSensorData) {
                    writeContent += buildSensorData(bwt901ble);
                    try {
                        sleep(5);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();
        } else {
            writeOnSensorData = false;
            writeSensorDataButton.setText(getString(R.string.escrever_dados));

            final Thread thread = new Thread(() -> {
                Date date = new Date();
                String dateString = dateFormatFile.format(date);
                writeSensorDataString("output-" + dateString + ".csv", true);
            });
            thread.start();
        }
    }

    private void handleAppliedCalibration() {
        if (isCalibrating) {
            Toast.makeText(this, "Termine a calibração antes de aplicar.", Toast.LENGTH_LONG)
                    .show();
        }

        for (int i = 0; i < bwt901bleList.size(); i++) {
            Bwt901ble bwt901ble = bwt901bleList.get(i);
            // unlock register
            bwt901ble.unlockReg();
            // send command
            bwt901ble.appliedCalibration();
        }
    }

    private void handleStartFieldCalibration() {
        isCalibrating = true;
        for (int i = 0; i < bwt901bleList.size(); i++) {
            Bwt901ble bwt901ble = bwt901bleList.get(i);
            // unlock register
            bwt901ble.unlockReg();
            // send command
            bwt901ble.startFieldCalibration();
        }
        Toast.makeText(this, "Calibrando.", Toast.LENGTH_LONG).show();
        startCalibrationButton.setText(R.string.End_field_calibration);
    }

    private void handleEndFieldCalibration() {
        isCalibrating = false;
        for (int i = 0; i < bwt901bleList.size(); i++) {
            Bwt901ble bwt901ble = bwt901bleList.get(i);
            // unlock register
            bwt901ble.unlockReg();
            // send command
            bwt901ble.endFieldCalibration();
        }
        Toast.makeText(this, "Calibrado.", Toast.LENGTH_LONG).show();
        startCalibrationButton.setText(R.string.Start_field_calibration);
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