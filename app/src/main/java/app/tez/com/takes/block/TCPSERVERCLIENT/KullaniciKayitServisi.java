package app.tez.com.takes.block.TCPSERVERCLIENT;

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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.mail.imap.Utility;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import app.tez.com.takes.block.CheckForSDCard;
import app.tez.com.takes.block.Models.DeviceDTO;
import app.tez.com.takes.block.Models.KayitBilgisiDTO;
import app.tez.com.takes.block.anasayfa.KayitOlEkrani;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static app.tez.com.takes.block.TCPSERVERCLIENT.CihazlarServis.cihazlar;

/**
 * Created by serhat on 28.04.2018.
 */

public class KullaniciKayitServisi extends Service {


    private Thread mythread;
    private boolean running;


    Intent intent;
    public static final String BROADCAST_ACTION = "BroadCastAction";
    static final int SocketServerPORT = 8080;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name

    SharedPreferences sharedPrefs;
    public static final String CIHAZLAR = "cihazlarshared";

    String kayitOlanBilgileri, socketIp, ip;

    @Nullable
    @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            kayitOlanBilgileri = intent.getStringExtra("kayitOlanBilgileri");
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(KullaniciKayitServisi.this);

            ip = sharedPrefs.getString("ipadresiSharedPrefences", "192.168.1.0");

            Toast.makeText(KullaniciKayitServisi.this,
                    " kayit servis " + ip,
                    Toast.LENGTH_LONG).show();

            String[] parts = ip.split("\\.");
            String part1 = parts[0];
            String part2 = parts[1];
            String part3 = parts[2];

            socketIp = part1 + "." + part2 + "." + part3 + ".";
            running = true;

            ClientRxThread clientRxThread =
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
        running = false;
        super.onDestroy();
        Toast.makeText(KullaniciKayitServisi.this, "Servis Destroy edildi. KullaniciKayitServisi.", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

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
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(KullaniciKayitServisi.this,
                                        socketIp,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        if (!socketIp.equals(ip)) {
                            s = new Socket(socketIp, SocketServerPORT);

                            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                            //send output msg
                            out.write("kayit/////" + kayitOlanBilgileri);
                            out.flush();
                            //accept server response
                            out.close();
                            //close connection
                            s.close();
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        final String eMsg = "Something wrong: " + e.getMessage();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(KullaniciKayitServisi.this,
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
                        Toast.makeText(KullaniciKayitServisi.this,
                                "cihaz yok",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

        }
    }


//    public class KayitRun extends AsyncTask<String, String, Boolean>  {
//        private Boolean result;
//        @Override
//        protected void onCancelled() {
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(KullaniciKayitServisi.this,
//                            "KayitRun İptal Edildi",
//                            Toast.LENGTH_LONG).show();
//                }
//            });
//
//            handleOnCancelled(this.result);
//        }
//        @Override
//        protected void onCancelled(Boolean result) {
//            handleOnCancelled(result);
//        }
//        //Both the functions will call this function
//        private void handleOnCancelled(Boolean result) {
//            // actual code here
//        }
//        @Override
//        protected Boolean doInBackground(String... strings) {
//            runTcpClient();
//            return null;
//        }
//
//
//    }
//
//    private void runTcpClient() {
//        ArrayList<DeviceDTO> cihazlar = new ArrayList<DeviceDTO>();
//        Socket s = null;
//        Gson gson = new Gson();
//        String json = sharedPrefs.getString(CIHAZLAR, null);
//        Type type = new TypeToken<ArrayList<DeviceDTO>>() {}.getType();
//        cihazlar = gson.fromJson(json, type);
//
//        if (cihazlar.size() > 0) {
//            for (int i = 0; i < cihazlar.size(); i++) {
//                try {
//                    socketIp = String.valueOf(cihazlar.get(i).getIp());
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(KullaniciKayitServisi.this,
//                                    socketIp,
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    });
//                    if (!socketIp.equals(ip)) {
//                        s = new Socket(socketIp, SocketServerPORT);
//
//                        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
//                        //send output msg
//                        out.write("kayit/////"+ kayitOlanBilgileri);
//                        out.flush();
//                        //accept server response
//                        out.close();
//                        //close connection
//                        s.close();
//                    }
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    final String eMsg = "Something wrong: " + e.getMessage();
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(KullaniciKayitServisi.this,
//                                    eMsg,
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    });
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(KullaniciKayitServisi.this,
//                            "cihaz yok",
//                            Toast.LENGTH_LONG).show();
//                }
//            });
//        }
//
//
//
//    }
//

}
