package com.xuzimian.dl.ic

import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.transform.CenterCrop
import ai.djl.modality.cv.transform.Normalize
import ai.djl.modality.cv.transform.Resize
import ai.djl.modality.cv.transform.ToTensor
import ai.djl.modality.cv.translator.ImageClassificationTranslator
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.DownloadUtils
import ai.djl.training.util.ProgressBar
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import java.nio.file.Files
import kotlin.io.path.Path

@Lazy
@Configuration
class ResNetModelConfig {

    private val logger = KotlinLogging.logger {}

    @Value("\${app.model.rootDirectory:#{null}}")
    private var modelRootDirectory: String? = null

    private val path by lazy { "${modelRootDirectory}/image-classification/resnet18" }

    @Bean
    @Lazy
    fun getResNetModel(): ZooModel<Image, Classifications> {
        logger.info { "尝试加载ResNet模型" }

        downloadModel

        val translator = ImageClassificationTranslator.builder()
            .addTransform(Resize(256))
            .addTransform(CenterCrop(224, 224))
            .addTransform(ToTensor())
            .addTransform(
                Normalize(
                    floatArrayOf(0.485f, 0.456f, 0.406f),
                    floatArrayOf(0.229f, 0.224f, 0.225f)
                )
            )
            .optApplySoftmax(true)
            // synset.txt is default name,No need to set
            .optSynsetArtifactName("synset.txt")
            .build()

        return Criteria.builder()
            .setTypes(Image::class.java, Classifications::class.java)
            .optModelPath(Path(path))
            .optOption("mapLocation", "true")
            .optTranslator(translator)
            .optProgress(ProgressBar())
            .build()
            .loadModel()
    }

    private val downloadModel: Unit by lazy {
        logger.info { "检查是否需要下载模型和分类标签文件" }

        val modelPath = "${path}/resnet18.pt"
        if (!Files.exists(Path(modelPath))) {
            logger.info { "开始下载模型文件..." }
            DownloadUtils.download(
                "https://djl-ai.s3.amazonaws.com/mlrepo/model/cv/image_classification/ai/djl/pytorch/resnet/0.0.1/traced_resnet18.pt.gz",
                modelPath,
                ProgressBar()
            )
        }


        val classLabels = "${path}/synset.txt"
        if (!Files.exists(Path(classLabels))) {
            logger.info { "开始下载分类标签文件..." }
            DownloadUtils.download(
                "https://djl-ai.s3.amazonaws.com/mlrepo/model/cv/image_classification/ai/djl/pytorch/synset.txt",
                classLabels,
                ProgressBar()
            )
        }

    }
}