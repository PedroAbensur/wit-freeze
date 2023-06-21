package com.wit.example;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.wit.example.utils.Bluetooth;
import com.wit.example.utils.Configuracoes;
import com.wit.example.utils.Info;
import com.wit.example.utils.Metodos;

public class ConfigurarActivity extends AppCompatActivity {
    private String TAG = ConfigurarActivity.class.getSimpleName();
    private Bluetooth bluetooth;
    public Button buttonManageConexao;
    public Button buttonManageCalibracao;
    public StatusReceiver statusReceiver;
    private String statusAtual;
    private String statusCalibragem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar);
        handleStatusReceiver();

        bluetooth = new Bluetooth(this);

        buttonManageConexao = findViewById(R.id.buttonManageConexao);
        buttonManageCalibracao = findViewById(R.id.buttonManageCalibracao);

        statusAtual =
                Configuracoes.sensorStatus() ? Info.Status.CONECTADO : Info.Status.DESCONECTADO;
        buttonManageConexao.setOnClickListener(view -> {
            handleConexao();
        });

        statusCalibragem = Info.Status.DESCALIBRADO;
        buttonManageCalibracao.setOnClickListener(view -> {
            handleCalibragem();
        });
    }

    /*
    Para melhorar os casos de desincronia de status de conexão do sensor, toda vez que sairmos dessa
    tela ela vai ser automaticamente destruida.
     */
    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    private void handleConexao() {
        switch (statusAtual) {
            case (Info.Status.DESCONECTADO):
                bluetooth.iniciarProcura();
                break;
            case (Info.Status.CONECTADO):
                bluetooth.desconectar();
                break;
            case (Info.Status.BUSCANDO):
                bluetooth.pararProcura();
                break;
        }
    }

    private void handleConexaoText() {
        switch (statusAtual) {
            case (Info.Status.DESCONECTADO):
                buttonManageConexao.setText(R.string.conectar);
                break;
            case (Info.Status.CONECTADO):
                buttonManageConexao.setText(R.string.desconectar);
                break;
            case (Info.Status.BUSCANDO):
                buttonManageConexao.setText(R.string.buscando);
                break;
        }
    }

    private void handleCalibragem() {
        switch (statusCalibragem) {
            case (Info.Status.CALIBRANDO):
                bluetooth.pararCalibragem();
                break;
            case (Info.Status.COM_CALIBRAGEM):
                bluetooth.aplicarCalibragem();
                break;
            default:
                bluetooth.iniciarCalibragem();
                break;
        }
    }

    private void handleCalibragemText() {
        switch (statusCalibragem) {
            case (Info.Status.DESCALIBRADO):
                buttonManageCalibracao.setText(getString(R.string.Start_field_calibration));
                break;
            case (Info.Status.CALIBRADO):
                buttonManageCalibracao.setText(R.string.calibrado);
                break;
            case (Info.Status.CALIBRANDO):
                buttonManageCalibracao.setText(R.string.calibrando);
                break;
            case (Info.Status.COM_CALIBRAGEM):
                buttonManageCalibracao.setText(getString(R.string.applied_calibration));
                break;
        }
    }

    private void handleStatusReceiver() {
        statusReceiver = new StatusReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Metodos.actionStatus(Info.Status.DESCONECTADO, this));
        intentFilter.addAction(Metodos.actionStatus(Info.Status.CONECTADO, this));
        intentFilter.addAction(Metodos.actionStatus(Info.Status.BUSCANDO, this));
        intentFilter.addAction(Metodos.actionStatus(Info.Status.DESCALIBRADO, this));
        intentFilter.addAction(Metodos.actionStatus(Info.Status.CALIBRADO, this));
        intentFilter.addAction(Metodos.actionStatus(Info.Status.CALIBRANDO, this));
        intentFilter.addAction(Metodos.actionStatus(Info.Status.COM_CALIBRAGEM, this));

        registerReceiver(statusReceiver, intentFilter);
    }

    public class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Metodos.actionStatus(Info.Status.DESCONECTADO, context))) {
                statusAtual = Info.Status.DESCONECTADO;
                Toast.makeText(context, getString(R.string.desconectado), Toast.LENGTH_SHORT).show();
            } else if (action.equals(Metodos.actionStatus(Info.Status.CONECTADO, context))) {
                statusAtual = Info.Status.CONECTADO;
                Toast.makeText(context, getString(R.string.conectado), Toast.LENGTH_SHORT).show();
            } else if (action.equals(Metodos.actionStatus(Info.Status.BUSCANDO, context))) {
                Toast.makeText(context, getString(R.string.buscando), Toast.LENGTH_SHORT).show();
                statusAtual = Info.Status.BUSCANDO;
            }

            if (action.equals(Metodos.actionStatus(Info.Status.DESCALIBRADO, context))) {
                statusCalibragem = Info.Status.DESCALIBRADO;
                Toast.makeText(context, getString(R.string.Start_field_calibration), Toast.LENGTH_SHORT).show();
            } else if (action.equals(Metodos.actionStatus(Info.Status.CALIBRADO, context))) {
                statusCalibragem = Info.Status.CALIBRADO;
                Toast.makeText(context, getString(R.string.calibrado), Toast.LENGTH_SHORT).show();
            } else if (action.equals(Metodos.actionStatus(Info.Status.CALIBRANDO, context))) {
                statusCalibragem = Info.Status.CALIBRANDO;
                Toast.makeText(context, getString(R.string.calibrando), Toast.LENGTH_SHORT).show();
            } else if (action.equals(Metodos.actionStatus(Info.Status.COM_CALIBRAGEM, context))) {
                statusCalibragem = Info.Status.COM_CALIBRAGEM;
                Toast.makeText(context, getString(R.string.applied_calibration), Toast.LENGTH_SHORT).show();
            }

            // Atualiza texto do botão automaticamente, sempre que o estado muda.
            // Pode quebrar se o dispositivo desconectar enquanto estiver fora dessa tela.
            handleConexaoText();
            handleCalibragemText();

            Log.d(TAG, "statusCalibragem = " + statusCalibragem);
            Log.d(TAG, "statusAtual = " + statusAtual);
        }
    }
}