package app.tez.com.takes.block.TCPSERVERCLIENT;

/**
 * Created by serhat on 23.04.2018.
 */

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;

import app.tez.com.takes.block.Block;
import app.tez.com.takes.block.CheckForSDCard;
import app.tez.com.takes.block.SifreMailGonder.Mail;
import app.tez.com.takes.block.anasayfa.KayitOlEkrani;

/**
 * Created by serhat on 18.04.2018.
 */

public class ServerService extends Service {
    static final int SocketServerPORT = 8080;
    ServerSocket serverSocket;
    JSONArray veritabani;
    JSONObject prevNesne;

    ServerSocketThread serverSocketThread;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name

    Intent intent;
    public static final String BROADCAST_ACTION = "Hello World";

    int randomNumber;
    JSONObject yeniKayit = new JSONObject();

    String password, prevKey, mail, nameSurname, ipadress, cryptedName, cryptedEmail, cryptedPass, ipaddressleri;

    String encrypPass = "takesPass";                            //şifreleme için key
    String dosyami = "";

    File veritabanımız;
    public static int difficulty = 5;
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static ArrayList<String> ipadressler = new ArrayList<String>();

    JSONObject veritabaniKontrolNesne;
    JSONObject gelenVeriKontrolNesne;
    JSONArray gelenveriArray;

    String nereye, gelenveri;

//    private KullaniciKayitServisi.KayitRun mThreadKayitRun;


    KayitTamamlaServis kayitTamamla = new KayitTamamlaServis();
    KullaniciKayitServisi kayitServis = new KullaniciKayitServisi();


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

        VeritabaniYukle();

        serverSocketThread = new ServerSocketThread();
        serverSocketThread.start();
        Toast.makeText(ServerService.this, "Server Service" + getIpAddress(), Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted


    }

