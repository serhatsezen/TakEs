package app.tez.com.takes.block.TCPSERVERCLIENT;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import app.tez.com.takes.block.Models.DeviceDTO;

/**
 * Created by serhat on 3.05.2018.
 */

public class PostGonderServis extends Service {
    Intent intent;
    public static final String BROADCAST_ACTION = "BroadCastAction";
    static final int SocketServerPORT = 8080;

    SharedPreferences sharedPrefs;
    public static final String CIHAZLAR = "cihazlarshared";

    String postBilgileri, socketIp, ip;

    ClientRxThread clientRxThread;
    public boolean postEkleServisiBasladi = false;

    @Nullable
    @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            postBilgileri = intent.getStringExtra("postBilgileri");
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(PostGonderServis.this);

            ip = sharedPrefs.getString("ipadresiSharedPrefences", "192.168.1.0");

            String[] parts = ip.split("\\.");
            String part1 = parts[0];
            String part2 = parts[1];
            String part3 = parts[2];

            socketIp = part1 + "." + part2 + "." + part3 + ".";

            clientRxThread =
                    new ClientRxThread(
                            socketIp,
                            SocketServerPORT);

            clientRxThread.start();

//            new KayitRun().execute();

        } catch (Exception e) {
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart(Intent intent, int startId) {


    }

    public class ClientRxThread extends Thread {
        String dstAddress;
        int dstPort;

        public ClientRxThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {

                ArrayList<DeviceDTO> cihazlar = new ArrayList<DeviceDTO>();
                Socket s = null;
                Gson gson = new Gson();
                String json = sharedPrefs.getString(CIHAZLAR, null);
                Type type = new TypeToken<ArrayList<DeviceDTO>>() {
                }.getType();
                cihazlar = gson.fromJson(json, type);


                if (cihazlar.size() > 0) {
                    for (int i = 0; i < cihazlar.size(); i++) {
                        try {
                            socketIp = String.valueOf(cihazlar.get(i).getIp());
                            if (!socketIp.equals(ip)) {
                                s = new Socket(socketIp, SocketServerPORT);

                                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                                //send output msg
                                out.write("postEkle/////" + postBilgileri);
                                out.flush();
                                //accept server response
                                out.close();
                                //close connection
                                s.close();
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PostGonderServis.this,
                                    "cihaz yok",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                clientRxThread.interrupt();
            }

        }
    }
}
