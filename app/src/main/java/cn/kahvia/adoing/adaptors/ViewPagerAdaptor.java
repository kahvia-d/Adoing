package cn.kahvia.adoing.adaptors;

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

        Button startButton;
        TextView counter;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.itemImg);
            title=itemView.findViewById(R.id.itemTitle);
            content=itemView.findViewById(R.id.itemContent);
            pageIndex=itemView.findViewById(R.id.pageIndex);
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
        holder.image.setImageResource(cardItem.getImageId());
        holder.title.setText(cardItem.getTitle());
        holder.content.setText(cardItem.getContent());
        holder.pageIndex.setText(position+"");

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
