package com.example.siabsen;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.thecode.aestheticdialogs.AestheticDialog;
import com.thecode.aestheticdialogs.DialogAnimation;
import com.thecode.aestheticdialogs.DialogStyle;
import com.thecode.aestheticdialogs.DialogType;
import com.thecode.aestheticdialogs.OnDialogClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.BreakIterator;

public class HomeActivity extends AppCompatActivity {
    Session sharedPrefManager;
    Boolean sudahabsenmasuk = false;
    Boolean sudahabsenKeluar = false;
    Boolean izinopenscan=false;
    TextView nama,sudahmasuk,sudahkeluar,selamat ;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        Button scan = findViewById(R.id.buttonscan);
        AndroidNetworking.initialize(getApplicationContext());
        sharedPrefManager = new Session(this);
        nama = findViewById(R.id.home_nama);
        sudahmasuk= findViewById(R.id.homesudah_absen_masuk);
        sudahkeluar=findViewById(R.id.homesudah_absen_keluar);

        nama.setText(sharedPrefManager.getSPNama());
        cek_absen();

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (izinopenscan){

                if (!sudahabsenKeluar){
                        IntentIntegrator intentIntegrator = new IntentIntegrator(HomeActivity.this);
                        intentIntegrator.setPrompt("Scan a barcode or QR Code");
                        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                        intentIntegrator.setOrientationLocked(true);
                        intentIntegrator.initiateScan();
                }else {
                    TipDialog.show("Anda Sudah Keluar",TipDialog.TYPE.ERROR);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TipDialog.dismiss();
                        }
                    }, 3000);
                }

                }else {
                    TipDialog.show("Belum Terhubung ke Server",TipDialog.TYPE.ERROR);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TipDialog.dismiss();
                        }
                    }, 3000);
                }
            }
        });
    }

    private void cek_absen() {
        AndroidNetworking.post(Link.URI+"Dashboard/index")
                .addBodyParameter("id", sharedPrefManager.getSpId())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            sudahabsenmasuk = response.getBoolean("masuk");
                            sudahabsenKeluar = response.getBoolean("keluar");

                            if (sudahabsenmasuk){
                                sudahmasuk.setText("Sudah Absen");
                            }else {
                                sudahmasuk.setText("Belum Absen");
                            }

                            if (sudahabsenKeluar){
                                sudahkeluar.setText("Sudah Absen");
                            }else {
                                sudahkeluar.setText("Belum Absen");
                            }
                            izinopenscan=true;
                        } catch (JSONException e) {
                            TipDialog.show("Terjadi Kesalahan",TipDialog.TYPE.ERROR);
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    TipDialog.dismiss();
                                }
                            }, 3000);
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.d(TAG, "onError: "+error);
                        new AestheticDialog.Builder(HomeActivity.this, DialogStyle.FLASH, DialogType.ERROR)
                                .setTitle("Gagal")
                                .setMessage("Tidak Terhubung Kedatabase")
                                .setCancelable(false)
                                .setDarkMode(true)
                                .setGravity(Gravity.CENTER)
                                .setAnimation(DialogAnimation.SHRINK)
                                .setOnClickListener(new OnDialogClickListener() {
                                    @Override
                                    public void onClick(AestheticDialog.Builder builder) {
                                        builder.dismiss();
                                        cek_absen();
                                    }
                                })
                                .show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"

        String in_out;
        if (sudahabsenmasuk){
            in_out = "Keluar";
        }else{
            in_out = "Masuk";
        }
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
               cek_scan(intentResult.getContents());
               String id = intentResult.getContents();
                Toast.makeText(this, intentResult.getContents(), Toast.LENGTH_SHORT).show();
                AndroidNetworking.post(Link.URI+"Scanbarcode/absen_masuk")
                        .addBodyParameter("in_out",in_out)
                        .addBodyParameter("barcode", id)
                        .addBodyParameter("id_user", sharedPrefManager.getSpId())
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    boolean status = response.getBoolean("status");
                                    if (status){
                                        TipDialog.show("Berhasil Absen",TipDialog.TYPE.SUCCESS);
                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                TipDialog.dismiss();
                                                finish();
                                                overridePendingTransition(0, 0);
                                                startActivity(getIntent());
                                                overridePendingTransition(0, 0);
                                            }
                                        }, 3000);
                                    }else{
                                        new AestheticDialog.Builder(HomeActivity.this, DialogStyle.FLAT, DialogType.ERROR)
                                                .setTitle("Gagal")
                                                .setMessage("Refresh Halaman Website")
                                                .setCancelable(false)
                                                .setDarkMode(true)
                                                .setGravity(Gravity.CENTER)
                                                .setAnimation(DialogAnimation.SHRINK)
                                                .setOnClickListener(new OnDialogClickListener() {
                                                    @Override
                                                    public void onClick(AestheticDialog.Builder builder) {
                                                        builder.dismiss();
                                                    }
                                                })
                                                .show();
                                    }
                                } catch (JSONException e) {
                                    TipDialog.show("Gagal Absen",TipDialog.TYPE.ERROR);
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            TipDialog.dismiss();
                                        }
                                    }, 3000);
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onError(ANError error) {
                                Log.d(TAG, "onError: "+error);
                                new AestheticDialog.Builder(HomeActivity.this, DialogStyle.FLASH, DialogType.ERROR)
                                        .setTitle("Gagal")
                                        .setMessage("Tidak Terhubung Kedatabase")
                                        .setCancelable(false)
                                        .setDarkMode(true)
                                        .setGravity(Gravity.CENTER)
                                        .setAnimation(DialogAnimation.SHRINK)
                                        .setOnClickListener(new OnDialogClickListener() {
                                            @Override
                                            public void onClick(AestheticDialog.Builder builder) {
                                                builder.dismiss();
                                            }
                                        })
                                        .show();
                            }
                        });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void cek_scan(String id){
        Log.d(TAG, "onResponse: "+id);

        }
    }

