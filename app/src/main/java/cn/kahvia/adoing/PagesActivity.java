package cn.kahvia.adoing;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
    private BroadcastReceiver myBroadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent!=null){
                int time=intent.getIntExtra("Counter",0);
                cardCounter.setText(TimeUtil.formatTime(time));
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        viewPager2.setOffscreenPageLimit(1);
        viewPager2.setCurrentItem(CounterService.pageIndex,false);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCardClickStart(View view){
        //获取父节点，即当前卡片，在其下寻找相应的组件

        View parent= (View) view.getParent();
        Button startButton=parent.findViewById(R.id.startButton);
        TextView pastTime=parent.findViewById(R.id.pastTime);
        TextView pageIndex=parent.findViewById(R.id.pageIndex);
        cardCounter=pastTime;
        startButton.setVisibility(View.INVISIBLE);
        pastTime.setVisibility(View.VISIBLE);

        //start my counter service
        Intent serviceIntent=new Intent(this, CounterService.class);
        serviceIntent.setAction("start");
        serviceIntent.putExtra("pageIndex",pageIndex.getText());
        startForegroundService(serviceIntent);
    }
    public void onCardClickEnd(View view){
        //获取父节点，即当前卡片
        View parent= (View) view.getParent();
        Button startButton=parent.findViewById(R.id.startButton);
        TextView pastTime=parent.findViewById(R.id.pastTime);
        TextView pageIndex=parent.findViewById(R.id.pageIndex);
        startButton.setVisibility(View.VISIBLE);
        pastTime.setVisibility(View.INVISIBLE);

        //stop my counter service
        Intent serviceIntent=new Intent(this, CounterService.class);
        serviceIntent.setAction("end");
        serviceIntent.putExtra("pageIndex",pageIndex.getText());
        startService(serviceIntent);
    }

    public void registerMyBroadcastReceiver(){
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("Counter");
        registerReceiver(myBroadcastReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }


}