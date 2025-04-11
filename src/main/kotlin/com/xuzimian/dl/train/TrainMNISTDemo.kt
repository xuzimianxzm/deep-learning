package com.xuzimian.dl.train

import ai.djl.Application
import ai.djl.Model
import ai.djl.basicdataset.cv.classification.Mnist
import ai.djl.basicmodelzoo.basic.Mlp
import ai.djl.modality.Classifications
import ai.djl.modality.cv.Image
import ai.djl.modality.cv.ImageFactory
import ai.djl.modality.cv.util.NDImageUtils
import ai.djl.ndarray.NDList
import ai.djl.ndarray.types.Shape
import ai.djl.training.DefaultTrainingConfig
import ai.djl.training.EasyTrain
import ai.djl.training.evaluator.Accuracy
import ai.djl.training.listener.TrainingListener
import ai.djl.training.loss.Loss
import ai.djl.training.util.ProgressBar
import ai.djl.translate.Batchifier
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorContext
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import java.util.stream.IntStream

object TrainDemo {

    /**
     * 构建最简单、最古老的深度学习网络之一：多层感知器 （MLP）
     * MLP 分为多个层。第一层是包含输入数据的输入层，最后一层是生成网络最终结果的输出层。它们之间是称为隐藏层的层。具有更多的隐藏层和更大的隐藏层允许 MLP 表示更复杂的函数。
     * MNIST，一个手写数字数据库。在 28x28 图像中，每个图像都包含一个从 0 到 9 的黑白数字。它通常在开始使用深度学习时使用，因为它体积小且训练速度快。
     *
     * MLP 模型使用一维向量作为输入和输出。您应该根据输入数据确定此向量的适当大小，以及您将使用模型输出的用途。
     *
     * 输入和输出大小: 我们的输入向量将具有大小，因为 MNIST 输入图像的高度和宽度为 28，并且只需要一个数字来表示每个像素。对于彩色图像，您需要进一步将其乘以 RGB 通道。28x283
     * 我们的输出向量具有大小，因为每个图像都有可能的类（0 到 9）
     *
     * NDArray: 用于深度学习的核心数据类型是 NDArray。NDArray 表示多维、固定大小的同类数组。它与 Numpy python 包的行为非常相似，但增加了高效的计算。
     * 我们还有一个辅助类 NDList，它是一个 NDArray 列表，可以具有不同的大小和数据类型。
     *
     * Block API:
     * 在 DJL 中，块的作用类似于将输入 NDList 转换为输出 NDList 的函数。它们可以表示单个操作、神经网络的某些部分，甚至整个神经网络。
     * 块的特殊之处在于它们包含许多在其函数中使用并在深度学习过程中进行训练的参数。随着这些参数的训练，块所表示的函数会变得越来越准确。
     * 构建这些块函数时，最简单的方法是使用组合。类似于通过调用其他函数来构建函数，块可以通过组合其他块来构建。我们将包含块的块称为父块，将子块称为子块。
     * 我们提供了一些辅助函数，以便于构建常见的块组合结构。对于 MLP，我们将使用 SequentialBlock，这是一个容器块，其子块组成一个块链，
     * 每个子块将其输出按顺序提供给下一个子块。
     *
     * MLP 分为多个层。每一层都由一个 Linear Block 和一个非线性激活函数组成。如果我们一行只有两个线性块，
     * 它将与组合线性块 （$f（x） = W_2（W_1x） = （W_2W_1）x = W_{combined}x$） 相同。激活用于穿插在线性块之间，
     * 以允许它们表示非线性函数。我们将使用流行的 ReLU 作为我们的激活函数。
     * 第一层和最后一层具有固定大小，具体取决于所需的输入和输出大小。但是，您可以自由选择网络中中间层的数量和大小。
     * 我们将创建一个较小的 MLP，其中包含两个中间层，这些中间层会逐渐减小大小。通常，您会尝试不同的值，以查看哪些值最适合您的数据集。
     */
    fun trainMLP() {

        /**
         * 在尝试构建神经网络时，就像构建大多数函数一样，首先要弄清楚的是您的函数签名是什么。
         * 您的输入类型和输出类型是什么？由于大多数模型使用相对一致的签名，因此我们将其称为 Applications。
         * 图像分类,在图像分类中，输入是单个图像，它根据图像的主要主题分为许多不同的可能类别。图像的类取决于你正在训练的特定数据。
         */
        val application = Application.CV.IMAGE_CLASSIFICATION


        /**
         * 一旦你弄清楚你想学习什么应用程序，接下来你需要收集你正在训练的数据并将其形成一个数据集。通常，尝试收集和清理数据是深度学习过程中最麻烦的任务。
         * 使用数据集可能涉及从各种来源收集自定义数据，或使用在线免费提供的众多数据集之一。
         * 自定义数据可能更适合您的使用案例，但免费数据集通常更快、更易于使用。您可以阅读我们的数据集指南以了解有关数据集的更多信息。
         */

        // 1. 准备用于训练的 MNIST 数据集
        //   创建一个 Dataset 类来包含您的训练数据。数据集是由神经网络表示的函数的示例输入/输出对的集合。每个输入/输出都由一个
        //   Record 表示。每条记录可以有多个输入或输出数组，例如图像问答数据集，其中输入既是图像又是有关图像的问题，而输出是问题的答案。
        //   由于数据学习是高度可并行化的，因此训练通常不是一次使用单个记录完成，而是使用 Batch 完成。这可以显著提高性能，尤其是在处理图像时
        //    - 必须确定从数据集加载数据的参数。MNIST 唯一需要的参数是 Sampler
        //      的选择。采样器在迭代每个批次时决定数据集中的哪些元素以及有多少元素是每个批次的一部分。我们将让它随机随机洗牌批次的元素，并使用
        //      batchSize 32。batchSize 通常是内存内 2 的最大幂。
        val batchSize = 32
        val mnist = Mnist.builder().setSampling(batchSize, true).build()
        mnist.prepare(ProgressBar())

        // 2. 创建模型
        // Model 包含一个神经网络 Block 以及用于训练过程的其他工件。它包含有关您将使用的输入、输出、形状和数据类型的其他信息。通常，将在完全完成 Block 后使用 Model。
        val model: Model = Model.newInstance("mlp")
        model.block = Mlp(28 * 28, 10, intArrayOf(128, 64))


        // 3. 创建 Trainer
        // trainer 是编排训练过程的主要类。通常，它们将使用 try-with-resources 打开，并在训练结束后关闭。
        // trainer 采用现有模型并尝试优化模型的 Block 内的参数，以最好地匹配数据集。大多数优化基于随机梯度下降 （SGD）
        // 3.1 设置训练配置
        //  - Loss function(required):：损失函数用于衡量我们的模型与数据集的匹配程度。因为函数的值越低越好，所以它被称为 “loss” 函数。Loss 是模型唯一必需的参数
        //  - Evaluator 函数：Evaluator 函数还用于衡量我们的模型与数据集的匹配程度。与损失不同，它们只是供人们查看，而不是用于优化模型。由于许多损失不那么直观，因此添加其他赋值器（如 Accuracy）有助于了解模型的表现。如果您知道任何有用的评估器，我们建议您添加它们。
        //  - Training Listeners：训练侦听器通过侦听器界面向训练过程添加其他功能。这可能包括显示训练进度、在训练未定义时提前停止或记录性能指标。我们提供了几组简单的默认侦听器。

        val config = DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
            .addEvaluator(Accuracy())
            .addTrainingListeners(*TrainingListener.Defaults.logging())
        val trainer = model.newTrainer(config)

        // 4：初始化训练
        // 在训练模型之前，您必须使用起始值初始化所有参数。您可以通过传入输入形状来使用 trainer 进行此初始化。
        // 输入形状的第一个轴是批量大小。这不会影响参数初始化，因此您可以在此处使用 1。
        // MLP 输入形状的第二个轴 - 输入图像中的像素数
        trainer.initialize(Shape(1, 28 * 28))

        // 5. 训练模型
        // 训练时，它通常被组织成 epoch，其中每个 epoch 在数据集中的每个项目上训练模型一次。它比随机训练略快。
        val epoch = 2 // 深度学习通常以 epoch 为单位进行训练，每个 epoch 对数据集中的每个项目进行一次模型训练。
        EasyTrain.fit(trainer, epoch, mnist, null)

        // 6. 保存模型
        // 训练模型后，您应该保存它，以便以后可以重新加载。还可以向其添加元数据，例如训练准确性、训练的纪元数等，这些元数据可在加载模型或检查模型时使用。
        val modelDir = Paths.get("build/mlp")
        Files.createDirectories(modelDir)
        model.setProperty("Epoch", epoch.toString())
        model.save(modelDir, "mlp")
    }


