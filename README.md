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

@version 1.0.2 修复多线程访问出现database is closed 的异常
@version 1.0.3 删除JsonInterface
```
### 引用方式  

``` 
dependencies {
    ...  
     compile 'com.aiitec.aiisqlite:aiisqlte:1.0.3'
}
```

```
Copyright 2017 AiiSQLite

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

```