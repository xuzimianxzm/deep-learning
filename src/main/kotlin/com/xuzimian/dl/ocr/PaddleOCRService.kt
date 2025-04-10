package com.xuzimian.dl.ocr

import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.ImageFactory
import ai.djl.modality.cv.output.BoundingBox
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.modality.cv.output.Rectangle
import ai.djl.modality.cv.util.NDImageUtils
import ai.djl.ndarray.NDManager
import com.xuzimian.dl.config.AIThreadPoolExecutorName
import jakarta.annotation.Resource
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.util.concurrent.CompletableFuture

@Service
class PaddleOCRService {

    private val logger = KotlinLogging.logger {}

    @Resource
    private lateinit var ocrDetector: OCRDetector

    @Value("#{environment.getProperty('debug') != null && environment.getProperty('debug') != 'false'}")
    private val debug: Boolean = false

    @Async(AIThreadPoolExecutorName)
    fun getImageTextsAsync(byteArray: ByteArray): CompletableFuture<List<String>> {
        logger.info { "识别图像: ${Thread.currentThread().name} - $byteArray" }
        return CompletableFuture.completedFuture(getImageTexts(byteArray))
    }

    @Async(AIThreadPoolExecutorName)
    fun markTextOnImageAsync(inputStream: InputStream): CompletableFuture<BufferedImage> {
        logger.info { "识别并标记图像: ${Thread.currentThread().name} " }
        return CompletableFuture.completedFuture(
            markTextOnImage(
                ImageFactory.getInstance().fromInputStream(inputStream)
            )
        )
    }

    private fun getImageTexts(byteArray: ByteArray): List<String> {
        return getImageTexts(ByteArrayInputStream(byteArray))
    }

    private fun getImageTexts(inputStream: InputStream): List<String> {
        var img = ImageFactory.getInstance().fromInputStream(inputStream)

        val maxSize = 1560
        if (img.width > maxSize || img.height > maxSize) {

            val isUpright = img.width > img.height

            val scale = if (isUpright)
                img.width.toFloat() / img.height.toFloat()
            else
                img.height.toFloat() / img.width.toFloat()

            img = img.resize(
                if (isUpright) maxSize else (maxSize / scale).toInt(),
                if (!isUpright) maxSize else (maxSize / scale).toInt(), false
            )
        }

        return getImageTexts(img)
    }

    /**
     * 获取识别的文字集合
     */
    private fun getImageTexts(img: Image): List<String> {
        val boxes: List<DetectedObjects.DetectedObject> = ocrDetector.predictTextArea(img).items()
        val ocrDetectedData = getImageTextAndBox(img, boxes)
        return ocrDetectedData.map { it.text }
    }

    /**
     *
     * 返回原图像，在原图上文字区域框选出来，并画上对应识别文字结果。
     */
    private fun markTextOnImage(img: Image): BufferedImage {
        val boxes: List<DetectedObjects.DetectedObject> = ocrDetector.predictTextArea(img).items()
        val ocrDetectedData = getImageTextAndBox(img, boxes)
        return img.getDrawBoundingBoxesBufferedImage(ocrDetectedData)
    }

    private fun getImageTextAndBox(
        img: Image,
        boxes: List<DetectedObjects.DetectedObject>,
        needRotateImage: Boolean = true
    ): List<OCRDetectedData> {
        val oCRDetectedDataList = mutableListOf<OCRDetectedData>()
        for (i in boxes.indices) {
            var (subImg: Image, subImgExtendRect: BoundingBox) = createSubImage(img, boxes[i].boundingBox)

            var imageRotateDegree = 0

            if (needRotateImage) {
                if (subImg.height * 1.0 / subImg.width > 1.5) {
                    subImg = rotateImage(subImg)
                    imageRotateDegree = 90
                }
                val result: Classifications.Classification = ocrDetector.predictRotate(subImg).best()
                if ("Rotate" == result.className && result.probability > 0.8) {
                    subImg = rotateImage(subImg)
                    imageRotateDegree += 90
                }
            }

            val name: String = ocrDetector.predictRecognize(subImg)
            logger.info { "${Thread.currentThread().name} - 已识别文字：$name" }
            val boundingBox = subImgExtendRect

            if (!oCRDetectedDataList.any { it.text == name }) {
                oCRDetectedDataList.add(OCRDetectedData(name, boundingBox, imageRotateDegree))
            }
        }

        if (debug) {
            val newImage = img.duplicate()
            newImage.drawBoundingBoxes(
                DetectedObjects(
                    oCRDetectedDataList.map { it.text },
                    oCRDetectedDataList.map { -1.0 },
                    oCRDetectedDataList.map { it.rect })
            )
            newImage.save(
                Files.newOutputStream(Paths.get("build").resolve("${Instant.now().toEpochMilli()}.jpg")),
                "jpeg"
            )
        }
        return oCRDetectedDataList
    }


