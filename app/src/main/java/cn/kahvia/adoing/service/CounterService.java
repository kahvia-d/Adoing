package cn.kahvia.adoing.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.jetbrains.annotations.Nullable;
import androidx.annotation.RequiresApi;

import cn.kahvia.adoing.R;

public class CounterService extends Service {
    public static int counter=0;
    public static int pageIndex=-1;
    public static Thread counterThread=null;
    //子线程是否继续进行的标准。
    public static boolean running=false;
    public static boolean threadEnd=true;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pageIndex=Integer.parseInt(intent.getStringExtra("pageIndex"));
        if (intent.getAction().equals("start")){
            running=true;
            threadEnd=false;
            counterThread=new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            while (running){
                                try {
                                    Thread.sleep(1000);
                                    counter++;
                                    sendMessageToActivity(counter);
//                                    Log.i("Counter",counter+"");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            //计时器停止工作后，计时器归零
                            counter=0;
                            //确认不会对计时器的数值进行操作后，标志线程终止
                            threadEnd=true;
                        }
                    }
            );
            counterThread.start();
            final String CHANNELID="TaskCounter";
            //定义通知栏通知面板
            NotificationChannel channel=new NotificationChannel(
                    CHANNELID,
                    CHANNELID,
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification=new Notification.Builder(this,CHANNELID)
                    .setContentTitle("ADoing")
                    .setContentText("Counter is working")
                    .setSmallIcon(R.drawable.notification);
            startForeground(666,notification.build());
        }else {
            //设置running为false，以结束线程上的计数循环
            running=false;
            //停止前台并移除通知
            stopForeground(true);
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendMessageToActivity(int counter) {
        //发送广播，传递计时器的数据
        Intent intent = new Intent("Counter");
        intent.putExtra("Counter", counter);
        sendBroadcast(intent);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
