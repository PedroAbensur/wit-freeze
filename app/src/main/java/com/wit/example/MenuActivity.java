package com.wit.example;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wit.example.utils.Configuracoes;

public class MenuActivity extends AppCompatActivity {
    public Button buttonColetar;
    public Button buttonConfigurar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Configuracoes.setSensor(null);
        Configuracoes.setStatus(false);

        buttonColetar = (Button) findViewById(R.id.buttonColetar);
        buttonConfigurar = (Button) findViewById(R.id.buttonConfigurar);

        buttonColetar.setOnClickListener(view -> {
            irParaActivity(ColetaActivity.class);
        });

        buttonConfigurar.setOnClickListener(view -> {
            irParaActivity(ConfigurarActivity.class);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        buttonColetar.setEnabled(Configuracoes.sensorStatus());
    }

    private void irParaActivity(Class activity) {
        Intent intent = new Intent(MenuActivity.this, activity);
        startActivity(intent);
    }
}