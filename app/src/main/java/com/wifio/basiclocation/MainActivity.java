package com.wifio.basiclocation;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public BroadcastReceiver receiver;
    final String url = "http://172.28.40.9:5000/fingerprint";
    LinearLayout layout;
    String macAddr;
    Context context;
    private Map<String, Float> coordinates = new HashMap<>();
    private SharedPreferences mPrefs;
    private IndoorBuildingView touch;
    private JSONArray previousMeasurements;
    private boolean showPrevious = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        layout = findViewById(R.id.layout);
        mPrefs  = getSharedPreferences("wifio_location", 0);
        previousMeasurements = new JSONArray();
        //test init

        try {
            JSONObject tempMeas = new JSONObject();
            tempMeas.put("x", 0.3);
            tempMeas.put("y", 0.8);
            previousMeasurements.put(tempMeas);
            tempMeas = new JSONObject();
            tempMeas.put("x", 0.8);
            tempMeas.put("y", 0.4);
            previousMeasurements.put(tempMeas);
            tempMeas = new JSONObject();
            tempMeas.put("x", 0.1);
            tempMeas.put("y", 0.8);
            previousMeasurements.put(tempMeas);
            tempMeas = new JSONObject();
            tempMeas.put("x", 0.5);
            tempMeas.put("y", 0.5);
            previousMeasurements.put(tempMeas);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ///////

        String meas = mPrefs.getString("meas", previousMeasurements.toString());
        try {
            previousMeasurements =  new JSONArray(meas);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PointerView pointerView = new PointerView(MainActivity.this);
        layout.addView(pointerView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        touch = findViewById(R.id.zoom_image);

        touch.setImageResource(R.drawable.katan);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (coordinates.get("x") != null && coordinates.get("y") != null) {
                    Snackbar.make(view, "x: " +coordinates.get("x") + " y: " + coordinates.get("y"),
                            Snackbar.LENGTH_LONG)
                            .setAction("Send", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Utils.postMeasurement(coordinates.get("x"), coordinates.get("y"),
                                            context, macAddr, previousMeasurements, mPrefs, url);
                                }
                            }).show();
                }
            }
        });
        touch.setCoordinates(coordinates);
        touch.setPointer(pointerView);
        macAddr = Utils.getMACAddress("wlan0");

        Log.i(TAG, "mac: " + macAddr);
        this.setTitle("mac: " + macAddr);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null) {
                    switch (intent.getAction()) {
                        case "MEASUREMENT_SAVED": {
                            if(showPrevious){
                                Bitmap bitmap = drawPrevious();
                                touch.setImageBitmap(bitmap);
                            }
                            Log.i(TAG, "measurement_saved");
                            break;
                        }
                        case "EXCEPTION": {
                            Log.i(TAG, "timeout");
                            break;
                        }
                    }
                }
            }
        };
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction("MEASUREMENT_SAVED");
        receiverFilter.addAction("EXCEPTION");
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),receiverFilter);
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
        if (id == R.id.show_previous) {
            if(item.getTitle().equals("Show Previous")) {
                showPrevious = true;
                item.setTitle("Hide Previous");
                Bitmap bitmap = drawPrevious();
                touch.setImageBitmap(bitmap);
            } else {
                showPrevious = false;
                item.setTitle("Show Previous");
                touch.setImageResource(R.drawable.katan);
            }

            return true;
        }
        if (id == R.id.clear_previous) {
            previousMeasurements = new JSONArray();
            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.putString("meas", previousMeasurements.toString()).commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Bitmap drawPrevious() {
        Drawable drawable = getDrawable(R.drawable.katan);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getColor(R.color.transparentColor));
        paint.setStyle(Paint.Style.FILL);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
//        canvas.drawCircle(20, 20, 40, paint);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        for(int i = 0; i <previousMeasurements.length(); i++) {
            try {
                JSONObject tempMeas = previousMeasurements.getJSONObject(i);
                Log.i(TAG, "pos: " + tempMeas.toString());
                double x = tempMeas.getDouble("x");
                double y = tempMeas.getDouble("y");
                canvas.drawCircle((float)x*canvas.getWidth(), (float)y*canvas.getHeight(), 10, paint);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }
}
