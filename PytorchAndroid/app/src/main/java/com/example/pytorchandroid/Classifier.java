package com.example.pytorchandroid;

import android.graphics.Bitmap;

import org.pytorch.Tensor;
import org.pytorch.Module;
import org.pytorch.IValue;
import org.pytorch.torchvision.TensorImageUtils;

import java.nio.ByteBuffer;


public class Classifier {

    Module model;
    float[] mean = {0.485f, 0.456f, 0.406f};
    float[] std = {0.229f, 0.224f, 0.225f};

    public Classifier(String modelPath){

        model = Module.load(modelPath);

    }

    public void setMeanAndStd(float[] mean, float[] std){

        this.mean = mean;
        this.std = std;
    }

    public Tensor preprocess(Bitmap bitmap, int size){

        bitmap = Bitmap.createScaledBitmap(bitmap,size,size,false);
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap,this.mean,this.std);

    }

    public int argMax(float[] inputs){

        int maxIndex = -1;
        float maxvalue = 0.0f;

        for (int i = 0; i < inputs.length; i++){

            if(inputs[i] > maxvalue) {

                maxIndex = i;
                maxvalue = inputs[i];
            }

        }


        return maxIndex;
    }

    public Bitmap predict(Bitmap bitmap){

        Tensor tensor = preprocess(bitmap,256);

        IValue inputs = IValue.from(tensor);
        Tensor outputs = model.forward(inputs).toTensor();
        float[] pixels = outputs.getDataAsFloatArray();
        return arrayFloatToBitmap(pixels);
    }

    private Bitmap arrayFloatToBitmap(float[] pixels){

        byte alpha = (byte) 255 ;
        int width = 256;
        int height = 256;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) ;

        ByteBuffer byteBuffer = ByteBuffer.allocate(width*height*4*3) ;

        float maxValue = pixels[0];
        float minValue = pixels[0];
        for(int i=1;i < pixels.length;i++){
            if(pixels[i] > maxValue){
                maxValue = pixels[i];
            }
            if(pixels[i] < minValue){
                minValue = pixels[i];
            }
        }
        float delta = maxValue - minValue ;

        for(int i=0; i < pixels.length; i++){
            int c = i / (height * width);
            int buffer_idx = i % (height * width);
            byte temValue = (byte) ((byte) ((((pixels[i]-minValue)/delta)*255)));
            byteBuffer.put(buffer_idx*4+c, temValue) ;
        }
        for(int i=0; i < height*width; i++){
            byteBuffer.put(i*4+3, alpha) ;
        }
        bmp.copyPixelsFromBuffer(byteBuffer) ;
        return bmp ;
    }
}
;