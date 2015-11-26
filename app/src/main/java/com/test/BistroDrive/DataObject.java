package com.test.BistroDrive;

import android.graphics.Bitmap;

public class DataObject {
    private String name;
    private String price;
    private String priceWithIng;
    private String type;
    private Bitmap img;

    public DataObject (String text1, Bitmap bt, String text3, String text4, String text5){
        name = text1;
        img = bt;
        price = text3;
        priceWithIng = text4;
        type = text5;
    }

    public String getName() {
        return name;
    }

    public void setName(String mText1) {
        this.name = mText1;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap bt) {
        this.img = bt;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String mText1) {
        this.price = mText1;
    }

    public String getPriceWithIng() {
        return priceWithIng;
    }

    public void setPriceWithIng(String mText1) {
        this.priceWithIng = mText1;
    }

    public String getType() {
        return type;
    }

    public void setType(String mText1) {
        this.type = mText1;
    }
}