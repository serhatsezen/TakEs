package app.tez.com.takes.block.MainPage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import app.tez.com.takes.R;
import app.tez.com.takes.block.KayitGiris.GirisEkrani;
import app.tez.com.takes.block.Models.DeviceDTO;
import app.tez.com.takes.block.TCPSERVERCLIENT.KullaniciKayitServisi;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class UsersProfiFrag extends Fragment {


    public TextView u_fullname, u_username, u_city;
    public ImageView profileImg, backgroundImg;
    public View backgroundView;
    public TextView num_post;
    public ImageButton editprof, dm_imgBtn;

    public static final int GALLERY_REQUEST = 1;
    public static final int CAMERA_REQUEST_CODE = 2;

    public RecyclerView profileList;

    public Uri mProfImageUri = null;
    public Uri mBackImageUri = null;

    public RelativeLayout tlUserProfileTabs;
    public boolean backgroundImage = false;
    public boolean profileImage = false;
    public String currentUserId;
    public String LostCount;
    FragmentManager manager;

    private SharedPreferences sharedpreferences;
    public static final String PREFS = "MyPrefs";
    public String themeStr;
    public String currentusername;

    SharedPreferences myPrefs;
    SharedPreferences.Editor editor;

    String oturumuAcan;

    private static File fileDirectory = null;//Main Directory File
    private static final String DirectoryName = "TakES";//Main Directory Name
    private static final String FileName = "veritabani.txt";//Text File Name

    JSONArray veritabani = new JSONArray();
    JSONObject nesneler;
    String password = "takesPass";
    String kayıtolusturan, oturumuAcanMail;
    String decryptkayıtolusturan, decryptkayıtolusturanMail;

    static final int SocketServerPORT = 8080;

    SharedPreferences sharedPrefs;
    public static final String CIHAZLAR = "cihazlarshared";

    String kayitOlanBilgileri, socketIp, ip;

    KullaniciKayitServisi.ClientRxThread clientRxThread;


    double coin = 0.0;
    String strCoin;

    public UsersProfiFrag() {
        // Required empty public constructor
    }


    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.user_profil_frag, container, false);
        manager = getFragmentManager();
        //get firebase auth instance
        tlUserProfileTabs = (RelativeLayout) v.findViewById(R.id.tlUserProfileTabs);
        vritabaniYukle();

        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) v.findViewById(R.id.result_tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER);

        sharedpreferences = getActivity().getSharedPreferences(PREFS, MODE_PRIVATE);
        editor = sharedpreferences.edit();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        myPrefs = getActivity().getSharedPreferences("myPrefs", MODE_PRIVATE);

        oturumuAcan = myPrefs.getString("OturumuAcan", "Hepsi");

        String[] kullaniciBilgileri = oturumuAcan.split("//");

        oturumuAcanMail = kullaniciBilgileri[1];
        coin = 0.0;
//        String oturmuAcan = decryptAdSoyad + "//" + decryptMail + "//" + decryptSifre + "//" + ip + "//" + userCoin;

        try {
            for (int sira = 0; sira < veritabani.length(); sira++) {
                nesneler = veritabani.getJSONObject(sira);
                try {
                    kayıtolusturan = nesneler.getString("kayıtolusturan");
                    decryptkayıtolusturan = AESCrypt.decrypt(password, kayıtolusturan);
                    String[] coinHesap = decryptkayıtolusturan.split("\\+");
                    decryptkayıtolusturanMail = coinHesap[0];

                    if (decryptkayıtolusturanMail.equals(oturumuAcanMail)) {
                        coin += 0.002;
                    }
                } catch (Exception e) {

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        u_fullname = (TextView) v.findViewById(R.id.fullnameuser);
        u_username = (TextView) v.findViewById(R.id.usernameprof);
        u_city = (TextView) v.findViewById(R.id.city);

        profileImg = (ImageView) v.findViewById(R.id.ivUserProfilePhoto);
        backgroundImg = (ImageView) v.findViewById(R.id.imageView3);
        dm_imgBtn = (ImageButton) v.findViewById(R.id.dm_imgBtn_another);


        editprof = (ImageButton) v.findViewById(R.id.edit_prof_btn);
        editprof.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        u_fullname.setText(kullaniciBilgileri[0]);
        u_username.setText(String.valueOf(coin));

        return v;
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bir işlem seç")
                .setItems(R.array.editButtonOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                signOut();
                                break;
                            default:
                                break;
                        }

                    }
                });
        builder.create();
        builder.show();
    }


    public void signOut() {
        myPrefs.edit().remove("OturumuAcan").commit();
        Intent intent = new Intent(getActivity(), GirisEkrani.class);
        startActivity(intent);
    }
    private void setupViewPager(ViewPager viewPager) {


        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new UserSattiklarim_Fragment(), "Sattıklarım");
        viewPager.setAdapter(adapter);
    }
    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onResume() {        // click back button

        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    manager.popBackStack();
                }
                return false;
            }
        });
    }

    //---------- JSON Dosyasındaki verileri burada uygulamaya yüklüyoruz.
    public void vritabaniYukle() {
        fileDirectory = new File(Environment.getExternalStorageDirectory() + "/" + DirectoryName);

        if (!fileDirectory.exists())
            fileDirectory.mkdir();

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


}