    //---------- JSON Dosyasındaki verileri burada uygulamaya yüklüyoruz.
    public void VeritabaniYukle() {
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

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ServerService.this, incomingMsg, Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
                    }
                });
                //receive a message

                if (incomingMsg == null) {
                } else {
                    VeritabaniYukle();

                    String[] parts = incomingMsg.split("/////");
                    nereye = parts[0];
                    gelenveri = parts[1];



                    if (nereye.equals("kayit")) {
                        String[] veriParcala = gelenveri.split("/");
                        String gelenmail = veriParcala[0];
                        if (veritabani.length() > 0) {
                            for (int sira = 0; sira < veritabani.length(); sira++) {
                                veritabaniKontrolNesne = veritabani.getJSONObject(sira);
                                String useremail = veritabaniKontrolNesne.getString("email");
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
                        stopService(new Intent(ServerService.this, KayitTamamlaServis.class));
                        stopService(new Intent(ServerService.this, KullaniciKayitServisi.class));
//                        kayitServis.new KayitRun().cancel(true);

//                        stopService(new Intent(ServerService.this, KayitTamamlaServis.class));
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ServerService.this, "Ulan Hash ÇÖzüldü servisler kapatldı.", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
                            }
                        });

                    } else if (nereye.equals("dosya")) {
//                        veritabaniKontrolNesne = veritabani.getJSONObject(veritabani.length());
//                        String useremail = veritabaniKontrolNesne.getString("email");
//                        String decryptMail = AESCrypt.decrypt(encrypPass, useremail);
//
//                        FileInputStream fileInputStream = new FileInputStream(gelenveri);
//                        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
//                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                        //--- Dosyadaki verileri bir JSON Nesnesine aktarıyoruz.
//                        gelenveriArray = new JSONArray(bufferedReader.readLine());
//
//                        gelenVeriKontrolNesne = gelenveriArray.getJSONObject(gelenveriArray.length());
//                        String useremailgelen = veritabaniKontrolNesne.getString("email");
//                        String decryptMailgelen = AESCrypt.decrypt(encrypPass, useremailgelen);

//                        veritabani.put(gelenVeriKontrolNesne);

                        //-- veritabani'nin son halini JSON dosyasına kaydediyoruz.
                        veritabanımız = new File(fileDirectory.getAbsolutePath() + "/" + FileName);
                        FileOutputStream fileOutputStream = new FileOutputStream(veritabanımız);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                        outputStreamWriter.write(gelenveri);
                        outputStreamWriter.close();
                        fileOutputStream.close();

//                        if (decryptMail.equals(decryptMailgelen)) {
//                        }
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

    public void kayitOl() {

        randomNumber = 20 + (int) (Math.random() * 30);    // şifre oluşturmak için 20 ile 50 arasında random sayı üretiyoruz.
        password = getAlphaNumeric(randomNumber);       // random sayı kadar basamaklı bir alfanumeric şifre üretiyoruz.

        if (!dosyami.equals("dosyagonderkontrol")) {
            //-- Yeni bir JSON Nesnesi oluşturuyoruz
            try {
                if (veritabani.length() > 0) {                                                 // veritabanında kayıt varsa
                    prevNesne = veritabani.getJSONObject(veritabani.length() - 1);         //yeni blocktan önceki blogu nesne olarak aldık.
                    prevKey = prevNesne.getString("hash");                               //o nesnenin chain id sini aldık

                    try {                                                                      // kullanıcı verilerini şifreledik.
                        cryptedName = AESCrypt.encrypt(encrypPass, nameSurname);
                        cryptedEmail = AESCrypt.encrypt(encrypPass, mail);
                        cryptedPass = AESCrypt.encrypt(encrypPass, password);
                    } catch (GeneralSecurityException e) {
                        //handle error
                    }

                    addBlock(new Block(cryptedEmail, cryptedName, ipadress, prevKey));

                } else {
                    String encrypPass = "takesPass";

                    try {
                        cryptedName = AESCrypt.encrypt(encrypPass, nameSurname);
                        cryptedEmail = AESCrypt.encrypt(encrypPass, mail);
                        cryptedPass = AESCrypt.encrypt(encrypPass, password);
                    } catch (GeneralSecurityException e) {
                        //handle error
                    }

                    addBlock(new Block(cryptedEmail, cryptedName, ipadress, "0"));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new SendMail().execute("");
        }

    }

    public void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);


        String hash = blockchain.get(0).hash;
        String previousHash = blockchain.get(0).previousHash;
        String mail = blockchain.get(0).mail;
        String adsoyad = blockchain.get(0).adsoyad;
        String ipad = blockchain.get(0).ipaddress;
        String timeStamp = String.valueOf(blockchain.get(0).timeStamp);
        String nonce = String.valueOf(blockchain.get(0).nonce);

        try {
            yeniKayit.put("hash", hash);
            yeniKayit.put("previousHash", previousHash);
            yeniKayit.put("mail", mail);
            yeniKayit.put("adsoyad", adsoyad);
            yeniKayit.put("ipaddres", ipad);
            yeniKayit.put("timeStamp", timeStamp);
            yeniKayit.put("nonce", nonce);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        blockchain.clear();
    }


    public String getAlphaNumeric(int len) {

        char[] ch = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

        char[] c = new char[len];
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < len; i++) {
            c[i] = ch[random.nextInt(ch.length)];
        }

        return new String(c);
    }

    private class SendMail extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                if (!dosyami.equals("dosyagonderkontrol")) {
                    sendMail();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }
    }

    public void sendMail() throws Exception {
        Mail m = new Mail("takesblock@gmail.com", "0123456789Sa");

        String mesaj = "Uygulamaya giriş için şifreniz: \n " + password + " \n Lütfen şifrenizi saklayınız. Şifre yenileme işlemi yapılamayacaktır. Şifrenizi kaybederseniz hesabınıza erişemeyeceksiniz!";
        String[] toArr = {mail};
        m.setTo(toArr);
        m.setFrom("takesblock@gmail.com");
        m.setSubject("Uygulamamıza hoşgeldiniz.");
        m.setBody(mesaj);

        if (m.send()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ServerService.this, "Kayıt tamam", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
                    if (!dosyami.equals("dosyagonderkontrol")) {
                        kayitTamamla();
                    }
                }
            });
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ServerService.this, "Kayıt yalan", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

                }
            });
        }

    }


    public void kayitTamamla() {
        try {
            if (!fileDirectory.exists())
                fileDirectory.mkdir();

            veritabani.put(gelenVeriKontrolNesne);

            //-- veritabani'nin son halini JSON dosyasına kaydediyoruz.
            veritabanımız = new File(fileDirectory.getAbsolutePath() + "/" + FileName);
            FileOutputStream fileOutputStream = new FileOutputStream(veritabanımız);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(veritabani.toString());
            outputStreamWriter.close();
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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