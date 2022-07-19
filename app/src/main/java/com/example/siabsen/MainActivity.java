package com.example.siabsen;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;


import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    Session sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        MaterialButton btn_login = findViewById(R.id.btn_login);
        TextInputEditText username = findViewById(R.id.login_username);
        TextInputEditText password = findViewById(R.id.login_password);
        sharedPrefManager = new Session(this);

        if (sharedPrefManager.getSPSudahLogin()){
            startActivity(new Intent(MainActivity.this, HomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaitDialog.show("Tunggu Sebentar");
                AndroidNetworking.post(Link.URI+"Auth/login")
                        .addBodyParameter("password", password.getText().toString())
                        .addBodyParameter("username", username.getText().toString())
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                String status = "";
                                Log.d(TAG, "onResponse: "+response);
                                try {
                                    status = response.getString("status");

                                    Log.d(TAG,status.toString());
                                    if (status.equals("succes")){
                                        JSONObject data = response.getJSONObject("data");
                                        sharedPrefManager.saveSPString(Session.SP_NAMA, data.getString("nama"));
                                        sharedPrefManager.saveSPString(Session.SP_ID, data.getString("id_user"));
                                        sharedPrefManager.saveSPBoolean(Session.SP_SUDAH_LOGIN, true);

                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                WaitDialog.dismiss();
                                                startActivity(new Intent(MainActivity.this, HomeActivity.class)
                                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                                    finish();
                                            }
                                        }, 2000);

//                                            Log.d(TAG, "onResponse: "+data.getString("nama"));
                                    }else{
                                        TipDialog.show("Data Tak Dikenal", TipDialog.TYPE.ERROR);
                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                TipDialog.dismiss();
                                            }
                                        }, 1000);
                                    }


                                } catch (JSONException e) {
//                                    WaitDialog.dismiss();
                                    TipDialog.show("Terjadi KesalahanHH !!", TipDialog.TYPE.ERROR);
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            TipDialog.dismiss();
                                        }
                                    }, 3000);
                                }

                            }
                            @Override
                            public void onError(ANError error) {
                                TipDialog.dismiss();
                                Toast.makeText(MainActivity.this,"Terjadi Kesalahan !! COba Lagi",Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}