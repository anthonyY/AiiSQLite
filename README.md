# AiiSQlite 库  
### 使用说明  
本库是作为Android 数据库存储的一个库，使用按对象存储读取的方式，节省写数据库的代码，并且增加字段能自动识别，是一个很好用的数据库框架。  
API如下：
```
AIIDBManager dbManager = new AIIDBManager(this);
//        long userId = 2;
//        aiiDbManager = new AIIDBManager(this, userId);//根据用户id建的数据库，不同用户的数据在不同数据库中
//        String dbName = "CompanyA.db";
//        aiiDbManager = new AIIDBManager(this, dbName);//根据数据库名建的数据库

void dbManager.save(对象T);
void dbManager.save(List集合<T>);
List<T> dbManager.findAll(对象T.class);
List<T> dbManager.findAll(对象T.class, "name=?",new String[]{"张三"});
T dbManager.findFirst(对象T.class, "name=?",new String[]{"张三"});
void dbManager.delete(对象T.class, "name=?",new String[]{"张三"});
void dbManager.deleteAll(对象T.class);
void dbManager.deleteById(id);  

```
### 引用方式  

``` 
dependencies {
    ...  
     compile 'com.aiitec.aiisqlite:aiisqlte:1.0.0'
}
```