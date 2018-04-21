package app.tez.com.takes.block.anasayfa;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.scottyab.aescrypt.AESCrypt;

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
import java.util.Enumeration;

import app.tez.com.takes.R;
import app.tez.com.takes.block.socket.ClientAct;
import app.tez.com.takes.block.socket.ServerService;
import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {
    Button girisBtn;
    TextView kayitOlBtn;
    EditText sifre, email;
    JSONArray veritabani = new JSONArray();
    JSONObject girisKontrolNesne;
    String useremail, usersifre, edtxEmail, edtxSifre;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.json";//Text File Name

    static final int SocketServerPORT = 8080;
    String ip = "";
    String lastip = "";
    Boolean truefalse = false;
    AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.girisekrani);
        dialog = new SpotsDialog(MainActivity.this, R.style.Custom);



        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);

        if (!fileDirectory.exists())
            fileDirectory.mkdir();

        IzinKontrolu();


        Intent servIntent = new Intent(MainActivity.this, ServerService.class);
        startService(servIntent);

        getIpAddress();


        String[] parts = ip.split("\\.");
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        String lip = part1 + "." + part2 + "." + part3 + ".";

        ClientRxThread clientRxThread =
                new ClientRxThread(
                        lip,
                        SocketServerPORT);

        clientRxThread.start();


        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });

        kayitOlBtn = (TextView) findViewById(R.id.link_signup);
        sifre = (EditText) findViewById(R.id.input_password);
        email = (EditText) findViewById(R.id.input_email);
        girisBtn = (Button) findViewById(R.id.btn_login);


        kayitOlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, KayitOlEkrani.class);
                i.putExtra("veritabani", veritabani.toString());
                startActivity(i);
                finish();
            }
        });

        girisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtxEmail = email.getText().toString();
                edtxSifre = sifre.getText().toString();

                String password = "takesPass";

                try {
                    for(int sira=0; sira<veritabani.length();sira++){
                        girisKontrolNesne = veritabani.getJSONObject(sira);
                        useremail = girisKontrolNesne.getString("email");
                        usersifre = girisKontrolNesne.getString("sifre");
                        String decryptMail = AESCrypt.decrypt(password, useremail);
                        String decryptSifre = AESCrypt.decrypt(password, usersifre);

                        if (decryptMail.equals(edtxEmail) && decryptSifre.equals(edtxSifre)) {
                            Intent anaSayfa = new Intent(MainActivity.this, ClientAct.class);
                            startActivity(anaSayfa);
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

    //------- Burada Uygulamada Dosya yazma-okuma işlemleri için gerekli izni alıyoruz.
    public void IzinKontrolu() {
        String[] perms = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE};
        int permsRequestCode = 67;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                VeritabaniYukle();
            } else {
                requestPermissions(perms, permsRequestCode);
                IzinKontrolu();
            }
        } else {
            VeritabaniYukle();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 67:
                boolean izin = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
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
                    if ((!ip.equals(lastip)) && truefalse == false) {
                        socket = new Socket(lastip, dstPort);

                        File file = new File(fileDirectory.getAbsolutePath() + "/" + FileName);

                        byte[] bytes = new byte[1024];
                        InputStream is = socket.getInputStream();
                        FileOutputStream fos = new FileOutputStream(file);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        int bytesRead;
                        while ((bytesRead = is.read(bytes)) > 0) {
                            bos.write(bytes, 0, bytesRead);
                        }
                        bos.close();
                        socket.close();

                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                truefalse = true;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                    }
                                });
                                Toast.makeText(MainActivity.this,
                                        "Finished",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch(IOException e){

                    e.printStackTrace();

                    final String eMsg = "Something wrong: " + e.getMessage();
                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                        }
                    });

                } finally{
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

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                dialog.dismiss();
                }
            });
            if (truefalse == false) {
                Toast.makeText(MainActivity.this,
                        "Güncelleme Başarısız.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


}