package app.tez.com.takes.block.TCPSERVERCLIENT;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import java.util.ArrayList;

import app.tez.com.takes.block.Block.Block;
import app.tez.com.takes.block.Block.BlockPaylasim;

/**
 * Created by serhat on 3.05.2018.
 */

public class PostEkleTamamlaServis extends Service {
    ServerSocket serverSocket;
    JSONArray veritabani;
    JSONObject prevNesne;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name

    Intent intent;
    public static final String BROADCAST_ACTION = "Hello World";

    JSONObject yeniKayit = new JSONObject();

    String gelenDesciption, gelenKategori,gelenAdSoyad, gelenIp, gelenMail, prevKey;
    String cryptedgelenDesciption, cryptedgelenAdSoyad, cryptedgelenIp, cryptedgelenImageUri, cryptedgelenMail, cryptedGelenKategori;

    String encrypPass = "takesPass";                            //şifreleme için key

    File veritabanımız;
    public static int difficulty = 5;
    public static ArrayList<BlockPaylasim> blockchain = new ArrayList<BlockPaylasim>();

    String gelenVeri;
    Uri imageUri;
    String[] veriParcala;
    public String hashCozulduPost = "";

    public boolean blockYazildi = false;

    public static PostEkleTamamlaServis postEkleTamamlaServis;

    public PostEkleAsynTask postEkleAsynTask;

    String oturumuacanmail = " ";
    String type, oturumuAcan;

    int sayi = 0;
    SharedPreferences myPrefs;

    public PostEkleTamamlaServis() {
        postEkleTamamlaServis = this;
        hashCozulduPost = String.valueOf(this);
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


        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);
        try {
            gelenVeri = intent.getStringExtra("postBilgileri");

            veriParcala = gelenVeri.split("/");
            gelenDesciption = veriParcala[0];
            gelenKategori = veriParcala[1];
            gelenAdSoyad = veriParcala[2];
            gelenIp = veriParcala[3];
            gelenMail = veriParcala[4];
            hashCozulduPost = "";
            blockYazildi = false;
        } catch (Exception e) {
        }

        veritabaniYukle();

        if (blockYazildi == false) {
            postEkleAsynTask = (PostEkleAsynTask) new PostEkleAsynTask().execute();
        }
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

    public class PostEkleAsynTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object... arg0) {
            if (blockYazildi == false) {
                postEkle();
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

    public void postEkle() {
        //-- Yeni bir JSON Nesnesi oluşturuyoruz
        try {
            if (veritabani.length() > 0) {                                                 // veritabanında kayıt varsa
                prevNesne = veritabani.getJSONObject(veritabani.length() - 1);         //yeni blocktan önceki blogu nesne olarak aldık.
                prevKey = prevNesne.getString("hash");                               //o nesnenin chain id sini aldık

                cryptedgelenDesciption = AESCrypt.encrypt(encrypPass, gelenDesciption);
                cryptedgelenAdSoyad = AESCrypt.encrypt(encrypPass, gelenAdSoyad);
                cryptedgelenMail = AESCrypt.encrypt(encrypPass, gelenMail);
                cryptedGelenKategori = AESCrypt.encrypt(encrypPass,gelenKategori);

                addBlock(new BlockPaylasim(gelenMail, cryptedgelenDesciption, gelenIp, cryptedgelenAdSoyad, prevKey));
            } else {
                cryptedgelenDesciption = AESCrypt.encrypt(encrypPass, gelenDesciption);
                cryptedgelenAdSoyad = AESCrypt.encrypt(encrypPass, gelenAdSoyad);
                cryptedgelenMail = AESCrypt.encrypt(encrypPass, gelenMail);
                cryptedGelenKategori = AESCrypt.encrypt(encrypPass,gelenKategori);

                addBlock(new BlockPaylasim(gelenMail, cryptedgelenDesciption, gelenIp, cryptedgelenAdSoyad, "0"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        if (hashCozulduPost != "dur") {
            postEkleTamamla();
        }
    }

    public void addBlock(BlockPaylasim newBlock) {
        if (veritabani.length() > 0) {                                                 // veritabanında kayıt varsa
            try {
                for (int i = 0; i < veritabani.length(); i++) {
                    prevNesne = veritabani.getJSONObject(i);
                    type = prevNesne.getString("tpye");
                    if (type.equals("kullanıcı")) {
                        sayi++;
                    }
                }
                difficulty = 1;
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
            newBlock.mineBlockPaylasim(difficulty);
            blockchain.add(newBlock);
        }

        blockYazildi = true;
        if (hashCozulduPost != "dur") {
            hashiCozdumDiyeSeslen();
        }

        String hash = blockchain.get(0).hash;
        String previousHash = blockchain.get(0).previousHash;
        String icerik = blockchain.get(0).icerik;
        String adsoyad = blockchain.get(0).adSoyad;
        String ipad = blockchain.get(0).ipaddress;
        String mail = blockchain.get(0).mail;
        String timeStamp = String.valueOf(blockchain.get(0).timeStamp);
        String nonce = String.valueOf(blockchain.get(0).nonce);
        String cryptMail = null;
        try {
            cryptMail = AESCrypt.encrypt(encrypPass, mail);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        try {
            yeniKayit.put("hash", hash);
            yeniKayit.put("previousHash", previousHash);
            yeniKayit.put("description", icerik);
            yeniKayit.put("mail", cryptMail);
            yeniKayit.put("kategori", cryptedGelenKategori);
            yeniKayit.put("adsoyad", adsoyad);
            yeniKayit.put("ipaddres", ipad);
            yeniKayit.put("resimAdi", "1323");
            yeniKayit.put("timeStamp", timeStamp);
            yeniKayit.put("nonce", nonce);
            yeniKayit.put("kayıtolusturan", oturumuacanmail);
            yeniKayit.put("type", "POST");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        blockchain.clear();
    }

    public void postEkleTamamla() {
        if (hashCozulduPost != "dur") {
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

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PostEkleTamamlaServis.this,
                                "PostEkleTamamlaServis Tamamlandı /////",
                                Toast.LENGTH_LONG).show();
                    }
                });

                alinBudaDosya();

                postEkleAsynTask.cancel(true);
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
        Intent intent = new Intent(PostEkleTamamlaServis.this, DosyaGonderService.class);
        intent.putExtra("benHashiCozdumPost", "benHashiCozdumPost/////" + blockchain.get(0).ipaddress);
        startService(intent);
    }

    public void alinBudaDosya() {
        Intent intent = new Intent(PostEkleTamamlaServis.this, DosyaGonderService.class);
        intent.putExtra("dosya", "dosya/////" + veritabani.toString());
        startService(intent);
    }
}
