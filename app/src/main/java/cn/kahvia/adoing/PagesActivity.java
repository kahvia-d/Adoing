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
                //???????????????????????????UI
                cardCounter.setText(TimeUtil.formatTime(time));
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        deleteDatabase("adoing.db");
        //???????????????
        mySqlHelper=MySqlHelper.getInstance(this);
        mySqlHelper.openReadLink();
        mySqlHelper.openWriteLink();


        frontAniSet= (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.front_anim);
        backAniSet= (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.back_anim);

        setContentView(R.layout.activity_pages);

        //?????????????????????
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
            //????????????????????????????????????????????????????????????
            if (CounterService.threadEnd){
                //??????????????????????????????????????????????????????????????????
                View parent= (View) view.getParent();
                Button startButton=parent.findViewById(R.id.startButton);
                TextView pastTime=parent.findViewById(R.id.pastTime);
                TextView pageIndex=parent.findViewById(R.id.pageIndex);

                //???????????????????????????????????????????????????UI???????????????????????????
                cardCounter=pastTime;
                //???????????????????????????
                startButton.setVisibility(View.INVISIBLE);
                pastTime.setVisibility(View.VISIBLE);

                //???????????????????????????????????????????????????
                pastTime.setText(TimeUtil.formatTime(CounterService.counter));

                //start my counter service
                Intent serviceIntent=new Intent(this, CounterService.class);
                serviceIntent.setAction("start");
                serviceIntent.putExtra("pageIndex",pageIndex.getText());
                startForegroundService(serviceIntent);
            }else {
                Toast.makeText(view.getContext(),"??????????????????????????????",Toast.LENGTH_SHORT).show();

            }
        }else {
            Toast.makeText(view.getContext(),"?????????????????????",Toast.LENGTH_SHORT).show();
        }

    }

    public void onCardClickEnd(View view){
        //??????????????????????????????????????????????????????????????????
        View parent= (View) view.getParent();
        Button startButton=parent.findViewById(R.id.startButton);
        TextView pastTime=parent.findViewById(R.id.pastTime);
        TextView pageIndex=parent.findViewById(R.id.pageIndex);

        //???????????????????????????
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

        //?????????????????????
        Animator.AnimatorListener listener=new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //????????????????????????????????????????????????????????????????????????????????????
                cardBack.setVisibility(View.VISIBLE);
                //?????????????????????
                backAniSet.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                cardFront.setVisibility(View.INVISIBLE);
                //??????????????????????????????????????????????????????????????????????????????
                frontAniSet.removeListener(this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        //???????????????????????????
        frontAniSet.setTarget(cardFront);
        backAniSet.setTarget(cardBack);
        //???????????????
        frontAniSet.addListener(listener);

        //??????????????????
        float scale=this.getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(scale*10000);
        cardBack.setCameraDistance(scale*10000);

        //????????????
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

        //???????????????Uri??????
        card.setImage(Uri.parse((String) imageBack.getTag()));
        imageFront.setImageURI(card.getImage());
        //????????????
        card.setTitle(titleBack.getText().toString());
        title.setText(card.getTitle());
        //??????????????????
        card.setContent(contentBack.getText().toString());
        content.setText(card.getContent());
        //???????????????
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

        //????????????????????????????????????????????????????????????????????????
        int index=Integer.parseInt(pageIndex.getText().toString());
        if (index==CounterService.pageIndex){
            Toast.makeText(view.getContext(),"??????????????????????????????",Toast.LENGTH_SHORT).show();
            return;
        }

        CardItem card=new CardItem();
        card.setId(Integer.parseInt(cardDbId.getText().toString()));
        mySqlHelper.deleteCard(card);

        //??????????????????????????????????????????????????????
        if (index<CounterService.pageIndex){
            CounterService.pageIndex-=1;
        }


        //??????????????????
        cardItems=mySqlHelper.readCards();
        viewPager2.setAdapter(new ViewPagerAdaptor(cardItems));
        viewPager2.setCurrentItem(CounterService.pageIndex,false);

    }

    //???????????????
    public void registerMyBroadcastReceiver(){
        IntentFilter intentFilter=new IntentFilter();
        //??????action???????????????????????????
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

            //?????????????????????????????????
            PieData pieData=new PieData(ChartsUtil.getPieDataSet(records));
            //????????????????????????
            pieData.setValueTextSize(12f);
            //???????????????
            dayChart.setData(pieData);
            //??????????????????
            dayChart.animateXY(500,500);
            //?????????????????????
            dayChart.setCenterText("Today(%)");
            //?????????????????????
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