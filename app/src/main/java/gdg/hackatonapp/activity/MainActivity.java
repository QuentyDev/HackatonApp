package gdg.hackatonapp.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.InputStream;

import gdg.hackatonapp.R;
import gdg.hackatonapp.entity.User;
import gdg.hackatonapp.services.Position;
import gdg.hackatonapp.util.Utils;

/**
 * Created by Kenji on 14/10/2015.
 */
public class MainActivity extends FragmentActivity {

    private Utils utils;
    private User user;
    ImageView imagenGoogle;
    ProfilePictureView imagenFacebook;
    GoogleMap mGoogleMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        utils = new Utils(this);
        user = utils.readUser();

        findViewById(R.id.btnCloseSession).setOnClickListener(closeSessionEvent);
        ((TextView) findViewById(R.id.tvMainName)).setText(user.getNombres());


        imagenFacebook = (ProfilePictureView) findViewById(R.id.image);
        imagenGoogle = ((ImageView) findViewById(R.id.imageView));

        if (user.getId_google() == null) {
            imagenFacebook.setProfileId(user.getId_facebook());
            imagenGoogle.setVisibility(View.GONE);
        } else {
            new ImageDownloader(imagenGoogle).execute(user.getId_google());
            imagenFacebook.setVisibility(View.GONE);
        }

        LatLng pos = new LatLng(-17.774135, -63.195169);
        mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mGoogleMap.addMarker(new MarkerOptions()
                .position(pos)
                .title("Pais: Bolivia"));

        CameraPosition camPos = new CameraPosition.Builder()
                .target(pos)
                .zoom(19)
                .bearing(45)
                .tilt(70)
                .build();

        CameraUpdate camUpd3 =
                CameraUpdateFactory.newCameraPosition(camPos);

        mGoogleMap.animateCamera(camUpd3);

    }

    private View.OnClickListener closeSessionEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LoginManager.getInstance().logOut();
            utils.writeUser(null);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    };

    class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public ImageDownloader(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mIcon = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            return mIcon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
