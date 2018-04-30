package app.tez.com.takes.block.anasayfa;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.scottyab.aescrypt.AESCrypt;
import com.sun.mail.imap.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;

import app.tez.com.takes.R;
import app.tez.com.takes.block.Block;
import app.tez.com.takes.block.Models.KayitBilgisiDTO;
import app.tez.com.takes.block.SifreMailGonder.Mail;
import app.tez.com.takes.block.TCPSERVERCLIENT.CihazlarServis;
import app.tez.com.takes.block.TCPSERVERCLIENT.KullaniciKayitServisi;
import dmax.dialog.SpotsDialog;


public class KayitOlEkrani extends AppCompatActivity {
    Button kaydet, vazgec;
    EditText firsName, email;
    JSONArray veritabani;
    JSONObject prevNesne;
    JSONArray gelenVeritabani;
    String prevKey, useremail, edtxEmail, edtxAdSoyad ,uuid, uid;
    int randomNumber;
    String kullaniciEmail, password;

    JSONObject yeniKayit = new JSONObject();
    JSONObject gelenKayitlar = new JSONObject();
    JSONObject eskiKayitlar = new JSONObject();

    JSONObject kayitKontrolNesne;

    Boolean varYok = false;
    String cryptedName,cryptedEmail,cryptedPass;

    String encrypPass = "takesPass";                            //şifreleme için key

    private static File fileDirectory = null;                   //Main Directory File
    private static final String DirectoryName = "TakES";        //Main Directory Name
    private static final String FileName = "veritabani.txt";   //Text File Name

    private static final String REQUEST_CONNECT_CLIENT = "request-connect-client";
    static final int SocketServerPORT = 8080;
    String ip = "";
    String lastip = "";
    Boolean dosyaYazildi = false;
    Boolean dosyaYazmayaDevam = true;
    AlertDialog dialog;

    File veritabanımız;
    public static int difficulty = 5;
    public static ArrayList<Block> blockchain = new ArrayList<Block>();

    //Preferences to Store TimStamp when data is Saved
    int bytesRead;
    InputStream is;
    byte[] bytes;
    BufferedOutputStream bos;
    FileOutputStream fos;
    String lip;

    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kayitekrani);

