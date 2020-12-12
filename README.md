# GANonMobile

### Introduction
The rise of versatile GANs allows us to create incredible applications that catch everyone’s eyes, such as image inpainting, super-resolution, style transfer etc. However, GANs are almost twice as compute intensive, when compared to the state-of-the-art supervised learning models used in production today. On the other hand, in today’s mobile first world, consumers are increasingly using camera’s ability to make real time changes to their pictures (e.g. adding photo filters). This creates a strong need of making DNNs small and faster such that it can be deployed on resource limited devices. In this project, we focus on making the inference of GANs more efficient. Specifically, we want to reduce the number of parameters and operations in GANs. We use the compressed GANs to showcase the real-time speedup on multiple hardware endpoints, including CPU, GPU and Mobile CPU.

### GAN Compression
To allow realtime processing of GAN, we need to reduce the number of parameters and MAC operations in the original model. In the [GAN Compression paper](https://arxiv.org/abs/2003.08936). They combine channel pruning, neural architecture search (NAS) and knowledge distillation and successfully reduce the CycleGAN by more than 20x. We follow the their (procedure)[https://github.com/mit-han-lab/gan-compression/blob/master/docs/lite_pipeline.md] and perform model compression on horse2zebra dataset. The workflow can be summarized as the below diagram:

![workflow](/figures/workflow.png)

### Mobile Deployment

To convert the Pytorch model into mobile compatible format: we follow the tutorial [here](https://pytorch.org/mobile/android/).

The main code is in \PytorchAndroid\app\src\main\java\com\example\pytorchandroid

The role of each files is explained below:
* In MainActivity.java, we get the control of several widgets in the app. We have one spinner to select which GAN model to use, one button to take photo with camera and one button to select image from the album. When users click any of the buttons. Our model will start the inference using the specified model and then jump to the Result activity.
* In Utils.java, we define functions to convert camera to the correct orientation. Since the image orientation will be affected by the phone orientation while taking the picture. We need to read the EXIF data from the image to get orientation.
* In Classifier.java, we use the libtorch API to perform preprocessing and postprocessing of images. Since the output of the Mobile Torch model is restricted to 1D vector format. We need to first convert it into float array in Java. Then reshape the array and combine RGB channels to construct an bitmap file.
* In Result.java, we receive the model prediction and latency and render it on the screen.

### Evaluation Result

#### Full vs. Compressed Model
Model: CycleGAN
Dataset: horse2zebra

|    Model    |   MACs  | # Params | CPU Latency (s) | GPU Latency (s) | Mobile Latency (s) |  FID |
|:-----------:|:-------:|:--------:|:---------------:|:---------------:|:------------------:|:----:|
|     Full    | 56.832G |  11.378M |      1.969      |      0.0234     |        2.413       | 65.2 |
|  Compressed |  2.546G |  0.357M  |      0.329      |      0.0059     |        0.913       | 65.0 |
| Improvement |   22x   |    32x   |        6x       |        4x       |        2.6x        |  0.2 |

#### Search Result under Different Budgets
|    Budget (G)    |   2.1  |   2.4  |   2.7  |   3.0  |   3.3  |
|:----------------:|:------:|:------:|:------:|:------:|:------:|
|      MAC (G)     |  2.07  |  2.33  |  2.64  |  2.98  |  3.20  |
| # Parameters (M) |  0.297 |  0.332 |  0.355 |  0.362 |  0.376 |
|    GPU Latency   | 0.0051 | 0.0052 | 0.0054 | 0.0056 | 0.0055 |
|    CPU Latency   | 0.0715 | 0.0756 | 0.0764 | 0.0824 | 0.0800 |
|        FID       |  89.00 |  69.83 |  64.99 |  61.92 |  60.24 |

![FID vs MAC](/figures/FIDvsMAC.png)
![FID vs Latency](/figures/FIDvsLatency.png)
