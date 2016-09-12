package zhong.coindetection;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jialiang on 5/28/2016.
 * This the where all the image manipulation functions are.  The class takes the input image from the
 * camera, uses thresholding, contour detection, and then Hough circle detection.  This class also
 * draws circles around the found coins and then returns the number of coins for display and text-to-speech.
 */
public class FindCoins {

    // Input mat image format.
    Mat mat;
    // Image of contours
    Mat contourMat;

    // Counter to keep number of coins found.
    int coinsFound;

    FindCoins(Mat m)
    {
        // Scalar values for thresholding to find silver objects.  Not currently implemented.
        Scalar silverMin = new Scalar(30, 30,30);
        Scalar silverMax = new Scalar(255, 255, 255);

        // Scalar values for thresholding to find metallic objects.
        Scalar threshMin = new Scalar(30,50,50);
        Scalar threshMax = new Scalar(255,255,255);

        mat = new Mat();
        mat = m;
        coinsFound = 0;
        //detect(silverMin,silverMax);
        detect(threshMin,threshMax);

    }

    // This function does the image manupluation to find circles and draws them onto
    // the original image.
    public void detect(Scalar min, Scalar max)
    {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat temp = new Mat();
        Mat circles = new Mat();

        contourMat = new Mat(mat.rows(),mat.cols(),mat.type());

        // Prep image for thresholding
        Imgproc.cvtColor(mat,temp,Imgproc.COLOR_BGR2HSV,3);

        // Thresholding
        Core.inRange(temp, min, max, contourMat);

        // Find and draw contours
        Imgproc.findContours(contourMat, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_TC89_KCOS);
        Imgproc.drawContours(contourMat,contours,-1,new Scalar(255,255,255),1);

        // Find circles
        Imgproc.HoughCircles(contourMat,circles,Imgproc.CV_HOUGH_GRADIENT, 2.0,contourMat.rows()/8,100, 60 ,40,120);

        // Draw circles
        if (circles.cols() > 0) {
            for (int x = 0; x < circles.cols(); x++) {
                double vCircle[] = circles.get(0, x);

                if (vCircle == null)
                    break;

                Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                int radius = (int) Math.round(vCircle[2]);

                // Coin found and up the counter
                coinsFound++;

                // Draw the found circle
                Imgproc.circle(mat, pt, radius, new Scalar(255, 255, 255), 5);
                Imgproc.circle(mat, pt, 3, new Scalar(255, 255, 255), 5);
            }
        }
    }

    // Return original image with circles drawn
    public Mat returnMat()
    {
        return mat;
    }

    // Returns the number of coins found.
    public int returnCoinsFound()
    {
        return coinsFound;
    }
}
