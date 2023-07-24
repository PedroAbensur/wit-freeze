package com.wit.example;

import static com.wit.example.utils.Dados.buildTextViewDeviceData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import com.wit.example.utils.Dados;
import com.wit.example.utils.Metodos;

public class ColetaActivity extends AppCompatActivity {
    private String TAG = ColetaActivity.class.getSimpleName();
    public TextView textViewDados;
    public DataReceiver dataReceiver;
    public Button buttonColetar;
    public static AppCompatButton buttonFOG;
    public static boolean fogApertado;
    public static boolean coletando;

    private CountDownTimer timer;
    private long totalTimeInMillis = 0;
    private boolean isTimerRunning = false;
    private TextView timerTextView;
    private Handler timerHandler = new Handler();
    private long elapsedTimeInMillis = 0;

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
        timerTextView = findViewById(R.id.timerTextView);
    }


    private ColorStateList getRoundButtonColors() {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_pressed }, // pressed
                new int[] {} // default
        };

        int[] colors = new int[] {
                Color.RED, // red color for pressed state
                Color.BLUE // gray color for default state
        };

        return new ColorStateList(states, colors);
    }

    private void handleButtonFog() {
        if (buttonFOG == null) {
            return;
        }

        buttonFOG.setBackgroundTintList(getRoundButtonColors());

        fogApertado = false;
        buttonFOG.setOnClickListener((view) -> {
            if (fogApertado) {
                fogApertado = false;
            } else {
                fogApertado = true;
            }
        });
    }

    private void handleButtonColetar() {
        if (buttonColetar == null) {
            return;
        }

        coletando = false;

        Dados dados = new Dados(this);

        buttonColetar.setOnClickListener((view) -> {
            if (coletando) {
                coletando = false;
                buttonColetar.setText(getString(R.string.escrever_dados));

                dados.finalizarColeta();
                stopTimer();
            } else {
                coletando = true;
                buttonColetar.setText(getString(R.string.parar_escrita));

                dados.iniciarColeta();
                startTimer();
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

    private void startTimer() {
        if (isTimerRunning) {
            return;
        }

        isTimerRunning = true;
        elapsedTimeInMillis = 0; // Reset the elapsed time when starting the timer

        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        if (!isTimerRunning) {
            return;
        }

        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        elapsedTimeInMillis = 0; // Reset the elapsed time when stopping the timer
        updateTimerText(); // Update the timer text immediately after stopping
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedTimeInMillis += 1000;
            updateTimerText();
            timerHandler.postDelayed(this, 1000);
        }
    };

    private void updateTimerText() {
        long hours = elapsedTimeInMillis / 3600000;
        long minutes = (elapsedTimeInMillis % 3600000) / 60000;
        long seconds = (elapsedTimeInMillis % 60000) / 1000;

        String timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        runOnUiThread(() -> timerTextView.setText(timerText));
    }
}