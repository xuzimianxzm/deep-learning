## Introduce

[Deep Java Library 官网](https://docs.djl.ai/master/docs/demos/jupyter/tutorial/01_create_your_first_network.html)
[Deep Java Library 官网文档](https://docs.djl.ai/master/docs/index.html)

1. [创建第一个深度学习神经网络](https://docs.djl.ai/master/docs/demos/jupyter/tutorial/01_create_your_first_network.html)

### 1 Train Model

### 数据集

数据集（或数据集）是用于训练机器学习模型的数据集合。
机器学习通常使用三个数据集：

- **训练数据集**:我们用于训练模型的实际数据集。该模型从此数据中学习权重和参数。
- **验证数据集**:验证集用于在训练过程中评估给定模型。它有助于机器学习 工程师在模型开发阶段微调 HyperParameters。
  该模型不从验证数据集中学习;验证数据集是可选的。
- **测试数据集**:Test 数据集提供了用于评估模型的黄金标准。 只有在模型完全训练后才使用它。 测试数据集应更准确地评估模型将如何对新数据执行。

DJL 提供了许多内置的基本和标准数据集。这些数据集用于训练深度学习模型。
此模块包含以下数据集：https://docs.djl.ai/master/docs/dataset.html