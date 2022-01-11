package com.huawei.itrustee.hiaihook.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.itrustee.hiaihook.R;
import com.huawei.itrustee.hiaihook.bean.ModelInfo;
import com.huawei.itrustee.hiaihook.utils.ModelManager;
import com.huawei.itrustee.hiaihook.utils.Untils;

import java.io.File;


public class ClassifyActivity extends AppCompatActivity implements View.OnClickListener{

    protected ModelInfo demoModelInfo = new ModelInfo();
    protected RecyclerView rv;
    protected boolean useNPU  = false;
    protected boolean interfaceCompatible = true;
    protected Button btnsync = null;
    protected Button btnasync = null;
    protected LinearLayoutManager manager = null;

    private int curType = -1; // -1: unhook; 0: hook-single; 1: hook-partial; 2: hook-all

    /*static{
        System.loadLibrary("fridagadget64");
    }*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classify);
        initView();
        initModels();
        copyModels();
        modelCompatibilityProcess();
    }

    //added by zy
    private void hookOrUnhook(int newType) {
        if(newType == curType) {
            return;
        }

        if(curType != -1) {
            NativeHacker.unhook();
            curType = -1;
        }

        if(newType != -1) {
            NativeHacker.hook(newType);
            curType = newType;
        }
    }

    public void onRadioButtonClicked(View view) {
        switch(view.getId()) {
            case R.id.radio_hook_single:
                hookOrUnhook(0);
                break;
            case R.id.radio_hook_partial:
                hookOrUnhook(1);
                break;
            case R.id.radio_hook_all:
                hookOrUnhook(2);
                break;
            case R.id.radio_unhook:
                hookOrUnhook(-1);
                break;
        }
    }

    public void onTestClick(View view) {
        NativeHookee.test();
    }
    //added by zy

    private void initView() {
        manager = new LinearLayoutManager(this);
        btnsync = (Button) findViewById(R.id.btn_sync);
        btnasync = (Button) findViewById(R.id.btn_async);
        btnsync.setOnClickListener(this);
        btnasync.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //added by zy
    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            //要做的事情
            Log.d("zytest", "start AsyncClassifyActivity...");
            Intent intent = new Intent(ClassifyActivity.this, AsyncClassifyActivity.class);
            intent.putExtra("modelInfo", demoModelInfo);
            startActivity(intent);
            handler.postDelayed(this, 20000);
        }
    };


    public void begin() {
        handler.postDelayed(runnable, 20000);//每20秒执行一次runnable.
    }
    //added by zy

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sync:
                if(interfaceCompatible) {
                    if (useNPU) {
                        Intent intent = new Intent(ClassifyActivity.this, SyncClassifyActivity.class);
                        intent.putExtra("modelInfo", demoModelInfo);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Model incompatibility or NO online Compiler interface or Compile model failed, Please run it on CPU", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(this, "Interface incompatibility, Please run it on CPU", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_async:
                if(interfaceCompatible) {
                    if (useNPU) {
                        Intent intent = new Intent(ClassifyActivity.this, AsyncClassifyActivity.class);
                        intent.putExtra("modelInfo", demoModelInfo);
                        startActivity(intent);
                        //begin();
                    } else {
                        Toast.makeText(this, "Model incompatibility or NO online Compiler interface or Compile model failed, Please run it on CPU", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "Interface incompatibility, Please run it on CPU", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
    private void copyModels(){
        AssetManager am = getAssets();
        if(!Untils.isExistModelsInAppModels(demoModelInfo.getOnlineModel(),demoModelInfo.getModelSaveDir())){
            Untils.copyModelsFromAssetToAppModelsByBuffer(am, demoModelInfo.getOnlineModel(),demoModelInfo.getModelSaveDir());
        }
        if(!Untils.isExistModelsInAppModels(demoModelInfo.getOnlineModelPara(),demoModelInfo.getModelSaveDir())){
            Untils.copyModelsFromAssetToAppModelsByBuffer(am, demoModelInfo.getOnlineModelPara(),demoModelInfo.getModelSaveDir());
        }
        if(!Untils.isExistModelsInAppModels(demoModelInfo.getOfflineModel(),demoModelInfo.getModelSaveDir())){
            Untils.copyModelsFromAssetToAppModelsByBuffer(am, demoModelInfo.getOfflineModel(),demoModelInfo.getModelSaveDir());
        }

    }

    private void modelCompatibilityProcess(){
        //load libhiaijni.so
        boolean isSoLoadSuccess = ModelManager.loadJNISo();

        if (isSoLoadSuccess) {//npu
            Toast.makeText(this, "load libhiai.so success.", Toast.LENGTH_SHORT).show();

            interfaceCompatible = true;
            useNPU = ModelManager.modelCompatibilityProcessFromFile(demoModelInfo.getModelSaveDir() + demoModelInfo.getOnlineModel(),
                    demoModelInfo.getModelSaveDir() + demoModelInfo.getOnlineModelPara(),
                    demoModelInfo.getFramework(),demoModelInfo.getModelSaveDir() + demoModelInfo.getOfflineModel(),demoModelInfo.isMixModel());

//            byte[] onlinemodebuffer = Untils.getModelBufferFromModelFile(demoModelInfo.getModelSaveDir() + demoModelInfo.getOnlineModel());
//            byte[] onlinemodeparabuffer = Untils.getModelBufferFromModelFile(demoModelInfo.getModelSaveDir() + demoModelInfo.getOnlineModelPara());
//            useNPU = ModelManager.modelCompatibilityProcessFromBuffer(onlinemodebuffer,onlinemodeparabuffer,demoModelInfo.getFramework(),
//                    demoModelInfo.getModelSaveDir()+demoModelInfo.getOfflineModel(),demoModelInfo.isMixModel());
        }
        else {
            interfaceCompatible = false;
            Toast.makeText(this, "load libhiai.so fail.", Toast.LENGTH_SHORT).show();
            Log.i("zytest", "load libhiai.so fail.");
        }
    }
    protected void initModels(){
        File dir =  getDir("models", Context.MODE_PRIVATE);
        String path = dir.getAbsolutePath() + File.separator;
        Log.i("zytest", path);

        demoModelInfo.setModelSaveDir(path);
        demoModelInfo.setOnlineModel("deploy.prototxt");
        demoModelInfo.setOnlineModelPara("squeezenet_v1.1.caffemodel");
        //demoModelInfo.setOnlineModel("vdsr.prototxt");
        //demoModelInfo.setOnlineModelPara("vdsr.caffemodel");
        demoModelInfo.setFramework("caffe");
        demoModelInfo.setOfflineModel("offline_squeezenet");
        demoModelInfo.setOfflineModelName("squeezenet");
        //demoModelInfo.setOfflineModel("offline_vdsr");
        //demoModelInfo.setOfflineModelName("vdsr");
        demoModelInfo.setMixModel(false);
        demoModelInfo.setInput_Number(1);
        demoModelInfo.setInput_N(1);
        demoModelInfo.setInput_C(3);
        demoModelInfo.setInput_H(227);
        demoModelInfo.setInput_W(227);
        demoModelInfo.setOutput_Number(1);
        demoModelInfo.setOutput_N(1);
        demoModelInfo.setOutput_C(1000);
        demoModelInfo.setOutput_H(1);
        demoModelInfo.setOutput_W(1);
        demoModelInfo.setOnlineModelLabel("labels_squeezenet.txt");
    }

}
