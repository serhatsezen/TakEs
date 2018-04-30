package app.tez.com.takes.block.TCPSERVERCLIENT;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import app.tez.com.takes.block.Models.DeviceDTO;

/**
 * Created by serhat on 28.04.2018.
 */

public class PortAcServis extends Service {
    static final int SocketServerPORT = 8080;
    ServerSocket serverSocket;
    ServerSocketThread serverSocketThread;
    Intent intent;
    public static final String BROADCAST_ACTION = "BroadCastAction";

    public String nereye, gelenveri;
    public String incomingMsg;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name
    JSONArray veritabani = new JSONArray();


    @Nullable
    @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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
        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);

        if (!fileDirectory.exists())
            fileDirectory.mkdir();

        vritabaniYukle();

        serverSocketThread = new ServerSocketThread();
        serverSocketThread.start();
        Toast.makeText(PortAcServis.this, "asdasdas", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

    }

    public class ServerSocketThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(SocketServerPORT);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PortAcServis.this, "Bekliyor" + serverSocket.getLocalPort(), Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
                    }
                });

                while (true) {
                    socket = serverSocket.accept();


                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PortAcServis.this, "Bağlandı", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
                        }
                    });

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

//    //gelen dosya
//    public class FileTxThread extends Thread {
//        Socket socket;
//
//        FileTxThread(Socket socket) {
//            this.socket = socket;
//        }
//
//        @Override
//        public void run() {
//            File file = new File(fileDirectory.getAbsolutePath() + "/" + FileName);
//
//            try {
//                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//
//                incomingMsg = in.readLine();
//
//
//                //receive a message
//
//                String[] parts = incomingMsg.split("/");
//                nereye = parts[0];
//                gelenveri = parts[1];
//
//                if (nereye.equals("kayit")) {
//                    Intent kayitService = new Intent(PortAcServis.this, KayitTamamlaServis.class);
//                    kayitService.putExtra("kayitBilgisi", gelenveri);
//                    startActivity(kayitService);
//                } else if (nereye.equals("blokyazıldi")) {
//                    //KayitTamamlaServis i durdur, iptal et
//                    stopService(new Intent(PortAcServis.this, KayitTamamlaServis.class));
//                }
//                socket.close();
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//    }

    //---------- JSON Dosyasındaki verileri burada uygulamaya yüklüyoruz.
    public void vritabaniYukle() {
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

}
