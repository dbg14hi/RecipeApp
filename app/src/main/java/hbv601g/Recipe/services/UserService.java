package hbv601g.Recipe.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Semaphore;

import hbv601g.Recipe.entities.User;

public class UserService extends Service {

    public Semaphore mLoginSemaphore;
    public Semaphore mRegisterSemaphore;
    private URL url;
    private HttpURLConnection con;
    private String jsonInputString;
    private String username;
    private String password;
    //private String email;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public UserService() throws MalformedURLException {

        mLoginSemaphore = new Semaphore(0);
        mRegisterSemaphore = new Semaphore(0);
    }

}

