package cn.kahvia.adoing.pojo;

public class CardItem {
    Integer imageId;
    String title,content;

    public CardItem(Integer imageId, String title, String content) {
        this.imageId = imageId;
        this.title = title;
        this.content = content;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
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
