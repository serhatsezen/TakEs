package app.tez.com.takes.block.ShareSc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import app.tez.com.takes.R;
import app.tez.com.takes.block.MainPage.BottomBarActivity;
import app.tez.com.takes.block.TCPSERVERCLIENT.PostEkleTamamlaServis;
import app.tez.com.takes.block.TCPSERVERCLIENT.PostGonderServis;
import app.tez.com.takes.block.UniversalImageLoader;


public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";

    //widgets
    private EditText mCaption;

    //vars
    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgUrl;
    private Bitmap bitmap;
    private Intent intent;
    private TextView location, lostOrFind, share;
    public String kategori = "";


    public String description;
    public String userNameU;
    public Uri imageUri = null;

    public EditText edtxCategory;

    private RecyclerView horizontal_recycler_view;
    private ArrayList<String> horizontalListkm;

    private RecyclerView horizontal_recycler_view_kategoriler;
    private ArrayList<String> horizontalListkategori;
    private HorizontalAdapterr horizontalAdapterkategori;
    private String category = "";

    String userEmail;
    String userAdSoyad;
    String userIpAdresi;

    String oturumuAcan;
    SharedPreferences myPrefs;

    String[] oturumuAcanVeriler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        myPrefs = this.getSharedPreferences("myPrefs", MODE_WORLD_READABLE);

        oturumuAcan = myPrefs.getString("OturumuAcan", "Default");

        if(!oturumuAcan.equals("Default")) {
            oturumuAcanVeriler = oturumuAcan.split("//");
            userAdSoyad = oturumuAcanVeriler[0];
            userEmail = oturumuAcanVeriler[1];
            userIpAdresi = oturumuAcanVeriler[2];
        }

        horizontal_recycler_view_kategoriler = (RecyclerView) findViewById(R.id.horizontal_recycler_view_kategoriler);

        horizontalListkategori = new ArrayList<>();
        horizontalListkategori.add("Kart");
        horizontalListkategori.add("Bisiklet");
        horizontalListkategori.add("Elektronik");
        horizontalListkategori.add("Cüzdan");
        horizontalListkategori.add("Kırtasiye");
        horizontalListkategori.add("Çanta");
        horizontalListkategori.add("Anahtar");
        horizontalListkategori.add("Aksesuar");


        horizontalAdapterkategori = new HorizontalAdapterr(horizontalListkategori);

        LinearLayoutManager horizontalLayoutManagaerKategori = new LinearLayoutManager(NextActivity.this, LinearLayoutManager.HORIZONTAL, false);

        horizontal_recycler_view_kategoriler.setLayoutManager(horizontalLayoutManagaerKategori);

        horizontal_recycler_view_kategoriler.setAdapter(horizontalAdapterkategori);

        //------------------------------------ for select category


        edtxCategory = (EditText) findViewById(R.id.edtxCategory);

        mCaption = (EditText) findViewById(R.id.caption);

        setImage();

        share = (TextView) findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description = mCaption.getText().toString().trim();

                if (intent.hasExtra(getString(R.string.selected_image))) {
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    imageUri = Uri.fromFile(new File(imgUrl));

                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    imageUri = intent.getParcelableExtra(getString(R.string.selected_bitmap));
                }
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(NextActivity.this, "Açıklamaya bir şey yazmadınız!", Toast.LENGTH_LONG).show();
                } else if (category.equals("")) {
                    Toast.makeText(NextActivity.this, "Kategori seçmediniz!", Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(NextActivity.this, "Paylaşılıyor!", Toast.LENGTH_LONG).show();
                    share.setText("Bekleyiniz!");

                    postEkleServiceCagir();

                    Intent bottombar = new Intent(NextActivity.this, BottomBarActivity.class);
                    startActivity(bottombar);
                }

            }
        });


    }

    public void postEkleServiceCagir() {
        String postBilgileri = description + "/" + userAdSoyad + "/" + userIpAdresi + "/" + imageUri;

        Intent intent = new Intent(NextActivity.this, PostGonderServis.class);
        intent.putExtra("postBilgileri", postBilgileri);
        startService(intent);

    }


    public class HorizontalAdapterr extends RecyclerView.Adapter<HorizontalAdapterr.MyViewHolder> {

        private List<String> horizontalListKategori;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView txtView;

            public MyViewHolder(View view) {
                super(view);
                txtView = (TextView) view.findViewById(R.id.txtView);
            }
        }

        public HorizontalAdapterr(List<String> horizontalList) {
            this.horizontalListKategori = horizontalList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.horizontal_item_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.txtView.setText(horizontalListKategori.get(position));

            LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpp.setMargins(10, 0, 10, 0);
            lpp.gravity = Gravity.LEFT;
            holder.txtView.setLayoutParams(lpp);
            holder.txtView.setBackgroundResource(R.drawable.categorycolor);
            holder.txtView.setTextColor(getResources().getColor(R.color.primary_darker));

            holder.txtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    edtxCategory.setText(holder.txtView.getText().toString());
                    category = holder.txtView.getText().toString();

                }
            });
        }

        @Override
        public int getItemCount() {
            return horizontalListKategori.size();
        }
    }

    private void setImage() {
        intent = getIntent();
        ImageView image = (ImageView) findViewById(R.id.imageShare);

        if (intent.hasExtra(getString(R.string.selected_image))) {
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            imageUri = Uri.fromFile(new File(imgUrl));
            UniversalImageLoader.setImage(imgUrl, image, null, mAppend);
        } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            imageUri = intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Glide.with(NextActivity.this.getApplicationContext())
                    .load(imageUri)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(image);
        }
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareact = new Intent(NextActivity.this, ShareActivity.class);
                startActivity(shareact);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NextActivity.this, ShareActivity.class);
        startActivity(intent);
    }
}
