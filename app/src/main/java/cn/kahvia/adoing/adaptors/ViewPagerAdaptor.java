package cn.kahvia.adoing.adaptors;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import cn.kahvia.adoing.R;
import cn.kahvia.adoing.PagesActivity;

import cn.kahvia.adoing.pojo.CardItem;
import cn.kahvia.adoing.service.CounterService;
import cn.kahvia.adoing.utils.TimeUtil;

//继承时传入泛型为内部类ViewHolder，这个Holder用来保存通过view findViewById方法获得的组件，这样只需要find一次足矣
public class ViewPagerAdaptor extends RecyclerView.Adapter<ViewPagerAdaptor.ViewHolder> {
    //要展示的列表数据
    List<CardItem> cardItems;

    //构造函数，必需。
    public ViewPagerAdaptor(List<CardItem> cardItems){
        this.cardItems=cardItems;
    }

    //内部类
    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView title;
        TextView content;
        TextView pageIndex;

        ImageView imageBack;
        TextView titleBack;
        TextView contentBack;
        TextView cardDbId;

        Button startButton;
        TextView counter;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.itemImg);
            title=itemView.findViewById(R.id.itemTitle);
            content=itemView.findViewById(R.id.itemContent);
            pageIndex=itemView.findViewById(R.id.pageIndex);

            imageBack=itemView.findViewById(R.id.itemImg_back);
            titleBack=itemView.findViewById(R.id.itemTitle_back);
            contentBack=itemView.findViewById(R.id.itemContent_back);
            cardDbId=itemView.findViewById(R.id.cardDbId);

            startButton=itemView.findViewById(R.id.startButton);
            counter=itemView.findViewById(R.id.pastTime);
        }
    }

    @NonNull
    @Override//创建ViewHolder
    public ViewPagerAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //创建一个view，在父节点的上下文中填充一个View
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager_item,parent,false);
        //创建一个ViewHolder。多个ViewHolder可以实现View的复用，无需反复创建View。
        return new ViewHolder(view);
    }

    @Override//快速赋值，实现view的迅速加载
    public void onBindViewHolder(@NonNull ViewPagerAdaptor.ViewHolder holder, int position) {
        CardItem cardItem=cardItems.get(position);

        //获取card图片的Uri地址
        Uri imagePath = cardItem.getImage();
        //使用Uri设置图片,而不是Resource id
        holder.image.setImageURI(imagePath);
        holder.imageBack.setImageURI(imagePath);
        //设置tag用于为ImageView保存Uri地址，方便卡片正反面数据交换
        holder.imageBack.setTag(imagePath.toString());

        holder.title.setText(cardItem.getTitle());
        holder.titleBack.setText(cardItem.getTitle());
        holder.content.setText(cardItem.getContent());
        holder.contentBack.setText(cardItem.getContent());
        holder.pageIndex.setText(position+"");
        holder.cardDbId.setText(cardItem.getId().toString());

        //应用重启的时候，恢复正在计时的任务卡片的样式，即应用重启后显示计时器。
        int index= CounterService.pageIndex;
        if (index==position){
            PagesActivity.cardCounter= holder.counter;
            holder.counter.setText(TimeUtil.formatTime(CounterService.counter));
            holder.startButton.setVisibility(View.INVISIBLE);
            holder.counter.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return cardItems.size();
    }


//    void initialCards(){
//        int index= CounterService.pageIndex;
//        if (index!=-1){
//            View temp=findViewById(R.id.startButton);
//            View parent=viewPager2.getChildAt(index);
//            Button startButton=parent.findViewById(R.id.startButton);
//            TextView pastTime=parent.findViewById(R.id.pastTime);
//            cardCounter=pastTime;
//            startButton.setVisibility(View.INVISIBLE);
//            pastTime.setVisibility(View.VISIBLE);
//        }
//
//    }
}
