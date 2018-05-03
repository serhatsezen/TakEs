package app.tez.com.takes.block.KayitGiris;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.util.Enumeration;

import app.tez.com.takes.R;
import app.tez.com.takes.block.MainPage.BottomBarActivity;
import app.tez.com.takes.block.Models.OturumuAcan;
import app.tez.com.takes.block.TCPSERVERCLIENT.CihazlarServis;
import app.tez.com.takes.block.TCPSERVERCLIENT.ServerService;

public class GirisEkrani extends AppCompatActivity {
    Button girisBtn;
    TextView kayitOlBtn;
    EditText sifre, email;
    JSONArray veritabani = new JSONArray();
    JSONObject girisKontrolNesne;
    String useremail, usersifre, userAdSoyad, edtxEmail, edtxSifre;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name
    String ip = "";
    public String isFirstTime;
    public static final String FIRST_TIME = "first_time";
    SharedPreferences myPrefs;
    SharedPreferences.Editor editor;
    String decryptMail, decryptSifre;
    String password = "takesPass";


    @SuppressLint("WifiManagerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.girisekrani);

        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);

        if (!fileDirectory.exists())
            fileDirectory.mkdir();

        myPrefs = this.getSharedPreferences("myPrefs", MODE_WORLD_READABLE);
        editor = myPrefs.edit();


        checkPermissions();

        vritabaniYukle();

        getIpAddress();

        Intent serverServisIntent = new Intent(GirisEkrani.this, ServerService.class);
        serverServisIntent.putExtra("veritabani", veritabani.toString());
        startService(serverServisIntent);

        Intent cihazlarServis = new Intent(GirisEkrani.this, CihazlarServis.class);
        startService(cihazlarServis);
//
//        Intent portServis = new Intent(GirisEkrani.this, PortAcServis.class);
//        startService(portServis);


        kayitOlBtn = (TextView) findViewById(R.id.link_signup);
        sifre = (EditText) findViewById(R.id.input_password);
        email = (EditText) findViewById(R.id.input_email);
        girisBtn = (Button) findViewById(R.id.btn_login);

        email.setText("serhattsezen@gmail.com");
        sifre.setText("srNsJfsW3cZCkfxhpEThx3N27kLK2cSqaW6E7m2Aij");

        kayitOlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GirisEkrani.this, KayitOlEkrani.class);
                i.putExtra("veritabani", veritabani.toString());
                startActivity(i);
            }
        });

        girisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vritabaniYukle();

                edtxEmail = email.getText().toString();
                edtxSifre = sifre.getText().toString();

                try {
                    for (int sira = 0; sira < veritabani.length(); sira++) {
                        girisKontrolNesne = veritabani.getJSONObject(sira);
                        try {
                            useremail = girisKontrolNesne.getString("mail");
                            usersifre = girisKontrolNesne.getString("sifre");
                            decryptMail = AESCrypt.decrypt(password, useremail);
                            decryptSifre = AESCrypt.decrypt(password, usersifre);
                        } catch (Exception e) {

                        }
                        userAdSoyad = girisKontrolNesne.getString("adsoyad");

                        String decryptAdSoyad = AESCrypt.decrypt(password, userAdSoyad);

                        if (decryptMail.equals(edtxEmail) && decryptSifre.equals(edtxSifre) && decryptMail != null && decryptSifre != null) {

                            OturumuAcan oturumuAcan = new OturumuAcan();
                            oturumuAcan.setAdsoyad(decryptAdSoyad);
                            oturumuAcan.setEmail(decryptMail);
                            oturumuAcan.setFromIP(ip);

                            String oturmuAcan = decryptAdSoyad + "//" + decryptMail + "//" + ip;

                            editor.putString("OturumuAcan", oturmuAcan);
                            editor.commit();

                            Intent i = new Intent(GirisEkrani.this, BottomBarActivity.class);
                            i.putExtra("veritabani", veritabani.toString());
                            i.putExtra("OturumuAcan", oturumuAcan);
                            startActivity(i);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    //handle error - could be due to incorrect password or tampered encryptedMsg
                }

            }
        });

    }

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

    //------- Burada Uygulamada Dosya yazma-okuma işlemleri için gerekli izni alıyoruz.
    public void checkPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CAMERA}, 1
        );

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
                        editor.putString("ipadresiSharedPrefences", ip);
                        editor.commit();
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


}