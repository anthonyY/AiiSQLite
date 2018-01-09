package com.aiitec.openapi.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.aiitec.openapi.db.annotation.Column;
import com.aiitec.openapi.db.annotation.Format;
import com.aiitec.openapi.db.utils.AiiJson;
import com.aiitec.openapi.db.utils.CombinationUtil;
import com.aiitec.openapi.db.utils.DbUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 数据库框架管理类
 *
 * @author Anthony
 *         <br>createTime 2016-1-29
 *         <br>本框架会把对象的每个一级字段当作数据库的字段保存到数据库中，就算新增了字段也没有关系，数据库自己会增加新的字段，<br>增删改查都非常方便，并且多线程执行也没有问题，
 *         <br>通过一级字段查找可以得到二级的内容，但是二级的字段是不可以当作查询条件。
 *         <br>比如  User 对象 里有userId 和 Address, Address里有regionId， <br>那么只能通过userId查询得到User对象再取Address对象，而不能通过regionId得到User对象
 *         <br>但是偶尔会出现一些问题，待检查修复，目前总的情况是比较稳定的
 *         使用方法
 *         <br>AIIDBManager dbManager = new AIIDBManager(this);
 *         <br>dbManager.save(对象);
 *         <br>dbManager.save(List集合);
 *         <br>dbManager.findAll(对象.class);
 *         <br>dbManager.findAll(对象.class, "name=?",new String[]{"张三"});
 *         <br>dbManager.findFirst(对象.class, "name=?",new String[]{"张三"});
 *         <br>dbManager.delete(对象.class, "name=?",new String[]{"张三"});
 *         <br>dbManager.deleteAll(对象.class);
 *         <br>dbManager.deleteById(id);
 */
public class AIIDBManager {

    private final String TAG = "TAG_AII_SQLITE";
    protected static JsonInterface jsonInterface;

    public static void setJsonInterface(JsonInterface jsonInterface) {
        AIIDBManager.jsonInterface = jsonInterface;
    }

    public static JsonInterface getJsonInterface() {
        return jsonInterface;
    }

    private AIIDbOpenHelper dbHelper;

    public AIIDBManager(Context context) {
        dbHelper = AIIDbOpenHelper.getInstance(context);
        setJsonInterface(new AiiJson());
    }

    public AIIDBManager(Context context, long userId) {
        dbHelper = AIIDbOpenHelper.getInstance(context, userId);
        setJsonInterface(new AiiJson());
    }

    public AIIDBManager(Context context, String dbName) {
        dbHelper = AIIDbOpenHelper.getInstance(context, dbName);
        setJsonInterface(new AiiJson());
    }


