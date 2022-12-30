package cn.kahvia.adoing.utils;

import android.graphics.Color;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import cn.kahvia.adoing.R;
import cn.kahvia.adoing.pojo.Record;

public class ChartsUtil {
    public static PieDataSet getPieDataSet(List<Record> records){
        ArrayList<PieEntry> entries=new ArrayList<>();
        //记录中任务总时长
        int timeAddition=0;
        for (int i = 0; i < records.size(); i++) {
            timeAddition+=records.get(i).getTime();
        }
        for (int i = 0; i < records.size(); i++) {
            PieEntry pieEntry=new PieEntry((float) (records.get(i).getTime()/(timeAddition*1.0)*100),records.get(i).getTitle());
            entries.add(pieEntry);
        }

        PieDataSet pieDataSet=new PieDataSet(entries,"today");
        pieDataSet.setColors(ColorTemplate.PASTEL_COLORS);
        pieDataSet.setValueTextColor(Color.WHITE);

        //返回数据集
        return pieDataSet;
    }
}
