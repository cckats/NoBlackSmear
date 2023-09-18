package com.example.noblacksmear;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.TextView;

public class GlobalData {
    static private GlobalData Instance = null;
    int CutoffP;
    int Intensity;
    int Xoffset;
    int brightmax;
    boolean ActGame;
    TextView[] Matrix;
    Overlay Serv;
    boolean ServBound;
    Handler ActHandler;

    private GlobalData ()
    {
        CutoffP=30;
        Intensity=100;
        Xoffset=3;
        brightmax =255;
        ActGame = false;
        Matrix = null;
        Serv = null;
        ServBound = false;
        ActHandler = null;
    }

    public static GlobalData getInstance ()
    {
        if (Instance == null)
            Instance = new GlobalData ();
        return Instance;
    }

   /* public static GlobalData storeData () {
        SharedPreferences prefs = getPreferences(0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("key_name", "key_value");
        editor.commit(); //important, otherwise it wouldn't save.
    }*/

}
