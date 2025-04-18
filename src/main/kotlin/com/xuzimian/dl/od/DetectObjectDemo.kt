package com.xuzimian.dl.od

import ai.djl.ModelException
import ai.djl.modality.cv.ImageFactory
import ai.djl.translate.TranslateException
import com.xuzimian.dl.utils.ViewerFrame
import com.xuzimian.dl.utils.toBufferedImage
import nu.pattern.OpenCV
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio
import java.awt.Dimension
import java.awt.Toolkit
import java.io.IOException
import javax.swing.JOptionPane
import kotlin.system.exitProcess

object DetectObjectDemo {

    @Throws(IOException::class, ModelException::class, TranslateException::class)
    fun detectCameraVideoObject() {
        val model = DetectObjectModelFactory.loadYolovModel()
        val predictor = model.newPredictor()

        OpenCV.loadLocally()
        val capture = VideoCapture(0)
        if (!capture.isOpened) {
            println("No camera detected")
            return
        }

        val ratio = (capture[Videoio.CAP_PROP_FRAME_WIDTH] / capture[Videoio.CAP_PROP_FRAME_HEIGHT])
        val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
        val height = (screenSize.height * 0.65f)
        var width = (height * ratio).toInt()
        if (width > screenSize.width) {
            width = screenSize.width
        }

        val image = Mat()
        var captured = false
        for (i in 0..9) {
            captured = capture.read(image)
            if (captured) {
                break
            }

            try {
                Thread.sleep(50)
            } catch (ignore: InterruptedException) {
                // ignore
            }
        }
        if (!captured) {
            JOptionPane.showConfirmDialog(null, "Failed to capture image from WebCam.")
        }

        val frame = ViewerFrame(width, height.toInt())
        val factory = ImageFactory.getInstance()
        val size: Size = Size(width.toDouble(), height.toDouble())

        while (capture.isOpened) {
            if (!capture.read(image)) {
                break
            }
            val resizeImage = Mat()
            Imgproc.resize(image, resizeImage, size)

            val img = factory.fromImage(resizeImage)
            val detections = predictor.predict(img)
            img.drawBoundingBoxes(detections)

            frame.showImage((img.wrappedImage as Mat).toBufferedImage())
        }

        capture.release()

        predictor.close()
        model.close()

        exitProcess(0)
    }
}