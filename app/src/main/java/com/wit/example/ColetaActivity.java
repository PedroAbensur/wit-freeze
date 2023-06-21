package com.wit.example;

import static com.wit.example.utils.Dados.buildTextViewDeviceData;
import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.wit.example.utils.Bluetooth;
import com.wit.example.utils.Metodos;

public class ColetaActivity extends AppCompatActivity {
    private String TAG = ColetaActivity.class.getSimpleName();
    private Bluetooth bluetooth;
    public TextView textViewDados;
    public boolean lifeCycleTextView;
    public DataReceiver dataReceiver;
    public Button buttonColetar;
    public AppCompatButton buttonFOG;
    public boolean fogApertado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coleta);
        handleDataReceiver();

        bluetooth = new Bluetooth(this);

        textViewDados = (TextView) findViewById(R.id.textViewDados);
        buttonColetar = (Button) findViewById(R.id.buttonColetar);
        buttonFOG = (AppCompatButton) findViewById(R.id.buttonFOG);

        handleTextViewDados();
        handleButtonColetar();
        handleButtonFog();
    }

    private void handleButtonFog() {
        if (buttonFOG == null) {
            return;
        }

        fogApertado = false;
        buttonFOG.setOnClickListener((view) -> {
            if (fogApertado) {

            } else {

            }
        });
    }

    private void handleButtonColetar() {
        if (buttonColetar == null) {
            return;
        }

        buttonColetar.setOnClickListener((view) -> {

        });
    }

    private void handleTextViewDados() {
        lifeCycleTextView = true;

        Thread textViewThread = new Thread(() -> {
            while (lifeCycleTextView) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    Log.e(TAG, "handleTextViewDados: " + e);
                }

                String dados = buildTextViewDeviceData();
                runOnUiThread(() -> {
                    textViewDados.setText(dados);
                });
            }
        });

        textViewThread.start();
    }

    private void handleDataReceiver() {
        dataReceiver =  new DataReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Metodos.novosDados(this));
        registerReceiver(dataReceiver, intentFilter);
    }

    public class DataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Metodos.novosDados(context))) {

            }
        }
    }
}