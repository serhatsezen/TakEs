package app.tez.com.takes.block.TCPSERVERCLIENT;

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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;

import app.tez.com.takes.block.Block;
import app.tez.com.takes.block.CheckForSDCard;
import app.tez.com.takes.block.SifreMailGonder.Mail;
import app.tez.com.takes.block.anasayfa.KayitOlEkrani;

/**
 * Created by serhat on 23.04.2018.
 */

public class KayitEkraniService extends Service {
    Intent intent;
    public static final String BROADCAST_ACTION = "Hello World";
    static final int SocketServerPORT = 8080;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name


    String adsoyad,ipadresim,sonhali, lastIp, socketIp;
    String gonderildi = "";

    JSONArray veritabani;
    JSONObject prevNesne;

    int randomNumber;
    JSONObject yeniKayit = new JSONObject();

    String password, prevKey, mail,nameSurname,ipadress
            ,cryptedName,cryptedEmail,cryptedPass, ipaddressleri;

    String encrypPass = "takesPass";                            //şifreleme için key
    String dosyami = "";

    File veritabanımız;
    public static int difficulty = 5;
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static ArrayList<String> ipadressler = new ArrayList<String>();


    @Nullable
    @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mail = intent.getStringExtra("mail");
        adsoyad = intent.getStringExtra("adsoyad");
        ipadresim = intent.getStringExtra("ipadresim");
        sonhali = mail + "/" + adsoyad + "/" + ipadresim;

        String[] parts = ipadresim.split("\\.");
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        lastIp = part1 + "." + part2 + "." + part3 + ".";

        new Connection().execute();

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

        if (new CheckForSDCard().isSDCardPresent()) {
            fileDirectory = new File(
                    Environment.getExternalStorageDirectory() + "/"
                            + DirectoryName);
        } else
            Toast.makeText(KayitEkraniService.this, "SD Card is not present.", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted


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
        for (int i = 20; i <= 28; i++) {
            try {
                socketIp = lastIp + i;
                if ((!ipadresim.equals(socketIp))) {
                    s = new Socket(socketIp, SocketServerPORT);
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    //send output msg
                    out.write(sonhali);
                    out.flush();
                    //accept server response

                    out.close();
                    gonderildi = "basarili";
                    final String inMsg = in.readLine();

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(KayitEkraniService.this, inMsg, Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

                        }
                    });

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
                        Toast.makeText(KayitEkraniService.this,
                                    eMsg,
                                    Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }
        }
        if (gonderildi.equals("")) {
            VeritabaniYukle();

            kayitOl();
        }


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

    public void kayitOl(){

        randomNumber = 20 + (int)(Math.random()*30);    // şifre oluşturmak için 20 ile 50 arasında random sayı üretiyoruz.
        password = getAlphaNumeric(randomNumber);       // random sayı kadar basamaklı bir alfanumeric şifre üretiyoruz.

        try {
            if (veritabani.length() > 0) {                                                 // veritabanında kayıt varsa
                prevNesne = veritabani.getJSONObject(veritabani.length()-1);         //yeni blocktan önceki blogu nesne olarak aldık.
                prevKey = prevNesne.getString("hash");                               //o nesnenin chain id sini aldık

                try {                                                                      // kullanıcı verilerini şifreledik.
                    cryptedName = AESCrypt.encrypt(encrypPass, adsoyad );
                    cryptedEmail = AESCrypt.encrypt(encrypPass, mail);
                    cryptedPass = AESCrypt.encrypt(encrypPass, password);
                }catch (GeneralSecurityException e){
                    //handle error
                }

                addBlock(new Block(cryptedEmail, cryptedName , ipadresim , prevKey));

            } else {
                String encrypPass = "takesPass";

                try {
                    cryptedName = AESCrypt.encrypt(encrypPass, adsoyad);
                    cryptedEmail = AESCrypt.encrypt(encrypPass, mail);
                    cryptedPass = AESCrypt.encrypt(encrypPass, password);
                }catch (GeneralSecurityException e){
                    //handle error
                }

                addBlock(new Block(cryptedEmail, cryptedName, ipadresim , "0"));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendMail().execute("");
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
            yeniKayit.put("hash",hash);
            yeniKayit.put("previousHash",previousHash);
            yeniKayit.put("mail",mail);
            yeniKayit.put("adsoyad",adsoyad);
            yeniKayit.put("ipaddres",ipad);
            yeniKayit.put("timeStamp",timeStamp);
            yeniKayit.put("nonce",nonce);
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

        if(m.send()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(KayitEkraniService.this, "Kayıt tamam", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted
                    if (!dosyami.equals("dosyagonderkontrol")) {
                        kayitTamamla();
                    }
                }
            });
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(KayitEkraniService.this, "Kayıt yalan", Toast.LENGTH_SHORT).show();//Show toast if SD Card is not mounted

                }
            });
        }

    }


    public void kayitTamamla(){
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

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
