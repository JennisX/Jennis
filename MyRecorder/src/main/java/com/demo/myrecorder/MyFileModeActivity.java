package com.demo.myrecorder;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by 20170716 on 2017/3/21.
 */

public class MyFileModeActivity extends AppCompatActivity {

    public final static String path = Environment.getExternalStorageDirectory()
            + "/Jennis/";

    public final static String prefix = ".m4a";

    public final static int RECORD_MIN_TIME = 1;

    @BindView(R.id.mr_mf_btn_1)
    public TextView btn_1;

    @BindView(R.id.mr_mf_btn_2)
    public TextView btn_2;

    @BindView(R.id.mr_mf_txt)
    public TextView txt;

    private Handler handler = null;

    private ExecutorService executorService = null;

    private MediaRecorder mediaRecorder = null;

    private File myFile = null;

    private long recordStartTime, recordStopTime, fileCreateTime;

    //主线程和后台播放线程数据同步
    private volatile boolean isPlaying = false;

    private MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_file);
        ButterKnife.bind(this);

        handler = new Handler(Looper.getMainLooper());

        //录音JNI函数不具备线程安全性，所以这里用单线程
        executorService = Executors.newSingleThreadExecutor();

        //按下说话，采用OnTouch...，不用OnClick...
        btn_2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecord();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stopRecord();
                        break;
                    default:
                }
                return true;
            }
        });
    }

    @OnClick(R.id.mr_mf_btn_1)
    public void doPlay() {
        //检查当前状态，防止重复播放
        if(myFile != null && !isPlaying) {
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

    //实际播放逻辑
    private void startPlay(File myFile) {
        try {
            //配置MediaPlayer
            mediaPlayer = new MediaPlayer();

            //设置声音文件
            mediaPlayer.setDataSource(myFile.getAbsolutePath());

            //设置监听回调
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //释放MediaPlayer
                    stopPlay();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    //提示用户
                    playerFailure();

                    //释放播放器
                    stopPlay();

                    //错误已处理
                    return true;
                }
            });

            //设置音量，是否循环
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(false);

            //播放开始
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch(IOException | RuntimeException e) {
            //异常处理，防止闪退
            e.printStackTrace();
            //提示用户
            playerFailure();
            //释放播放器
            stopPlay();
        }
    }

    //停止播放逻辑
    private void stopPlay() {
        try {
            //重置状态
            isPlaying = false;

            //释放播放器
            if(mediaPlayer != null) {
                //重置监听器，防止内存泄漏
                mediaPlayer.setOnCompletionListener(null);
                mediaPlayer.setOnErrorListener(null);

                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void playerFailure() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyFileModeActivity.this,
                        "录音播放失败，emoji", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startRecord() {
        //改变UI状态
        btn_2.setText(R.string.recording);

        //提交后台任务，执行录音逻辑
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                //释放之前录音的recorder
                releaseRecord();

                //执行开始录音逻辑，失败告知用户
                if (!doStartRecord()) {
                    recordFailure();
                }
            }
        });
    }

    private void stopRecord() {
        //改变UI状态
        btn_2.setText(R.string.start_record);

        //提交后台任务，执行录音逻辑
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                //执行结束录音逻辑，失败告知用户
                if (!doStopRecord()) {
                    recordFailure();
                }

                //释放之前录音的recorder
                releaseRecord();
            }
        });
    }

    //录音开始逻辑
    private boolean doStartRecord() {
        try {
            //创建MediaRecorder
            mediaRecorder = new MediaRecorder();

            //创建录音文件
            fileCreateTime = System.currentTimeMillis();
            myFile = new File(path + fileCreateTime + prefix);
            myFile.getParentFile().mkdirs();
            myFile.createNewFile();

            //配置MediaRecorder
            //从麦克风采集数据
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //文件格式保存为MP4
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //所有安卓系统都支持的采样频率
            mediaRecorder.setAudioSamplingRate(44100);
            //通用的ACC编码格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //音质比较好的频率
            mediaRecorder.setAudioEncodingBitRate(96000);
            //录音文件位置
            mediaRecorder.setOutputFile(myFile.getAbsolutePath());

            //开始录音
            mediaRecorder.prepare();
            mediaRecorder.start();

            //记录录音开始时间
            recordStartTime = System.currentTimeMillis();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            //录音失败，告知用户，捕获异常为了防止闪退
            return false;
        }
        //录音成功
        return true;
    }

    //录音停止逻辑
    private boolean doStopRecord() {
        try {
            //停止录音
            mediaRecorder.stop();

            //记录录音结束时间
            recordStopTime = System.currentTimeMillis();

            //只接受超过n秒的录音，在UI显现出来
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
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        //停止成功
        return true;
    }

    private void releaseRecord() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void recordFailure() {
        myFile = null;

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyFileModeActivity.this,
                        "录音失败了，emoji", Toast.LENGTH_SHORT).show();
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

        //Activity销毁时，为避免内存泄漏，要停止后台任务
        executorService.shutdownNow();

        releaseRecord();

        stopPlay();
    }
}
