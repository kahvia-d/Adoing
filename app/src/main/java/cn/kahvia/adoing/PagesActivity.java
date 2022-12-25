package cn.kahvia.adoing;

import androidx.annotation.Nullable;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    public static ImageView cardBackImage;
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

        //创建动画监听器
        Animator.AnimatorListener listener=new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //正面动画开始的时候，设置反面可见，这是为了防止动画不可见
                cardBack.setVisibility(View.VISIBLE);
                //开始反面的动画
                backAniSet.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //动画结束的时候，设置正面不可见，这是为了防止用户与反面交互的时候，触发了正面的按钮事件
                cardFront.setVisibility(View.INVISIBLE);
                //移除当前监听器，防止监听器的叠加导致反复触发以上操作
                frontAniSet.removeListener(this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        //设置动画生效的目标
        frontAniSet.setTarget(cardFront);
        backAniSet.setTarget(cardBack);
        //添加监听器
        frontAniSet.addListener(listener);

        //设置观察距离
        float scale=this.getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(scale*10000);
        cardBack.setCameraDistance(scale*10000);

        //动画开始
        frontAniSet.start();


    }

    public void onCardFlip2(View view){
        View cardsParent=(View) view.getParent().getParent().getParent();
        View cardFront=cardsParent.findViewById(R.id.card_front);
        View cardBack=cardsParent.findViewById(R.id.card_back);

        Animator.AnimatorListener listener=new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                cardFront.setVisibility(View.VISIBLE);
                backAniSet.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                cardBack.setVisibility(View.INVISIBLE);
                frontAniSet.removeListener(this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        frontAniSet.setTarget(cardBack);
        backAniSet.setTarget(cardFront);
        frontAniSet.addListener(listener);

        float scale=this.getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(scale*10000);
        cardBack.setCameraDistance(scale*10000);

        frontAniSet.start();

        if (view.getId()==R.id.card_applyChange_button)
            applyCardChanges(cardFront,cardBack);
    }

    public void applyCardChanges(View cardFront,View cardBack){
        ImageView imageFront= cardFront.findViewById(R.id.itemImg);
        ImageView imageBack=cardBack.findViewById(R.id.itemImg_back);
        TextView title=cardFront.findViewById(R.id.itemTitle);
        EditText titleBack=cardBack.findViewById(R.id.itemTitle_back);
        TextView content=cardFront.findViewById(R.id.itemContent);
        EditText contentBack=cardBack.findViewById(R.id.itemContent_back);

        //更新图片的Uri地址
        imageFront.setImageURI(Uri.parse((String) imageBack.getTag()));
        //更新标题
        title.setText(titleBack.getText());
        //更新卡片描述
        content.setText(contentBack.getText());
    }

    public void addEmptyCard(View view){
        int index=viewPager2.getCurrentItem();
        cardItems.add(new CardItem(R.drawable.bg1,"DEMO","Hello world!"));
        viewPager2.setAdapter(new ViewPagerAdaptor(cardItems));
        viewPager2.setCurrentItem(index,false);
        viewPager2.setCurrentItem(cardItems.size()-1);
    }

    //广播接收器
    public void registerMyBroadcastReceiver(){
        IntentFilter intentFilter=new IntentFilter();
        //添加action，标记广播接收范围
        intentFilter.addAction("Counter");
        registerReceiver(myBroadcastReceiver,intentFilter);
    }

    public void selectImage(View view){
        cardBackImage= (ImageView) view;
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,666);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==666&&resultCode==RESULT_OK){
            View view=viewPager2.getChildAt(viewPager2.getCurrentItem());
            Uri photo=data.getData();
            cardBackImage.setImageURI(photo);
            cardBackImage.setTag(photo.toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }


}