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
import android.util.Size;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.kahvia.adoing.adaptors.ViewPagerAdaptor;
import cn.kahvia.adoing.pojo.CardItem;
import cn.kahvia.adoing.pojo.Record;
import cn.kahvia.adoing.service.CounterService;
import cn.kahvia.adoing.utils.ChartsUtil;
import cn.kahvia.adoing.utils.MySqlHelper;
import cn.kahvia.adoing.utils.TimeUtil;

public class PagesActivity extends AppCompatActivity {
    private MySqlHelper mySqlHelper;
    private ViewPager2 viewPager2;
    private PieChart dayChart;
    private List<CardItem> cardItems;
    private List<Record> records;
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

//        deleteDatabase("adoing.db");
        //加载数据库
        mySqlHelper=MySqlHelper.getInstance(this);
        mySqlHelper.openReadLink();
        mySqlHelper.openWriteLink();


        frontAniSet= (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.front_anim);
        backAniSet= (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.back_anim);

        setContentView(R.layout.activity_pages);

        //注册广播接收器
        registerMyBroadcastReceiver();

        viewPager2=findViewById(R.id.viewPager2);
        cardItems=mySqlHelper.readCards();
        viewPager2.setAdapter(new ViewPagerAdaptor(cardItems));
        viewPager2.setOffscreenPageLimit(4);
        viewPager2.setCurrentItem(CounterService.pageIndex,false);

        dayChart=findViewById(R.id.dayChart);
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

        //save the counter data
        TextView cardTitle=parent.findViewById(R.id.itemTitle);
        mySqlHelper.addRecord(cardTitle.getText().toString(),CounterService.counter);

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
        TextView cardDbId=cardBack.findViewById(R.id.cardDbId);

        CardItem card=new CardItem();

        //更新图片的Uri地址
        card.setImage(Uri.parse((String) imageBack.getTag()));
        imageFront.setImageURI(card.getImage());
        //更新标题
        card.setTitle(titleBack.getText().toString());
        title.setText(card.getTitle());
        //更新卡片描述
        card.setContent(contentBack.getText().toString());
        content.setText(card.getContent());
        //更新数据库
        card.setId(Integer.parseInt(cardDbId.getText().toString()));
        mySqlHelper.updateCard(card);
    }

    public void addEmptyCard(View view){
        int index=viewPager2.getCurrentItem();
        CardItem demo=new CardItem(Uri.parse("android.resource://cn.kahvia.adoing/" + R.drawable.bg1),"DEMO","Hello world!");
        long rowId=mySqlHelper.addNewCard(demo);
        if (rowId!=-1){
            cardItems=mySqlHelper.readCards();
            viewPager2.setAdapter(new ViewPagerAdaptor(cardItems));
            viewPager2.setCurrentItem(index,false);
            viewPager2.setCurrentItem(cardItems.size()-1);
        }
    }

    public void deleteCard(View view){
        View parent= (View) view.getParent().getParent().getParent();
        TextView cardDbId=parent.findViewById(R.id.cardDbId);
        TextView pageIndex=parent.findViewById(R.id.pageIndex);

        //获得要删除的卡片的索引，查看这个卡片是否正在计时
        int index=Integer.parseInt(pageIndex.getText().toString());
        if (index==CounterService.pageIndex){
            Toast.makeText(view.getContext(),"计时中的卡片无法删除",Toast.LENGTH_SHORT).show();
            return;
        }

        CardItem card=new CardItem();
        card.setId(Integer.parseInt(cardDbId.getText().toString()));
        mySqlHelper.deleteCard(card);

        //删除卡片后，修正计时卡片的对应索引页
        if (index<CounterService.pageIndex){
            CounterService.pageIndex-=1;
        }


        //刷新卡片列表
        cardItems=mySqlHelper.readCards();
        viewPager2.setAdapter(new ViewPagerAdaptor(cardItems));
        viewPager2.setCurrentItem(CounterService.pageIndex,false);

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


    public void changeCardOrCharts(View view){
        if (viewPager2.getVisibility()==View.VISIBLE){
            viewPager2.setVisibility(View.INVISIBLE);
            records=mySqlHelper.readRecords();

            //获取用于展示的数据清单
            PieData pieData=new PieData(ChartsUtil.getPieDataSet(records));
            //设置值的字体大小
            pieData.setValueTextSize(12f);
            //设置数据源
            dayChart.setData(pieData);
            //设置动画时长
            dayChart.animateXY(500,500);
            //设置中间提示语
            dayChart.setCenterText("Today(%)");
            //设置圆饼图可见
            dayChart.setVisibility(View.VISIBLE);
        }else{
            dayChart.setVisibility(View.INVISIBLE);
            viewPager2.setVisibility(View.VISIBLE);
        }
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
        mySqlHelper.closeAllDBLinks();
    }


}