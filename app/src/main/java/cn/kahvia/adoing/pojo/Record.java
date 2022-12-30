package cn.kahvia.adoing.pojo;

public class Record {
    int id;
    String title;
    int year,month,day,time;

    public Record(){}

    public Record(int id, String title, int year, int month, int day, int time) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.month = month;
        this.day = day;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void addTime(int time){
        this.time=time;
    }
}
