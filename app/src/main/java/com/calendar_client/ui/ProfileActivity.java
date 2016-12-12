package com.calendar_client.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.calendar_client.R;
import com.calendar_client.data.User;
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.Data;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private User user;
    private EditText etUserName;
    private EditText etMobilePhone;
    private EditText etEmail;
    private TextView tvEditPassword;
    private Button btnEditUser;
    private ImageView imgViewUser;
    private Uri imgUri;
    private final Data dataHolder = Data.getInstance();
    private View view;
    public static final int CAMERA_REQUEST_CODE = 1;


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(this.getTitle());

        view = findViewById(R.id.layout);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userString = sharedPreferences.getString("user", "");
        if (!userString.equals("")) {
            Gson gson = new Gson();
            user = gson.fromJson(userString, User.class);
            Log.e("DEBUG", "user: " + userString);
        }
        etUserName = (EditText) findViewById(R.id.etUserName);
        etUserName.setText(user.getUserName());
        etMobilePhone = (EditText) findViewById(R.id.etMobilePhone);
        etMobilePhone.setText(user.getPhoneNumber());
        etEmail = (EditText) findViewById(R.id.etEmail);
        etEmail.setText(user.getEmail());
        tvEditPassword = (TextView) findViewById(R.id.tvEditPassword);
        tvEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
                LayoutInflater layout = getLayoutInflater();
                final View dialogView = layout.inflate(R.layout.change_password_dialog, null);
                alertDialogBuilder.setView(dialogView);
                alertDialogBuilder
                        .setPositiveButton(getResources().getString(R.string.profile_btn_update),
                                new DialogInterface.OnClickListener() {

                                    EditText oldPassword;
                                    EditText newPassword;
                                    EditText confirmNewPassword;

                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        oldPassword = (EditText) dialogView.findViewById(R.id.etOldPassword);
                                        newPassword = (EditText) dialogView.findViewById(R.id.etNewPassword);
                                        confirmNewPassword = (EditText) dialogView.findViewById(R.id.etConfirmPassword);
                                        String toastText = "";
                                        if (oldPassword.getText().toString().equals(user.getPassword())) {
                                            if (newPassword.getText().toString().equals(
                                                    confirmNewPassword.getText().toString())) {
                                                // save new password
                                                user.setPassword(newPassword.getText().toString());
                                                toastText = getResources().getString(R.string.dialog_success);
                                            } else {
                                                toastText = getResources().getString(R.string.dialog_fail);
                                            }
                                        } else {
                                            // password not correct
                                            toastText = getResources().getString(R.string.dialog_old_fail);
                                        }
                                        Toast.makeText(ProfileActivity.this, toastText, Toast.LENGTH_LONG).show();
                                    }
                                }).create().show();
            }
        });


                btnEditUser = (Button) findViewById(R.id.btnEditUser);
                btnEditUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        user.setUserName(etUserName.getText().toString());
                        user.setPhoneNumber(etMobilePhone.getText().toString());
                        user.setEmail(etEmail.getText().toString());
                        btnEditUser.setEnabled(false);
                        new UpdateUserTask().execute();
                    }
                });

                imgViewUser = (ImageView) findViewById(R.id.imgviewUser);
                if (user.getImage() != null){
                    Bitmap b = BitmapFactory
                            .decodeByteArray(user.getImage(),0,user.getImage().length);
                    imgViewUser.setImageBitmap(b);
                }

                imgViewUser.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onClick(View view) {
                        int permission;

                        if (Build.VERSION.SDK_INT < 23) {
                            permission = PermissionChecker.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            if (permission == PermissionChecker.PERMISSION_GRANTED) {
                                takePicture();
                            } else {
                                ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.CAMERA,
                                }, REQUEST_EXTERNAL_STORAGE);
                            }
                        } else { //api 23 and above
                            int permission1;
                            int permission2;
                            int permission3;
                            permission1 = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            permission2 = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                            permission3 = checkSelfPermission(Manifest.permission.CAMERA);

                            if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED ||permission3 != PackageManager.PERMISSION_GRANTED) {
                                // We don't have permission so prompt the user
                                // We don't have permission so prompt the user
                                requestPermissions(
                                        PERMISSIONS_STORAGE,
                                        REQUEST_EXTERNAL_STORAGE
                                );
                            } else {
                                takePicture();
                            }
                        }

                    }
                });

            }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                Intent intent = new Intent(ProfileActivity.this, CalendarActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void takePicture() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File img = null;
        try {
            img = File.createTempFile("temp", ".jpg", Environment.getExternalStorageDirectory());
            Log.e("delete file", "file delete: " + img.delete());
        } catch (IOException e) {
            Log.e("debug", "IOException- failed to create temporary file");
            e.printStackTrace();
        }

        imgUri = Uri.fromFile(img);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    takePicture();
                } else {
                    // Permission Denied
                    Toast.makeText(ProfileActivity.this, "Picture taking Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class UpdateUserTask extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean result = false;
            Gson gson = new Gson();
            String jsonUser = gson.toJson(user, User.class);
            URL url = null;
            try {
                url = new URL(ApplicationConstants.UPDATE_USER);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "text/plain");
                con.setRequestProperty("Accept", "text/plain");
                con.setRequestMethod("POST");

                OutputStream os = con.getOutputStream();
                os.write(jsonUser.getBytes("UTF-8"));
                os.flush();

                if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                // Response
                StringBuilder response = new StringBuilder();
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));

                String line;
                while ((line = input.readLine()) != null) {
                    response.append(line + "\n");
                }

                input.close();

                con.disconnect();

                if (response.toString().trim().equals("OK")) {
                    result = true;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result != null && result) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                String customerJSON = gson.toJson(user);
                editor.putString("user", customerJSON);
                editor.apply();
                btnEditUser.setEnabled(true);
                btnEditUser.setEnabled(true);
                showSnackBar(getResources().getString(R.string.dialog_update_success));
            } else {
                btnEditUser.setEnabled(true);
                showSnackBar(getResources().getString(R.string.dialog_update_fail));
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK ) {

            final LinearLayout l1 = (LinearLayout) findViewById(R.id.primaryLayout);
            final LinearLayout l2 = (LinearLayout) findViewById(R.id.progressLayout);
            class PicTask extends AsyncTask<Void,Void,Void> {
                private File file = new File( imgUri.getPath());
                private Bitmap bit = null;
                @Override
                protected Void doInBackground(Void... voids) {

                    try {
                        bit = Picasso.with(ProfileActivity.this).
                                load(file).get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] byteArray = null;
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Log.e("DEBUG", "Bitmap is :" + bit);
                    bit = Bitmap.createScaledBitmap(bit,350,350,false);
                    if( bit != null ) {
                        bit.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                        byteArray = stream.toByteArray();
                    }
                    user.setImage(byteArray);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    //Set it in the ImageView
                    imgViewUser.setImageBitmap(bit);
                    file.delete();
                    l1.setVisibility(View.VISIBLE);
                    l2.setVisibility(View.GONE);
                    super.onPostExecute(aVoid);
                }
            }

            l1.setVisibility(View.GONE);
            l2.setVisibility(View.VISIBLE);
            new PicTask().execute();
            Log.d("uri","IMG URI: " + imgUri);
        }
    }


    private void showSnackBar(String text){
        Snackbar snackbar = Snackbar
                .make(view, text, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(ProfileActivity.this, android.R.color.white));
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.BLACK);
        snackbar.show();
    }

}
