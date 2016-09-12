package zhong.coindetection;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.content.Context;
import java.io.IOException;


/**
 * Created by Jialiang on 5/27/2016.
 * This class controls the camera preview view and sets up the camera hardware to use.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback{

    // The SurfaceHolder is the preview display for the camera
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraView(Context context, Camera camera){
        super(context);
        mCamera = camera;
        mCamera.setDisplayOrientation(270);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override

    // Links the preview display from the camera with the surface holder.
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
        }
    }

    @Override

    // When the camera switches orientation, stop the preview.
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if(mHolder.getSurface() == null)
            return;

        try{
            mCamera.stopPreview();
        } catch (Exception e){
        }
        // After the orientation change, restart the preview.
        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
        }
    }

    @Override

    // Stops the preview when switching views.
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
    }
}