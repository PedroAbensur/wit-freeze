package com.wit.example.utils;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Info {
    public static String NOVOS_DADOS = "NOVOS_DADOS";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            Status.DESCONECTADO,
            Status.CONECTADO,
            Status.BUSCANDO,
            Status.CALIBRADO,
            Status.DESCALIBRADO,
            Status.CALIBRANDO,
            Status.COM_CALIBRAGEM
    })
    public @interface Status {
        String DESCONECTADO = "DESCONECTADO";
        String CONECTADO = "CONECTADO";
        String BUSCANDO = "BUSCANDO";
        String CALIBRADO = "CALIBRADO";
        String DESCALIBRADO =  "DESCALIBRADO";
        String CALIBRANDO = "CALIBRANDO";
        String COM_CALIBRAGEM = "APLICAR CALIBRAGEM";
    }
}
