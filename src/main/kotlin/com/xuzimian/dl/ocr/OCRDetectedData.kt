package com.xuzimian.dl.ocr

import ai.djl.modality.cv.output.BoundingBox

class OCRDetectedData(
    val text: String,
    val rect: BoundingBox,
    val rotateDegree: Int = 0
)
