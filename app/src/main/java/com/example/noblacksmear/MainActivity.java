package com.example.noblacksmear;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import static java.lang.StrictMath.abs;

/*
TODO LIST---------------------------
        get max brightness of any device
        Android 11,12 added checks for overlay used by playstore and other
            google apps preventing usage unless disabling overlay
        on/off switch check
        change slider names
        reset to default values
        Bigger start button ->enable
        Add floats as parameters (low % limited)
        Add custom curve for advanced users
        Explain sliders with ? buttons
        re Add Landscape mode with changes
        Find better way to handle handlers than sleep and infinite loops
        Smooth transition between opacities
        if user has disabled autobrightness on exit it will be enabled
        stop auto darkmode miui
        ----------------------------LATER---------------------------
        Add info of why, drawbacks and advantages and site research
        Optimize + listeners instead of handlers
        Database of custom curves per device and user sharing

        ----------------------------DONE---------------------------
        Add intensity and cutoff point as global variables and in main ui
        Add graph to show opacity change per brightness level
        Store Global Data to disk
        Fix Start switch
        Add rotate listener for faster rotate or square size
*/

public class MainActivity extends AppCompatActivity {
    GlobalData GD = GlobalData.getInstance ();
    Switch start;
    Button brightSub;
    Button brightAdd;
    SeekBar brightBar;
    SeekBar CutBar;
    SeekBar InteBar;
    SeekBar OffBar;
    TextView CutText;
    TextView InteText;
    TextView OffText;
    int brightness;
    TextView bright;
    LinearLayout SmearImg;
    private LineChart chart;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            brightness = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, -1)*100/GD.brightmax;
            bright=(TextView) findViewById(R.id.textbright);
            bright.setText("Brightness: "+brightness+"%");
            brightBar.setProgress(brightness);
        }
    };
    @SuppressLint("HandlerLeak")
    Handler handlerAnim = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AnimateSmear(SmearImg);
        }
    };