//        dialog = new SpotsDialog(KayitOlEkrani.this, R.style.Custom);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(KayitOlEkrani.this);

        kaydet = (Button) findViewById(R.id.btn_signup);
        firsName = (EditText) findViewById(R.id.input_name);
        email = (EditText) findViewById(R.id.input_email);

        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);

        try {
            veritabani = new JSONArray(getIntent().getSerializableExtra("veritabani").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ip = sharedPrefs.getString("ipadresiSharedPrefences","192.168.1.0");

        firsName.setText("Aydın Serhat");
        email.setText("serhatssezen@gmail.com");

        String[] parts = ip.split("\\.");
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        lip = part1 + "." + part2 + "." + part3 + ".";

//        ClientRxThread clientRxThread =
//                new ClientRxThread(
//                        lip,
//                        SocketServerPORT);
//
//        clientRxThread.start();

//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                dialog.show();
//            }
//        });


        kaydet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtxEmail = email.getText().toString();
                edtxAdSoyad = firsName.getText().toString();
                varYok = false;

                if (!firsName.getText().toString().equals("") && !edtxEmail.equals("")) {
                    try {
                        if (veritabani.length() > 0) {                          //veritabanında kayıt varsa
                            for (int t = 0; t < veritabani.length(); t++) {
                                kayitKontrolNesne = veritabani.getJSONObject(t);    //her bir blogu nesne olarak alıyoruz
                                useremail = kayitKontrolNesne.getString("mail");  //email aldık.
                                uid = kayitKontrolNesne.getString("hash");      //chainId yi aldık.
                                String decryptMail = AESCrypt.decrypt(encrypPass, useremail); //emaili decrypt ediyoruz
                                if (decryptMail.equals(edtxEmail)){         // varmı yok mu kontrol ediyoruz
                                    alertMessage("Hata","kayit","Bu mail adresiyle zaten hesap oluşturulmuş.");
                                    varYok = true;
                                }
                            }
                        } else {
                            kayitServiceCagir();
//                            kayitOl();
                            varYok = true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        //
                    }

                    if (varYok == false) {
                        kayitServiceCagir();
//                        kayitOl();
                    }


                } else {
                    new AlertDialog.Builder(KayitOlEkrani.this)
                            .setTitle("HATA")
                            .setMessage("Tüm Alanları Doldurmalısınız.")
                            .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
        });
    }



    public void kayitOl(){

        randomNumber = 20 + (int)(Math.random()*30);    // şifre oluşturmak için 20 ile 50 arasında random sayı üretiyoruz.
        password = getAlphaNumeric(randomNumber);       // random sayı kadar basamaklı bir alfanumeric şifre üretiyoruz.

        kullaniciEmail = email.getText().toString();
        //-- Yeni bir JSON Nesnesi oluşturuyoruz
        try {
            if (veritabani.length() > 0) {          // veritabanında kayıt varsa
                prevNesne = veritabani.getJSONObject(veritabani.length()-1);        //yeni blocktan önceki blogu nesne olarak aldık.
                prevKey = prevNesne.getString("hash");                           //o nesnenin chain id sini aldık

                try {                                                                     // kullanıcı verilerini şifreledik.
                    cryptedName = AESCrypt.encrypt(encrypPass, firsName.getText().toString());
                    cryptedEmail = AESCrypt.encrypt(encrypPass, kullaniciEmail);
                    cryptedPass = AESCrypt.encrypt(encrypPass, password);
                }catch (GeneralSecurityException e){
                    //handle error
                }

                addBlock(new Block(cryptedEmail, cryptedName , ip ,prevKey));

            } else {
                String encrypPass = "takesPass";

                try {
                    cryptedName = AESCrypt.encrypt(encrypPass, firsName.getText().toString());
                    cryptedEmail = AESCrypt.encrypt(encrypPass, kullaniciEmail);
                    cryptedPass = AESCrypt.encrypt(encrypPass, password);
                }catch (GeneralSecurityException e){
                    //handle error
                }

                addBlock(new Block(cryptedEmail, cryptedName, ip , "0"));

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
        String[] toArr = {kullaniciEmail};
        m.setTo(toArr);
        m.setFrom("takesblock@gmail.com");
        m.setSubject("Uygulamamıza hoşgeldiniz.");
        m.setBody(mesaj);

        if(m.send()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    alertMessage("Mail hesabınıza şifreniz gönderildi.", "login", "Mail gelmezse biraz bekleyiniz.");
                    kayitTamamla();
                }
            });
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    alertMessage("Hata","kayit", "Mail göndermede hata oldu.Tekrar kayıt olmayı deneyiniz.");
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
    public void alertMessage(String title, final String yonlendir, String mesaj) {
        new AlertDialog.Builder(KayitOlEkrani.this)
                .setTitle(title)
                .setMessage(mesaj)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(yonlendir == "login") {
                            Intent loginScreen = new Intent(KayitOlEkrani.this, MainActivity.class);
                            startActivity(loginScreen);
                        } else {

                        }
                    }
                }).show();
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
            Socket socket = null;
            for (int i = 20; i <= 28; i++) {
                try {
                    lastip = dstAddress + i;
                    if ((!ip.equals(lastip)) && dosyaYazildi == false) {
                        socket = new Socket(lastip, dstPort);
                        File gelenJson = new File(fileDirectory.getAbsolutePath() + "/" + "gelenJson.json");

                        byte[] bytes = new byte[1024];
                        InputStream is = socket.getInputStream();
                        FileOutputStream fos = new FileOutputStream(gelenJson);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        int bytesRead;
                        while ((bytesRead = is.read(bytes)) > 0) {
                            bos.write(bytes, 0, bytesRead);
                        }
                        bos.close();
                        FileInputStream fileInputStream = new FileInputStream(gelenJson);
                        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        //--- Dosyadaki verileri bir JSON Nesnesine aktarıyoruz.
                        gelenVeritabani = new JSONArray(bufferedReader.readLine());

                        if (gelenVeritabani.length() > veritabani.length()) {
                            for(int obj = 0; obj < gelenVeritabani.length(); obj++) {
                                yeniKayit = gelenVeritabani.getJSONObject(obj);
                                if (veritabani.toString().contains(yeniKayit.toString())) {

                                } else {
                                    kayitTamamla();
                                    gelenJson.delete();
                                    dosyaYazmayaDevam = false;
                                }
                            }
                        } else {
                            gelenJson.delete();
                        }

                        socket.close();

                        KayitOlEkrani.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                dosyaYazildi = true;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                    }
                                });
                                Toast.makeText(KayitOlEkrani.this,
                                        "Finished: " + lastip, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (IOException e) {

                    e.printStackTrace();

                    final String eMsg = "Something wrong: " + e.getMessage();
                    KayitOlEkrani.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
//                            Toast.makeText(KayitOlEkrani.this,
//                                    eMsg,
//                                    Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (JSONException e) {
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
                        ip += inetAddress.getHostAddress();
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


    public void kayitServiceCagir(){
        String kayitOlanBilgiler = edtxEmail + "/" + edtxAdSoyad + "/" + ip;

        Intent intent = new Intent(KayitOlEkrani.this, KullaniciKayitServisi.class);
        intent.putExtra("kayitOlanBilgileri", kayitOlanBilgiler);
        intent.putExtra("veritabani", "veritabani");
        startService(intent);

    }

}
