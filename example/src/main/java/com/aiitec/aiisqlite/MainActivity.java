package com.aiitec.aiisqlite;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aiitec.aiisqlite.entity.Goods;
import com.aiitec.aiisqlite.entity.UserInfo;
import com.aiitec.aiisqlite.entity.Video;
import com.aiitec.openapi.db.AIIDBManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * 数据库存储例子
 *
 * @author Anthony
 *         createTime 2017/10/11 12:32
 *         <p>
 *         <br>本框架会把对象的每个一级字段当作数据库的字段保存到数据库中，就算新增了字段也没有关系，数据库自己会增加新的字段，<br>增删改查都非常方便，并且多线程执行也没有问题，
 *         <br>通过一级字段查找可以得到二级的内容，但是二级的字段是不可以当作查询条件。
 *         <br>比如  User 对象 里有userId 和 Address, Address里有regionId， <br>那么只能通过userId查询得到User对象再取Address对象，而不能通过regionId得到User对象
 *         <br>但是偶尔会出现一些问题，待检查修复，目前总的情况是比较稳定的
 *         使用方法
 *         <br>AIIDBManager dbManager = new AIIDBManager(this);
 *         <br>dbManager.save(对象);
 *         <br>dbManager.update(对象);
 *         <br>dbManager.save(List集合);
 *         <br>dbManager.update(List集合);
 *         <br>dbManager.findAll(对象.class);
 *         <br>dbManager.findAll(对象.class, "name=?",new String[]{"张三"});
 *         <br>dbManager.findFirst(对象.class, "name=?",new String[]{"张三"});
 *         <br>dbManager.delete(对象.class, "name=?",new String[]{"张三"});
 *         <br>dbManager.deleteAll(对象.class);
 *         <br>dbManager.deleteById(id);
 *
 *  @version 1.0.2 修复多线程访问出现database is closed 的异常
 */
public class MainActivity extends AppCompatActivity {

//    private AIIDBManager aiiDbManager;
    private Button btnSave, btnRead, btnCustom, btnCount;
    private EditText etTitle;
    private MyAdapter adapter;
    private ListView listView;
    ExecutorService cachedThreadPool;
    private AIIDBManager aiiDBManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String currontDbName = this.getPackageName()+".db";
//        aiiDbManager = new AIIDBManager(this,  currontDbName,4);//默认数据库
//        long userId = 2;
//        aiiDbManager = new AIIDBManager(this, userId);//根据用户id建的数据库，不同用户的数据在不同数据库中
//        String dbName = "CompanyA.db";
//        aiiDbManager = new AIIDBManager(this, dbName);//根据数据库名建的数据库
//        String dbName = "CompanyA.db";
//        int dbVersion = 3; // 数据库版本，本来这个有自动数据库字段升级的，所以应该不存在版本问题，但是我想覆盖别人的框架写的数据库时，
//        别人的版本号已经很大了，这个框架的版本就是1，无法覆盖，所以要增加这个版本号
//        aiiDbManager = new AIIDBManager(this, dbName, dbVersion);//根据数据库名建的数据库

        listView = findViewById(R.id.listView);
        btnSave = findViewById(R.id.btnSave);
        btnRead = findViewById(R.id.btnRead);
        etTitle = findViewById(R.id.etTitle);
        btnCount = findViewById(R.id.btnCount);
        btnCustom = findViewById(R.id.btnCustom);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDatas(etTitle.getText().toString());
            }
        });
        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readDatas();
            }
        });
        btnCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    aiiDBManager.rawQuery("select * from Video where audio_id > ?", new String[]{"80"},  new Function<Cursor, Void>() {
                        @Override
                        public Void apply(Cursor cursor) {
                            while (cursor.moveToNext()){
                                String title = cursor.getString(cursor.getColumnIndex("title"));
                                Log.i("AiiDbManager","title == "+ title);
                            }
                            return null;
                        }
                    });
                }

            }
        });
        btnCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long count = aiiDBManager.count(Video.class, "audio_id > ?", new String[]{"90"});
                Log.i("AiiDbManager", "count = "+count);
                Toast.makeText(MainActivity.this, "Video 表的自定义条件的数量有"+count, Toast.LENGTH_SHORT).show();
            }
        });
        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);
