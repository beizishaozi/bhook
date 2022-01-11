package com.huawei.itrustee.hiaihook.view;


import static com.huawei.itrustee.hiaihook.utils.Constant.AI_OK;
import static com.huawei.itrustee.hiaihook.utils.Constant.GALLERY_REQUEST_CODE;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.huawei.itrustee.hiaihook.bean.ModelInfo;
import com.huawei.itrustee.hiaihook.utils.ModelManager;
import com.huawei.itrustee.hiaihook.utils.ModelManagerListener;
import com.huawei.itrustee.hiaihook.utils.Untils;

import java.io.IOException;


public class AsyncClassifyActivity extends NpuClassifyActivity {

    private static final String TAG = AsyncClassifyActivity.class.getSimpleName();


    ModelManagerListener listener = new ModelManagerListener() {

        @Override
        public void onStartDone(final int taskId) {
            Log.e(TAG, " java layer onStartDone: " + taskId);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (taskId > 0) {
                        Toast.makeText(AsyncClassifyActivity.this, "load model success. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AsyncClassifyActivity.this, "load model fail. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onRunDone(final int taskId, final float[][] output,final float inferencetime) {

            Log.e(TAG, " java layer onRunDone: " + taskId);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (taskId > 0) {
                        Toast toast = Toast.makeText(AsyncClassifyActivity.this, "run model success. taskId is:" + taskId, Toast.LENGTH_SHORT);
                        CustomToast.showToast(toast, 50);
                        outputData = output;
                        inferenceTime = inferencetime/1000;
                        postProcess(outputData);
                    } else {
                        Toast toast = Toast.makeText(AsyncClassifyActivity.this, "run model fail. taskId is:" + taskId, Toast.LENGTH_SHORT);
                        CustomToast.showToast(toast, 50);
                    }
                }
            });

        }

        @Override
        public void onStopDone(final int taskId) {
            Log.e(TAG, "java layer onStopDone: " + taskId);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (taskId > 0) {
                        Toast.makeText(AsyncClassifyActivity.this, "unload model success. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AsyncClassifyActivity.this, "unload model fail. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onTimeout(final int taskId) {
            Log.e(TAG, "java layer onTimeout: " + taskId);
        }

        @Override
        public void onError(final int taskId, final int errCode) {
            Log.e(TAG, "onError:" + taskId + " errCode:" + errCode);
        }

        @Override
        public void onServiceDied() {
            Log.e(TAG, "onServiceDied: ");
        }
    };
    //added by zy
    Handler handler1=new Handler();
    Runnable runnable1=new Runnable() {
        @Override
        public void run() {
            //要做的事情
            Log.d("zytest", "startAnalyze....");
            startAnalyze();
        }
    };

    Handler handler2=new Handler();
    Runnable runnable2=new Runnable() {
        @Override
        public void run() {
            //要做的事情
            Log.d("zytest", "start finish....");
            finish();
        }
    };
    //added by zy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //handler1.postDelayed(runnable1, 6000);//5秒之后执行一次图片分析，前5秒执行oncreate函数中模型文件加载.
        //handler2.postDelayed(runnable2, 13000);//15之后执行一次activity退出，中间10秒用于执行分析.
    }

    @Override
    protected void loadModelFromFile(String offlineModelName, String offlineModelPath,boolean isMixModel) {
        int ret = ModelManager.registerListenerJNI(listener);

        Log.e(TAG, "loadModelFromFile: " + ret + ".. offlineModelPath : "+offlineModelPath+".offlineModelName = "+offlineModelName);
        if (AI_OK == ret) {
            Toast.makeText(this,
                    "load model success.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    "load model fail.", Toast.LENGTH_SHORT).show();
        }
        Log.d("zytest", offlineModelName+"//"+offlineModelPath);
        ModelManager.loadModelFromFileAsync(offlineModelName, offlineModelPath, isMixModel);
        //it will call method "vendor::huawei::hardware::ai::V1_1::BpHwAiModelMngr::_hidl_startModelFromMem2()"
    }

    public void startAnalyze(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    GALLERY_REQUEST_CODE);
        } else {
            try {
                Bitmap bitmap;
                ContentResolver resolver = getContentResolver();
                Uri originalUri = Uri.parse("content://media/external/images/media/23573");
                Log.d("zytest", originalUri.toString());
                bitmap = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                cursor.moveToFirst();
                Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Log.i("zytest", "GALLERY_REQUEST_CODE getInput_W:" + modelInfo.getInput_W() + ",getInput_H:" + modelInfo.getInput_H());
                initClassifiedImg = Bitmap.createScaledBitmap(rgba, modelInfo.getInput_W(), modelInfo.getInput_H(), false);

                float[] inputData = Untils.getPixels(modelInfo.getFramework(), initClassifiedImg, modelInfo.getInput_W(), modelInfo.getInput_H());
                float inputdatas[][] = new float[1][];
                inputdatas[0] = inputData;
                runModel(modelInfo, inputdatas);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    @Override
    protected void loadModelFromBuffer(String offlineModelName, byte[] offlineModelBuffer,boolean isMixModel) {
        int ret = ModelManager.registerListenerJNI(listener);

        Log.e(TAG, "loadModelFromBuffer: " + ret);
        ModelManager.loadModelAsyncFromBuffer(offlineModelName,offlineModelBuffer,isMixModel);
    }

    @Override
    protected void runModel(ModelInfo modelInfo, float[][] inputData) {
        ModelManager.runModelAsync(modelInfo, inputData);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ModelManager.unloadModelAsync();
    }
}
