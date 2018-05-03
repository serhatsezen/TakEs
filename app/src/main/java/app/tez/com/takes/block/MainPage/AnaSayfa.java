package app.tez.com.takes.block.MainPage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.tez.com.takes.R;
import app.tez.com.takes.block.Adapter.PostAdapter;
import app.tez.com.takes.block.KayitGiris.GirisEkrani;
import app.tez.com.takes.block.Models.OturumuAcan;
import app.tez.com.takes.block.Models.PostDTO;

public class AnaSayfa extends Fragment {

    private static ListView lost_main_list;
    private static RelativeLayout relativeLayLost_Main;
    private static AppBarLayout appBarLayout;


    SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;

    public static final String PREFS = "MyPrefs";
    FragmentManager manager;
    public String category;


    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name
    JSONArray veritabani = new JSONArray();

    ArrayList<PostDTO> postDTO;
    ListView listView;
    private static PostAdapter postAdapter;
    JSONObject girisKontrolNesne;
    String resimAdi, postDescription, userAdSoyad;
    String decryptResim, decryptDescription, decryptAdSoyad;
    String password = "takesPass";


    public AnaSayfa() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_lost_main, container, false);

        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);

        if (!fileDirectory.exists())
            fileDirectory.mkdir();


        vritabaniYukle();


        manager = getFragmentManager();


        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);

        //for crate home button
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        //get firebase auth instance

        appBarLayout = (AppBarLayout) v.findViewById(R.id.LostappBarLayout);
        relativeLayLost_Main = (RelativeLayout) v.findViewById(R.id.relativeLayLost_Main);

        sharedpreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        category = sharedpreferences.getString("categoryShared", "Hepsi");

        lost_main_list = (ListView) v.findViewById(R.id.list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        postDTO = new ArrayList<>();

        try {
            for (int sira = 0; sira < veritabani.length(); sira++) {
                try {
                    girisKontrolNesne = veritabani.getJSONObject(sira);
                    userAdSoyad = girisKontrolNesne.getString("adsoyad");
                    postDescription = girisKontrolNesne.getString("description");
                    resimAdi = girisKontrolNesne.getString("resimAdi");

                    decryptAdSoyad = AESCrypt.decrypt(password, userAdSoyad);
                    decryptDescription = AESCrypt.decrypt(password, postDescription);
                    decryptResim = AESCrypt.decrypt(password, resimAdi);
                    postDTO.add(new PostDTO(decryptAdSoyad, decryptDescription, decryptResim));

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {

        }
        Collections.reverse(postDTO); // ADD THIS LINE TO REVERSE ORDER!

        postAdapter = new PostAdapter(postDTO, getActivity().getApplicationContext());

        lost_main_list.setAdapter(postAdapter);

        return v;
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

    public void onStart() {
        super.onStart();
    }


    @Override
    public void onResume() {        // click back button
        super.onResume();
    }
}
