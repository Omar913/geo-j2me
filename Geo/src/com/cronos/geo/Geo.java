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
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Image;

public class Geo {

    private static final String URL_UNRESERVED =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789-_.~";
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    // these 2 properties will be used with map scrolling methods. You can remove them if not needed
    public static final int offset = 268435456;
    public static final double radius = offset / Math.PI;
    private String apiKey = null;

    private Vector markers = new Vector();

    private String mapType = "";

    /**
     *  Mapa callejero
     */
    public static String NORMAL = "normal";

    /**
     * Mapa satelital
     */
    public static String SATELLITE = "satellite";

    /**
     * Mapa hibrido entre callejero y satelital
     */
    public static String HYBRID = "hybrid";

    /**
     * Constructor Principal
     *
     * @param key - Recibe la clave para la utilización de los mapas de google
     */
    public Geo(String key) {
        apiKey = key;
    }

    /**
     * Constructor para inidicar el tipo de Mapa
     *  - Callejero
     *  - Satelite
     *  - Hibrido
     *
     * @param key - Clave de desarrollador Google Maps
     * @param type - Tipo de mapa
     */
    public Geo(String key, String type) {
        apiKey = key;
        mapType = "&maptype=" + type;
    }


    /**
     * Obtiene una coordenada de acuerdo a la direccion dada
     *
     * @param address - Dirección requerida
     * @return - Devuelve las coordenadas
     * @throws java.lang.Exception
     */
    public double[] geocodeAddress(String address) throws Exception {
        byte[] res = loadHttpFile(getGeocodeUrl(address));
        String[] data = split(new String(res, 0, res.length), ',');

        if (data[0].compareTo("200") != 0) {
            int errorCode = Integer.parseInt(data[0]);
            throw new Exception("Google Maps Exception: " + getGeocodeError(errorCode));
        }

        return new double[]{
                    Double.parseDouble(data[2]), Double.parseDouble(data[3])
                };
    }

    /**
     * Devuelve la imagen del Mapa
     *
     * @param width - Ancho de la imagen
     * @param height - Alto de la imagen
     * @param lat - Latitud
     * @param lng - Longitud
     * @param zoom - Nivel de Zoom ( 1 - 16 )
     * @param format - Formato de la imagen
     *                  + PNG 32 - 16 - 8
     *                  + GIF
     *                  + JPG
     * @param mark - Marca si dibuja o no los marcadores en el mapa
     * @return
     * @throws java.io.IOException
     */
    public Image retrieveStaticImage(int width, int height, double lat, double lng, int zoom,
            String format, boolean mark) throws IOException {
        byte[] imageData = loadHttpFile(getMapUrl(width, height, lng, lat, zoom, format));

        return Image.createImage(imageData, 0, imageData.length);
    }

    private static String getGeocodeError(int errorCode) {
        switch (errorCode) {
            case 400:
                return "Bad request";
            case 500:
                return "Server error";
            case 601:
                return "Missing query";
            case 602:
                return "Unknown address";
            case 603:
                return "Unavailable address";
            case 604:
                return "Unknown directions";
            case 610:
                return "Bad API key";
            case 620:
                return "Too many queries";
            default:
                return "Generic error";
        }
    }

    private String getGeocodeUrl(String address) {
        return "http://maps.google.com/maps/geo?q=" + urlEncode(address) + "&output=csv&key=" + apiKey;
    }

    private String getMapUrl(int width, int height, double lng, double lat, int zoom, String format) {
        if( markers.size() == 0 ) return "http://maps.google.com/staticmap?center=" + lat + "," + lng + "&format=" + format + "&zoom=" + zoom + "&size=" + width + "x" + height + mapType + "&key=" + apiKey;
        else return "http://maps.google.com/staticmap?center=" + lat + "," + lng + this.parseMarkers() + "&format=" + format + "&zoom=" + zoom + "&size=" + width + "x" + height + mapType + "&key=" + apiKey;

    }

