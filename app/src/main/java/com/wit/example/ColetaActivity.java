package com.wit.example;

import static com.wit.example.utils.Dados.buildTextViewDeviceData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.wit.example.utils.Dados;
import com.wit.example.utils.Metodos;

public class ColetaActivity extends AppCompatActivity {
    private String TAG = ColetaActivity.class.getSimpleName();
    public TextView textViewDados;
    public DataReceiver dataReceiver;
    public Button buttonColetar;
    public AppCompatButton buttonFOG;
    public static boolean fogApertado;
    public static boolean coletando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coleta);
        handleDataReceiver();

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

        buttonFOG.setBackgroundTintList(ColorStateList.valueOf(R.drawable.round_button_colors));

        fogApertado = false;
        buttonFOG.setOnClickListener((view) -> {
            if (fogApertado) {
                buttonFOG.setPressed(false);
                fogApertado = false;
            } else {
                buttonFOG.setPressed(true);
                fogApertado = true;
            }
        });
    }

    private void handleButtonColetar() {
        if (buttonColetar == null) {
            return;
        }

        coletando = false;

        buttonColetar.setOnClickListener((view) -> {
            if (coletando) {
                coletando = false;
                buttonColetar.setText(getString(R.string.escrever_dados));

                Dados.finalizarColeta();
            } else {
                coletando = true;
                buttonColetar.setText(getString(R.string.parar_escrita));

                Dados.iniciarColeta();
            }
        });
    }

    private void handleTextViewDados() {
        String dados = buildTextViewDeviceData();
        runOnUiThread(() -> {
            textViewDados.setText(dados);
        });
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
                handleTextViewDados();
            }
        }
    }
}