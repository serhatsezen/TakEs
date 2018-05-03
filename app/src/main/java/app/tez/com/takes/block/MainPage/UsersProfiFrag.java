package app.tez.com.takes.block.MainPage;

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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import app.tez.com.takes.R;

import static android.app.Activity.RESULT_OK;

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
    private SharedPreferences.Editor editor;
    public static final String PREFS = "MyPrefs";
    public String themeStr;
    public String currentusername;

    public UsersProfiFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.user_profil_frag, container, false);
        manager = getFragmentManager();
        //get firebase auth instance
        tlUserProfileTabs = (RelativeLayout) v.findViewById(R.id.tlUserProfileTabs);

        sharedpreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        themeStr = sharedpreferences.getString("theme", "DayTheme");          //eğer null ise DayTheme


        u_fullname = (TextView) v.findViewById(R.id.fullnameuser);
        u_username = (TextView) v.findViewById(R.id.usernameprof);
        u_city = (TextView) v.findViewById(R.id.city);

        profileImg = (ImageView) v.findViewById(R.id.ivUserProfilePhoto);
        backgroundImg = (ImageView) v.findViewById(R.id.imageView3);
        dm_imgBtn = (ImageButton) v.findViewById(R.id.dm_imgBtn_another);

        editprof = (ImageButton) v.findViewById(R.id.edit_prof_btn);

        return v;
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
}