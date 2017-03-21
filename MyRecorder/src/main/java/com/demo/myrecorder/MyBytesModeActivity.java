package com.demo.myrecorder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by 20170716 on 2017/3/21.
 */

public class MyBytesModeActivity extends AppCompatActivity {

    public final static String path = Environment.getExternalStorageDirectory()
            + "/Jennis/";

    public final static String prefix = ".pcm";

    public final static int RECORD_MIN_TIME = 1;

    @BindView(R.id.mr_mb_txt)
    public TextView txt;

    @BindView(R.id.mr_mb_btn_2)
    public TextView btn_2;

    private Handler handler = null;

    private ExecutorService executorService = null;

    private volatile boolean isRecording = false;

    //buffer不能太大，避免OOM
    private final static int BUFFER_SIZE = 2048;

    private byte[] bytes = null;

    private File myFile = null;

    private FileOutputStream fileOutputStream = null;

    private AudioRecord audioRecord = null;

    private long recordStartTime, recordStopTime, fileCreateTime;

    private volatile boolean isPlaying = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_bytes);
        ButterKnife.bind(this);
        handler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();
        bytes = new byte[BUFFER_SIZE];
    }

    @OnClick(R.id.mr_mb_btn_1)
    public void doPlay() {
        //检查当前状态，防止重复播放
        if (myFile != null && !isPlaying && !isRecording) {
            //设置当前播放状态
            isPlaying = true;
            //提交后台任务，开始播放
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    startPlay(myFile);
                }
            });
        }
    }

    private void startPlay(File myFile) {
        //配置播放器
        //音乐类型，扬声器播放
        int streamType = AudioManager.STREAM_MUSIC;
        int sampleRate = 44100;
        //MONO 单声道，录音输入单声道，播放输出单声道
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        //PCM 16是所有安卓系统都支持的格式
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        //流模式
        int mode = AudioTrack.MODE_STREAM;
        //计算AudioRecord内部buffer最小的大小
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                channelConfig, audioFormat);
        //构造AudioTrack
        AudioTrack audioTrack = new AudioTrack(
                streamType,
                sampleRate,
                channelConfig,
                audioFormat,
                Math.max(minBufferSize, BUFFER_SIZE),
                mode
        );
        audioTrack.play();

        //从文件流读数据
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(myFile);

            //循环读取数据，写到播放器去播放
            int read = -1;
            //只要没读完，循环写播放
            while ((read = fileInputStream.read(bytes)) != -1) {
                int ret = audioTrack.write(bytes, 0, read);
                //检查write返回值，错误处理
                switch (ret) {
                    case AudioTrack.ERROR_INVALID_OPERATION:
                    case AudioTrack.ERROR_BAD_VALUE:
                    case AudioManager.ERROR_DEAD_OBJECT:
                        playerFailure();
                        return;
                    default:
                }
            }
        } catch (IOException | RuntimeException e) {
            //错误处理，防止闪退
            e.printStackTrace();
            //提示用户
            playerFailure();
        } finally {
            //重置状态
            isPlaying = false;

            //静默关闭文件输入流
            if (fileInputStream != null) {
                closeQuietly(fileInputStream);
            }
            //静默释放播放器
            releaseQuietly(audioTrack);
        }
    }

    private void playerFailure() {
        myFile = null;

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyBytesModeActivity.this,
                        "录音播放失败，emoji", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //输入流的静默关闭
    private void closeQuietly(FileInputStream fileInputStream) {
        try {
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseQuietly(AudioTrack audioTrack) {
        try {
            audioTrack.stop();
            audioTrack.release();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.mr_mb_btn_2)
    public void record() {
        //根据当前状态，改变UI，执行开始/停止录音的逻辑
        if (isRecording) {
            //改变UI的状态
            btn_2.setText(R.string.mb_start);
            //改变录音状态
            isRecording = false;
        } else {
            //改变UI的状态
            btn_2.setText(R.string.mb_stop);
            //改变录音状态
            isRecording = true;
            //提交后台任务，执行开始录音逻辑
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    if (!doStartRecord()) {
                        recorderFailure();
                    }
                }
            });
        }
    }

    private boolean doStartRecord() {
        try {
            //创建录音文件
            fileCreateTime = System.currentTimeMillis();
            myFile = new File(path + fileCreateTime + prefix);
            myFile.getParentFile().mkdirs();
            myFile.createNewFile();

            //创建文件输出流
            fileOutputStream = new FileOutputStream(myFile);

            //配置AudioRecord
            int audioSource = MediaRecorder.AudioSource.MIC;
            int sampleRate = 44100;
            //单声道输入
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;
            //PCM 16是所有安卓系统都支持的格式
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            //计算AudioRecord内部buffer最小的大小
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                    channelConfig, audioFormat);
            //buffer不能小于最低要求，也不能小于我们每次读取的大小
            audioRecord = new AudioRecord(
                    audioSource,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    Math.max(minBufferSize, BUFFER_SIZE)
            );

            //开始录音
            audioRecord.startRecording();

            //记录开始录音时间
            recordStartTime = System.currentTimeMillis();

            //循环读取数据写到输出流中
            while (isRecording) {
                //只要处于录音状态，就一直读取数据
                int read = audioRecord.read(bytes, 0, BUFFER_SIZE);
                if (read > 0) {
                    //读取成功写入文件中
                    fileOutputStream.write(bytes, 0, read);
                } else {
                    //读取失败，返回提示用户
                    return false;
                }
            }
            //退出循环，结束录音，释放资源
            return doStopRecord();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return false;
        } finally {
            //释放AudioRecord
            if (audioRecord != null) {
                audioRecord.release();
                audioRecord = null;
            }
        }
    }

    private boolean doStopRecord() {
        try {
            //停止录音
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;

            //关闭文件输出流
            fileOutputStream.close();

            //记录录音结束时间
            recordStopTime = System.currentTimeMillis();

            //在UI线程改变UI
            final int sec = (int) ((recordStopTime - recordStartTime) / 1000);
            if (sec >= RECORD_MIN_TIME) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        txt.setText("录音成功了亲，总计"
                                + sec + "秒");
                    }
                });
            } else {
                return false;
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void recorderFailure() {
        myFile = null;

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyBytesModeActivity.this,
                        "录音失败了，emoji", Toast.LENGTH_SHORT).show();
                //重置录音和UI状态
                isRecording = false;
                btn_2.setText(R.string.mb_start);
            }
        });

        recordFailureFileDelete();
    }

    private void recordFailureFileDelete() {
        File file = new File(path);
        if (file.exists()) {
            File[] childFiles = file.listFiles();
            for (File c : childFiles
                    ) {
                if (c.getName().equals(fileCreateTime + prefix)) {
                    c.delete();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}
