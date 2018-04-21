package app.tez.com.takes.block.anasayfa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.UUID;

import app.tez.com.takes.R;
import app.tez.com.takes.block.SifreMailGonder.Mail;


public class KayitOlEkrani extends AppCompatActivity {
    Button kaydet, vazgec;
    EditText firsName, email;
    JSONArray veritabani;
    JSONObject prevNesne;
    String prevKey, useremail, edtxEmail, uuid, uid;
    int randomNumber;
    String kullaniciEmail, password;

    JSONObject yeniKayit = new JSONObject();
    JSONObject kayitKontrolNesne;

    Boolean varYok = false;
    String cryptedName,cryptedEmail,cryptedPass;

    String encrypPass = "takesPass";                            //şifreleme için key

    private static File fileDirectory = null;                   //Main Directory File
    private static final String DirectoryName = "TakES";        //Main Directory Name
    private static final String FileName = "veritabani.json";   //Text File Name


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kayitekrani);

        kaydet = (Button) findViewById(R.id.btn_signup);
        firsName = (EditText) findViewById(R.id.input_name);
        email = (EditText) findViewById(R.id.input_email);
        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);

        try {
            veritabani = new JSONArray(getIntent().getSerializableExtra("veritabani").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        kaydet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtxEmail = email.getText().toString();
                uuid = UUID.randomUUID().toString();                //chain id
                varYok = false;

                if (!firsName.getText().toString().equals("") && !edtxEmail.equals("")) {
                    try {
                        if (veritabani.length() > 0) {                          //veritabanında kayıt varsa
                            for (int t = 0; t < veritabani.length(); t++) {
                                kayitKontrolNesne = veritabani.getJSONObject(t);    //her bir blogu nesne olarak alıyoruz
                                useremail = kayitKontrolNesne.getString("email");  //email aldık.
                                uid = kayitKontrolNesne.getString("chainId");      //chainId yi aldık.
                                String decryptMail = AESCrypt.decrypt(encrypPass, useremail); //emaili decrypt ediyoruz
                                if (decryptMail.equals(edtxEmail)){         // varmı yok mu kontrol ediyoruz
                                    alertMessage("Hata","kayit","Bu mail adresiyle zaten hesap oluşturulmuş.");
                                    varYok = true;
                                }
                                if (uid.equals(uuid)) {     // eğer chain ıd aynı olursa yenisini oluşturuyoruz.        ***************************---------------------- TEKRAR BAK!! -------------------------*************
                                    uuid = UUID.randomUUID().toString();
                                }
                            }
                        } else {
                            kayitOl();
                            varYok = true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        //
                    }

                    if (varYok == false) {
                        kayitOl();
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
                prevKey = prevNesne.getString("chainId");                           //o nesnenin chain id sini aldık


                try {                                                                     // kullanıcı verilerini şifreledik.
                    cryptedName = AESCrypt.encrypt(encrypPass, firsName.getText().toString());
                    cryptedEmail = AESCrypt.encrypt(encrypPass, kullaniciEmail);
                    cryptedPass = AESCrypt.encrypt(encrypPass, password);
                }catch (GeneralSecurityException e){
                    //handle error
                }


                yeniKayit.put("id", uuid);
                yeniKayit.put("prevKey", prevKey);
                yeniKayit.put("adi", cryptedName);
                yeniKayit.put("email", cryptedEmail);
                yeniKayit.put("sifre", cryptedPass);

            } else {
                String encrypPass = "takesPass";

                try {
                    cryptedName = AESCrypt.encrypt(encrypPass, firsName.getText().toString());
                    cryptedEmail = AESCrypt.encrypt(encrypPass, kullaniciEmail);
                    cryptedPass = AESCrypt.encrypt(encrypPass, password);
                }catch (GeneralSecurityException e){
                    //handle error
                }

                yeniKayit.put("id", uuid);
                yeniKayit.put("adi", cryptedName);
                yeniKayit.put("email", cryptedEmail);
                yeniKayit.put("sifre", cryptedPass);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendMail().execute("");
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
            File dosya = new File(fileDirectory.getAbsolutePath() + "/" + FileName);
            FileOutputStream fileOutputStream = new FileOutputStream(dosya);
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
                })
                .show();
    }

}
