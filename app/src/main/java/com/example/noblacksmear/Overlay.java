package com.example.noblacksmear;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.hardware.input.InputManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;


public class Overlay extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_NOT_STICKY;
    }

    public class MyBinder extends Binder
    {
        Overlay getService()
        {
            return Overlay.this;
        }
    }

    IBinder Binder;
    public Overlay ()
    {
        Binder = new MyBinder ();
        //ShowMessage ("Service: Constructing....");
    }
    public void DoStop ()
    {
        stopSelf();
    }

    GlobalData GD = GlobalData.getInstance ();
    private WindowManager wm;
    private LinearLayout ll;
    int brightness;
    int LAYOUT_FLAG;
    int offset;
    Toast t;
    int once =0;
    Thread refr;
    TextView textView;
    //@Nullable
    //@Override
    //public IBinder onBind(Intent intent) {
     //   return null;
   // }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){

        int opacity,oldbright=-1,size;

        @Override
        public void handleMessage(Message msg) {
            //try {
            //    t.cancel();
            //} catch (Exception e) {
            //    e.printStackTrace();
            //}

            brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1)*100/GD.brightmax;

            opacity = GD.Intensity/(brightness+GD.Xoffset)-GD.Intensity/(GD.CutoffP+GD.Xoffset);
            //opacity = 100/(brightness+3)-100/(offset+5);
            if (brightness > GD.CutoffP){
                opacity = 0;
            }
            else if (opacity < 2) {
                opacity = 2;
            }
            //t =  Toast.makeText(Overlay.this,"brightness:"+brightness+" opacity:"+GD.ServBound+opacity+GD.CutoffP, Toast.LENGTH_SHORT);
            //t.show();
            //if(oldbright !=brightness) {
            size = 3000;//Resources.getSystem().getDisplayMetrics().widthPixels + 250
            //final WindowManager.LayoutParams Wparameters = new WindowManager.LayoutParams(Resources.getSystem().getDisplayMetrics().widthPixels + 250,Resources.getSystem().getDisplayMetrics().heightPixels + 250, LAYOUT_FLAG,

            final WindowManager.LayoutParams Wparameters = new WindowManager.LayoutParams(size,size, LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT);
            Wparameters.x = 0;
            Wparameters.y = 0;
            Wparameters.alpha = 0.7f;
            String textToUpdate = "br:"+brightness+" cut"+GD.CutoffP+"     opacity:"+opacity+"   ";
            textToUpdate = "";

            textView.setText(textToUpdate);

            //offset = 26;

            /*else if (opacity < 2 &&) {
                opacity = 2;
            }
            else if (opacity > 80) {
                opacity = 80;
            }*/
            ll.addView(textView);
            ll.setBackgroundColor(Color.argb(opacity, 255, 255, 255));

            if (once == 0) {
                wm.addView(ll, Wparameters);
                once = 1;
            } else {
                wm.updateViewLayout(ll, Wparameters);
            }
            if (!GD.ServBound){
                wm.removeView(ll);
                refr.interrupt();
            }


            //}
        }
    };

    @Override
    public void onCreate() {

        super.onCreate();
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        textView = new TextView(this);
        ll = new LinearLayout(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
         else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        LinearLayout.LayoutParams llparameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llparameters);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,5260));
        textView.setTextSize(20);
        textView.setTextColor(Color.MAGENTA);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setShadowLayer(4, 0, 0, Color.BLACK);
        textView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        refr = new Thread(new Runnable() {
            public void run() {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                try {
                    for (;;) {
                        Thread.sleep(500);
                        if(pm.isInteractive()) {
                            Message msg = new Message();
                            handler.sendMessage(msg);
                        }
                    }
                } catch (Throwable t) {
                    //Toast.makeText(Overlay.this, "still running", Toast.LENGTH_SHORT).show();
                }
            }
        });
        refr.start();

    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //GD.ServBound=false;

        //refr.interrupt();
       // wm.removeView(ll);
    }
}
