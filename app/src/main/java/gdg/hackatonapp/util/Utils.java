package gdg.hackatonapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import gdg.hackatonapp.entity.User;

/**
 * Created by Kenji on 14/10/2015.
 */
public class Utils {
    private Context context;

    public Utils(Context context) {
        this.context = context;
    }

    public void getKeyHash() {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "gdg.hackatonapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    public User readUser() {
        SharedPreferences prefs = this.context.getSharedPreferences("user", this.context.MODE_PRIVATE);
        String s = prefs.getString("user", null);
        return s != null ? new Gson().fromJson(s, User.class) : null;
    }

    public void writeUser(User user) {
        SharedPreferences prefs = this.context.getSharedPreferences("user", this.context.MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("user", new Gson().toJson(user));
        ed.commit();
    }
}
