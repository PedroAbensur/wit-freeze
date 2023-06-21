package com.wit.example.utils;


import android.content.Context;

public class Metodos {
    public static String actionStatus(String status, Context context) {
        return context.getPackageName() + ".status." + status;
    }

    public static String novosDados(Context context) {
        return context.getPackageName() + ".data." + Info.NOVOS_DADOS;
    }
}
