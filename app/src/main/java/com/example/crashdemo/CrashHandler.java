package com.example.crashdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Description:
 * Author: qiubing
 * Date: 2017-07-17 15:47
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final String CRASH_FILE_NAME = "crash-";
    private static final String CRASH_FILE_NAME_SUFFIX = ".txt";

    // 系统默认的异常处理（默认情况下，系统会终止当前的异常程序）
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;

    private Context mContext;

    public void init(Context context){
        // 获取系统默认的异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Log.e(TAG, "mDefaultCrashHandler = " + mDefaultCrashHandler);
        // 将当前实例设置为系统默认的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);

        mContext = context;
    }

    /**
     * 当程序中有未捕获的异常时，系统会自动调用uncaughtException方法来处理
     * @param thread 为出现未捕获异常的线程
     * @param throwable 为未捕获的异常，可以从中得到异常信息
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e(TAG, "uncaughtException()...thread = " + thread + ",throwable = " + throwable);
        try {
            // 导出异常信息到SD卡中
            dumpExceptionToSDCard(throwable);
            // TODO 通过网络上传异常信息到服务器，便于开发人员分析日志从而解决Bug

        }catch (IOException e){
            e.printStackTrace();
        }

        //打印出当前调用栈信息
        throwable.printStackTrace();

        // 如果系统提供了默认的异常处理器，则交给系统结束我们的程序，否则我们自己结束。
        if (mDefaultCrashHandler != null){
            mDefaultCrashHandler.uncaughtException(thread,throwable);
        }else {
            android.os.Process.killProcess(Process.myPid());
        }
    }

    // 记录Crash信息到SD卡中
    private void dumpExceptionToSDCard(Throwable ex) throws IOException{
        // 保存Crash日志文件目录
        String crashFilePath = getDiskCacheDir(mContext) + "/log/";
        File dir = new File(crashFilePath);
        if (!dir.exists()){
            dir.mkdirs();
        }

        long currentTime = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currentTime));

        //以当前时间创建log文件
        File file = new File(crashFilePath + CRASH_FILE_NAME + time + CRASH_FILE_NAME_SUFFIX);
        Log.e(TAG, "file = " + file.getName() + ",path = " + file.getPath());
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            //记录发生异常的时间
            pw.println(time);

            //获取手机的信息
            getPhoneInfo(pw);

            pw.println();

            // 导出异常的调用堆栈信息
            ex.printStackTrace(pw);

            pw.close();
        }catch (Exception e){
            Log.e(TAG,"dump crash info failed");
        }
    }

    private void getPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException{
        //TODO 上报一些辅助信息，例如应用版本号、系统版本号、手机型号等，方便数据分析和归类
    }

    private String getDiskCacheDir(Context context){
        String crashPath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
               || !Environment.isExternalStorageRemovable()){
            crashPath = context.getExternalCacheDir().getPath();
        }else {
            crashPath = context.getCacheDir().getPath();
        }
        return crashPath;
    }
}


