package app.tez.com.takes.block.ShareSc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import app.tez.com.takes.R;
import app.tez.com.takes.block.Permissions;


public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";

    //constant
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 2;
    private static final int  CAMERA_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Log.d(TAG, "onCreateView: started.");

        Button btnLaunchCamera = (Button) view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: launching camera.");

                if(((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM){
                    if(((ShareActivity)getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0])){
                        Log.d(TAG, "onClick: starting camera");
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    }else{
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });

        return view;
    }

    private boolean isRootTask(){
        if(((ShareActivity)getActivity()).getTask() == 0){
            return true;
        }
        else{
            return false;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE){
            Log.d(TAG, "onActivityResult: done taking a photo.");
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen.");
            Bitmap bitmap;
            Uri uri = null;

            try {
                bitmap = (Bitmap) data.getExtras().get("data");
                uri = getImageUri(getActivity().getApplicationContext(), bitmap);

            }catch (Exception e){

            }

            if(isRootTask()){
                if(uri != null) {
                    try {
                        Intent intent = new Intent(getActivity(), NextActivity.class);
                        intent.putExtra(getString(R.string.selected_bitmap), uri);
                        startActivity(intent);
                    } catch (NullPointerException e) {
                        Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                    }
                }else{
                    Intent intent = new Intent(getActivity(), ShareActivity.class);
                    startActivity(intent);
                }
            }else{
                try{
                    Log.d(TAG, "onActivityResult: received new bitmap from camera: " + uri);
                }catch (NullPointerException e){
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                }
            }

        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}