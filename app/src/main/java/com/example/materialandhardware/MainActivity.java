package com.example.materialandhardware;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private Location location, base;
    private float distanceFactor = 10, distance = 0, speed;
    private double longDiff, latDiff, size = 50;
    private MediaPlayer mediaPlayer1, mediaPlayer2, mediaPlayer3, mediaPlayer4;
    private Bitmap bitmap;
    private int r = 0, g = 0, b = 0;

    /**
     * create MOOVZ activity
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setGPS();
        initSongs();

        setButtons();
    }

    /**
     * config buttons actions
     */
    private void setButtons() {
        ((Button) findViewById(R.id.setBase)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (location != null && location.getAccuracy() <= 20) {
                    base = location;
                    Toast.makeText(getApplicationContext(), "Base location changed!", Toast.LENGTH_SHORT).show();

                }
            }
        });

        ((Button) findViewById(R.id.editBtn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (findViewById(R.id.edit).getVisibility() == View.VISIBLE)
                    findViewById(R.id.edit).setVisibility(View.INVISIBLE);
                else
                    findViewById(R.id.edit).setVisibility(View.VISIBLE);

            }
        });

        ((EditText) findViewById(R.id.factorInput)).setText(String.valueOf(distanceFactor));
        ((Button) findViewById(R.id.setFactor)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String value = ((EditText) findViewById(R.id.factorInput)).getText().toString();
                distanceFactor = Float.parseFloat(value);
                Toast.makeText(getApplicationContext(), "Distance factor changed!", Toast.LENGTH_SHORT).show();
            }
        });

        ((EditText) findViewById(R.id.sizeInput)).setText(String.valueOf(size));
        ((Button) findViewById(R.id.setSize)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String value = ((EditText) findViewById(R.id.sizeInput)).getText().toString();
                size = Float.parseFloat(value);
                Toast.makeText(getApplicationContext(), "Size changed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * check GPS permissions, config and get location
     */
    private void setGPS() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Error", "Can't get location permissions");
            return;
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this, Looper.getMainLooper());
    }

    /**
     * draw color circle in the specific position
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void drawPosition() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        if (bitmap == null)
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setARGB(255 / 2, r, g, b);

        updateRGBValues();

        // calculate location
        float x = width / 2f, y = height / 2f;

        x += (longDiff / (size * 2)) * width;
        y -= (latDiff / (size * 2)) * height;

        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(x, y, 20, paint);

        paint.setColor(Color.GRAY);
        canvas.drawCircle(width / 2, height / 2, 20, paint);

        ImageView imageView = (ImageView) findViewById(R.id.iv);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * updates rgb values for circle color
     */
    private void updateRGBValues() {
        if (r < 250)
            r += 5;
        else {
            if (g < 250)
                g += 5;
            else {
                if (b < 250)
                    b += 5;
                else {
                    r = 0;
                    g = 0;
                    b = 0;
                }
            }
        }
    }


    /**
     * config all songs in media players
     */
    private void initSongs() {
        mediaPlayer1 = setSong(R.raw.song1);
        mediaPlayer2 = setSong(R.raw.song2);
        mediaPlayer3 = setSong(R.raw.song3);
        mediaPlayer4 = setSong(R.raw.song4);
    }

    /**
     * set one song to media player
     *
     * @param song song id
     * @return media player confid to specific song
     */
    private MediaPlayer setSong(int song) {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), song);
        mp.start();
        mp.setVolume(0f, 0f);
        return mp;
    }

    /**
     * function which updates location, media players and state
     *
     * @param location the new location
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onLocationChanged(Location location) {
        if (base == null && location.getAccuracy() <= 20) {
            base = location;
            ((TextView) findViewById(R.id.LoadingText)).setText("Start!");
        }
        if (location.getAccuracy() > 20)
            return;
        this.location = location;
        float[] res = new float[1];
        Location.distanceBetween(base.getLatitude(), base.getLongitude(),
                location.getLatitude(), location.getLongitude(), res);
        distance = res[0];

        double longDir = Math.signum(location.getLongitude() - base.getLongitude());
        double latDir = Math.signum(location.getLatitude() - base.getLatitude());

        Location.distanceBetween(base.getLatitude(), location.getLongitude(),
                location.getLatitude(), location.getLongitude(), res);
        latDiff = res[0] * latDir;

        Location.distanceBetween(location.getLatitude(), base.getLongitude(),
                location.getLatitude(), location.getLongitude(), res);
        longDiff = res[0] * longDir;

        setSettings(1, 1, mediaPlayer1);
        setSettings(-1, 1, mediaPlayer2);
        setSettings(1, -1, mediaPlayer3);
        setSettings(-1, -1, mediaPlayer4);

        @SuppressLint("DefaultLocale") String msg = String.format("long: %f, %f\nlat: %f, %f\naccuracy: %f\ndistance: %f\nspeed: %f",
                longDiff, longDir, latDiff, latDir, location.getAccuracy(), distance, speed);
        ((TextView) findViewById(R.id.Diff)).setText(msg);

        drawPosition();
    }

    /**
     * changes media player settings according to location
     *
     * @param lon   longitude
     * @param lat   latitude
     * @param media media player
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setSettings(int lon, int lat, MediaPlayer media) {
        float max = distanceFactor * 3;

        double dif1 = longDiff * lon, dif2 = latDiff * lat;
        if (dif1 > -distanceFactor && dif2 > -distanceFactor) {
            float dis = (float) Math.max((float) dif1, dif2);
            float currentVolume = Math.min(dis + distanceFactor, max);

            float volume = currentVolume / (float) max;
            media.setVolume(volume, volume);
        } else {
            media.setVolume(0f, 0f);
        }

        if (distance < size) {
            float minSpeed = 0.75f;
            speed = minSpeed + 0.5f * (distance / (float) size);

            media.setPlaybackParams(media.getPlaybackParams().setSpeed(speed));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * on app destroy, stop players
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer1.stop();
        mediaPlayer1.release();
        mediaPlayer2.stop();
        mediaPlayer2.release();
        mediaPlayer3.stop();
        mediaPlayer3.release();
        mediaPlayer4.stop();
        mediaPlayer4.release();
    }
}
