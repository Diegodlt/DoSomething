package group6.spring16.cop4656.floridapoly.navfragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import group6.spring16.cop4656.floridapoly.MainActivity;
import group6.spring16.cop4656.floridapoly.R;
import group6.spring16.cop4656.floridapoly.util.SelectPhotoDialog;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements SelectPhotoDialog.OnPhotoSelectedListener {

    private OnFragmentInteractionListener mListener;

    private static final String TAG = "SettingsFragment";

    // Views
    private ImageView profilePicture;
    private EditText userNameEditText;

    // Function global variables
    private Bitmap selectedImageBitmap;
    private Uri selectedImageUri;
    private byte[] pictureBytes;
    private String userValue;
    private double mProgress = 0;
    private static final int REQUEST_CODE = 1;

    // FireBase Global variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference userStorageRef;
    private StorageReference pictureReference;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(final @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // FireBase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser userID = mAuth.getCurrentUser();
        userStorageRef = FirebaseStorage.getInstance().getReference();
        assert userID != null;
        userValue = userID.getUid();
        pictureReference = userStorageRef.child("users/" +
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/" + "profilePicture");

        // Set up views
        profilePicture = view.findViewById(R.id.profilePicture);
        TextView emailTextView = view.findViewById(R.id.emailTextView);
        userNameEditText = view.findViewById(R.id.userNameEditText);

        // Buttons
        Button signOutButton = view.findViewById(R.id.signOutButton);
        // Buttons
        Button saveButton = view.findViewById(R.id.saveButton);

        // Set view values
        emailTextView.setText(userID.getEmail());
        emailTextView.setTextColor(Color.BLACK);

        try {
            Log.e(TAG, "Setting user profile picture");
            downloadUserInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Buttons on click listeners
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Save button pressed");
                String userName = userNameEditText.getText().toString().trim();
                if (userName.isEmpty()){
                    Toast.makeText(getContext(),"Please enter a user name", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> userNameEdit = new HashMap<>();
                    userNameEdit.put("User Name", userName);
                    db.collection("users").document(userValue).set(userNameEdit,
                            SetOptions.merge());
                    Toast.makeText(getContext(), "User name updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Sign out button pressed");
                mAuth.signOut();
                Intent mainScreen = new Intent(getActivity(), MainActivity.class);
                startActivity(mainScreen);
            }
        });

        // Set profile picture updater click listener
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Profile picture clicked to update");
                verifyPermissions();
            }
        });

        return view;
    }


    private void verifyPermissions(){
        Log.e(TAG, "verifyPermissions: asking user for permissions");

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        // If permission are met, open dialog box
        if(ContextCompat.checkSelfPermission(Objects.requireNonNull(this.getActivity()).getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED){
            myDialogBox();
        }else{
            ActivityCompat.requestPermissions(this.getActivity(), permissions,REQUEST_CODE);
            verifyPermissions();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        verifyPermissions();
    }


    private void myDialogBox(){
        Log.e(TAG,"myDialogBox: Opening dialog box");
        SelectPhotoDialog dialog = new SelectPhotoDialog();
        assert getFragmentManager() != null;
        dialog.show(getFragmentManager(),getString(R.string.dialog_select_photo));
        dialog.setTargetFragment(SettingsFragment.this, 1);
    }


    // Functions to catch the result from dialog box
    @Override
    public void getImagePath(Uri imagePath) throws IOException {
        Log.d(TAG, "getImagePath: setting the image to image view");
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getContext()).getContentResolver(), imagePath);
        profilePicture.setImageBitmap(bitmap);
        //assign to global variable
        selectedImageBitmap = null;
        selectedImageUri = imagePath;
        uploadNewPhoto(selectedImageUri);
    }
    @Override
    public void getImageBitmap(Bitmap bitmap) {
        Log.d(TAG, "getImageBitmap: setting the image to image view");
        profilePicture.setImageBitmap(bitmap);
        //assign to a global variable
        selectedImageUri = null;
        selectedImageBitmap = bitmap;
        uploadNewPhoto(selectedImageBitmap);
    }


    private void uploadNewPhoto(Bitmap bitmap){
        Log.d(TAG, "uploadNewPhoto: uploading a new image bitmap to storage");
        BackgroundImageResize resize = new BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }


    private void uploadNewPhoto(Uri imagePath){
        Log.d(TAG, "uploadNewPhoto: uploading a new image uri to storage.");
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imagePath);
    }


    @SuppressLint("StaticFieldLeak")
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]>{
        Bitmap mBitmap;
        BackgroundImageResize(Bitmap bitmap) {
            if(bitmap != null){
                this.mBitmap = bitmap;
            }
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), "compressing image", Toast.LENGTH_SHORT).show();
        }
        @Override
        protected byte[] doInBackground(Uri... params) {
            Log.d(TAG, "doInBackground: started.");
            if(mBitmap == null){
                try{
                    mBitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(),
                            params[0]);
                }catch (IOException e){
                    Log.e(TAG, "doInBackground: IOException: " + e.getMessage());
                }
            }
            return getBytesFromBitmap(mBitmap);
        }
        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            pictureBytes = bytes;
            String picture = "picture";
            executeUploadTask(picture);
        }
    }


    // Method to perform image upload
    private void executeUploadTask(String file){

            UploadTask uploadPicture = pictureReference.putBytes(pictureBytes);
            uploadPicture.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getActivity(), "Profile picture updated!", Toast.LENGTH_SHORT).show();

                    //insert the download URL into the FireBase database
                    userStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "Could not upload photo", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    if (currentProgress > (mProgress + 15)) {
                        mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Toast.makeText(getActivity(), mProgress + "%", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }


    // Method to convert the Bitmap picture to an array of bytes
    private static byte[] getBytesFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100,imageStream);
        return imageStream.toByteArray();
    }


    public void downloadUserInfo() throws IOException {
        final File userImage = File.createTempFile("image", "jpg");
        pictureReference.getFile(userImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                String userImagePath = userImage.getPath();
                Bitmap userImageBitmap = BitmapFactory.decodeFile(userImagePath);
                profilePicture.setImageBitmap(userImageBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("DownloadImage", "No image found");
            }
        });

        db.collection("users").document(userValue).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                String userName = document.getString("User Name");
                userNameEditText.setText(userName);
                userNameEditText.setTextColor(Color.BLACK);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Download user id", "No user id found");
            }
        });
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
