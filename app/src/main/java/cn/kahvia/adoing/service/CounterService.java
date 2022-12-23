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
    //多线程是否进行的标准。
    public static boolean running=true;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pageIndex=Integer.parseInt(intent.getStringExtra("pageIndex"));
        if (intent.getAction().equals("start")){
            running=true;
            counterThread=new Thread(
                    new Runnable() {
                        @Override
                        public void run() {

                            while (running){
                                try {
                                    counter++;
                                    sendMessageToActivity(counter);
                                    Log.i("Counter",counter+"");
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );
            counterThread.start();
            final String CHANNELID="TaskCounter";
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
            counter=0;
            running=false;
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
        Intent intent = new Intent("Counter");
        intent.putExtra("Counter", counter);
        sendBroadcast(intent);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
