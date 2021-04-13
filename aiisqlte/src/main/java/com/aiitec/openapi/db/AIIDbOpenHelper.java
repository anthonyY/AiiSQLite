package com.aiitec.openapi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.aiitec.openapi.db.annotation.Column;
import com.aiitec.openapi.db.annotation.NotNull;
import com.aiitec.openapi.db.annotation.Unique;
import com.aiitec.openapi.db.utils.AiiJson;
import com.aiitec.openapi.db.utils.CombinationUtil;
import com.aiitec.openapi.db.utils.DbUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author  Anthony
 * 数据库helper类
 *
 */
public class AIIDbOpenHelper extends SQLiteOpenHelper {

    /**解决多线程并发*/
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private static final int dbVersion = 1;
    private static HashMap<String, AIIDbOpenHelper> instances = new HashMap<>();
    /**默认数据库名*/
    private static String currontDbName = "currentDbName.db";
    private Context context;
    private SQLiteDatabase mDatabase;

    private AIIDbOpenHelper(Context context) {
        super(context, currontDbName, null, dbVersion);
        this.context = context;

    }

    private AIIDbOpenHelper(Context context, String dbName) {
        super(context, dbName, null, dbVersion);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static AIIDbOpenHelper getInstance(Context context) {
        currontDbName = context.getPackageName()+".db";
        if (instances.get(currontDbName) == null) {
            instances.put(currontDbName, new AIIDbOpenHelper(context.getApplicationContext()));
        }
        return instances.get(currontDbName);
    }

    public static AIIDbOpenHelper getInstance(Context context, long userId) {
        currontDbName = context.getPackageName()+"_"+userId+".db";
        if (instances.get(currontDbName) == null) {
            instances.put(currontDbName, new AIIDbOpenHelper(context.getApplicationContext()));
        }
        return instances.get(currontDbName);
    }

    public static AIIDbOpenHelper getInstance(Context context, String dbName) {
        currontDbName = dbName;
        if (instances.get(dbName) == null) {
            instances.put(dbName, new AIIDbOpenHelper(context.getApplicationContext(), dbName));
        }
        return instances.get(dbName);
    }

    /**
     * 打开数据库对象
     * @return
     */
    public synchronized SQLiteDatabase openDatabase() {

        int index = mOpenCounter.incrementAndGet();
        if (index == 1) {
            // Opening new database
            mDatabase = getWritableDatabase();
        } else if(mDatabase == null){
            mDatabase = getWritableDatabase();
        }
        return mDatabase;
    }
    /**
     * 多线程下关闭
     */
    public synchronized void closeDatabase() {
        int index = mOpenCounter.decrementAndGet();
        if (index == 0) {
            if(mDatabase == null){
                return;
            }
            try {
                mDatabase.close();
            } catch (Exception e){
                e.printStackTrace();
            }
            mDatabase = null;
        }
    }

    /**
     * 创建或者更新表
     * @param clazz 类名，一般表名就是 类名.simpleName
     */
    public void createOrUpdateTable(Class<?> clazz) {
        if(mDatabase == null){
            mDatabase = openDatabase();
        }
        SQLiteDatabase db = mDatabase;
        boolean isExit = DbUtils.checkTableState(db, clazz);
        if (isExit) {// 存在表并且字段没有改变，就不需要建表了
            DbUtils.updateTable(db, clazz);
            return;
        }
        List<Field> fields = CombinationUtil.getAllFields(clazz);
        String tableName = DbUtils.getTableName(clazz);

        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append("(");
        // 让这个字段自动累加，而不影响其他主键
        sb.append("_id integer primary key autoincrement,");

        for (int i = 0; i < fields.size(); i++) {
            // 内部类会出现这个字段，所以我们设计不要有内部类
            if (fields.get(i).getName().equals("this$0")) {
                continue;
            }

            Column column = fields.get(i).getAnnotation(Column.class);
            String columnName = fields.get(i).getName();
            if (column != null ) {
                if(!TextUtils.isEmpty(column.value())){
                    columnName = column.value();
                }
                else if(!TextUtils.isEmpty(column.column())){
                    columnName = column.column();
                }
            }
            Unique unique = fields.get(i).getAnnotation(Unique.class);
            NotNull notNull = fields.get(i).getAnnotation(NotNull.class);

            sb.append(" " + columnName);
            Class fieldClazz = fields.get(i).getType();
            if (fieldClazz.equals(int.class) || fieldClazz.equals(long.class) ||
                    fieldClazz.equals(Integer.class) || fieldClazz.equals(Long.class)) {
                sb.append(" NUMRIC ");
            } else if (fieldClazz.equals(float.class) || fieldClazz.equals(double.class) ||
                    fieldClazz.equals(Float.class) || fieldClazz.equals(Double.class)) {
                sb.append(" NUMRIC ");
            } else if (fieldClazz.equals(char.class) || fieldClazz.equals(String.class)) {
                sb.append(" TEXT ");
            } else if (fieldClazz.equals(Date.class)) {
                sb.append(" INTEGER ");
            } else {
                sb.append(" TEXT ");
            }
            if (unique != null) {
                sb.append(" unique ");
            }
            if (notNull != null) {
                sb.append(" not null ");
            }
            if (i != fields.size() - 1) {
                sb.append(',');
            }
        }
        if (sb.toString().endsWith(",")) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(");");
        db.execSQL(sb.toString());
    }

    synchronized protected <T> void save(SQLiteDatabase db, T t) {

        String table = DbUtils.getTableName(t.getClass());

        List<Field> fields = CombinationUtil.getAllFields(t.getClass());

        ContentValues values = new ContentValues();
        for (int i = 0; i < fields.size(); i++) {
        	String columnName = fields.get(i).getName();
            if (columnName.equals("this$0")) {
                continue;
            }
            putValueToContentValues(fields.get(i), t, values);
        }
        db.replace(table, null, values);

    }
    synchronized protected <T> void update(SQLiteDatabase db, T t) {
    	
    	String table = DbUtils.getTableName(t.getClass());
    	
    	List<Field> fields = CombinationUtil.getAllFields(t.getClass());
        //保持数据库唯一性的字段，可能是多个
    	List<Field> uniqueField = new ArrayList<Field>();
    	ContentValues values = new ContentValues();
    	for (int i = 0; i < fields.size(); i++) {
    		if (fields.get(i).getName().equals("this$0")) {
    			continue;
    		}
    		//如果字段是唯一的，就加入唯一字段的List
    		Unique unique = fields.get(i).getAnnotation(Unique.class);
    		if(unique != null) {
                uniqueField.add(fields.get(i));
            }
    		putValueToContentValues(fields.get(i), t, values);
    	}
    	String whereClause = null;
    	String[] whereArgs = null;
    	if(uniqueField.size() > 0){
    		int index = 0;
    		StringBuilder sb = new StringBuilder();
    		whereArgs = new String[uniqueField.size()];
    		for (Field field: uniqueField) {
                //如果已经有字段了，就加 AND
    			if(sb.toString().trim().length() > 0){
    				sb.append(" AND ");
    			}
    			Column column = field.getAnnotation(Column.class);
    			String columnName = field.getName();
    			if (column != null && !TextUtils.isEmpty(column.column())) {
    				columnName = column.column();
    			}
    			sb.append(columnName).append("=?");
    			whereClause = sb.toString();
    			try {
					whereArgs[index] = field.get(t).toString();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
    			index ++;
    		}
    		
    	}
    	db.update(table, values, whereClause, whereArgs);
    }

    private void putValueToContentValues(Field field, Object t, ContentValues values) {
        Column column = field.getAnnotation(Column.class);
        String columnName = field.getName();
        if (column != null) {
            if(!TextUtils.isEmpty(column.value())){
                columnName = column.value();
            }
            else if(!TextUtils.isEmpty(column.column())){
                columnName = column.column();
            }
        }
        Object o = null;
        try {
            field.setAccessible(true);
            o = field.get(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (o != null ) {

            Class clazz = field.getType();
            if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
                //保存可以不用考虑这些东西，但是更新就要防止空和-1
//                if(!Integer.valueOf(-1).equals(o)){
                    values.put(columnName, (Integer) o);
//                }
            } else if (clazz.equals(long.class)||clazz.equals(Long.class)) {
//                if(!Long.valueOf(-1).equals(o)){
                    values.put(columnName, (Long) o);
//                }
            } else if (clazz.equals(float.class)||clazz.equals(Float.class)) {
//                if(!Float.valueOf("-1").equals(o)){
                    values.put(columnName, (Float) o);
//                }
            } else if (clazz.equals(double.class)||clazz.equals(Double.class)) {
//                if(!Double.valueOf(-1).equals(o)){
                    values.put(columnName, (Double) o);
//                }
            } else if (clazz.equals(char.class) || clazz.equals(String.class)) {
                values.put(columnName, o.toString());
            } else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
                boolean booleanValue = (boolean) o;
                if(booleanValue){
                    values.put(columnName, 1);
                } else {
                    values.put(columnName, 0);
                }
            } else if (clazz.equals(List.class) || clazz.equals(ArrayList.class)) {
                try {
                    values.put(columnName, AiiJson.toJsonString(o));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (clazz.equals(Date.class)) {
                values.put(columnName, ((Date)o).getTime());
            } else if(!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    values.put(columnName, AiiJson.toJsonString(o));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
