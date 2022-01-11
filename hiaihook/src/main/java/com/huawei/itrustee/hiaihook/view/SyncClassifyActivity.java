package com.huawei.itrustee.hiaihook.view;


import static com.huawei.itrustee.hiaihook.utils.Constant.AI_OK;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.huawei.itrustee.hiaihook.bean.ModelInfo;
import com.huawei.itrustee.hiaihook.utils.ModelManager;


public class SyncClassifyActivity extends NpuClassifyActivity {
    private static final String TAG = SyncClassifyActivity.class.getSimpleName();

    public String offlineModelName;
    public String offlineModelPath;
    public boolean isMixModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void loadModelFromFile(String offlineModelName, String offlineModelPath,boolean isMixModel) {
        this.offlineModelName = offlineModelName;
        this.offlineModelPath = offlineModelPath;
        this.isMixModel = isMixModel;
    }

    @Override
    protected void loadModelFromBuffer(String offlineModelName, byte[] offlineModelBuffer,boolean isMixModel) {
            int ret = ModelManager.loadModelSyncFromBuffer(offlineModelName,offlineModelBuffer,isMixModel);
        if (AI_OK == ret) {
            Toast.makeText(SyncClassifyActivity.this,
                    "load model success.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SyncClassifyActivity.this,
                    "load model fail.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void runModel(ModelInfo modelInfo, float[][] inputData) {
        long start = System.currentTimeMillis();
        outputData = ModelManager.runModelSync(modelInfo, inputData);
        long end = System.currentTimeMillis();
        inferenceTime = end - start;
        if(outputData == null){
            Log.e(TAG,"runModelSync fail ,outputData is null");
            return;
        }
        Log.i(TAG, "runModel outputdata length : " + outputData.length + "/inferenceTime = "+inferenceTime);

        postProcess(outputData);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        int result = ModelManager.unloadModelSync();

        if (AI_OK == result) {
            Toast.makeText(this, "unload model success.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "unload model fail.", Toast.LENGTH_SHORT).show();
        }
    }
}
