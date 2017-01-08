package com.zyx.android.phonerecords;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;


/**
 * 此页面的关键在于保证服务在通话过程中一直存活。
 * 注意测试
 */
public class RecorderService extends Service {

    private SharedPreferences sp;
    private MediaRecorder mediaRecorder;

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences("config",MODE_PRIVATE);
        String filename = sp.getString("filename","");

        if ((!TextUtils.isEmpty(filename))){
            //当文件名存在，开始录音

            //注意：这个API就是个大坑货，使用时一定要小心，尤其是声音源和路径这两块
            // 1.实例化一个录音机
            mediaRecorder = new MediaRecorder();
            //2.指定录音机的声音源
            //此处的MediaRecorder.AudioSource.VOICE_CALL不可用，造成mediaRecorder.start()失败
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //3.设置录制的文件输出的格式
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //4.设置文件路径
            //若SD卡上的文件夹不存在，必须先创建好文件夹
            //然而4.4无法在SD卡中新建文件夹
            //会报错:open failed: ENOENT (No such file or directory)
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + filename + ".3gp";
            mediaRecorder.setOutputFile(path);
            //5.设置音频的编码
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //6.准备开始录音
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //7.开始录音
            mediaRecorder.start();

            Toast.makeText(this, "录音开始", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mediaRecorder!=null) {
            sp = getSharedPreferences("config",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("filename","");
            editor.commit();

            //8.停止捕获
            mediaRecorder.stop();
            //9.释放资源
            mediaRecorder.release();
            mediaRecorder = null;

            Toast.makeText(this, "录音已保存", Toast.LENGTH_SHORT).show();
        }
    }


}