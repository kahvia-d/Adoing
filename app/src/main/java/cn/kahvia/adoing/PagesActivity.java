package cn.kahvia.adoing;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.kahvia.adoing.adaptors.ViewPagerAdaptor;
import cn.kahvia.adoing.pojo.CardItem;
import cn.kahvia.adoing.service.CounterService;
import cn.kahvia.adoing.utils.TimeUtil;

public class PagesActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private List<CardItem> cardItems;
    public static TextView cardCounter;
    private AnimatorSet frontAniSet;
    private AnimatorSet backAniSet;
    private BroadcastReceiver myBroadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent!=null){
                int time=intent.getIntExtra("Counter",0);
                //根据广播内容，更新UI
                cardCounter.setText(TimeUtil.formatTime(time));
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        frontAniSet= (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.front_anim);
        backAniSet= (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.back_anim);

        setContentView(R.layout.activity_pages);

        //注册广播接收器
        registerMyBroadcastReceiver();

        viewPager2=findViewById(R.id.viewPager2);

        int[] images={R.drawable.bg1,R.drawable.girlandcat};
        String[] titles={"Study","Game"};
        String[] contents={"I want to study from now on. No matter how I am tired, it should keep moving on.","I want to play games from now on."};

        cardItems=new ArrayList<CardItem>();
        for (int i=0;i<images.length;i++){
            cardItems.add(new CardItem(images[i],titles[i],contents[i]));
        }

        viewPager2.setAdapter(new ViewPagerAdaptor(cardItems));
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(2);
        viewPager2.setCurrentItem(CounterService.pageIndex,false);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCardClickStart(View view){
        if (!CounterService.running){
            //计时器服务，线程停止后可开启新的计时任务
            if (CounterService.threadEnd){
                //获取父节点，即当前卡片，在其下寻找相应的组件
                View parent= (View) view.getParent();
                Button startButton=parent.findViewById(R.id.startButton);
                TextView pastTime=parent.findViewById(R.id.pastTime);
                TextView pageIndex=parent.findViewById(R.id.pageIndex);

                //保存展示计时器数据的对象，用以更新UI，显示最新计时数据
                cardCounter=pastTime;
                //隐藏按钮，显示计时
                startButton.setVisibility(View.INVISIBLE);
                pastTime.setVisibility(View.VISIBLE);

                //重新开始计时的时候，设定计时初始值
                pastTime.setText(TimeUtil.formatTime(CounterService.counter));

                //start my counter service
                Intent serviceIntent=new Intent(this, CounterService.class);
                serviceIntent.setAction("start");
                serviceIntent.putExtra("pageIndex",pageIndex.getText());
                startForegroundService(serviceIntent);
            }else {
                Toast.makeText(view.getContext(),"正在记录数据，请稍后",Toast.LENGTH_SHORT).show();

            }
        }else {
            Toast.makeText(view.getContext(),"只能开启单任务",Toast.LENGTH_SHORT).show();
        }

    }
    public void onCardClickEnd(View view){
        //获取父节点，即当前卡片，在其下寻找相应的组件
        View parent= (View) view.getParent();
        Button startButton=parent.findViewById(R.id.startButton);
        TextView pastTime=parent.findViewById(R.id.pastTime);
        TextView pageIndex=parent.findViewById(R.id.pageIndex);

        //显示按钮，隐藏计时
        startButton.setVisibility(View.VISIBLE);
        pastTime.setVisibility(View.INVISIBLE);

        //stop my counter service
        Intent serviceIntent=new Intent(this, CounterService.class);
        serviceIntent.setAction("end");
        serviceIntent.putExtra("pageIndex",pageIndex.getText());
        startService(serviceIntent);
    }

    public void onCardFlip(View view){
        View cardsParent=(View) view.getParent().getParent().getParent();
        View cardFront=cardsParent.findViewById(R.id.card_front);
        View cardBack=cardsParent.findViewById(R.id.card_back);

        frontAniSet.setTarget(cardFront);
        backAniSet.setTarget(cardBack);

        float scale=this.getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(scale*10000);
        cardBack.setCameraDistance(scale*10000);

        cardBack.setVisibility(View.VISIBLE);
        frontAniSet.start();
        Thread thread=new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        cardFront.setVisibility(View.INVISIBLE);
                    }
                }
        );
        //排队等待cpu
        thread.start();

        backAniSet.start();



    }

    //广播接收器
    public void registerMyBroadcastReceiver(){
        IntentFilter intentFilter=new IntentFilter();
        //添加action，标记广播接收范围
        intentFilter.addAction("Counter");
        registerReceiver(myBroadcastReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }


}