//        aiiDBManager = new AIIDBManager(this);
        cachedThreadPool = Executors.newCachedThreadPool();
        aiiDBManager = new AIIDBManager(this,  currontDbName,4);//默认数据库
        boolean isExsist = aiiDBManager.checkTableIsExsist(Video.class);
        Log.i("AiiDbManager", "Video table is exsist :"+isExsist);


    }

    /**
     * insert or update data
     * if a field is had Annatation Unique and the value is same, than it will be update, or will insert
     */
    private void insertOrUpdateData() {
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
        aiiDBManager.save(goods);
    }

    /**
     * delete by id
     */
    private void deleteById(long id) {
        aiiDBManager.deleteById(Goods.class, id);
    }

    /**
     * find all datas
     */
    private void findAll()  {
        List<Goods> all = aiiDBManager.findAll(Goods.class);
    }

    /**
     * find all datas by condition
     */
    private void findAllByCondition() {
        List<Goods> all = aiiDBManager.findAll(Goods.class, "name=?", new String[]{"小米手机6"});
    }

    /**
     * find one data
     */
    private void findOneByCondition()  {
        Goods goods = aiiDBManager.findFirst(Goods.class, "name=?", new String[]{"小米手机6"});
    }


    /**
     * 多线程插入
     * @param title
     */
    void saveDatas(final String title) {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
//                insertMqData();
                Runtime r = Runtime.getRuntime();
                long startRAM = r.freeMemory();
                long startTime = System.currentTimeMillis();
                List<UserInfo> userInfos = new ArrayList<>();
                int total = 10000;
                for (int i = 0; i < total; i++) {
//                    Video video = new Video();
//                    video.courseId = i / 10 + 1;
//                    video.play_path = "http://www.text.com/path$i.mp4";
//                    video.play_length = "0"+i%10+":"+i%8+""+i%3;
//                    video.title = title+i;
//                    video.audioId = i;
//                    video.audio_synopsis = "啦啦啦"+i;
//                    video.audio_type = i % 3;
//                    video.imagePath = "http://www.text.com/image"+i+".jpg";
//                    video.time = System.currentTimeMillis();
//                    video.timestamp = DbUtils.date2TimeStamp(new Date());
//                    aiiDBManager.save(video);

                    UserInfo userInfo = new UserInfo();
                    userInfo.setDepartment("行政部");
                    userInfo.setFaceplus("a8cf4ceaad88e22849070f27f36e3dbcc82694290590e4a301b42352e41e71f49a6cf8cd59d3daa01dcd4194ed0c5049276243966e259feb863e56885e22bc9817b9450f15bf4827c1fe2d3d5d2085fbf02a4fd68c80c8702acf96b709e9741b758d193237ae4ab62e46b4652a008f91d7d771aab01943c799cc7d226258871ec652c1fbd856d51e3dbf033da4c0dbe9546ee9762df4c3345e65d8357f8c932fe20231f2235c453c3c5c336a0b85a5f9e2f52abde3c8907acd3185c91a3f8d59f53cb983d14940f2dd1afcdf4a800c0830c64651fb6a4ba1896503fabbfe1cb2e8b41f6a95c870d65b21e94c0583d93abcc8ca59b62342d19d70e9c8db291de6be64fddb089ba8edd7b6ac81077d9da30d07a4f5fdcf2f592cf3676b563a6f0792b0f8c0b5b98e9a97e53271004c4934630b32c5b611298d882be38ff97216a9e6ae3ea0cca838877c41f8eeb25da8c48832df70ccf305078972fd33e76b74d33c0b520877a5c0b3b44fa64a4f85359c962698bf1a289f285c48a9f71bdb11210237b25b21384d064536313ce22c3dced5ebed4e7a2357d9c962fae7391a4f81751d82ce123e0d416257de9d28de820c7af52395f1cda7ad1ebd55c14ff4b486ed90348319a47d2db577f6486fb6d1060d7058ea5a6b9f65f7cab0093c910017ba25c81dff79ea4325a13fbf23d94dddfe1ce64e6ed04eb78b342c7d54b34dfe142d49cb164955fe3856b86da963fcc3572b3b7bba02aac0cda6f9450e97c2708c7d27fc7bbb84d9a0a7a49189dcbd90e562ef8a3ec987512736878940ae1d881fd3c806a6e09cd5f4827a20aae90e7ad14cc6a60b93d7bfcde7ccb7000bd93b072935298b9a442b63753d2ebc30746578b8b08f0a9fffa580a942eb58abc193c5a45b8e364e06e44400f01b08c2645b551cfb94da03d9769cc82302c29869f405a7b4154aa1a4a4716daa656fd3f8744e66f3ca79aa231198794b757b5cb12d72815b0410a23f7c6301650d7c3b9e5b9218c2b8548ed2b191076aeae307817f8b0b95737e161e365eb8a2189713142a8c8254fef54e367accccc059fac86992ae711101f0f498ce7f4087d4a448cf60edfbaaae64fe591afe7d6f8e7acfa8570b5851e2499b853fd53b3ec8700b8e7e84df3b5676c4764aa35655afa2780e11e190503a43b70d416b3cc8b8da060269579a92b985aa428b3fb2089167e6b657b999522bf070e8aa479708dcbc280d223f79477fd1afc3d7cfb7dda0647c05d727e9c7f341f5c4f414938d9dd46a675859f1376cea889e96a2c2f3b6f815a3a49f69c3fcdb065347de7c84f5c8397c27bcdcde1e2d5645f977842ec5800b73fb80ff3f51a22176430a3be424bbfcbffdb481a0553cd05cb5fbbd6a92173b8cc48501b86c8e90fbf2147e522046d98249ac2167bfc9624db2681cb21c6c5f846f96e5f2685046c37271bac468bd4ebf0133b376cca6dfad84c4a5067808ff9970c932d0be97d306ac91dbb8b7088ab5c7e2732c911ac2984c718c131005f53c96cd7bfdabc4ddae8cb10483b3892cdb72d978872e0775a82ffed2b40eca8bc2997d082000009604c9e6ac33bc97d452559fc551da3e1fa8f9d48b63490d1ab7f2edbbf8df0ffe13fa5fc191747da9dbf788c6b94f90d3a0c87cd5a82186b492d8479956e5b613090f6ba4082ebd88e08280a66d40660420abb064a04406e48f6de17ab07cc3b4cb430e55a53568a83eed9cd441e97658ef7d804bfbf1da3b2171f2680aa308d231ff3d289e1aa2f94f75e16704ebcc0220417b86502d21d7ac85e708e0324a7122a247ff0165e3296ad5dc9e537cfe7d262cce4fed3891941e90aefab82aa1fbfcc094838efa2838a513b0c06efd47c757388ca2f7b264e5a0adfc182c0e157c6beb9780bddbe6af69b98b9c23bed0bdd564ee0fd7f55ef3b1b07151733f09b51a35eb5b90aad8cae3847860c3a91bff773a0ed8493162ae43daa2554b4b33ca5d4661ca3072c147f34476e6b847c735095f6569b1494db449203bf9a4b4109df0090f5e9f974345c953fe8b929a705f5c25652c02e3a6a59f7e6c88cfbd02c49bf2117f075872fb902bb3081f2adb93cd6cd2ba315931de6ddb47051dac94fe4aaf720eefd399cfe729a8aaef933e7791c94d9bc0880c762b660ca6aba93e8fd37975196f88f8b6925bacac45e0690d3975a47f5084858886ef981bb11eb8d4dc5ec95e25cccac22a0b57199e6228dcd0151c784ac2961b2726640d03d6b296ed695737b7bdcd7095b514657c690fbf4b64e0ed6218ac08c86a00eda50ab7febf8e74586f6352a0496b8729dad878116c94988ab7f2fdbac4f6441ca47bb4a6fa4db190b27d188f6301b6187ad337a7a9407038ca226ffefd3b50254ba05eec39ab976e58046d1eb4361a4ae5a90bf7488f771fb54cf5a1bd8cfa4831e27e513a6272872b18f8b98549f3398bd3ff1bebfe51cf89fe7a10ca748167330242d63e104cd1467031f9ead5cf9ba0534efd26c6c640cea5fdb67e1823067331a57eaf935f4f796016793271c6cb4a66c10ad8428810d4d25f4b3237bcf23220f7528f5ff94e681fda67fd1158433b3092272426673a74d368a088e7d8404d5bbd2e85e9906e08ceb6e8a124d453f418b1feab33ff67e520e69318c583983d77ace072385f0f85275473d7f88f35d17d5ef69b15681a1cd522e0201223ab88f9429c596e1d6d9b197de1ef8ffb8d6e56aabd13679ee64b29c0e049672de0da109ff884eea1dcbe42308b5daf9aa8eb45f03ff9776c8f8f7495383c9d861a2025cc1bf5c6707c282f137404b22735c2d20fcfb66857ee877d66dfdb8be40c10e0a4d3854f306a77a9feda2b4839b19b0f3a103e2d3298e90d52b3928f");
                    userInfo.setFeatureImg("http://file.microsmartinfo.com/cardcloud-uploadimg/2021-04-02/cardcloud1377868669820215296.jpg");
                    userInfo.setName(title+""+i);
                    userInfo.setOrganizationId(67);
                    userInfo.setPassTime("00:01-23:59");
                    userInfo.setSerialNumber("20039");
                    userInfo.setTypes("1,2,3,4");
                    userInfo.setUserId(i);
                    userInfo.setUsername(title+""+i);
                    userInfo.setAge(10+i);
                    userInfos.add(userInfo);
                    aiiDBManager.save(userInfo);
                    if( i != 0 && i % 1000 == 0){
//                        aiiDBManager.save(userInfos);

                        Log.i("TAG_AII", "已经插入"+(i+1)+"条");
                        toast("已经插入"+(i+1)+"条");
//                        userInfos.clear();
                    }
                }
                long endRAM = r.freeMemory();


                String result = "测试RAM结束，测试占用内存空间约为 : " + (startRAM - endRAM);
                Log.i("TAG_AII", result);
                if(userInfos.size() > 0){
                    aiiDBManager.save(userInfos);
                    Log.i("TAG_AII", "已经插入"+total+"条");
                    toast("已经插入"+total+"条");
                }

                userInfos.clear();


                long endTime = System.currentTimeMillis();
                Log.i("TAG_AII", "插入"+total+"条数据耗时："+(endTime-startTime));
                toast("插入完成，耗时"+(endTime-startTime));
            }


        });

        // 插入 50000 条数据 单条插入 超级耗时，耗时超过18分钟
        // 1000条 插入一次 ，耗时 42.8秒
        // 10000 条插入一次，耗时36.2秒
        // 50000 条一次性插入，耗时46秒

