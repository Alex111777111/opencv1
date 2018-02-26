package org.silentpom.javafx.first;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.silentpom.javafx.filters.RectFilter;
import org.silentpom.javafx.first.utils.Utils;

/**
 * The controller for our application, where the application logic is
 * implemented. It handles the button for starting/stopping the camera and the
 * acquired video stream.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="http://max-z.de">Maximilian Zuleger</a> (minor fixes)
 * @version 2.0 (2016-09-17)
 * @since 1.0 (2013-10-20)
 */
public class FXHelloCVController {
    // the FXML button
    @FXML
    private Button button;
    // the FXML image view
    @FXML
    private ImageView currentFrame;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    private static int cameraId = 0;

    private RectFilter filter = new RectFilter(1, 1, 1, 1, 1, 1, 1, 1, 1, 0.75, 0.5, 0.5);

    private CascadeClassifier faceDetector;

    public FXHelloCVController() {
        File xmlConfig = new File(getClass().getResource("lbpcascade_frontalface.xml").getPath());
        faceDetector = new CascadeClassifier(xmlConfig.getAbsolutePath());

		/*if(faceDetector.empty()) {
            boolean result = faceDetector.load("D:/Projects/javafx/first-try/target/classes/org/silentpom/javafx/first/lbpcascade_frontalface.xml");
			if(!result) {
				throw new RuntimeException("No detector");
			}

		}*/
    }

    /**
     * The action triggered by pushing the button on the GUI
     *
     * @param event the push button event
     */
    @FXML
    protected void startCamera(ActionEvent event) {
        if (!this.cameraActive) {
            // start the video capture
            this.capture.open(cameraId);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run() {
                        // effectively grab and process a single frame
                        long time = System.currentTimeMillis();
                        Mat frame = grabFrame();
                        time = System.currentTimeMillis() - time;
                        try {
                            System.out.printf("Time need to grab %d ms\n", time);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        // convert and show the frame
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(currentFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
                //this.timer.scheduleAtFixedRate(frameGrabber, 0, 100, TimeUnit.MILLISECONDS);

                // update the button content
                this.button.setText("Stop Camera");
            } else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.button.setText("Start Camera");

            // stop the timer
            this.stopAcquisition();
        }
    }

    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Mat} to show
     */
    private Mat grabFrame() {
        // init everything
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {
                    //Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    MatOfRect faceDetections = new MatOfRect();
                    faceDetector.detectMultiScale(frame, faceDetections);

                    Rect[] rects = faceDetections.toArray();
                    //System.out.println(String.format("Detected %s faces", rects.length));
                    // Draw a bounding box around each face RED.
                    for (Rect rect : rects) {
                        //System.out.println(String.format("Face: %s", rect.toString()));
                        Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 0, 255));
                    }

                    // only one face correct, magenta
                    if (rects.length == 1) {
                        Rect rect = filter.filterElem(rects[0]);
                        Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(255, 0, 255));
                    } else {
                        // paint it green
                        System.out.println("No face!!!!!!!");
                        Rect rect = filter.filterLost();
                        if (rect != null) {
                            Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0));
                        }
                    }
                }

            } catch (Exception e) {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return frame;
    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view  the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    protected void setClosed() {
        this.stopAcquisition();
    }

}
