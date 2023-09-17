package com.wit.example.utils;

import static com.wit.example.ColetaActivity.coletando;
import static java.lang.Thread.sleep;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

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
import java.util.LinkedList;

public class Dados {
    private static final String TAG = Dados.class.getSimpleName();

    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd-MM-YYYY HH:mm:ss:SSS");
    private static final SimpleDateFormat dateFormatFile =
            new SimpleDateFormat("dd-MM-YYYY_(HH.mm.ss.SSS)");

    private static final String ILLEGAL_CHARACTERS =
            "@\"^[\\w\\-. ]+$\";\n";

    public static String nomeArquivo;

    public static LinkedList<String> dadosEscrita;

    public static int totalSeconds;

    private static Context mContext;

    private static Boolean perguntando;



    public Dados(Context context) {
        this.nomeArquivo = "";
        this.mContext = context;
        this.perguntando = false;
        this.dadosEscrita = new LinkedList<>();
    }

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
        data += terminaLinha("Bebendo Água: " + ColetaActivity.drinking);
        // data += terminaLinha(r.getString(R.string.t) + sensorDadoT(WitSensorKey.T));A
        // data += terminaLinha(r.getString(R.string.p) + sensorDadoT(WitSensorKey.ElectricQuantityPercentage));
        // data += terminaLinha(r.getString(R.string.versionNumber) + sensorDadoT(WitSensorKey.VersionNumber));

        return data;
    }

    public static String buildSensorDataTable() {
        StringBuilder table = new StringBuilder();

        table.append("time,")
                .append("accX,")
                .append("accY,")
                .append("accZ,")
                .append("asX,")
                .append("asY,")
                .append("asZ,")
                .append("angleX,")
                .append("angleY,")
                .append("angleZ,")
                .append("hX,")
                .append("hY,")
                .append("hZ,")
                .append("tag\n");

        return table.toString();
    }

    private static String finalizaLinha(String s) {
        return s + "\",\"";
    }

    private static String sensorDadoF(String key) {
        return Configuracoes.getSensor().getDeviceData(key);
    }

    public static String buildSensorData() {
        if (Configuracoes.getSensor() == null) {
            return null;
        }

        StringBuilder data = new StringBuilder();

        Date date = new Date();
        String stringDate = dateFormat.format(date);

        data.append(stringDate).append(",\"")
                .append(finalizaLinha(sensorDadoF(WitSensorKey.AccX)))
                .append(finalizaLinha(sensorDadoF(WitSensorKey.AccY)))
                .append(finalizaLinha(sensorDadoF(WitSensorKey.AccZ)))
                .append(finalizaLinha(sensorDadoF(WitSensorKey.AsX)))
                .append(finalizaLinha(sensorDadoF(WitSensorKey.AsY)))
                .append(finalizaLinha(sensorDadoF(WitSensorKey.AsZ)))
                .append(finalizaLinha(sensorDadoF(WitSensorKey.AngleX)))
                .append(finalizaLinha(sensorDadoF(WitSensorKey.AngleY)))
                .append(finalizaLinha(sensorDadoF(WitSensorKey.AngleZ)))
                .append(finalizaLinha(sensorDadoF(WitSensorKey.HX)))
                .append(finalizaLinha(sensorDadoF(WitSensorKey.HY)))
                .append(sensorDadoF(WitSensorKey.HZ)).append("\",")
                .append(ColetaActivity.drinking? "drinking" : "0").append("\n");

        return data.toString();
    }
    public static void iniciarColeta() {
        final Thread thread = new Thread(() -> {
            dadosEscrita.clear();

            while (coletando) {
                dadosEscrita.add(buildSensorData());


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
        gerarNomeArquivo();
        final Thread thread = new Thread(() -> {
            while (perguntando) {
                // Do nothing =P
            }
            gerarArquivoDados(nomeArquivo, true);
        });
        thread.start();
    }

    private static void gerarArquivoDados(String fileName, boolean deleteOld) {
        String path = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                .toString();
        File file =
                new File(path, fileName);
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

            for (String linha: dadosEscrita){
                bufferedWriter.write(linha);
            }
            bufferedWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while writing to the file: " + e);
        }
    }

    private static String gerarNomeComData() {
        final Date date = new Date();
        final String dateString = dateFormatFile.format(date);
        return "output-" + dateString + ".csv";
    }

    private static void gerarNomeArquivo() {
        perguntando = true;

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Escolha o nome do arquivo:");

        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Aceitar", (dialogInterface, i) -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                if (!text.matches(ILLEGAL_CHARACTERS)) {
                    nomeArquivo = text + ".csv";
                    perguntando = false;
                    return;
                }
            }
            nomeArquivo = gerarNomeComData();
            perguntando = false;
        });

        builder.setNegativeButton("Cancelar", (dialogInterface, i) -> {
            nomeArquivo = gerarNomeComData();
            perguntando = false;
        });

        builder.show();
    }
}
