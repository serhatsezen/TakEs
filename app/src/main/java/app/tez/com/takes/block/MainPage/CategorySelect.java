package app.tez.com.takes.block.MainPage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import app.tez.com.takes.R;

public class CategorySelect extends Fragment {


    private GridView categorylist;
    public String currentUserId;
    public String str;
    private SharedPreferences sharedpreferences;
    public static String themeStr;
    private static AppBarLayout appBarLayout;

    final ArrayList<String> categorynames = new ArrayList<String>();
    final ArrayList<Category> categoriesInformation = new ArrayList<Category>();
    CategoryAdapter categoryAdapter;
    Typeface tf1;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final String PREFS = "MyPrefs";

    public TextView selectedCategory;
    public TextView toolbarText;
    public TextView categoryAll;
    public TextView txtategory;
    public LinearLayout gridLinear;
    public String category;

    public CategorySelect() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_category_select, container, false);

        categorylist = (GridView) v.findViewById(R.id.categorylist);
        selectedCategory = (TextView) v.findViewById(R.id.selectedCategory);
        categoryAll = (TextView) v.findViewById(R.id.categoryAll);
        toolbarText = (TextView) v.findViewById(R.id.textView);
        txtategory = (TextView) v.findViewById(R.id.txtategory);
        appBarLayout = (AppBarLayout) v.findViewById(R.id.kategoriBarLayout);
        gridLinear = (LinearLayout) v.findViewById(R.id.gridLinear);

        Typeface type = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Ubuntu-B.ttf");
        toolbarText.setTypeface(type);

        categoryAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("categoryShared", "Hepsi");
                editor.commit();
                selectedCategory.setText("Hepsi");


                Intent refresh = new Intent(getActivity(), BottomBarActivity.class);
                startActivity(refresh);//Start the same Activity

            }
        });

        sharedpreferences = getActivity().getSharedPreferences(PREFS, 0);
        category = sharedpreferences.getString("categoryShared", "Hepsi");          //eğer null ise DayTheme
        selectedCategory.setText(category);

        return v;


    }

    public void onStart() {
        super.onStart();


    }


    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();

    }


}
