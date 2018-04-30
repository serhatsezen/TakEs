package app.tez.com.takes.block.TCPSERVERCLIENT;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;

import app.tez.com.takes.block.Block;
import app.tez.com.takes.block.Models.DeviceDTO;
import app.tez.com.takes.block.SifreMailGonder.Mail;
import app.tez.com.takes.block.anasayfa.KayitOlEkrani;

/**
 * Created by serhat on 29.04.2018.
 */

public class KayitTamamlaServis extends Service {


    private Thread mythread;
    private boolean running;
    private boolean hashCozdumGonderildi = false;
    private boolean dosyaGonderildi = false;


    static final int SocketServerPORT = 8080;
    ServerSocket serverSocket;
    JSONArray veritabani;
    JSONObject prevNesne;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name

    Intent intent;
    public static final String BROADCAST_ACTION = "Hello World";

    int randomNumber;
    JSONObject yeniKayit = new JSONObject();

    String password, prevKey, mail, nameSurname, ipadress, cryptedName, cryptedEmail, cryptedPass, ipaddressleri;

    String encrypPass = "takesPass";                            //şifreleme için key

    File veritabanımız;
    public static int difficulty = 5;
    public static ArrayList<Block> blockchain = new ArrayList<Block>();

    String gelenVeri;
    String[] veriParcala;

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
    public void onStart(final Intent intent, int startId) {
        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);

        veritabaniYukle();

        running = true;
        mythread = new Thread() {
            @Override
            public void run() {
                while (running) {
                    try {
                        gelenVeri = intent.getStringExtra("kayitBilgisi");
                        veriParcala = gelenVeri.split("/");
                        mail = veriParcala[0];
                        nameSurname = veriParcala[1];
                        ipadress = veriParcala[2];

                        kayitOl();


                    } catch (Exception e) {

                    }
                }
            }
        };
        mythread.start();

        Toast.makeText(KayitTamamlaServis.this, " ", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted


    }

    public class KayitTamamla extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {

            return null;
        }

        protected void onCancelled() {
            // Do something when async task is cancelled

        }

        // After each task done
        protected void onProgressUpdate(Integer... progress) {

        }
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

    public void kayitOl() {

        randomNumber = 20 + (int) (Math.random() * 30);    // şifre oluşturmak için 20 ile 50 arasında random sayı üretiyoruz.
        password = getAlphaNumeric(randomNumber);       // random sayı kadar basamaklı bir alfanumeric şifre üretiyoruz.

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

    public void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(KayitTamamlaServis.this, "BlokYazıldı", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
            }
        });

        hashiCozdumDiyeSeslen();

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
            yeniKayit.put("cihazAdi", Build.MODEL);
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
                sendMail();
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
                    Toast.makeText(KayitTamamlaServis.this, "Kayıt tamam", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
                    kayitTamamla();
                }
            });
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(KayitTamamlaServis.this, "Kayıt yalan", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

                }
            });
        }

    }


    public void kayitTamamla() {
        try {
            if (!fileDirectory.exists())
                fileDirectory.mkdir();

            veritabani.put(yeniKayit);
            //-- veritabani'nin son halini JSON dosyasına kaydediyoruz.
            veritabanımız = new File(fileDirectory.getAbsolutePath() + "/" + FileName);
            FileOutputStream fileOutputStream = new FileOutputStream(veritabanımız);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(veritabani.toString());
            outputStreamWriter.close();
            fileOutputStream.close();

            alinBudaDosya();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        running = false;
        super.onDestroy();
        Toast.makeText(KayitTamamlaServis.this, "Servis Destroy edildi. KayitTamamlaServisi.", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void hashiCozdumDiyeSeslen() {
        running = false;

        if (hashCozdumGonderildi = false) {
            hashCozdumGonderildi = true;
            Intent intent = new Intent(KayitTamamlaServis.this, DosyaGonderService.class);
            intent.putExtra("benHashiCozdum", "benHashiCozdum/////" + blockchain.get(0).hash);
            startService(intent);
        }
    }

    public void alinBudaDosya() {
        running = false;

        if (dosyaGonderildi == false) {
            dosyaGonderildi = true;
            Intent intent = new Intent(KayitTamamlaServis.this, DosyaGonderService.class);
            intent.putExtra("dosya", "dosya/////" + veritabani.toString());
            startService(intent);
        }
    }
}
