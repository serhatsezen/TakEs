package app.tez.com.takes.block.TCPSERVERCLIENT;

/**
 * Created by serhat on 23.04.2018.
 */

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import app.tez.com.takes.block.Models.DeviceDTO;

public class DosyaGonderService extends Service {
    Intent intent;
    public static final String BROADCAST_ACTION = "Hello World";
    static final int SocketServerPORT = 8080;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name

    JSONArray veritabani = new JSONArray();
    JSONObject girisKontrolNesne;


    String gonderilecekDosya, socketIp, kendiIpAdresi, veritabanindanIpAdresi;

    SharedPreferences sharedPrefs;
    public static final String CIHAZLAR = "cihazlarshared";

    @Nullable
    @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);
        try {
            if (intent.getStringExtra("benHashiCozdum") != null) {
                gonderilecekDosya = intent.getStringExtra("benHashiCozdum");
            } else {
            }

        } catch (Exception e) {
        }
        try {
            if (intent.getStringExtra("benHashiCozdumPost") != null) {
                gonderilecekDosya = intent.getStringExtra("benHashiCozdumPost");
            }
        } catch (Exception e) {
        }
        try {
            if (intent.getStringExtra("dosya") != null) {
                gonderilecekDosya = intent.getStringExtra("dosya");
            } else {
            }
        } catch (Exception e) {
        }


        if (gonderilecekDosya == null) {
            gonderilecekDosya = "";
        }

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(DosyaGonderService.this);

        kendiIpAdresi = sharedPrefs.getString("ipadresiSharedPrefences", "192.168.1.0");

        String[] parts = kendiIpAdresi.split("\\.");
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        socketIp = part1 + "." + part2 + "." + part3 + ".";

        ClientRxThread clientRxThread =
                new ClientRxThread(
                        socketIp,
                        SocketServerPORT);
        clientRxThread.start();

        return super.onStartCommand(intent, flags, startId);
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

    //---------- JSON Dosyasındaki verileri burada uygulamaya yüklüyoruz.
    public void veritabaniYukle() {
        File dosya = new File(fileDirectory.getAbsolutePath() + "/" + FileName);
        try {
            if (!dosya.exists()) {
                FileOutputStream fileOutputStream = new FileOutputStream(dosya);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                outputStreamWriter.write(veritabani.toString());
                outputStreamWriter.close();
                fileOutputStream.close();
            } else {
                FileInputStream fileInputStream = new FileInputStream(dosya);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                //--- Dosyadaki verileri bir JSON Nesnesine aktarıyoruz.
                veritabani = new JSONArray(bufferedReader.readLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            ArrayList<DeviceDTO> cihazlar = new ArrayList<DeviceDTO>();
            Socket s = null;
            Gson gson = new Gson();
            String json = sharedPrefs.getString(CIHAZLAR, null);
            Type type = new TypeToken<ArrayList<DeviceDTO>>() {
            }.getType();
            cihazlar = gson.fromJson(json, type);


            for (int sira = 0; sira < veritabani.length(); sira++) {                                       //veritabanindaki ip adresini kontrol ediyoruz
                try {                                                                               //cihazlar listesinde yoksa ekliyoruz.
                    girisKontrolNesne = veritabani.getJSONObject(sira);
                    veritabanindanIpAdresi = girisKontrolNesne.getString("ipaddres");

                    if (!cihazlar.contains(veritabanindanIpAdresi)) {
                        DeviceDTO veritabanindanIp = new DeviceDTO();
                        veritabanindanIp.setIp(veritabanindanIpAdresi);
                        cihazlar.add(veritabanindanIp);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DosyaGonderService.this,
                                        "cihazEklendi/////" + veritabanindanIpAdresi,
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


            if (cihazlar.size() > 0) {
                for (int i = 0; i < cihazlar.size(); i++) {
                    try {
                        socketIp = String.valueOf(cihazlar.get(i).getIp());

                        if (!socketIp.equals(kendiIpAdresi)) {

                            s = new Socket(socketIp, SocketServerPORT);

                            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                            //send output msg
                            if (gonderilecekDosya.contains("benHashiCozdum")) {
                                out.write(gonderilecekDosya);
                            } else if (gonderilecekDosya.contains("benHashiCozdumPost")) {
                                out.write(gonderilecekDosya);
                            } else if (gonderilecekDosya.contains("dosya")) {
                                out.write(gonderilecekDosya);
                            }
                            out.flush();
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
                        Toast.makeText(DosyaGonderService.this,
                                "cihaz yok",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }


        }
    }

    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");

    }

}