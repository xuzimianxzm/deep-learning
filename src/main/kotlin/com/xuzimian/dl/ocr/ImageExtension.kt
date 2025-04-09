package com.xuzimian.dl.ocr

import ai.djl.modality.cv.Image
import ai.djl.modality.cv.output.BoundingBox
import ai.djl.modality.cv.output.Landmark
import ai.djl.modality.cv.output.Mask
import ai.djl.util.RandomUtils
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.OutputStream
import javax.imageio.ImageIO


fun BufferedImage.saveAsPNG(outputStream: OutputStream) {
    ImageIO.write(this, "png", outputStream)
}


fun Image.getDrawBoundingBoxesBufferedImage(ocrDetectedDataList: List<OCRDetectedData>): BufferedImage {
    val image = convertIdNeeded(this.wrappedImage as BufferedImage)

    val g = image.graphics as Graphics2D
    val stroke = 2
    g.stroke = BasicStroke(stroke.toFloat())
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.font = Font(g.font.name, g.font.style, g.font.size + 5)
    val imageWidth: Int = this.width
    val imageHeight: Int = this.height

    for (n in ocrDetectedDataList.indices) {
        val className = ocrDetectedDataList[n].text
        val rotateDegree = ocrDetectedDataList[n].rotateDegree
        val box = ocrDetectedDataList[n].rect
        val rectangle = box.bounds
        val x = (rectangle.x * imageWidth).toInt()
        val y = (rectangle.y * imageHeight).toInt()
        g.paint = Color(103, 181, 102, 128)
        val width = (rectangle.width * imageWidth).toInt()
        val height = (rectangle.height * imageHeight).toInt()
        g.fillRect(x, y, width, height)
        drawText(g, className, x, y, stroke, rotateDegree)
        // If we have a mask instead of a plain rectangle, draw tha mask
        if (box is Mask) {
            drawMask(image, box)
        } else (box as? Landmark)?.let { drawLandmarks(g, it) }
    }
    g.dispose()

    return image
}

/**
 * 如果是JPEG图像需要转换图像的新的副本.
 * Alpha 通道是在某些图像文件格式（如 PNG 和 TIFF）中发现的附加 8 位通道，用于存储透明度信息。
 * Alpha 通道用于定义图像中每个像素的透明度或不透明度。它通常用于创建透明或半透明图像，可以叠加在其他图像或背景之上。
 * 在 Alpha 通道中，为每个像素分配一个介于 0 和 255 之间的值，其中 0 表示完全透明，255 表示完全不透明。介于 0 和 255 之间的值表示不同级别的透明度。
 * 另一方面，JPEG 图像没有 Alpha 通道，JPEG 是一种有损图像格式，它不支持透明度。
 * JPEG使用一种称为色度子采样的技术，该技术会丢弃一些颜色信息以实现更高的压缩率。因此，保存 JPEG 图像时，所有透明度信息都将丢失。
 * 如果需要保存具有透明度的图像，则应使用支持Alpha通道的无损格式，例如PNG或TIFF。
 */
private fun convertIdNeeded(image: BufferedImage): BufferedImage {
    if (image.type == BufferedImage.TYPE_INT_ARGB) {
        return image
    }
    val newImage = BufferedImage(
        image.width, image.height, BufferedImage.TYPE_INT_ARGB
    )
    val g = newImage.createGraphics()
    g.drawImage(image, 0, 0, null)
    g.dispose()
    return newImage
}


private fun drawText(g: Graphics2D, text: String, x: Int, y: Int, stroke: Int, rotateDegree: Int, padding: Int = 4) {
    var xCopy = x
    var yCopy = y
    val metrics = g.fontMetrics
    xCopy += stroke / 2
    yCopy += stroke / 2


    if (rotateDegree == 0) {
        g.paint = Color.blue
        g.drawString(text, xCopy + padding, yCopy + metrics.ascent)
    } else {
        drawRotate(g, xCopy + padding, yCopy + metrics.ascent, rotateDegree, text)
    }
}

private fun drawMask(image: BufferedImage, mask: Mask) {
    val r = RandomUtils.nextFloat()
    val g = RandomUtils.nextFloat()
    val b = RandomUtils.nextFloat()
    val imageWidth: Int = image.width
    val imageHeight: Int = image.height
    var x = (mask.x * imageWidth).toInt()
    var y = (mask.y * imageHeight).toInt()
    val probDist = mask.probDist
    // Correct some coordinates of box when going out of image
    if (x < 0) {
        x = 0
    }
    if (y < 0) {
        y = 0
    }
    val maskImage = BufferedImage(
        probDist.size, probDist[0].size, BufferedImage.TYPE_INT_ARGB
    )
    for (xCor in probDist.indices) {
        for (yCor in probDist[xCor].indices) {
            val opacity = probDist[xCor][yCor] * 0.8f
            maskImage.setRGB(xCor, yCor, Color(r, g, b, opacity).darker().rgb)
        }
    }
    val gR = image.graphics as Graphics2D
    gR.drawImage(maskImage, x, y, null)
    gR.dispose()
}

private fun drawLandmarks(g: Graphics2D, box: BoundingBox) {
    g.color = Color(246, 96, 0)
    val bStroke = BasicStroke(4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)
    g.stroke = bStroke
    for (point in box.path) {
        g.drawRect(point.x.toInt(), point.y.toInt(), 2, 2)
    }
    g.dispose()
}

/**
 * 旋转对应角度输出文字
 */
fun drawRotate(g2d: Graphics2D, x: Int, y: Int, angle: Int, text: String) {
    // 旋转角度
    g2d.translate(x.toFloat().toDouble(), y.toFloat().toDouble())
    g2d.rotate(Math.toRadians(angle.toDouble()))
    // 写上识别文字
    g2d.paint = Color.blue
    g2d.drawString(text, 0, 0)

    // 转回角度
    g2d.rotate(-Math.toRadians(angle.toDouble()))
    g2d.translate(-x.toFloat().toDouble(), -y.toFloat().toDouble())
}
