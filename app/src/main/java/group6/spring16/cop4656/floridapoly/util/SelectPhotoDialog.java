package group6.spring16.cop4656.floridapoly.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v4.app.DialogFragment;

import java.io.IOException;

import group6.spring16.cop4656.floridapoly.R;


public class SelectPhotoDialog extends DialogFragment{

    private static final String TAG = "SelectPhotoDialog";
    private static final int PICKFILE_REQUEST_CODE = 11;
    private static final int CAMERA_REQUEST_CODE = 12;

    //Interface method
    public interface OnPhotoSelectedListener{
        void getImagePath(Uri imagePath) throws IOException;
        void getImageBitmap(Bitmap bitmap);
    }

    //Interface object
    private OnPhotoSelectedListener mOnPhotoSelectedListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_selectphoto, container,false);

        TextView selectPhoto = view.findViewById(R.id.dialogChoosePhoto);
        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick accessing phone's memory.");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICKFILE_REQUEST_CODE);
            }
        });

        TextView takePhoto = view.findViewById(R.id.dialogOpenCamera);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: starting camera.");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });
        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Results when selecting image from memory
        if(requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Uri selectedImageUri = data.getData();
            Log.d(TAG, "OnActivityResult: image Uri: " + selectedImageUri);
            try {
                mOnPhotoSelectedListener.getImagePath(selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            getDialog().dismiss();
        }

        // Results when taking a new photo with camera
        else if(requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Log.d(TAG, "OnActivityResult: done taking a photo");
            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");
            mOnPhotoSelectedListener.getImageBitmap(bitmap);
            getDialog().dismiss();
        }
    }
    @Override
    public void onAttach(Context context) {
        try{
            mOnPhotoSelectedListener = (OnPhotoSelectedListener) getTargetFragment();
        }catch (ClassCastException e){
            Log.e(TAG,"onAttach: ClassCastException: " + e.getMessage() );
        }
        super.onAttach(context);
    }
}


