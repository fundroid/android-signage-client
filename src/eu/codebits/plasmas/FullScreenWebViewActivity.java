package eu.codebits.plasmas;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkResourceClient;
import android.widget.Toast;
import static eu.codebits.plasmas.util.NetworkInterfaces.getIPAddress;
import static eu.codebits.plasmas.util.NetworkInterfaces.getMACAddress;
import eu.codebits.plasmas.util.SystemUiHider;


/** 
 * A textbook JavaScript interface class that exposes our utility functions 
 */

class HardwareInterface {
    Context mContext;
    
    HardwareInterface(Context c) {
        mContext = c;
    }
    
    @JavascriptInterface
    public String IPAddress() {
        return getIPAddress(null, true);
    }

    @JavascriptInterface
    public String MACAddress() {
        return getMACAddress(null);
    }
}


/**
 * A full-screen activity that shows and hides the system UI (i.e. status bar
 * and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullScreenWebViewActivity extends Activity {

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 10;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private void rebuildWebview() {
    }

    private static final String TAG = "WebViewActivity";
    private Activity activity;
    private Intent intent;

    XWalkView webView;
    
    class CustomResourceClient extends XWalkResourceClient {
        
        CustomResourceClient(XWalkView view) {
            super(view);
        }
        
        @Override
        public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String url) {
            return null; // continue loading everything
        }


        @Override
        public void onReceivedLoadError(final XWalkView view, int errorCode, String description, String failingUrl) {
            // load a blank page and retry, rather heavy-handedly
            Log.w(TAG, String.format("%d: Could not load %s: %s", errorCode, failingUrl, description));
            view.load(null, getString(R.string.blank_page));
            Toast.makeText(activity, String.format("Error: %s", description), Toast.LENGTH_SHORT).show();
            final String retryUrl = failingUrl;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Retrying...", Toast.LENGTH_SHORT).show();
                    view.load(retryUrl,null);
                }
            }, 1000);
            //super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onLoadFinished(XWalkView view, String url) {
            // inject device information into the DOM
            webView.load(
                    String.format("javascript:(function() {window.device = Object({'MACAddress':'%s', 'IPAddress':'%s'})})()",
                            getMACAddress(null), getIPAddress(null, true)), null
            );
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        String url;
        if (uri == null) {
            url = getString(R.string.initial_url);
        } else {
            url = uri.toString();
        }
        //Log.i(TAG, "Displaying: " + url);
        webView.load(url,null);
    }

    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        intent = getIntent();
        activity = this;
        setContentView(R.layout.activity_fullscreen);
        webView = (XWalkView) findViewById(R.id.webView);
        //webView.setWebChromeClient(new CustomWebChromeClient());
        webView.addJavascriptInterface(new HardwareInterface(this), "Android");

        webView.setResourceClient(new CustomResourceClient(webView));
        /*
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setGeolocationEnabled(true); // let us send back minimal info from JS
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false); // start media automatically (if we can get it to work)
        webView.getSettings().setUseWideViewPort(true); // use viewport meta tag if present
        */
        webView.load(getString(R.string.initial_url), null);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);


        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, webView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
            // Cached values.
            int mControlsHeight;
            int mShortAnimTime;

            @Override
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
            public void onVisibilityChange(boolean visible) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    // If the ViewPropertyAnimator API is available
                    // (Honeycomb MR2 and later), use it to animate the
                    // in-layout UI controls at the bottom of the
                    // screen.
                    if (mControlsHeight == 0) {
                        mControlsHeight = controlsView.getHeight();
                    }
                    if (mShortAnimTime == 0) {
                        mShortAnimTime = getResources().getInteger(
                                android.R.integer.config_shortAnimTime);
                    }
                    controlsView.animate()
                            .translationY(visible ? 0 : mControlsHeight)
                            .setDuration(mShortAnimTime);
                } else {
                    // If the ViewPropertyAnimator APIs aren't
                    // available, simply show or hide the in-layout UI
                    // controls.
                    controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                }

                if (visible && AUTO_HIDE) {
                    // Schedule a hide().
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