/*
    ServiceConnection ServConn = new ServiceConnection (){
        @Override
        public void onServiceConnected(ComponentName name, IBinder Serv) {
            Overlay.MyBinder Binder = (Overlay.MyBinder) Serv;
            GD.Serv = Binder.getService ();
            GD.ServBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            GD.ServBound = false;
        }
    };
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);


    // Load values from disk //
        {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            GD.CutoffP = sharedPref.getInt("CutoffP",30);
            GD.Intensity = sharedPref.getInt("Intensity",100);
            GD.Xoffset = sharedPref.getInt("Xoffset", 3);
            GD.brightmax= sharedPref.getInt("MaxBright",255);
        }
    // Get Max Brightness of device //


    // Start Button and Overlay Init //
        {
            start = (Switch) findViewById(R.id.start);

            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 1234);
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    while (!Settings.System.canWrite(this)) {
                    }
                }
            }
            //Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            //Overlay.MyBinder Binder = (Overlay.MyBinder) Serv;

            final Intent in = new Intent(MainActivity.this, Overlay.class);

            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (start.isChecked()) {
                        startService(in);
                        //bindService(in, ServConn, Context.BIND_AUTO_CREATE);
                        GD.ServBound = true;
                    }
                    else {
                        //Intent i = new Intent(MainActivity.this, Overlay.class);
                        stopService(in);
                        GD.ServBound = false;
                        //Toast.makeText(MainActivity.this, "tried to stop", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    // Brightness Control //
        {
            brightSub = (Button) findViewById(R.id.brightSub);
            brightAdd = (Button) findViewById(R.id.brightAdd);
            brightSub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    int temp = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1) - GD.brightmax/100;
                    if(temp >=0)
                     Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, temp);
                }
            });
            brightAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    int temp = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1) + GD.brightmax/100;
                    if(temp <= GD.brightmax)
                      Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, temp);
                }
            });
            brightBar = findViewById(R.id.brightBar);
            brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1) + GD.brightmax/100;
            brightBar.setProgress(brightness);
            brightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, progress * GD.brightmax/100);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            new Thread(new Runnable() {
                public void run() {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    try {
                        for (; ; ) {
                            Thread.sleep(100);
                            if (pm.isInteractive()) {
                                Message msg = new Message();
                                handler.sendMessage(msg);
                            }
                        }
                    } catch (Throwable t) {
                        //Toast.makeText(Overlay.this, "failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).start();
        }

    // Smear Image //
        {
            SmearImg = (LinearLayout) findViewById(R.id.SmearImg);
            AnimateSmear(SmearImg);
            new Thread(new Runnable() {
                public void run() {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    try {
                        for (; ; ) {
                            Thread.sleep(1000);
                            if (pm.isInteractive()) {
                                Message msg = new Message();
                                handlerAnim.sendMessage(msg);
                            }
                        }
                    } catch (Throwable t) {
                        //Toast.makeText(Overlay.this, "failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).start();
        }

    // Chart Init //
        {
        chart = findViewById(R.id.chart);
        chart.setViewPortOffsets(40f, 20f, 20f, 40f);
        chart.setBackgroundColor(Color.parseColor("#222222"));

        // no description text
        chart.getDescription().setEnabled(false);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        chart.setDrawGridBackground(false);
        chart.setMaxHighlightDistance(300);

        XAxis x = chart.getXAxis();
        x.setLabelCount(10, false);
        x.setTextColor(Color.WHITE);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        //x.setDrawGridLines(false);
        x.setAxisLineColor(Color.WHITE);
        // vertical grid lines
        //x.enableGridDashedLine(10f, 10f, 0f);
        x.setAxisMaximum(100f);

        YAxis y = chart.getAxisLeft();
        //y.setTypeface(tfLight);
        y.setLabelCount(6, false);
        y.setTextColor(Color.WHITE);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setAxisLineColor(Color.WHITE);
        //y.setAxisLineColor(Color.WHITE);
        y.setAxisMinimum(0f);
        y.setAxisMaximum(80f);
        setData();

        //limit line
        /*LimitLine llXAxis = new LimitLine(GD.CutoffP, "Cutoff point");
        llXAxis.setLineWidth(3f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        llXAxis.setTextColor(Color.WHITE);
        //llXAxis.setTypeface(tfRegular);
        x.addLimitLine(llXAxis);*/

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateXY(1000, 1000);
        // don't forget to refresh the drawing
        chart.invalidate();
    }

    // Controls //
        {
        CutBar = (SeekBar) findViewById(R.id.CutBar);
        CutText = (TextView) findViewById(R.id.CutText);
        InteBar = (SeekBar) findViewById(R.id.InteBar);
        InteText = (TextView) findViewById(R.id.InteText);
        OffBar = (SeekBar) findViewById(R.id.OffBar);
        OffText = (TextView) findViewById(R.id.OffText);

        CutBar.setMax(100);
        InteBar.setMax(255);
        OffBar.setMax(9);

        CutBar.setProgress(GD.CutoffP);
        CutText.setText("Start Point: "+GD.CutoffP+"%");
        InteBar.setProgress(GD.Intensity);
        InteText.setText("Intensity: "+GD.Intensity);
        OffBar.setProgress(abs(GD.Xoffset-10));
        OffText.setText("Low % Boost: "+(abs(GD.Xoffset-10)));

            CutBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress>5) {
                        GD.CutoffP = progress;
                        CutText.setText("Start Point: " + GD.CutoffP+"%");
                        setData();
                        chart.invalidate();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            InteBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    GD.Intensity = progress;
                    InteText.setText("Intensity: "+GD.Intensity);
                    setData();
                    chart.invalidate();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            OffBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    GD.Xoffset = 10-progress;
                    OffText.setText("Low % Boost: "+(abs(GD.Xoffset-10)));
                    setData();
                    chart.invalidate();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

        }
    }
    private void setData() {

            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < GD.CutoffP+2; i++) {
                float val = (float) (GD.Intensity/(i+GD.Xoffset)-GD.Intensity/(GD.CutoffP+GD.Xoffset));
                if (i > GD.CutoffP){
                    val = 0;
                }
                else if (val < 2) {
                    val = 2;
                }
                else if (val > 80) {
                    val = 80;
                }
                values.add(new Entry(i, val));
            }

            LineDataSet set1;

            if (chart.getData() != null &&
                    chart.getData().getDataSetCount() > 0) {
                set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
                set1.setValues(values);
                chart.getData().notifyDataChanged();
                chart.notifyDataSetChanged();
            } else {
                // create a dataset and give it a type
                set1 = new LineDataSet(values, "DataSet 1");

                //set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                //set1.setCubicIntensity(0.2f);
                set1.setDrawFilled(true);
                set1.setDrawCircles(false);
                set1.setLineWidth(1.8f);
                set1.setCircleRadius(4f);
                set1.setCircleColor(Color.WHITE);
                set1.setHighLightColor(Color.rgb(244, 117, 117));
                set1.setColor(Color.WHITE);
                set1.setFillColor(Color.WHITE);
                set1.setFillAlpha(100);
                //set1.setDrawHorizontalHighlightIndicator(false);
                set1.setFillFormatter(new IFillFormatter() {
                    @Override
                    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                        return chart.getAxisLeft().getAxisMinimum();
                    }
                });

                // create a data object with the data sets
                LineData data = new LineData(set1);
               // data.setValueTypeface(tfLight);
                //data.setValueTextSize(9f);
                data.setDrawValues(false);

                // set data
                chart.setData(data);
            }
        }


    void AnimateSmear (final LinearLayout v){
        final ObjectAnimator oa1 = ObjectAnimator.ofFloat(v, "translationX", 490f);
        final ObjectAnimator oa2 = ObjectAnimator.ofFloat(v, "translationX", 0);
        oa1.setInterpolator(new AccelerateDecelerateInterpolator());
        oa2.setInterpolator(new AccelerateDecelerateInterpolator());
        oa1.setDuration(500);
        oa2.setDuration(500);
        oa1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                oa2.start();
            }
        });
        oa1.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        //SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("CutoffP", GD.CutoffP);
        editor.putInt("Intensity", GD.Intensity);
        editor.putInt("Xoffset", GD.Xoffset);
        //editor.putInt("CutoffP", GD.CutoffP);
        editor.apply(); //important, otherwise it wouldn't save.
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}