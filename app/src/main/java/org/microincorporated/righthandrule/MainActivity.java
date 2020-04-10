package org.microincorporated.righthandrule;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor sensor;
    private RelativeLayout green;
    private TextView text, teslas;
    private float max;
    int height;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        green = findViewById(R.id.GreenDisp);
        text = findViewById(R.id.Display);
        teslas = findViewById(R.id.Teslas);
        max = sensor.getMaximumRange();
    }
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(sensorListener);
    }
    public SensorEventListener sensorListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void onSensorChanged(SensorEvent event) {
                float[] v = event.values;
                float highest = Math.max(Math.max(v[0], v[1]), v[2]);
                float prop = Math.abs(highest / max);
                mixColors(prop);
                text.setText(String.format("%.2f", prop * 100.0f) + "%");
                teslas.setText(format(highest) + "T");
        }
    };
    private final static int PREFIX_OFFSET = 5;
    private final static String[] PREFIX_ARRAY = {"f", "p", "n", "µ", "m", "", "k", "M", "G", "T"};

    public static String convert(double val, int dp)
    {
        // If the value is zero, then simply return 0 with the correct number of dp
        if (val == 0) return String.format("%." + dp + "f", 0.0);

        // If the value is negative, make it positive so the log10 works
        double posVal = (val<0) ? -val : val;
        double log10 = Math.log10(posVal);

        // Determine how many orders of 3 magnitudes the value is
        int count = (int) Math.floor(log10/3);

        // Calculate the index of the prefix symbol
        int index = count + PREFIX_OFFSET;

        // Scale the value into the range 1<=val<1000
        val /= Math.pow(10, count * 3);
        if (index >= 0 && index < PREFIX_ARRAY.length)
        {
            // If a prefix exists use it to create the correct string
            return String.format("%." + dp + "f%s", val, PREFIX_ARRAY[index]);
        }
        else
        {
            // If no prefix exists just make a string of the form 000e000
            return String.format("%." + dp + "fe%d", val, count * 3);
        }
    }
    public static String format(float val)
    {
        return convert(val / (Math.pow(10, 6)), 0);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void  mixColors(double percent){
        double inverse_percent = 1.0 - percent;
        int redPart = (int) (255*percent + 0*inverse_percent);
        int greenPart = (int) (0*percent + 255*inverse_percent);
        int bluePart = (int) (0*percent + 0*inverse_percent);

        green.setBackgroundColor(Color.rgb(redPart,greenPart,bluePart));
    }
}
