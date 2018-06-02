package app.tez.com.takes.block.TCPSERVERCLIENT;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;

import app.tez.com.takes.block.Block.Block;
import app.tez.com.takes.block.SifreMailGonder.Mail;

/**
 * Created by serhat on 29.04.2018.
 */

public class KayitTamamlaServis extends Service {

    ServerSocket serverSocket;
    JSONArray veritabani;
    JSONObject prevNesne;
    String type, oturumuAcan;

    int sayi = 0;
    SharedPreferences myPrefs;

    int difficulty = 2;
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

    public static ArrayList<Block> blockchain = new ArrayList<Block>();

    String gelenVeri;
    String[] veriParcala;
    public String hashCozuldu = "";

    public static KayitTamamlaServis kayitTamamlaServis;

    public KayitTamamla kayitTamamla;

    public boolean blockYazildi = false;


    String oturumuacanmail = " ";

    public KayitTamamlaServis() {
        kayitTamamlaServis = this;
        hashCozuldu = String.valueOf(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);

        oturumuAcan = myPrefs.getString("OturumuAcan", "Hepsi");

//        if (!oturumuAcan.equals("Hepsi")){
        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);
        try {
            gelenVeri = intent.getStringExtra("kayitBilgisi");
            veriParcala = gelenVeri.split("/");
            mail = veriParcala[0];
            nameSurname = veriParcala[1];
            ipadress = veriParcala[2];
            hashCozuldu = "";
        } catch (Exception e) {
        }

        veritabaniYukle();

        if (blockYazildi == false) {
            kayitTamamla = (KayitTamamla) new KayitTamamla().execute();
        }
//        }
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
    }

    //    public class KayitTamamlaThread extends Thread {
//        public void run() {
//            try {
//
//            } catch (Exception e) {
//            }
//        }
//    }

    public class KayitTamamla extends AsyncTask {
        @Override
        protected Object doInBackground(Object... arg0) {
            if (blockYazildi == false) {
                kayitOl();
            }
            return null;
        }

        protected void onCancelled() {
            // Do something when async task is cancelled
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
                addBlock(new Block(cryptedEmail, cryptedName, ipadress, cryptedPass, prevKey));

            } else {
                String encrypPass = "takesPass";
                try {
                    cryptedName = AESCrypt.encrypt(encrypPass, nameSurname);
                    cryptedEmail = AESCrypt.encrypt(encrypPass, mail);
                    cryptedPass = AESCrypt.encrypt(encrypPass, password);
                } catch (GeneralSecurityException e) {
                    //handle error
                }

                addBlock(new Block(cryptedEmail, cryptedName, ipadress, cryptedPass, "0"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (hashCozuldu != "durlan") {
            new SendMail().execute("");
        }
    }

    public void addBlock(Block newBlock) {


        if (veritabani.length() > 0) {                                                 // veritabanında kayıt varsa
            try {
                for (int i = 0; i < veritabani.length(); i++) {
                    prevNesne = veritabani.getJSONObject(i);
                    type = prevNesne.getString("tpye");
                    if (type.equals("kullanıcı")) {
                        sayi++;
                    }
                }
                difficulty = difficulty * sayi;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//        decryptAdSoyad + "//" + decryptMail + "//" + ip + "//" + userCoin;

        if (!oturumuAcan.equals("Hepsi")) {
            String[] oturumuacanparcala = oturumuAcan.split("//");
            oturumuacanmail = oturumuacanparcala[1];
            try {
                oturumuacanmail =  AESCrypt.encrypt(encrypPass, oturumuacanmail);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }

        }

        if (oturumuacanmail.equals(" ")) {
            String ip = myPrefs.getString("ipadresiSharedPrefences","192.168.1.0");

            try {
                oturumuacanmail =  AESCrypt.encrypt(encrypPass, ip);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }

        if (blockYazildi == false) {
            newBlock.mineBlockKayit(difficulty);
            blockchain.add(newBlock);
        }

        blockYazildi = true;

        if (hashCozuldu != "durlan") {
            hashiCozdumDiyeSeslen();
        }

        String hash = blockchain.get(0).hash;
        String previousHash = blockchain.get(0).previousHash;
        String mail = blockchain.get(0).mail;
        String adsoyad = blockchain.get(0).adsoyad;
        String ipad = blockchain.get(0).ipaddress;
        String timeStamp = String.valueOf(blockchain.get(0).timeStamp);
        String nonce = String.valueOf(blockchain.get(0).nonce);
        String sifre = blockchain.get(0).sifre;

        try {
            yeniKayit.put("hash", hash);
            yeniKayit.put("previousHash", previousHash);
            yeniKayit.put("mail", mail);
            yeniKayit.put("adsoyad", adsoyad);
            yeniKayit.put("ipaddres", ipad);
            yeniKayit.put("sifre", sifre);
            yeniKayit.put("timeStamp", timeStamp);
            yeniKayit.put("nonce", nonce);
            yeniKayit.put("kayıtolusturan", oturumuacanmail);
            yeniKayit.put("type", "kullanıcı");
            yeniKayit.put("coin", "0");

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
                if (hashCozuldu != "durlan") {
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
            kayitTamamla();
            blockYazildi = false;
        } else {

        }
    }

    public void kayitTamamla() {
        if (hashCozuldu != "durlan") {
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

                kayitTamamla.cancel(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public void hashiCozdumDiyeSeslen() {
        Intent intent = new Intent(KayitTamamlaServis.this, DosyaGonderService.class);
        intent.putExtra("benHashiCozdum", "benHashiCozdum/////" + blockchain.get(0).hash);
        startService(intent);
    }

    public void alinBudaDosya() {
        Intent intent = new Intent(KayitTamamlaServis.this, DosyaGonderService.class);
        intent.putExtra("dosya", "dosya/////" + veritabani.toString());
        startService(intent);

    }
}
