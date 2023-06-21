package com.wit.example.utils;

import android.content.res.Resources;

import com.wit.example.App;
import com.wit.example.R;
import com.wit.witsdk.modular.sensor.example.ble5.Bwt901ble;
import com.wit.witsdk.modular.sensor.modular.processor.constant.WitSensorKey;

import java.util.Calendar;
import java.util.Date;

public class Dados {
    private static String terminaLinha(String s) {
        return s += "\n";
    }

    private static String sensorDadoT(String key) {
        return ": " + Configuracoes.getSensor().getDeviceData(key);
    }
    public static String buildTextViewDeviceData() {
        Resources r = App.getContext().getResources();

        if (Configuracoes.getSensor() == null || r == null ) {
            return null;
        }

        String data = "";

        Date currentTime = Calendar.getInstance().getTime();

        data += terminaLinha(currentTime.toString());
        // data += terminaLinha(sensor.getDeviceName());
        data += terminaLinha(r.getString(R.string.accX) + sensorDadoT(WitSensorKey.AccX) +"g");
        data += terminaLinha(r.getString(R.string.accY) + sensorDadoT(WitSensorKey.AccY) +"g");
        data += terminaLinha(r.getString(R.string.accZ) + sensorDadoT(WitSensorKey.AccZ) +"g");
        data += terminaLinha(r.getString(R.string.asX) + sensorDadoT(WitSensorKey.AsX) +"°/s");
        data += terminaLinha(r.getString(R.string.asY) + sensorDadoT(WitSensorKey.AsY) +"°/s");
        data += terminaLinha(r.getString(R.string.asZ) + sensorDadoT(WitSensorKey.AsZ) +"°/s");
        data += terminaLinha(r.getString(R.string.angleX) + sensorDadoT(WitSensorKey.AngleX) +"°");
        data += terminaLinha(r.getString(R.string.angleY) + sensorDadoT(WitSensorKey.AngleY) +"°");
        data += terminaLinha(r.getString(R.string.angleZ) + sensorDadoT(WitSensorKey.AngleZ) +"°");
        data += terminaLinha(r.getString(R.string.hX) + sensorDadoT(WitSensorKey.HX));
        data += terminaLinha(r.getString(R.string.hY) + sensorDadoT(WitSensorKey.HY));
        data += terminaLinha(r.getString(R.string.hZ) + sensorDadoT(WitSensorKey.HZ));
        data += terminaLinha(r.getString(R.string.hZ) + sensorDadoT(WitSensorKey.HZ));
        data += terminaLinha(r.getString(R.string.hZ) + sensorDadoT(WitSensorKey.HZ));
        // data += terminaLinha(r.getString(R.string.t) + sensorDadoT(WitSensorKey.T));
        // data += terminaLinha(r.getString(R.string.p) + sensorDadoT(WitSensorKey.ElectricQuantityPercentage));
        // data += terminaLinha(r.getString(R.string.versionNumber) + sensorDadoT(WitSensorKey.VersionNumber));

        return data;
    }
}
