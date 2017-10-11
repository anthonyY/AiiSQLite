package com.aiitec.aiisqlite;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aiitec.openapi.db.AIIDBManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库存储例子
 * @author Anthony
 * createTime 2017/10/11 12:32
 *
 * <br>本框架会把对象的每个一级字段当作数据库的字段保存到数据库中，就算新增了字段也没有关系，数据库自己会增加新的字段，<br>增删改查都非常方便，并且多线程执行也没有问题，
 * <br>通过一级字段查找可以得到二级的内容，但是二级的字段是不可以当作查询条件。
 * <br>比如  User 对象 里有userId 和 Address, Address里有regionId， <br>那么只能通过userId查询得到User对象再取Address对象，而不能通过regionId得到User对象
 * <br>但是偶尔会出现一些问题，待检查修复，目前总的情况是比较稳定的
 * 使用方法
 *  <br>AIIDBManager dbManager = new AIIDBManager(this);
 *  <br>dbManager.save(对象);
 *  <br>dbManager.save(List集合);
 *  <br>dbManager.findAll(对象.class);
 *  <br>dbManager.findAll(对象.class, "name=?",new String[]{"张三"});
 *  <br>dbManager.findFirst(对象.class, "name=?",new String[]{"张三"});
 *  <br>dbManager.delete(对象.class, "name=?",new String[]{"张三"});
 *  <br>dbManager.deleteAll(对象.class);
 *  <br>dbManager.deleteById(id);
 */
public class MainActivity extends AppCompatActivity {

    private AIIDBManager aiiDbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aiiDbManager = new AIIDBManager(this);//默认数据库
//        long userId = 2;
//        aiiDbManager = new AIIDBManager(this, userId);//根据用户id建的数据库，不同用户的数据在不同数据库中
//        String dbName = "CompanyA.db";
//        aiiDbManager = new AIIDBManager(this, dbName);//根据数据库名建的数据库
    }

    /**
     * insert or update data
     * if a field is had Annatation Unique and the value is same, than it will be update, or will insert
     */
    private void insertOrUpdateData(){
        Goods goods = new Goods();
        goods.setId(1);
        goods.setName("小米手机6");
        goods.setPrice(33.24);
        goods.setOriginalPrice(42.00);
        List<String> images = new ArrayList<>();
        images.add("http://www.mi.com/img/1.jpg");
        images.add("http://www.mi.com/img/2.jpg");
        images.add("http://www.mi.com/img/3.jpg");
        goods.setImages(images);
        aiiDbManager.save(goods);
    }

    /**
     * delete by id
     */
    private void deleteById(long id){
        aiiDbManager.deleteById(Goods.class, id);
    }

    /**
     * find all datas
     */
    private void findAll() throws IllegalAccessException, InstantiationException {
        List<Goods> all = aiiDbManager.findAll(Goods.class);
    }
    /**
     * find all datas by condition
     */
    private void findAllByCondition() throws IllegalAccessException, InstantiationException {
        List<Goods> all = aiiDbManager.findAll(Goods.class, "name=?", new String[]{"小米手机6"});
    }

    /**
     * find one data
     */
    private void findOneByCondition() throws IllegalAccessException, InstantiationException {
        Goods goods = aiiDbManager.findFirst(Goods.class, "name=?", new String[]{"小米手机6"});
    }


}
