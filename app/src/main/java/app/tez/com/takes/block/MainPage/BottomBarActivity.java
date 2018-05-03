package app.tez.com.takes.block.MainPage;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import app.tez.com.takes.R;
import app.tez.com.takes.block.Models.OturumuAcan;
import app.tez.com.takes.block.ShareSc.ShareActivity;

public class BottomBarActivity extends AppCompatActivity {

    private static final String TAG_FRAGMENT_NEWS = "tag_frag_news";
    private static final String TAG_FRAGMENT_SHARE = "tag_frag_share";
    private static final String TAG_FRAGMENT_PROFILE = "tag_frag_profile";
    private static final String TAG_FRAGMENT_DM_LIST = "tag_frag_dm_list";

    Context ctx;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final String PREFS = "MyPrefs";

    OturumuAcan oturumuAcan;

    private BottomNavigationView bottomNavigationView;

    /**
     * Maintains a list of Fragments for {@link BottomNavigationView}
     */
    private List<MainPage> fragments = new ArrayList<>(1);
    private List<UsersProfiFrag> fragmentsPro = new ArrayList<>(1);

    protected void onStart() {
        checkPermissions();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_bar);
        preferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        Intent intent = getIntent();

        oturumuAcan = (OturumuAcan) intent.getSerializableExtra("OturumuAcan");

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_bottombar_main:
                                switchFragment(0, TAG_FRAGMENT_NEWS);
                                return true;
                            case R.id.action_bottombar_share:
                                Intent shareint = new Intent(BottomBarActivity.this, ShareActivity.class);
                                shareint.putExtra("OturumuAcan", oturumuAcan);
                                startActivity(shareint);
                                return true;
                            case R.id.action_bottombar_profil:
                                switchFragment2(0, TAG_FRAGMENT_PROFILE);
                                return true;
                        }
                        return false;
                    }
                });
        preferences = getSharedPreferences(PREFS, 0);
        editor = preferences.edit();

        buildFragmentsList();

        // Set the 0th Fragment to be displayed by default.
        switchFragment(0, TAG_FRAGMENT_NEWS);

    }

    private void switchFragment(int pos, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.group, fragments.get(pos), "tag_frag_main")
                .addToBackStack("addmain")
                .commit();
    }

    private void switchFragment2(int pos, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.group, fragmentsPro.get(pos), "tag_frag_profile")
                .addToBackStack("addprof")
                .commit();
    }

    private void buildFragmentsList() {
        MainPage mainScreen = buildFragment();
        UsersProfiFrag profileScreen = buildFragmentt();

        fragments.add(mainScreen);
        fragmentsPro.add(profileScreen);

    }


    private MainPage buildFragment() {
        MainPage fragment = new MainPage();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }


    private UsersProfiFrag buildFragmentt() {
        UsersProfiFrag fragment = new UsersProfiFrag();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {     //backpress
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            FragmentManager manager = getFragmentManager();
            if (manager.getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
                super.onBackPressed();
            } else {
                moveTaskToBack(false);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    // API 23 ve üzeri için gerekli.
    public void checkPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                permission.ACCESS_COARSE_LOCATION,
                permission.ACCESS_FINE_LOCATION,
                permission.WRITE_EXTERNAL_STORAGE}, 1
        );
    }
}