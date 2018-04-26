package app.tez.com.takes.block.TCPSERVERCLIENT;

/**
 * Created by serhat on 23.04.2018.
 */

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import app.tez.com.takes.block.CheckForSDCard;

public class DosyaGonderService extends Service {
    Intent intent;
    public static final String BROADCAST_ACTION = "Hello World";
    static final int SocketServerPORT = 8080;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name


    String dosya,ipadresi,kendiIpAdresi;
    String[] parts;

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
    public void onCreate()
    {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart(Intent intent, int startId)
    {
        dosya = intent.getStringExtra("dosya");
        ipadresi = intent.getStringExtra("ipadresi");
        getIpAddress();

        new Connection().execute("");
    }

    private class Connection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            runTcpClient();
            return null;
        }

    }

    private void runTcpClient() {
        Socket s = null;
        parts = ipadresi.split("//");

        try {
            for(int t = 0; t <= parts.length; t++) {
                if(kendiIpAdresi != parts[t]) {
                    s = new Socket(parts[t], SocketServerPORT);
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    //send output msg
                    out.write("dosyagonderkontrol" + dosya);
                    out.flush();
                    //accept server response
                    out.close();

                    final String inMsg = in.readLine();

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DosyaGonderService.this, inMsg, Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
                        }
                    });

                    //close connection
                    s.close();
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
}