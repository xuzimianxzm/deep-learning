package com.xuzimian.dl.ocr

import ai.djl.Device
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.paddlepaddle.zoo.cv.imageclassification.PpWordRotateTranslator
import ai.djl.paddlepaddle.zoo.cv.objectdetection.PpWordDetectionTranslator
import ai.djl.paddlepaddle.zoo.cv.wordrecognition.PpWordRecognitionTranslator
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import mu.KotlinLogging

object PaddleOCRFactory {

    private val logger = KotlinLogging.logger {}

    private val classLoader = PaddleOCRFactory::class.java.classLoader

    /**
     * 文字区域检测器: 单词检测模型中，我们加载从 PaddleOCR 导出的模型。 之后，我们可以从中生成一个名为检测器的 DJL 预测器。
     *  - 可以从中检测出单词块。 模型的原始输出是标记所有单词区域的位图。 PpWordDetectionTranslator 将输出位图转换为矩形边界框，以便我们裁剪图像。
     *  - 可以看到的，单词块非常狭窄，不包括所有单词的整体。 让我们尝试稍微扩展一下以获得更好的结果。 extendRect 将框的高度和宽度扩展至一定比例。 getSubImage 将裁剪图像并提取单词块。
     *  - model: https://resources.djl.ai/test-models/paddleOCR/mobile/det_db.zip
     */
    fun createTextAreaDetectorModel(
        modelRootDirectory: String? = null,
        device: Device? = null
    ): ZooModel<Image, DetectedObjects> {
        val modelPath = if (modelRootDirectory != null)
            "${modelRootDirectory}/ocr/ch_PP-OCRv4_det_infer.tar"
        else classLoader.getResource("./ocr/ch_PP-OCRv4_det_infer.tar")!!.path.removePrefix("/")

        logger.info { "从地址: $modelPath 加载模型ch_PP-OCRv4_det_infer.tar" }

        val criteria = Criteria.builder()
            .optDevice(device ?: Device.cpu())
            .optEngine("PaddlePaddle")
            .setTypes(Image::class.java, DetectedObjects::class.java)
            .optModelPath(Path(modelPath))
            .optTranslator(PpWordDetectionTranslator(ConcurrentHashMap<String, String>()))
            .build()

        return criteria.loadModel()
    }


    /**
     * 文字角度检测器
     * 该模型是从 PaddleOCR 导出的，可以帮助识别图像是否需要旋转。 以下代码将加载此模型并创建一个 rotateClassifier。
     * - model: https://resources.djl.ai/test-models/paddleOCR/mobile/cls.zip
     */
    fun createTextRotateClassifierModel(
        modelRootDirectory: String? = null,
        device: Device? = null
    ): ZooModel<Image, Classifications> {
        val modelPath = if (modelRootDirectory != null)
            "${modelRootDirectory}/ocr/cls.zip"
        else classLoader.getResource("./ocr/cls.zip")!!.path.removePrefix("/")

        logger.info { "从地址: $modelPath 加载模型cls.zip" }

        val criteria = Criteria.builder()
            .optEngine("PaddlePaddle")
            .optDevice(device ?: Device.cpu())
            .setTypes(Image::class.java, Classifications::class.java)
            .optModelPath(Path(modelPath))
            .optTranslator(PpWordRotateTranslator())
            .build()
        return criteria.loadModel()
    }


    /**
     * 文本识别器
     * 文字识别模型是从PaddleOCR导出的，可以识别图像上的文字。
     * https://resources.djl.ai/test-models/paddleOCR/mobile/rec_crnn.zip
     * - ch_PP-OCRv4_rec_infer.zip_backup 模型比较好
     * - 识别效果不好的标签 2,3,5,6(照片比较模糊)，7
     */
    fun createTextRecognizerModel(modelRootDirectory: String? = null, device: Device? = null): ZooModel<Image, String> {
        val modelPath = if (modelRootDirectory != null)
            "${modelRootDirectory}/ocr/ch_PP-OCRv4_rec_infer.zip"
        else classLoader.getResource("./ocr/ch_PP-OCRv4_rec_infer.zip")!!.path.removePrefix("/")

        logger.info { "从地址: $modelPath 加载模型ch_PP-OCRv4_rec_infer.zip" }

        val criteria = Criteria.builder()
            .optEngine("PaddlePaddle")
            .optDevice(device ?: Device.cpu())
            .setTypes(Image::class.java, String::class.java)
            .optModelPath(Path(modelPath))
            .optTranslator(PpWordRecognitionTranslator())
            .build()
        return criteria.loadModel()
    }
}
