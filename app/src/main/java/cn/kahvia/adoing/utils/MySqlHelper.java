package cn.kahvia.adoing.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cn.kahvia.adoing.pojo.CardItem;

public class MySqlHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="adoing.db";
    private static final String TABLE_NAME_CARDS="cards";
    private static final String TABLE_NAME_RECORDS="records";
    private static final int DATABASE_VERSION=1;
    private static MySqlHelper mySqlHelper=null;
    private SQLiteDatabase dbRead=null;
    private SQLiteDatabase dbWrite=null;

    public MySqlHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //获取全局单例Helper
    public static MySqlHelper getInstance(Context context){
        //无则创建
        if (mySqlHelper==null)
            mySqlHelper=new MySqlHelper(context);
        return mySqlHelper;
    }

    //获得可执行读操作的数据库
    public SQLiteDatabase openReadLink(){
        if (dbRead==null)
            dbRead=mySqlHelper.getReadableDatabase();
        return dbRead;
    }

    //获得可执行写操作的数据库
    public SQLiteDatabase openWriteLink(){
        if(dbWrite==null)
            dbWrite=mySqlHelper.getWritableDatabase();
        return dbWrite;
    }

    //关闭上述两种数据库
    public void closeAllDBLinks(){
        if (dbRead.isOpen()){
            dbRead.close();
            dbRead=null;
        }
        if (dbWrite.isOpen()){
            dbWrite.close();
            dbWrite=null;
        }

    }

    //添加新卡片
    public long addNewCard(CardItem card){
        ContentValues contentValues=new ContentValues();
        contentValues.put("image",card.getImage().toString());
        contentValues.put("title",card.getTitle());
        contentValues.put("content",card.getContent());
        return dbWrite.insert(TABLE_NAME_CARDS,null,contentValues);
    }

    //获取本地保存的所有卡片
    public List<CardItem> readCards(){
        List<CardItem> cards=new ArrayList<>();
        Cursor cursor=dbRead.query(TABLE_NAME_CARDS,null,null,null,null,null,null);
        //Move the cursor to the next row.
        //This method will return false if the cursor is already past the last entry in the result set.
        while (cursor.moveToNext()){
            CardItem card=new CardItem();
            card.setId(cursor.getInt(0));
            card.setImage(Uri.parse(cursor.getString(1)));
            card.setTitle(cursor.getString(2));
            card.setContent(cursor.getString(3));
            cards.add(card);
        }
        return cards;
    }

    public void updateCard(CardItem card){
        ContentValues contentValues=new ContentValues();
        contentValues.put("image",card.getImage().toString());
        contentValues.put("title",card.getTitle());
        contentValues.put("content",card.getContent());
        dbWrite.update(TABLE_NAME_CARDS,contentValues,"id=?",new String[]{String.valueOf(card.getId())});
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlStr="create table  if not exists "+TABLE_NAME_CARDS+"(" +
                "  id INTEGER primary key AUTOINCREMENT not null ," +
                "  image varchar(255) not null ," +
                "  title varchar(30) not null ," +
                "  content varchar(255) not null" +
                ");";
        String sqlStr2="create table  if not exists "+TABLE_NAME_RECORDS+"(" +
                "  id INTEGER primary key AUTOINCREMENT not null ," +
                "  title varchar(30) not null ," +
                "  time long not null" +
                ");";
        //创建两张表，一张用于存储卡片的信息，一张用于存储任务耗时
        db.execSQL(sqlStr);
        db.execSQL(sqlStr2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
