package com.wit.example.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wit.witsdk.modular.sensor.device.exceptions.OpenDeviceException;
import com.wit.witsdk.modular.sensor.example.ble5.Bwt901ble;
import com.wit.witsdk.modular.sensor.example.ble5.interfaces.IBwt901bleRecordObserver;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.BluetoothBLE;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.BluetoothSPP;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.WitBluetoothManager;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.exceptions.BluetoothBLEException;
import com.wit.witsdk.modular.sensor.modular.connector.modular.bluetooth.interfaces.IBluetoothFoundObserver;

public class Bluetooth implements IBluetoothFoundObserver, IBwt901bleRecordObserver {
    private String TAG = Bluetooth.class.getSimpleName();

    private WitBluetoothManager mBluetoothManager;
    private Context mContext;

    public Bluetooth(Context context) {
        mContext = context;

        WitBluetoothManager.initInstance(mContext);
        try {
            mBluetoothManager = WitBluetoothManager.getInstance();
        } catch (BluetoothBLEException e) {
            Log.e(TAG, "Erro ao criar o bluetooth manager: " + e);
            mBluetoothManager = null;
        }
    }

    @Override
    public void onRecord(Bwt901ble bwt901ble) {

    }

    @Override
    public void onFoundBle(BluetoothBLE bluetoothBLE) {
        enviarMensagem(Info.Status.CONECTADO);

        Bwt901ble sensor = new Bwt901ble(bluetoothBLE);
        sensor.registerRecordObserver(this);

        boolean throwError = false;
        try {
            sensor.open();
        } catch (OpenDeviceException e) {
            Log.e(TAG, "Erro ao tentar utilizar device blueetooth conectado: " + e);
            throwError = true;
        } finally {
            if (!throwError) {
                Configuracoes.setStatus(true);
            } else{
                Configuracoes.setStatus(false);
            }
        }

        Configuracoes.setSensor(sensor);
        pararProcura();
    }

    @Override
    public void onFoundSPP(BluetoothSPP bluetoothSPP) {

    }

    public void iniciarProcura() {
        if (mBluetoothManager == null) {
            Log.d(TAG, "pararProcura: classe não foi instanciada corretamente.");
            return;
        }

        enviarMensagem(Info.Status.BUSCANDO);

        Bwt901ble sensor = Configuracoes.getSensor();

        if (sensor != null) {
            sensor.removeRecordObserver(this);
            sensor.close();

            Configuracoes.setSensor(null);
        }

        // Para garantir que possuimos a mesma referencia o tempo inteiro.
        sensor = Configuracoes.getSensor();

        mBluetoothManager.registerObserver(this);
        mBluetoothManager.startDiscovery();
    }

    public void pararProcura() {
        if (mBluetoothManager == null) {
            Log.d(TAG, "pararProcura: classe não foi instanciada corretamente.");
            return;
        }

        mBluetoothManager.removeObserver(this);
        mBluetoothManager.stopDiscovery();

        if (!Configuracoes.sensorStatus()) {
            enviarMensagem(Info.Status.DESCONECTADO);
        }
    }

    public void desconectar() {
        Bwt901ble sensor = Configuracoes.getSensor();

        if (sensor != null) {
            sensor.removeRecordObserver(this);
            sensor.close();

            Configuracoes.setSensor(null);
        }

        Configuracoes.setStatus(false);
        enviarMensagem(Info.Status.DESCONECTADO);
    }

    public void iniciarCalibragem() {
        Bwt901ble sensor = Configuracoes.getSensor();

        if (sensor == null) {
            Log.d(TAG, "Impossivel calibrar um sensor não conectado.");
            return;
        }

        sensor.unlockReg();
        sensor.startFieldCalibration();
        enviarMensagem(Info.Status.CALIBRANDO);
    }

    public void pararCalibragem() {
        Bwt901ble sensor = Configuracoes.getSensor();

        if (sensor == null) {
            Log.d(TAG, "Impossivel calibrar um sensor não conectado.");
            return;
        }

        sensor.unlockReg();
        sensor.endFieldCalibration();
        enviarMensagem(Info.Status.COM_CALIBRAGEM);
    }

    public void aplicarCalibragem() {
        Bwt901ble sensor = Configuracoes.getSensor();

        if (sensor == null) {
            Log.d(TAG, "Impossivel calibrar um sensor não conectado.");
            return;
        }

        sensor.unlockReg();
        sensor.appliedCalibration();
        enviarMensagem(Info.Status.CALIBRADO);
    }

    public void enviarMensagem(String status) {
        Intent intent = new Intent();
        intent.setAction(Metodos.actionStatus(status, mContext));
        mContext.sendBroadcast(intent);
    }

    public void enviarDados() {
        Intent intent = new Intent();

    }
}