//        cachedThreadPool.execute(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 100; i < 200; i++) {
//                    Video video = new Video();
//                    video.courseId = i / 10 + 1;
//                    video.play_path = "http://www.text.com/path$i.mp4";
//                    video.play_length = "0"+i%10+":"+i%8+""+i%3;
//                    video.title = title+i;
//                    video.audioId = i;
//                    video.audio_synopsis = "啦啦啦"+i;
//                    video.audio_type = i % 3;
//                    video.imagePath = "http://www.text.com/image"+i+".jpg";
//                    video.time = System.currentTimeMillis();
//                    video.timestamp = DbUtils.date2TimeStamp(new Date());
//                    aiiDBManager.save(video);
//                }
//            }
//        });
//        cachedThreadPool.execute(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 200; i < 300; i++) {
//                    Video video = new Video();
//                    video.courseId = i / 10 + 1;
//                    video.play_path = "http://www.text.com/path$i.mp4";
//                    video.play_length = "0"+i%10+":"+i%8+""+i%3;
//                    video.title = title+i;
//                    video.audioId = i;
//                    video.audio_synopsis = "啦啦啦"+i;
//                    video.audio_type = i % 3;
//                    video.imagePath = "http://www.text.com/image"+i+".jpg";
//                    video.time = System.currentTimeMillis();
//                    video.timestamp = DbUtils.date2TimeStamp(new Date());
//                    aiiDBManager.save(video);
//                }
//            }
//        });
    }

    /**
     * 读取数据库
     */
    void readDatas() {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                long startTime = System.currentTimeMillis();
                    final List<UserInfo> videos = aiiDBManager.findAll(UserInfo.class, "name=?", new String[]{etTitle.getText().toString()+"1000"}, null);
                long endTime = System.currentTimeMillis();
                Log.i("TAG_AII", "查询数据耗时"+(endTime-startTime));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.clear();
                            if (videos != null) {
                                adapter.addAll(videos);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });


            }
        });
    }

    public void toast(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    class MyAdapter extends ArrayAdapter<UserInfo> {
        public MyAdapter(@NonNull Context context) {
            super(context, 0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView view = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null, false);
            UserInfo item = getItem(position);
//            view.setText(item.getAudioId()+"  "+item.getTitle()+"  "+item.timestamp);
            view.setText(item.getName()+"  "+item.getUsername()+"  "+item.getOrganizationId());
            return view;
        }
    }


}
