package com.example.siabsen;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {
    public static final String SP_Absen = "spabsen";

    public static final String SP_ID = "sp_id";
    public static final String SP_NAMA = "spNama";

    public static final String SP_SUDAH_LOGIN = "spSudahLogin";

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    public Session(Context context){
        sp = context.getSharedPreferences(SP_Absen, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public void saveSPString(String keySP, String value){
        spEditor.putString(keySP, value);
        spEditor.commit();
    }

    public void saveSPInt(String keySP, int value){
        spEditor.putInt(keySP, value);
        spEditor.commit();
    }

    public void saveSPBoolean(String keySP, boolean value){
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public String getSPNama(){
        return sp.getString(SP_NAMA, "");
    }

    public String getSpId(){
        return  sp.getString(SP_ID,"");
    }

    public  Boolean getSPSudahLogin(){
        return sp.getBoolean(SP_SUDAH_LOGIN, false);
    }
}
