package app.tez.com.takes.block.KayitGiris;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import app.tez.com.takes.R;
import app.tez.com.takes.block.Block.Block;
import app.tez.com.takes.block.TCPSERVERCLIENT.KullaniciKayitServisi;


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


    public void kayitServiceCagir(){
        String kayitOlanBilgiler = edtxEmail + "/" + edtxAdSoyad + "/" + ip;

        Intent intent = new Intent(KayitOlEkrani.this, KullaniciKayitServisi.class);
        intent.putExtra("kayitOlanBilgileri", kayitOlanBilgiler);
        intent.putExtra("veritabani", "veritabani");
        startService(intent);

    }

    public void alertMessage(String title, final String yonlendir, String mesaj) {
        new AlertDialog.Builder(KayitOlEkrani.this)
                .setTitle(title)
                .setMessage(mesaj)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(yonlendir == "login") {
                            Intent loginScreen = new Intent(KayitOlEkrani.this, GirisEkrani.class);
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




}
