package com.xuzimian.dl.utils

import org.opencv.core.Mat
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte


fun Mat.toBufferedImage(): BufferedImage {
    var type = BufferedImage.TYPE_BYTE_GRAY
    if (this.channels() > 1) {
        type = BufferedImage.TYPE_3BYTE_BGR
    }
    val bufferSize = this.channels() * this.cols() * this.rows()
    val buffer = ByteArray(bufferSize)
    this[0, 0, buffer]
    val image = BufferedImage(this.cols(), this.rows(), type)
    val targetPixels = (image.raster.dataBuffer as DataBufferByte).data
    System.arraycopy(buffer, 0, targetPixels, 0, buffer.size)
    return image
}
