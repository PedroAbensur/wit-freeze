package com.wit.example.utils;

import static com.wit.example.ColetaActivity.coletando;
import static java.lang.Thread.sleep;

import android.content.res.Resources;
import android.util.Log;

import com.wit.example.App;
import com.wit.example.ColetaActivity;
import com.wit.example.R;
import com.wit.witsdk.modular.sensor.modular.processor.constant.WitSensorKey;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Dados {
    private static final String TAG = Dados.class.getSimpleName();

    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd-MM-YYYY HH:mm:ss:SSS");
    private static final SimpleDateFormat dateFormatFile =
            new SimpleDateFormat("dd-MM-YYYY_(HH.mm.ss.SSS)");
    public static String dadosEscrita;


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
        // data += terminaLinha(r.getString(R.string.t) + sensorDadoT(WitSensorKey.T));
        // data += terminaLinha(r.getString(R.string.p) + sensorDadoT(WitSensorKey.ElectricQuantityPercentage));
        // data += terminaLinha(r.getString(R.string.versionNumber) + sensorDadoT(WitSensorKey.VersionNumber));

        return data;
    }

    public static String buildSensorDataTable() {
        String table = "";

        table += "time,";
        table += "accX,";
        table += "accZ,";
        table += "asX,";
        table += "asY,";
        table += "asZ,";
        table += "angleX,";
        table += "angleY,";
        table += "angleZ,";
        table += "hX,";
        table += "hY,";
        table += "hZ,";
        table += "tag\n";

        return table;
    }

    private static String finalizaLinha(String s) {
        return s += "\",\"";
    }

    private static String sensorDadoF(String key) {
        return Configuracoes.getSensor().getDeviceData(key);
    }

    public static String buildSensorData() {
        if (Configuracoes.getSensor() == null) {
            return null;
        }

        String data = "";

        Date date = new Date();
        String stringDate = dateFormat.format(date);

        data += date.toString() + ",\"";
        data += finalizaLinha(sensorDadoF(WitSensorKey.AccX));
        data += finalizaLinha(sensorDadoF(WitSensorKey.AccY));
        data += finalizaLinha(sensorDadoF(WitSensorKey.AccZ));
        data += finalizaLinha(sensorDadoF(WitSensorKey.AsX));
        data += finalizaLinha(sensorDadoF(WitSensorKey.AsY));
        data += finalizaLinha(sensorDadoF(WitSensorKey.AsZ));
        data += finalizaLinha(sensorDadoF(WitSensorKey.AngleX));
        data += finalizaLinha(sensorDadoF(WitSensorKey.AngleY));
        data += finalizaLinha(sensorDadoF(WitSensorKey.AngleZ));
        data += finalizaLinha(sensorDadoF(WitSensorKey.HX));
        data += finalizaLinha(sensorDadoF(WitSensorKey.HY));
        data += sensorDadoF(WitSensorKey.HZ) + "\",";
        data += (ColetaActivity.fogApertado ? "1" : "0") + "\n";

        return data;
    }

    public static void iniciarColeta() {
        final Thread thread = new Thread(() -> {
            dadosEscrita = "";

            while (coletando) {
                dadosEscrita += Dados.buildSensorData();
                try {
                    sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    public static void finalizarColeta() {
        final Thread thread = new Thread(() -> {
            Date date = new Date();
            String dateString = dateFormatFile.format(date);
            gerarArquivoDados("output-" + dateString + ".csv", true);
        });
        thread.start();
    }

    private static void gerarArquivoDados(String fileName, boolean deleteOld) {
        File file = new File(App.getContext().getExternalFilesDir(null), fileName);
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
            bufferedWriter.write(dadosEscrita);

            bufferedWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while writing to the file: " + e);
        }
    }
}
