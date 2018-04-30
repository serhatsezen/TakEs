package app.tez.com.takes.block.TCPSERVERCLIENT;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import app.tez.com.takes.block.Models.DeviceDTO;

import static android.content.ContentValues.TAG;

/**
 * Created by serhat on 28.04.2018.
 */

public class CihazlarServis extends Service {

    public static ArrayList<DeviceDTO> cihazlar = new ArrayList<DeviceDTO>();


    static final int SocketServerPORT = 8080;
    String ipadresi,arananipadresleri,olusanIpAdresi;

    public static final String CIHAZLAR = "cihazlarshared";
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart(Intent intent, int startId)
    {


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(CihazlarServis.this);
        editor = sharedPrefs.edit();

        if (sharedPrefs.getString("firstTime","firstTime") == "firstTime") {
            getIpAddress();
        } else {
            ipadresi = sharedPrefs.getString("ipadresiSharedPrefences","192.168.1.0");
            Toast.makeText(CihazlarServis.this,ipadresi + " kullanici", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

        }



        String[] parts = ipadresi.split("\\.");
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        arananipadresleri = part1 + "." + part2 + "." + part3 + ".";

        Toast.makeText(CihazlarServis.this,arananipadresleri, Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

        ClientRxThread clientRxThread =
                new ClientRxThread(
                        arananipadresleri,
                        SocketServerPORT);

        clientRxThread.start();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        Toast.makeText(CihazlarServis.this,"CihazlarServis destroy", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

    }

    private class ClientRxThread extends Thread {
        String dstAddress;
        int dstPort;

        ClientRxThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;
            for (int i = 18; i <= 255; i++) {
                olusanIpAdresi = arananipadresleri + i;
                try {
                    if ((!ipadresi.equals(olusanIpAdresi))) {
                        socket = new Socket(olusanIpAdresi, dstPort);

                        DeviceDTO deviceDTO = new DeviceDTO();
                        deviceDTO.setIp(olusanIpAdresi);
                        if (!cihazlar.contains(deviceDTO)) {
                            cihazlar.add(deviceDTO);
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CihazlarServis.this, "Başarılı" + olusanIpAdresi, Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
                            }
                        });

                        saveArrayList(cihazlar, CIHAZLAR);
                        socket.close();

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CihazlarServis.this,
                                        "Finished",
                                        Toast.LENGTH_LONG).show();
                            }
                        });


                    }
                } catch (IOException e) {

                    e.printStackTrace();

                    final String eMsg = "Something wrong: " + e.getMessage();
                    final int finalI = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (finalI == 5 || finalI == 50 || finalI == 100 || finalI == 150 || finalI == 200 || finalI == 255) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(CihazlarServis.this,
                                                eMsg,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                        }
                    });


                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void saveArrayList(ArrayList<DeviceDTO> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CihazlarServis.this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }
    private String getIpAddress() {
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ipadresi += inetAddress.getHostAddress();
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ipadresi += "Something Wrong! " + e.toString() + "\n";
        }

        if (ipadresi.contains("null")) {
            String[] nummcikart = ipadresi.split("null");
            ipadresi = nummcikart[1];
            Toast.makeText(CihazlarServis.this,
                    ipadresi+ "  Null YOK",
                    Toast.LENGTH_LONG).show();
        }
        editor.putString("ipadresiSharedPrefences", ipadresi);
        editor.putString("firstTime", "2oldu");
        editor.commit();

        return ipadresi;
    }
}
