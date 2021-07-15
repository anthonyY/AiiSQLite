package com.aiitec.aiisqlite.entity;

import com.aiitec.openapi.db.annotation.Column;
import com.aiitec.openapi.db.annotation.NotNull;
import com.aiitec.openapi.db.annotation.Table;
import com.aiitec.openapi.db.annotation.Unique;

import java.util.List;

/**
 * @author Anthony
 * @version 1.0
 * createTime 2017/10/11.
 *
 * Table 注解可以不写，默认使用类名作为表名
 */
@Table(name = "Goods")
public class Goods {
    /**唯一注解，一般用于id上，如果不用，如果不加这个注解，相同数据的类保存多次，会生成多条数据*/
    @Unique
    private int id;

    /**不为空*/
    @NotNull
    private String name;

    private List<String> images;

    private double price;

    /**数据库中使用字段名称是original_price， 代码中使用originalPrice*/
    @Column("original_price")
    private double originalPrice ;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }
}