    private static String urlEncode(String str) {
        StringBuffer buf = new StringBuffer();
        byte[] bytes = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeUTF(str);
            bytes = bos.toByteArray();
        } catch (IOException e) {
            // ignore
        }
        for (int i = 2; i < bytes.length; i++) {
            byte b = bytes[i];
            if (URL_UNRESERVED.indexOf(b) >= 0) {
                buf.append((char) b);
            } else {
                buf.append('%').append(HEX[(b >> 4) & 0x0f]).append(HEX[b & 0x0f]);
            }
        }
        return buf.toString();
    }

    private static byte[] loadHttpFile(String url) throws IOException {
        byte[] byteBuffer;

        HttpConnection hc = (HttpConnection) Connector.open(url);
        try {
            hc.setRequestMethod(HttpConnection.GET);
            InputStream is = hc.openInputStream();
            try {
                int len = (int) hc.getLength();
                if (len > 0) {
                    byteBuffer = new byte[len];
                    int done = 0;
                    while (done < len) {
                        done += is.read(byteBuffer, done, len - done);
                    }
                } else {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[512];
                    int count;
                    while ((count = is.read(buffer)) >= 0) {
                        bos.write(buffer, 0, count);
                    }
                    byteBuffer = bos.toByteArray();
                }
            } finally {
                is.close();
            }
        } finally {
            hc.close();
        }

        return byteBuffer;
    }

    private static String[] split(String s, int chr) {
        Vector res = new Vector();

        int curr;
        int prev = 0;

        while ((curr = s.indexOf(chr, prev)) >= 0) {
            res.addElement(s.substring(prev, curr));
            prev = curr + 1;
        }
        res.addElement(s.substring(prev));

        String[] splitted = new String[res.size()];
        res.copyInto(splitted);

        return splitted;
    }

    public double[] adjust(double lat, double lng, int deltaX, int deltaY, int z) {
        return new double[]{
                    XToL(LToX(lng) + (deltaX << (21 - z))),
                    YToL(LToY(lat) + (deltaY << (21 - z)))
                };
    }

    double LToX(double x) {
        return round(offset + radius * x * Math.PI / 180);
    }

    double LToY(double y) {
        return round(
                offset - radius *
                Double.longBitsToDouble(com.cronos.net.dclausen.microfloat.MicroDouble.log(
                Double.doubleToLongBits(
                (1 + Math.sin(y * Math.PI / 180)) /
                (1 - Math.sin(y * Math.PI / 180))))) / 2);
    }

    double XToL(double x) {
        return ((round(x) - offset) / radius) * 180 / Math.PI;
    }

    double YToL(double y) {
        return (Math.PI / 2 - 2 * Double.longBitsToDouble(
                com.cronos.net.dclausen.microfloat.MicroDouble.atan(
                com.cronos.net.dclausen.microfloat.MicroDouble.exp(Double.doubleToLongBits((round(y) - offset) / radius))))) * 180 / Math.PI;
    }

    double round(double num) {
        double floor = Math.floor(num);

        if (num - floor >= 0.5) {
            return Math.ceil(num);
        } else {
            return floor;
        }
    }

    /**
     * Cambiar tipo de mapa
     *
     * @param type - Tipo de Mapa
     */
    public void setMapType(String type){
        if(type.equals("")) mapType = "";
        else mapType = "&maptype=" + type;
    }

    private String parseMarkers() {
        String m = "&markers=";

        for( int i = 0 ; i <= ( markers.size() - 1 ) ; i++ ) {
            m += ( (gMarker)markers.elementAt(i) ).getMarker();

            if( i != ( markers.size() - 1 ) ) m += "|";
        }

        return m;
    }

    /**
     * Agrega un Marcardor al mapa
     *
     * @param lat - Latitud
     * @param lng - Longitud
     * @param c - Color
     *              + gMarker.RED
     *              + gMarker.BLUE
     *              + gMarker.GREEN
     * @param m - Una letra para identificarla
     */
    public void addMarker(double lat, double lng, String c, char m) {
        markers.addElement( new gMarker(lat, lng, c, m) );
    }
}
