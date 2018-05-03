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
import android.widget.Toast;

import com.google.gson.Gson;
import com.scottyab.aescrypt.AESCrypt;

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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Enumeration;

import app.tez.com.takes.block.Block.Block;
import app.tez.com.takes.block.Models.DeviceDTO;

/**
 * Created by serhat on 18.04.2018.
 */

public class ServerService extends Service {
    static final int SocketServerPORT = 8080;
    ServerSocket serverSocket;
    JSONArray veritabani;
    JSONObject prevNesne;

    public boolean hashCozuldu;
    ServerSocketThread serverSocketThread;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name

    Intent intent;
    public static final String BROADCAST_ACTION = "Hello World";

    String encrypPass = "takesPass";                            //şifreleme için key
    File veritabanımız;

    JSONObject veritabaniKontrolNesne;
    String nereye, gelenveri;

    public static final String CIHAZLAR = "cihazlarshared";
    public static ArrayList<DeviceDTO> cihazlar = new ArrayList<DeviceDTO>();

    public KayitTamamlaServis kayitTamamlaServis = new KayitTamamlaServis();
    public PostEkleTamamlaServis postEkleTamamlaServis = new PostEkleTamamlaServis();


    @Nullable
    @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);

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
        hashCozuldu = false;

        serverSocketThread = new ServerSocketThread();
        serverSocketThread.start();
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
    private String getIpAddress() {
        String ip = "";
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
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    public class ServerSocketThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(SocketServerPORT);

                while (true) {
                    socket = serverSocket.accept();

                    FileTxThread fileTxThread = new FileTxThread(socket);
                    fileTxThread.start();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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

    //gelen dosya
    public class FileTxThread extends Thread {
        Socket socket;

        FileTxThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                final String incomingMsg = in.readLine();

                if (incomingMsg == null) {

                } else if (incomingMsg.contains("cihazlaraEkleBeni")) {
                    String[] parts = incomingMsg.split("/////");
                    String cihazlaraEKleBeni = parts[0];
                    final String gelenIP = parts[1];


                    DeviceDTO deviceDTO = new DeviceDTO();
                    deviceDTO.setIp(gelenIP);
                    if (!cihazlar.contains(deviceDTO)) {
                        cihazlar.add(deviceDTO);
                    }
                    saveArrayList(cihazlar, CIHAZLAR);
                } else {

                    String[] parts = incomingMsg.split("/////");
                    nereye = parts[0];
                    gelenveri = parts[1];

                    if (nereye.equals("kayit")) {
                        veritabaniYukle();

                        String[] veriParcala = gelenveri.split("/");
                        String gelenmail = veriParcala[0];
                        if (veritabani.length() > 0) {
                            for (int sira = 0; sira < veritabani.length(); sira++) {
                                veritabaniKontrolNesne = veritabani.getJSONObject(sira);
                                String useremail = veritabaniKontrolNesne.getString("mail");
                                String decryptMail = AESCrypt.decrypt(encrypPass, useremail);
                                if (!decryptMail.equals(gelenmail)) {
                                    Intent kayitService = new Intent(ServerService.this, KayitTamamlaServis.class);
                                    kayitService.putExtra("kayitBilgisi", gelenveri);
                                    startService(kayitService);
                                } else {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ServerService.this, "Mail Adresi Kayıtli ", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
                                        }
                                    });
                                }
                            }
                        } else {
                            Intent kayitService = new Intent(ServerService.this, KayitTamamlaServis.class);
                            kayitService.putExtra("kayitBilgisi", gelenveri);
                            startService(kayitService);
                        }
                    } else if (nereye.equals("benHashiCozdum")) {
                        KayitTamamlaServis.kayitTamamlaServis.stopSelf();
                        KayitTamamlaServis.kayitTamamlaServis.hashCozuldu = "durlan";

                    } else if (nereye.equals("benHashiCozdumPost")) {
                        PostEkleTamamlaServis.postEkleTamamlaServis.stopSelf();
                        PostEkleTamamlaServis.postEkleTamamlaServis.hashCozulduPost = "dur";
                    } else if (nereye.equals("postEkle")) {
                        Intent kayitService = new Intent(ServerService.this, PostEkleTamamlaServis.class);
                        kayitService.putExtra("postBilgileri", gelenveri);
                        startService(kayitService);
                    } else if (nereye.equals("dosya")) {
                        //-- veritabani'nin son halini JSON dosyasına kaydediyoruz.
                        veritabanımız = new File(fileDirectory.getAbsolutePath() + "/" + FileName);
                        FileOutputStream fileOutputStream = new FileOutputStream(veritabanımız);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                        outputStreamWriter.write(gelenveri);
                        outputStreamWriter.close();
                        fileOutputStream.close();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ServerService.this, "Post paylaşıldı. ", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
                            }
                        });
                    }
                }
                socket.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    public void saveArrayList(ArrayList<DeviceDTO> list, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ServerService.this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }


    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}