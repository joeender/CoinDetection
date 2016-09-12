package zhong.coindetection;

import android.annotation.SuppressLint;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.hardware.Camera;
import android.widget.FrameLayout;
import android.widget.Button;
import android.graphics.*;
import android.widget.ImageView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import android.util.Log;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import android.widget.Toast;
import java.util.Locale;




/**
 * Created by Jialiang on 5/28/2016.
 * This application takes an image and displays the number of coins found and uses text to
 * speech to declare it as well.
 */
public class MainScreen extends AppCompatActivity {

    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private ImageView imageDisplay = null;
    private Bitmap bmapSaved;
    private Boolean done = false;
    Camera.PictureCallback mPicture;
    private FrameLayout camera_view = null;
    private Mat matImage;
    private FindCoins findCoins;
    private TextToSpeech tts = null;


    /// Default code when using MainActivity template
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    /// Code to load OPENCV
    protected BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    onOpenCVReady();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /// Displays confirmation when OpenCV has been loaded.
    protected void onOpenCVReady(){
        Toast.makeText(getApplicationContext(), "Opencv Ready", Toast.LENGTH_LONG).show();

    }


    // The main of the application, sets up displays and buttons.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /// Main View
        setContentView(R.layout.activity_main_screen);
        /// View for picture taken
        imageDisplay = (ImageView)findViewById(R.id.imageView);
        /// Turn this view off initially to allow for camera preview
        imageDisplay.setVisibility(View.INVISIBLE);

        tts = new TextToSpeech(MainScreen.this, onInitListener);

        // Loads OpenCV
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }

        // Turns camera on
        try{
            mCamera = Camera.open();
        } catch (Exception e){
        }

        // Display camera preview on surface.
        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
            camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.camera_view);

        // Sets up capture coins button, also the take picture button
        final Button button = (Button)findViewById(R.id.capture_button);

        // When an image has been taken from the camera.
        mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                // Saves image as a bitmap
                bmapSaved  = BitmapFactory.decodeByteArray(data, 0, data.length);

                // Covert bitmap to mat format to be used in image manipulation
                matImage = new Mat (bmapSaved.getHeight(), bmapSaved.getWidth(), CvType.CV_8UC3);
                Utils.bitmapToMat(bmapSaved, matImage);

                //  Calls the find coins method
                findCoins = new FindCoins(matImage);

                // Convert image back from mat to bitmap
                bmapSaved = Bitmap.createBitmap(matImage.cols(), matImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(findCoins.returnMat(), bmapSaved);

                // Sets image to be displayed
                imageDisplay.setImageBitmap(bmapSaved);

                // Turns off camera preview
                camera_view.setVisibility(View.INVISIBLE);

                // Turn on image view
                imageDisplay.setRotation(270);
                imageDisplay.setVisibility(View.VISIBLE);

                // Displays number of coins found
                Toast.makeText(getApplicationContext(), findCoins.returnCoinsFound() + " Coins Found", Toast.LENGTH_LONG).show();

                // Uses text-to-speech
                tts.speak(findCoins.returnCoinsFound()  + " coins found.", TextToSpeech.QUEUE_ADD, null);
                //
                done = true;
            }
        };


        /// Controls for the capture coin button.  Hitting it once will capture the image and detects
        /// coins.  Hitting it again will reset back to camera preview.
        button.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!done) {
                            mCamera.stopPreview();
                            button.setText("DONE");
                            mCamera.takePicture(null, null, mPicture);

                        }
                        if(done){
                            imageDisplay.setVisibility(View.INVISIBLE);
                            camera_view.setVisibility(View.VISIBLE);
                            mCamera.startPreview();
                            done = false;
                            button.setText("CAPTURE COINS");
                        }
                    }
                }
        );
    }

    /// Text-to-speech initializer
    private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("error", "This Language is not supported");
                }
            } else {
                Log.e("error", "Initialization Failed!");
            }
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();

        Log.i("DEMO", "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, this, mOpenCVCallBack))
        {
            Log.e("DEMO", "Cannot connect to OpenCV Manager");
        }
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
    }



}
