package com.xuzimian.dl.ocr

import ai.djl.Device
import ai.djl.engine.Engine
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.output.DetectedObjects
import ai.djl.repository.zoo.ZooModel
import ai.djl.util.cuda.CudaUtils
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor


const val PrefixThreadName = "AI-Executor-"

const val AIThreadPoolExecutorName = "AIThreadPoolExecutor"

const val CommonThreadPoolExecutorName = "CommonThreadPoolExecutor"

const val SymbolDetectionThreadPoolExecutor = "SymbolDetectionThreadPoolExecutor"

@Lazy
@Configuration
class PaddleOCRConfig {

    private val logger = KotlinLogging.logger {}

    @Value("\${app.model.rootDirectory:#{null}}")
    private var modelRootDirectory: String? = null

    @Value("\${app.aiThreadPoolSize:#{5}}")
    private var aiThreadPoolSize = 5

    @Bean
    fun getRecognizerModel(): ZooModel<Image, String> {
        return PaddleOCRFactory.createTextRecognizerModel(modelRootDirectory)
    }

    @Bean
    fun getDetectorModel(): ZooModel<Image, DetectedObjects> {
        return PaddleOCRFactory.createTextAreaDetectorModel(modelRootDirectory)
    }

    @Bean
    fun getRotateClassifierModel(): ZooModel<Image, Classifications> {
        return PaddleOCRFactory.createTextRotateClassifierModel(modelRootDirectory)
    }


    @Bean(name = [AIThreadPoolExecutorName])
    fun aiThreadPoolExecutor(): ThreadPoolTaskExecutor {
        val threadPoolExecutor = ThreadPoolTaskExecutor()

        threadPoolExecutor.corePoolSize = aiThreadPoolSize
        threadPoolExecutor.maxPoolSize = aiThreadPoolSize
        threadPoolExecutor.queueCapacity = threadPoolExecutor.maxPoolSize * 20// 队列程度
        threadPoolExecutor.threadPriority = Thread.MAX_PRIORITY
        threadPoolExecutor.isDaemon = false
        threadPoolExecutor.keepAliveSeconds = 300 // 线程空闲时间
        threadPoolExecutor.setThreadNamePrefix(PrefixThreadName) // 线程名字前缀
        return threadPoolExecutor
    }

    /**
     * 因为上面定义了AIThreadPoolExecutor会覆盖common包的bootstrap中的配置的ThreadPoolTaskExecutor
     * 这里采用代码方式覆盖回来。
     */
    @Bean(name = [CommonThreadPoolExecutorName])
    fun commonThreadPoolExecutor(): ThreadPoolTaskExecutor {
        val threadPoolExecutor = ThreadPoolTaskExecutor()
        threadPoolExecutor.corePoolSize = 8
        threadPoolExecutor.maxPoolSize = 16
        threadPoolExecutor.queueCapacity = 1000// 队列程度
        threadPoolExecutor.setAllowCoreThreadTimeOut(true)
        threadPoolExecutor.keepAliveSeconds = 10 * 60 // 线程空闲时间
        threadPoolExecutor.setThreadNamePrefix(" my-executor-") // 线程名字前缀
        return threadPoolExecutor
    }

    /**
     * 图像符号识别线程池
     */
    @Bean(name = [SymbolDetectionThreadPoolExecutor])
    fun symbolDetectionThreadPoolExecutor(): ThreadPoolTaskExecutor {
        val threadPoolExecutor = ThreadPoolTaskExecutor()
        threadPoolExecutor.corePoolSize = 2
        threadPoolExecutor.maxPoolSize = 2
        threadPoolExecutor.queueCapacity = 1000// 队列程度
        threadPoolExecutor.setAllowCoreThreadTimeOut(true)
        threadPoolExecutor.keepAliveSeconds = 10 * 60 // 线程空闲时间
        threadPoolExecutor.setThreadNamePrefix("symbol-detection-executor-") // 线程名字前缀
        return threadPoolExecutor
    }

    private fun tryGetAllGpus(): List<Device> {
        val devices = ArrayList<Device>()
        val gpuCount = Engine.getInstance().getGpuCount()
        if (gpuCount > 0) {
            for (i in 0 until gpuCount) {
                devices.add(Device.gpu(i))
            }
        }

        if (!devices.any { it.isGpu }) {
            try {
                val gpu = Device.gpu()
                val mem = CudaUtils.getGpuMemory(gpu)
                logger.info("GPU${gpu.deviceId}显存:${mem.max}")
                devices.add(gpu)
            } catch (e: Exception) {
                logger.warn("***找不到显卡设备***", e)
            }
        }

        return devices
    }
}
