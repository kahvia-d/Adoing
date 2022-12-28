package cn.kahvia.adoing.pojo;

import android.net.Uri;

public class CardItem {
    Integer id=0;
    Uri image;
    String title,content;

    public CardItem(){}

    public CardItem(Uri image, String title, String content) {
        this.image = image;
        this.title = title;
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri imageId) {
        this.image = imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
