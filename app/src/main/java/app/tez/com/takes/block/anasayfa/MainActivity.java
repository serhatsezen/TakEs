package app.tez.com.takes.block.anasayfa;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;

import app.tez.com.takes.R;
import app.tez.com.takes.block.TCPSERVERCLIENT.CihazlarServis;
import app.tez.com.takes.block.TCPSERVERCLIENT.PortAcServis;
import app.tez.com.takes.block.TCPSERVERCLIENT.ServerService;

public class MainActivity extends AppCompatActivity {
    Button girisBtn;
    TextView kayitOlBtn;
    EditText sifre, email;
    JSONArray veritabani = new JSONArray();
    JSONObject girisKontrolNesne;
    String useremail, usersifre, edtxEmail, edtxSifre;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name
    String ip = "";
    public String isFirstTime;
    public static final String FIRST_TIME = "first_time";
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;


    @SuppressLint("WifiManagerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.girisekrani);

        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);

        if (!fileDirectory.exists())
            fileDirectory.mkdir();

        sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPrefs.edit();


        checkPermissions();

        vritabaniYukle();

        getIpAddress();


        Intent serverServisIntent = new Intent(MainActivity.this, ServerService.class);
        serverServisIntent.putExtra("veritabani",veritabani.toString());
        startService(serverServisIntent);

        Intent cihazlarServis = new Intent(MainActivity.this, CihazlarServis.class);
        startService(cihazlarServis);

        Intent portServis = new Intent(MainActivity.this, PortAcServis.class);
        startService(portServis);


        kayitOlBtn = (TextView) findViewById(R.id.link_signup);
        sifre = (EditText) findViewById(R.id.input_password);
        email = (EditText) findViewById(R.id.input_email);
        girisBtn = (Button) findViewById(R.id.btn_login);

        email.setText("serhattsezen@gmail.com");
        sifre.setText("UPVaWMNqSII6aigGpWv96W");

        kayitOlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, KayitOlEkrani.class);
                i.putExtra("veritabani", veritabani.toString());
                startActivity(i);
            }
        });

        girisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MainActivity.class);
//                i.putExtra("veritabani", veritabani.toString());
                startActivity(i);
//                edtxEmail = email.getText().toString();
//                edtxSifre = sifre.getText().toString();
//
//                String password = "takesPass";
//
//                try {
//                    for(int sira=0; sira<veritabani.length();sira++){
//                        girisKontrolNesne = veritabani.getJSONObject(sira);
//                        useremail = girisKontrolNesne.getString("email");
//                        usersifre = girisKontrolNesne.getString("sifre");
//                        String decryptMail = AESCrypt.decrypt(password, useremail);
//                        String decryptSifre = AESCrypt.decrypt(password, usersifre);
//
//                        if (decryptMail.equals(edtxEmail) && decryptSifre.equals(edtxSifre)) {
//
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (GeneralSecurityException e) {
//                    //handle error - could be due to incorrect password or tampered encryptedMsg
//                }

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
    public void checkPermissions(){
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE}, 1
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