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

    String gelenDesciption, gelenAdSoyad, gelenIp, gelenImageUri, prevKey;
    String cryptedgelenDesciption, cryptedgelenAdSoyad, cryptedgelenIp, cryptedgelenImageUri;

    String encrypPass = "takesPass";                            //şifreleme için key

    File veritabanımız;
    public static int difficulty = 5;
    public static ArrayList<Block> blockchain = new ArrayList<Block>();

    String gelenVeri;
    String[] veriParcala;
    public String hashCozulduPost = "";

    public boolean blockYazildi = false;

    public static PostEkleTamamlaServis postEkleTamamlaServis;

    public PostEkleAsynTask postEkleAsynTask;

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
        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);
        try {
            gelenVeri = intent.getStringExtra("postBilgileri");
            veriParcala = gelenVeri.split("/");
            gelenDesciption = veriParcala[0];
            gelenAdSoyad = veriParcala[1];
            gelenIp = veriParcala[2];
            gelenImageUri = veriParcala[3];
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
                sifrele();
                addBlock(new Block(cryptedgelenDesciption, cryptedgelenAdSoyad, gelenIp, cryptedgelenImageUri, prevKey));
            } else {
                sifrele();
                addBlock(new Block(cryptedgelenDesciption, cryptedgelenAdSoyad, gelenIp, cryptedgelenImageUri, "0"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (hashCozulduPost != "dur") {
            postEkleTamamla();
        }
    }

    public void sifrele() {
        try {                                                                      // kullanıcı verilerini şifreledik.
            cryptedgelenDesciption = AESCrypt.encrypt(encrypPass, gelenDesciption);
            cryptedgelenAdSoyad = AESCrypt.encrypt(encrypPass, gelenAdSoyad);
            cryptedgelenImageUri = AESCrypt.encrypt(encrypPass, gelenImageUri);
        } catch (GeneralSecurityException e) {
            //handle error
        }
    }

    public void addBlock(Block newBlock) {

        if (blockYazildi == false) {
            newBlock.mineBlockKayit(difficulty);
            blockchain.add(newBlock);
        }

        blockYazildi = true;
        if (hashCozulduPost != "dur") {
            hashiCozdumDiyeSeslen();
        }

        String hash = blockchain.get(0).hash;
        String previousHash = blockchain.get(0).previousHash;
        String description = blockchain.get(0).mail;
        String adsoyad = blockchain.get(0).adsoyad;
        String ipad = blockchain.get(0).ipaddress;
        String timeStamp = String.valueOf(blockchain.get(0).timeStamp);
        String nonce = String.valueOf(blockchain.get(0).nonce);
        String imageUri = blockchain.get(0).sifre;

        try {
            yeniKayit.put("hash", hash);
            yeniKayit.put("previousHash", previousHash);
            yeniKayit.put("description", description);
            yeniKayit.put("adsoyad", adsoyad);
            yeniKayit.put("ipaddres", ipad);
            yeniKayit.put("resimAdi", imageUri);
            yeniKayit.put("timeStamp", timeStamp);
            yeniKayit.put("nonce", nonce);
            yeniKayit.put("cihazAdi", Build.MODEL);
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
