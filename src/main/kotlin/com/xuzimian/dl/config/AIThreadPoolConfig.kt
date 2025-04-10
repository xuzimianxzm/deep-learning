package com.xuzimian.dl.config

import ai.djl.Device
import ai.djl.engine.Engine
import ai.djl.util.cuda.CudaUtils
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor


const val PrefixThreadName = "AI-Executor-"

const val AIThreadPoolExecutorName = "AIThreadPoolExecutor"

@Lazy
@Configuration
class AIThreadPoolConfig {

    private val logger = KotlinLogging.logger {}

    @Value("\${app.aiThreadPoolSize:#{5}}")
    private var aiThreadPoolSize = 5

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