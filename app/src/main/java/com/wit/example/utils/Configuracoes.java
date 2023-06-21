package com.wit.example.utils;

import com.wit.witsdk.modular.sensor.example.ble5.Bwt901ble;

public class Configuracoes {
    private static boolean conectado;
    private static Bwt901ble sensor;

    public static boolean getStatus() {
        return conectado;
    }

    public static void setStatus(boolean conectado) {
        Configuracoes.conectado = conectado;
    }

    public static Bwt901ble getSensor() {
        return sensor;
    }

    public static void setSensor(Bwt901ble sensor) {
        Configuracoes.sensor = sensor;
    }

    public static boolean sensorStatus() {
        return Configuracoes.getStatus() && (Configuracoes.getSensor() != null);
    }
}