    synchronized public <T> void save(List<T> datas) {
        T t = null;
        if (datas != null && datas.size() > 0) {
            t = datas.get(0);
        } else {
            return;
        }
        SQLiteDatabase db = dbHelper.openDatabase();
        if (!db.isOpen()) {
            return;
        }

        db.beginTransaction();

        dbHelper.createOrUpdateTable(t.getClass());
        for (int j = 0; j < datas.size(); j++) {
            dbHelper.save(db, datas.get(j));
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        //每次执行完成必须关闭数据库
        closeCursor();
    }

    synchronized public <T> void save(T t) {

        SQLiteDatabase db = dbHelper.openDatabase();
        dbHelper.createOrUpdateTable(t.getClass());
        if (!db.isOpen()) {
            return;
        }

        db.beginTransaction();

        dbHelper.save(db, t);

        db.setTransactionSuccessful();
        db.endTransaction();
        //每次执行完成必须关闭数据库
       
        closeCursor();
    }

    synchronized public <T> void update(T t) {

        SQLiteDatabase db = dbHelper.openDatabase();
        dbHelper.createOrUpdateTable(t.getClass());
        if (!db.isOpen()) {
            return;
        }

        db.beginTransaction();

        dbHelper.update(db, t);

        db.setTransactionSuccessful();
        db.endTransaction();
        //每次执行完成必须关闭数据库
        closeCursor();
    }


    /**
     * 获取好友list
     *
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public <T> List<T> findAll(Class<T> clazz) throws InstantiationException, IllegalAccessException {
        return findAll(clazz, null, null);
    }

    public <T> List<T> findAll(Class<T> clazz, String selection, String[] selectionArgs) throws InstantiationException, IllegalAccessException {
        return findAll(clazz, null, selection, selectionArgs, null, null, null);
    }

    public <T> List<T> findAll(Class<T> clazz, String selection, String[] selectionArgs, String orderBy) throws InstantiationException, IllegalAccessException {
        return findAll(clazz, null, selection, selectionArgs, null, null, orderBy);
    }

    public <T> List<T> findAll(Class<T> clazz, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) throws InstantiationException, IllegalAccessException {
        SQLiteDatabase db = dbHelper.openDatabase();
        List<T> list = new ArrayList<T>();
        if (db.isOpen()) {
            boolean isExit = DbUtils.checkTableState(db, clazz);
            if (isExit) {
                DbUtils.updateTable(db, clazz);
                Cursor cursor = db.query(DbUtils.getTableName(clazz), columns, selection, selectionArgs, groupBy, having, orderBy);

                while (cursor.moveToNext()) {
                    T t = clazz.newInstance();
                    List<Field> allFields = CombinationUtil.getAllFields(clazz);
                    for (Field field : allFields) {
                        setValueToObject(field, cursor, t);
                    }
                    list.add(t);
                }
                cursor.close();
            }
        }
        //每次执行完成必须关闭数据库
        closeCursor();
        return list;
    }

    public <T> T findFirst(Class<T> clazz) throws InstantiationException, IllegalAccessException {
        return findFirst(clazz, null, null, null);
    }

    public <T> T findFirst(Class<T> clazz, String selection, String[] selectionArgs, String orderBy) throws InstantiationException, IllegalAccessException {
        return findFirst(clazz, null, selection, selectionArgs, null, null, orderBy);
    }

    public <T> T findFirst(Class<T> clazz, String selection, String[] selectionArgs) throws InstantiationException, IllegalAccessException {
        return findFirst(clazz, null, selection, selectionArgs, null, null, null);
    }

    public <T> T findObjectFromId(Class<T> clazz, long id) throws InstantiationException, IllegalAccessException {
        return findFirst(clazz, null, "id=?", new String[]{id + ""}, null, null, null);
    }

    public <T> T findFirst(Class<T> clazz, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) throws InstantiationException, IllegalAccessException {
        SQLiteDatabase db = dbHelper.openDatabase();
        T t = null;
        if (db.isOpen()) {
            boolean isExit = DbUtils.checkTableState(db, clazz);
            if (isExit) {
                DbUtils.updateTable(db, clazz);
                Cursor cursor = db.query(DbUtils.getTableName(clazz), columns, selection, selectionArgs, groupBy, having, orderBy);
                if (cursor.moveToFirst()) {
                    t = clazz.newInstance();

                    List<Field> allFields = CombinationUtil.getAllFields(clazz);
                    for (Field field : allFields) {
                        setValueToObject(field, cursor, t);
                    }
                }
                cursor.close();
            }
        }
        //每次执行完成必须关闭数据库
        closeCursor();
        return t;
    }

    synchronized private <T> void setValueToObject(Field field, Cursor cursor, T t) {

        field.setAccessible(true);
        String fieldName = field.getName();
        Column column = field.getAnnotation(Column.class);

        if (column != null) {
            if(!TextUtils.isEmpty(column.value())){
                fieldName = column.value();
            }
            else if(!TextUtils.isEmpty(column.column())){
                fieldName = column.column();
            }
        }
        try {
            if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                int intValue = cursor.getInt(cursor.getColumnIndex(fieldName));
                field.set(t, intValue);
            } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                long longValue = cursor.getLong(cursor.getColumnIndex(fieldName));
                field.set(t, longValue);
            } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                double doubleValue = cursor.getDouble(cursor.getColumnIndex(fieldName));
                field.set(t, doubleValue);
            } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                float floatValue = cursor.getFloat(cursor.getColumnIndex(fieldName));
                field.set(t, floatValue);
            } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                int intValue = cursor.getInt(cursor.getColumnIndex(fieldName));
                if (intValue == 1) {
                    field.set(t, true);
                } else {
                    field.set(t, false);
                }
            } else if (field.getType().equals(String.class) || field.getType().equals(char.class)) {
                String stringValue = cursor.getString(cursor.getColumnIndex(fieldName));
                field.set(t, stringValue);
            } else if (field.getType().equals(List.class) || field.getType().equals(ArrayList.class)) {
                String stringValue = cursor.getString(cursor.getColumnIndex(fieldName));
                if (!TextUtils.isEmpty(stringValue) && jsonInterface != null) {

                    ParameterizedType type = (ParameterizedType) field.getGenericType();
                    if (type.getActualTypeArguments() != null && type.getActualTypeArguments().length > 0) {
                        Class<?> childClass = (Class<?>) type.getActualTypeArguments()[0];
                        List<?> list = jsonInterface.parseArray(stringValue, childClass);
                        field.set(t, list);
                    }
                }
            } else if (field.getType().equals(Date.class)) {
                String stringValue = cursor.getString(cursor.getColumnIndex(fieldName));
                Format format = field.getAnnotation(Format.class);
                if(format != null && !TextUtils.isEmpty(format.value())){
                    Date date = DbUtils.timeStamp2Date(stringValue, format.value());
                    field.set(t, date);
                } else {
                    Date date = DbUtils.timeStamp2Date(stringValue);
                    field.set(t, date);
                }

            } else {
                try {
                    String stringValue = cursor.getString(cursor.getColumnIndex(field.getName()));
                    if (!TextUtils.isEmpty(stringValue) && !stringValue.trim().equals("{}") && jsonInterface != null) {
                        if(!Modifier.isAbstract(field.getType().getModifiers())) {
                            Object obj = jsonInterface.parseObject(stringValue, field.getType());
                            field.set(t, obj);
                        }

                    }
                } catch (Exception e) {
                    Log.w(TAG, "this field " + field.getType() + "' values is with some error " + e.getMessage());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除一个表的所有数据
     *
     * @param clazz 表名
     */
    synchronized public void deleteAll(Class<?> clazz) {
        delete(clazz, null, null);
    }

    /**
     * 删除
     *
     * @param clazz       表的类
     * @param whereClause 条件
     * @param whereArgs   条件值
     */
    synchronized public void delete(Class<?> clazz, String whereClause, String[] whereArgs) {

        SQLiteDatabase db = dbHelper.openDatabase();
        db.beginTransaction();
        if (db.isOpen()) {
            boolean isExit = DbUtils.checkTableState(db, clazz);
            if (isExit) {
                //必须存在表才可以删除
                DbUtils.updateTable(db, clazz);

                db.delete(DbUtils.getTableName(clazz), whereClause, whereArgs);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        //每次执行完成必须关闭数据库
        closeCursor();
    }

    synchronized public void deleteById(Class<?> clazz, long id) {
        delete(clazz, "id=?", new String[]{id + ""});
    }

    synchronized public void execute(String sql) {
        SQLiteDatabase db = dbHelper.openDatabase();
        db.beginTransaction();
        if (db.isOpen()) {
            db.execSQL(sql);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        //每次执行完成必须关闭数据库
        closeCursor();
    }

    synchronized public Cursor rawQuery(String sql) {
        SQLiteDatabase db = dbHelper.openDatabase();
        db.beginTransaction();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(sql, null);
            db.setTransactionSuccessful();
            return cursor;
        } else {
            return null;
        }
    }

    synchronized public void closeCursor() {
        //每次执行完成必须关闭数据库
        if(dbHelper == null){
            return;
        }
        dbHelper.closeDatabase();
    }

}