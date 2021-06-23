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
@version 1.0.7
    * 增加自定义查询方法 public void rawQuery(String sql, String[] whereArgs, Function<Cursor,Void> fun)
    * 增加查询总数量方法 public Long count(Class<?> clazz, String whereClause, String[] whereArgs)
    * 增加查询表是否存在的方法 public boolean checkTableIsExsist(Class<?> clazz)
    * -1 变为可用值，之前的版本设计时字段考虑用 int,没有空的情况，就用-1代替空，现在更正，-1就是-1，不能代表空，需要空请定义 Integer

jitpack 版本
@version 1.0.0  同 jcenter 1.0.7
@version 1.0.1  增加版本号
@version 1.0.3  异常 try catch 改到事务开始结束中间，否则 try 到 异常却没有结束 事务，会导致不可思议的问题

```
### 引用方式  

```

allprojects {
    repositories {
        ...
        maven{ url "https://jitpack.io" }
    }
}

dependencies {
    ...  
     //compile 'com.aiitec.aiisqlite:aiisqlte:1.0.3'
     // jcenter 转到 jitpack.io

     implementation 'com.github.anthonyY:AiiSQLite:1.0.3'
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