    /**
     *  使用上一步(trainMLP)训练出来的模型进行识别.
     */
    fun useTrainMPLModel(path: String? = null): String {
        // 1. 加载手写数字图像
        val img = ImageFactory.getInstance().fromUrl("https://resources.djl.ai/images/0.png")
        img.wrappedImage

        // 2. 加载模型
        val modelDir = Paths.get(path ?: "build/mlp")
        val model = Model.newInstance("mlp")
        model.block = Mlp(28 * 28, 10, intArrayOf(128, 64))
        model.load(modelDir)

        // 3. 创建一个Translator
        // Translator 用于封装应用程序的预处理和后处理功能。processInput 和 processOutput 的输入应该是单个数据项，而不是批处理。
        val translator = object : Translator<Image, Classifications> {

            override fun processInput(ctx: TranslatorContext, input: Image): NDList {
                // 将图像转换为 NDArray
                val array = input.toNDArray(ctx.ndManager, Image.Flag.GRAYSCALE)
                return NDList(NDImageUtils.toTensor(array))
            }

            override fun processOutput(ctx: TranslatorContext, list: NDList): Classifications {
                // 使用输出概率创建分类
                val probabilities = list.singletonOrThrow().softmax(0)
                val classNames = IntStream.range(0, 10).mapToObj { i: Int -> java.lang.String.valueOf(i) }
                    .collect(Collectors.toList())
                return Classifications(classNames, probabilities)
            }

            override fun getBatchifier(): Batchifier {
                // 批处理器描述了如何将一批数据组合在一起，最常见的批处理器 Stacking 是将 N [X1, X2, ...] 数组合并为单个 [N, X1, X2, ...] 数组
                return Batchifier.STACK
            }
        }

        /**
         * 4. 创建 Predictor
         * 使用转换器，我们将创建一个新的 Predictor。预测器是编排推理过程的主要类。在推理过程中，使用经过训练的模型来预测值，
         * 通常用于生产使用案例。predictor 不是线程安全的，因此如果要并行执行预测，则应多次调用 newPredictor 以为每个线程创建一个 predictor 对象。
         */
        var predictor = model.newPredictor(translator)

        // 5. 运行推理
        var classifications = predictor.predict(img)
        val result = classifications.toString()
        println(result)
        return result
    }
}