    /**
     * 尝试进行二次切割文字区域的版本
     */
    private fun getImageTextAndBox2(
        img: Image,
        boxes: List<DetectedObjects.DetectedObject>,
        needRotateImage: Boolean = true
    ): List<OCRDetectedData> {
        val oCRDetectedDataList = mutableListOf<OCRDetectedData>()
        for (i in boxes.indices) {
            var (subImg: Image, subImgExtendRect: BoundingBox) = createSubImage(img, boxes[i].boundingBox)

            // 进行图片二次切割，防止切割出来的图片存在多行文本情况导致不能识别
//            var subSubImgs: List<Image> = listOf(subImg)
            var subBoxes: List<DetectedObjects.DetectedObject> = listOf(boxes[i])
            if (subImg.height * 1.0 / subImg.width > 0.4 && subImg.height > 130) {
                val detectedObjects = ocrDetector.predictTextArea(subImg)
                subBoxes =
                    if (detectedObjects.items<DetectedObjects.DetectedObject>().size > 0) detectedObjects.items() else subBoxes
//                subSubImgs = subBoxes.map { createSubImage(subImg, it.boundingBox).first }
            }

            for (n in subBoxes.indices) {
                //var subSubImg = subSubImgs[n]
                var imageRotateDegree = 0
                if (needRotateImage) {
                    if (subImg.height * 1.0 / subImg.width > 1.5) {
                        subImg = rotateImage(subImg)
                        imageRotateDegree = 90
                    }
                    val result: Classifications.Classification = ocrDetector.predictRotate(subImg).best()
                    if ("Rotate" == result.className && result.probability > 0.8) {
                        subImg = rotateImage(subImg)
                        imageRotateDegree += 90
                    }
                }

                val name: String = ocrDetector.predictRecognize(subImg)
                logger.info { "已识别文字：$name" }
                val boundingBox = if (subBoxes.size == 1) subBoxes[0].boundingBox else {
                    val subBoundBox = subBoxes[n].boundingBox
                    Rectangle(
                        ((subImgExtendRect.bounds.x * img.width) + (subBoundBox.point.x * subImg.width)) / img.width,
                        ((subImgExtendRect.bounds.y * img.height) + (subBoundBox.point.y * subImg.height)) / img.height,
                        (subBoundBox.bounds.width * subImg.width) / img.width,
                        (subBoundBox.bounds.height * subImg.height) / img.height
                    )
                }

                if (!oCRDetectedDataList.any { it.text == name }) {
                    oCRDetectedDataList.add(OCRDetectedData(name, boundingBox, imageRotateDegree))
                }
            }
        }


        if (debug) {
            // 将识别文字画图片上，并保存为PNG图片到本地build目录下
            img.getDrawBoundingBoxesBufferedImage(oCRDetectedDataList)
                .saveAsPNG(Files.newOutputStream(Paths.get("build").resolve("${Instant.now().toEpochMilli()}.png")))
        }
        return oCRDetectedDataList
    }


    /**
     * 将裁剪图像并提取单词块。
     */
    private fun createSubImage(img: Image, box: BoundingBox): Pair<Image, BoundingBox> {
        val rect: Rectangle = box.bounds
        val width = img.width
        val height = img.height
        val extended: DoubleArray = getExtendRect(rect.x, rect.y, rect.width, rect.height)

        val recovered = intArrayOf(
            (extended[0] * width).toInt(),
            (extended[1] * height).toInt(),
            (extended[2] * width).toInt(),
            (extended[3] * height).toInt()
        )

        return Pair(
            img.getSubImage(recovered[0], recovered[1], recovered[2], recovered[3]),
            Rectangle(
                extended[0],
                extended[1],
                extended[2],
                extended[3]
            )
        )
    }

    private fun getExtendRect2(rect: Rectangle, imageWidth: Int, imageHeight: Int): DoubleArray {
        return doubleArrayOf(
            rect.x - (100 / imageWidth),
            rect.y - (100 / imageHeight),
            rect.width + (100 / imageWidth),
            rect.height + (100 / imageHeight)
        )
    }


    private val heightScale = 1.7
    private val widthScale = 1.2

    /**
     * 扩展文字框的大小,将框的高度和宽度扩展至一定比例。
     */
    private fun getExtendRect(xMin: Double, yMin: Double, width: Double, height: Double): DoubleArray {
        var widthCopy = width
        var heightCopy = height
        val centerX = xMin + widthCopy / 2
        val centerY = yMin + heightCopy / 2
        if (widthCopy > heightCopy) {
            widthCopy += heightCopy * 2.0
            heightCopy *= heightScale
        } else {
            heightCopy += widthCopy * 2.0
            widthCopy *= widthScale
        }
        val newX: Double = if (centerX - widthCopy / 2 < 0) 0.0 else centerX - widthCopy / 2
        val newY: Double = if (centerY - heightCopy / 2 < 0) 0.0 else centerY - heightCopy / 2
        val newWidth = if (newX + widthCopy > 1) 1 - newX else widthCopy
        val newHeight = if (newY + heightCopy > 1) 1 - newY else heightCopy
        return doubleArrayOf(newX, newY, newWidth, newHeight)
    }

    /*
      旋转图像
     */
    private fun rotateImage(image: Image): Image {
        NDManager.newBaseManager().use { manager ->
            val rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1)
            return ImageFactory.getInstance().fromNDArray(rotated)
        }
    }

    private fun getImage(url: String): Image {
        return ImageFactory.getInstance().fromUrl(url)
    }
}
