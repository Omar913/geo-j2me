package com.cronos.geo;

/**
 * Google digital maps processor API
 * API para el proceso de Mapas digitales de Google
 *
 *  Creada por Román A. Sarria
 *          By Cronos
 *
 * Este API esta protegida por Creative Commons 2.5 para Argentina
 * y 3.0 para el resto del mundo.
 *
 * ------------------------------------------------
 * Rutinas para el procesado de Marcadores en mapas
 * ------------------------------------------------
 **/

public class gMarker {
    double mLat, mLng;
    String color;
    char mark;

    public static String RED = "red";
    public static String BLUE = "blue";
    public static String GREEN = "green";


    /**
     * Constructor para crear un marcador
     *
     * @param lat - Latitud
     * @param lng - Longitud
     * @param c - Color
     * @param m - Letra para marcado
     */
    public gMarker( double lat, double lng, String c, char m ) {
        mLat = lat;
        mLng = lng;

        mark = m;
        color = c;
    }

    /**
     * Devuelve el marcador
     *
     * @return - Devuelve la cadena necesaria para crear un marcador
     */
    public String getMarker() {
        return mLat + "," + mLng + "," + color + mark;
    }
}
