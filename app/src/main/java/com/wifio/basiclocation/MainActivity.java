package com.wifio.basiclocation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    final String url = "http://myurl.xyz:5000";
    LinearLayout layout;
    float x = 0;
    float y = 0;
    String macAddr;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        layout=findViewById(R.id.layout);
        CustomView customView = new CustomView(MainActivity.this);
        layout.addView(customView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NewZoomableImageView touch = findViewById(R.id.zoom_image);

        touch.setImageResource(R.drawable.katan);
        final TextView yCoordinate = findViewById(R.id.y_coordinates);
        final TextView xCoordinate = findViewById(R.id.x_coordinates);
        xCoordinate.setVisibility(View.GONE);
        yCoordinate.setVisibility(View.GONE);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (xCoordinate.getText() != "null" && yCoordinate.getText() != "null") {
                    final float x = Float.parseFloat((String) xCoordinate.getText().subSequence(3,xCoordinate.getText().length()));
                    final float y = Float.parseFloat((String) yCoordinate.getText().subSequence(3,yCoordinate.getText().length()));
                    Snackbar.make(view, "x: " + x + " y: " + y, Snackbar.LENGTH_LONG)
                            .setAction("Send", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    poster(x, y);
                                }
                            }).show();
                }
            }
        });
        touch.setCoordinates(xCoordinate, yCoordinate);
        touch.setPointer(customView);
        macAddr = Utils.getMACAddress("wlan0");

        Log.i(TAG, "mac: " + macAddr);
        this.setTitle("mac: " + macAddr);
    }

    public void poster(final float x, final float y){
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        try {
            JSONObject object = new JSONObject();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray array = response.getJSONArray("items");
//                        itemsData.clear();
//                        for(int i = 0 ; i < array.length() ; i++){
//                            itemsData.add(new itemObject(array.getJSONObject(i).getString("name"),
//                                    array.getJSONObject(i).getBoolean("checked")));
//                        }
//                        itemsAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                }
            }){
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
                @Override
                public byte[] getBody() {
                    JSONObject jsonBody = new JSONObject();
                    try {
                        jsonBody.put("x", x);
                        jsonBody.put("y", y);
                        jsonBody.put("mac", macAddr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    final String requestBody = jsonBody.toString();
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("Content-Type","application/json");
                    return params;
                }
            };
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public class CustomView extends View {

        Bitmap mBitmap;
        Paint paint;
        Canvas canvas;
        public CustomView(Context context) {
            super(context);
//            mBitmap = Bitmap.createBitmap(400, 800, Bitmap.Config.ARGB_8888);
            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            this.canvas = canvas;
            if(x!=0 && y!=0) {
//                canvas.drawCircle(x, y, 40, paint);
                Resources res = getResources();
                Bitmap bitmap = getBitmap(R.drawable.ic_location);
                canvas.drawBitmap(bitmap, (int) x - 100, (int) y - 150 , paint);
//                Drawable d = getResources().getDrawable(R.drawable.ic_location);
//                d.setBounds((int)x-20, (int)y+20, (int)x+20, (int)y-20);
//                d.draw(canvas);
            }
        }

        private Bitmap getBitmap(int drawableRes) {
            Drawable drawable = getResources().getDrawable(drawableRes);
            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);

            return bitmap;
        }

        public void clearCanvas(){
            invalidate();
            x=0;
            y=0;
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                invalidate();
                x = event.getX();
                y = event.getY();
                invalidate();
            }
            return false;
        }

    }
}
