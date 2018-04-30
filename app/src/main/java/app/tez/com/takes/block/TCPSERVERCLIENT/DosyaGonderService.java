package app.tez.com.takes.block.TCPSERVERCLIENT;

/**
 * Created by serhat on 23.04.2018.
 */

import android.annotation.SuppressLint;
import android.app.Service;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import app.tez.com.takes.block.CheckForSDCard;
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

    JSONObject prevNesne;

    String gonderilecekDosya,socketIp,kendiIpAdresi,veritabanindanIpAdresi;
    String[] parts;
    String gonderildi = "";

    SharedPreferences sharedPrefs;
    public static final String CIHAZLAR = "cihazlarshared";

    @Nullable
    @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//
//
//
//        return super.onStartCommand(intent, flags, startId);
//    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart(Intent intent, int startId)
    {

        if (intent.getStringExtra("benHashiCozdum") != null) {
            gonderilecekDosya = intent.getStringExtra("benHashiCozdum");
        } else {

        }

        if (intent.getStringExtra("dosya") != null) {
            gonderilecekDosya = intent.getStringExtra("dosya");
        } else {

        }

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(DosyaGonderService.this);

        kendiIpAdresi = sharedPrefs.getString("ipadresiSharedPrefences","192.168.1.0");


        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);

        veritabaniYukle();

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
            Type type = new TypeToken<ArrayList<DeviceDTO>>() {}.getType();
            cihazlar = gson.fromJson(json, type);


            for(int sira=0; sira<veritabani.length();sira++){                                       //veritabanindaki ip adresini kontrol ediyoruz
                try {                                                                               //cihazlar listesinde yoksa ekliyoruz.
                    girisKontrolNesne = veritabani.getJSONObject(sira);
                    veritabanindanIpAdresi = girisKontrolNesne.getString("ipaddres");

                    if(!cihazlar.contains(veritabanindanIpAdresi)) {
                        DeviceDTO veritabanindanIp = new DeviceDTO();
                        veritabanindanIp.setIp(veritabanindanIpAdresi);
                        cihazlar.add(veritabanindanIp);
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
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DosyaGonderService.this,
                                            "Dosya Gonder Servisi" + socketIp,
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            s = new Socket(socketIp, SocketServerPORT);

                            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                            //send output msg
                            if (gonderilecekDosya.contains("benHashiCozdum")) {
                                out.write(gonderilecekDosya);
                                out.flush();
                                out.close();
                                //close connection
                                s.close();
                            } else if (gonderilecekDosya.contains("dosya")) {
//                                parts = gonderilecekDosya.split("/////");
//                                gonderilecekDosya = parts[1];

                                out.write(gonderilecekDosya);
                                out.flush();
                                out.close();
                                //close connection
                                s.close();
                            }
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        final String eMsg = "Something wrong: " + e.getMessage();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DosyaGonderService.this,
                                        eMsg,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
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
                        kendiIpAdresi += inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            kendiIpAdresi += "Something Wrong! " + e.toString() + "\n";
        }

        return kendiIpAdresi;
    }

    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        Toast.makeText(DosyaGonderService.this,"CihazlarServis destroy", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

    }

}