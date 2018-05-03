package app.tez.com.takes.block.MainPage;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import app.tez.com.takes.R;


public class  MainPage extends Fragment {


    private ImageButton commentButton;
    private RecyclerView shareList;

    private static String questionName = null;


    private static String question = null;
    private static String username = null;

    public static final String ARG_TITLE = "arg_title";
    public String user_key = null;
    public static String userNames = null;
    public String cityFilter = "Genel";
    ProgressBar imgPg;

    private String latUsers;
    private String lngUsers;

    private String latPost;
    private String lngPost;

    double latPostL;
    double lngPostL;

    double latUserL;
    double lngUserL;
    double dist;
    String distStr;

    private final static String TAG_FRAGMENT = "TAG_FRAGMENT";
    FragmentManager manager;

    public MainPage() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main_screen, container, false);

        manager = getFragmentManager();
        //get firebase auth instance

        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager_mainpage);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) v.findViewById(R.id.main_page_tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER);

        return v;
    }


    private void setupViewPager(ViewPager viewPager) {

        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new CategorySelect(), "Kategoriler");
        adapter.addFragment(new AnaSayfa(), "Kayıp Eşyalar");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
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
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){

                    MainPage fragmentMain = (MainPage) manager.findFragmentByTag(getTag());
                    getFragmentManager()
                            .beginTransaction()
                            .remove(fragmentMain)
                            .commit();
                }
                return false;
            }
        });
    }
}
