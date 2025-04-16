package com.xuzimian.dl.utils

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
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


fun Mat.toBufferedImage2(): BufferedImage {
    val type =
        if (this.channels() != 1) BufferedImage.TYPE_3BYTE_BGR else BufferedImage.TYPE_BYTE_GRAY

    if (type == BufferedImage.TYPE_3BYTE_BGR) {
        Imgproc.cvtColor(this, this, Imgproc.COLOR_BGR2RGB)
    }

    val width = this.width()
    val height = this.height()
    val data = ByteArray(width * height * this.elemSize().toInt())
    this[0, 0, data]

    val ret = BufferedImage(width, height, type)
    ret.raster.setDataElements(0, 0, width, height, data)

    return ret
}
