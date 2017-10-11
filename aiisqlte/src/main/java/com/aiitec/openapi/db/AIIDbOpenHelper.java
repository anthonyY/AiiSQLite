package com.aiitec.openapi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.aiitec.openapi.db.annotation.Column;
import com.aiitec.openapi.db.annotation.NotNull;
import com.aiitec.openapi.db.annotation.Unique;
import com.aiitec.openapi.db.utils.CombinationUtil;
import com.aiitec.openapi.db.utils.DbUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIIDbOpenHelper extends SQLiteOpenHelper {

    private static final int dbVersion = 1;
    private static HashMap<String, AIIDbOpenHelper> instances = new HashMap<>();
    private static String currontDbName = "currentDbName.db";//默认数据库名,
    private Context context;
    private AIIDbOpenHelper(Context context) {
        super(context, currontDbName, null, dbVersion);
        this.context = context;

    }
    private AIIDbOpenHelper(Context context, long userId) {
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
            instances.put(currontDbName, new AIIDbOpenHelper(context.getApplicationContext(), userId));
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

    public void closeAllDB() {
        if (instances.size() == 0) return;
        for (Map.Entry<String, AIIDbOpenHelper> entry : instances.entrySet()) {
            closeDatabace(entry.getValue());
        }
        instances.clear();
    }
    public void closeDB(long userId) {
        if (instances.size() == 0) return;
        String dbName = context.getPackageName()+"_"+userId+".db";
        closeDB(dbName);
    }
    public void closeDB(String dbName) {
        if (instances.size() == 0) return;
        AIIDbOpenHelper helper = instances.get(dbName);
        if(helper == null) return;
        closeDatabace(helper);
        instances.remove(dbName);
    }
    private void closeDatabace(AIIDbOpenHelper helper){
        if (helper != null) {
            try {
                SQLiteDatabase db = helper.getWritableDatabase();
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void createOrUpdateTable(Class<?> clazz) {

        SQLiteDatabase db = getWritableDatabase();
        boolean isExit = DbUtils.checkTableState(db, clazz);
        if (isExit) {// 存在表并且字段没有改变，就不需要建表了
            DbUtils.updateTable(db, clazz);
            return;
        }

        // else if(state == TableStatus.IS_CHANGE){
        // //表示表有改动， 暂时懒得考虑那么多，
        // db.execSQL("DROP TABLE "+tableName);
        // }
        //
        // Field[] fields = clazz.getDeclaredFields();
        List<Field> fields = CombinationUtil.getAllFields(clazz);
        String tableName = DbUtils.getTableName(clazz);

        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append("(");
        sb.append("_id integer primary key autoincrement,");// 让这个字段自动累加，而不影响其他主键
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getName().equals("this$0")) {// 内部类会出现这个字段，所以我们设计不要有内部类
                continue;
            }

            Column column = fields.get(i).getAnnotation(Column.class);
            String columnName = fields.get(i).getName();
            if (column != null && !TextUtils.isEmpty(column.column())) {
                columnName = column.column();
            }
            Unique unique = fields.get(i).getAnnotation(Unique.class);
            NotNull notNull = fields.get(i).getAnnotation(NotNull.class);

            sb.append(" " + columnName);
            if (fields.get(i).getType().equals(int.class)
                    || fields.get(i).getType().equals(long.class)) {
                // if(fields.get(i).getName().equals("id")||fields.get(i).getName().equals("_id")){
                // sb.append(" NUMRIC PRIMARY KEY,");
                // } else {
                // }
                sb.append(" NUMRIC ");
            } else if (fields.get(i).getType().equals(float.class)
                    || fields.get(i).getType().equals(double.class)) {
                sb.append(" NUMRIC ");
            } else if (fields.get(i).getType().equals(char.class)
                    || fields.get(i).getType().equals(String.class)) {
                sb.append(" TEXT ");
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
        // Field[] fields = t.getClass().getDeclaredFields();

        ContentValues values = new ContentValues();
        for (int i = 0; i < fields.size(); i++) {
        	String columnName = fields.get(i).getName();
            if (columnName.equals("this$0")) {
                continue;
            }

            Column column = fields.get(i).getAnnotation(Column.class);
            
            // String defaultValue = null;
            if (column != null && !TextUtils.isEmpty(column.column())) {
                columnName = column.column();
                // defaultValue = column.defaultValue();
            }

            Object o = null;
            try {
                // 两种方式都可以，
                // 方式一 不用暴力，和平解决战争，但是如果get 与 set方法写错就获取不了，比如布尔类型的应isXXX,
                // 实体类里也需要加get set方法
                // o =
                // t.getClass().getMethod(PacketUtil.getMethodName(fields.get(i).getName())).invoke(t);
                // 方式二 暴力反射，准确，不用考虑太多，但是感觉很野蛮
                fields.get(i).setAccessible(true);
                o = fields.get(i).get(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (o != null) {

                if (fields.get(i).getType().equals(int.class)) {
                    values.put(columnName, (Integer) o);

                } else if (fields.get(i).getType().equals(long.class)) {
                    values.put(columnName, (Long) o);
                } else if (fields.get(i).getType().equals(float.class)) {
                    values.put(columnName, (Float) o);

                } else if (fields.get(i).getType().equals(double.class)) {
                    values.put(columnName, (Double) o);
                } else if (fields.get(i).getType().equals(char.class)
                        || fields.get(i).getType().equals(String.class)) {
                    values.put(columnName, o.toString());
                } else if (fields.get(i).getType().equals(List.class)
                        || fields.get(i).getType().equals(ArrayList.class)) {

                    try {
                        values.put(columnName, AIIDBManager.jsonInterface.toJsonString(o));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (fields.get(i).getType().equals(Date.class)) {
                    String time = DbUtils.date2TimeStamp((Date) o);
                    values.put(columnName, time);
                } else {
                    try {
                        values.put(columnName, AIIDBManager.jsonInterface.toJsonString(o));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        // db.insert(table, null, values);
        db.replace(table, null, values);

    }
    synchronized protected <T> void update(SQLiteDatabase db, T t) {
    	
    	String table = DbUtils.getTableName(t.getClass());
    	
    	List<Field> fields = CombinationUtil.getAllFields(t.getClass());
    	// Field[] fields = t.getClass().getDeclaredFields();
    	List<Field> uniqueField = new ArrayList<Field>();//保持数据库唯一性的字段，可能是多个
    	ContentValues values = new ContentValues();
    	for (int i = 0; i < fields.size(); i++) {
    		if (fields.get(i).getName().equals("this$0")) {
    			continue;
    		}
    		
    		//如果字段是唯一的，就加入唯一字段的List
    		Unique unique = fields.get(i).getAnnotation(Unique.class);
    		if(unique != null) uniqueField.add(fields.get(i));
    		
    		Column column = fields.get(i).getAnnotation(Column.class);
    		String columnName = fields.get(i).getName();
    		if (column != null && !TextUtils.isEmpty(column.column())) {
    			columnName = column.column();
    		}
    		
    		Object o = null;
    		try {
    			fields.get(i).setAccessible(true);
    			o = fields.get(i).get(t);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		if (o != null ) {
    			
    			if (fields.get(i).getType().equals(int.class)) {
    				if((Integer) o != -1){//保存可以不用考虑这些东西，但是更新就要防止空和-1
    					values.put(columnName, (Integer) o);
    				}
    			} else if (fields.get(i).getType().equals(long.class)) {
    				if((Long) o != -1){
    					values.put(columnName, (Long) o);
    				}
    			} else if (fields.get(i).getType().equals(float.class)) {
    				if((Float) o != -1){
    					values.put(columnName, (Float) o);
    				}
    			} else if (fields.get(i).getType().equals(double.class)) {
    				if((Double) o != -1){
    					values.put(columnName, (Double) o);
    				}
    			} else if (fields.get(i).getType().equals(char.class)
    					|| fields.get(i).getType().equals(String.class)) {
    				values.put(columnName, o.toString());
    			} else if (fields.get(i).getType().equals(List.class)
    					|| fields.get(i).getType().equals(ArrayList.class)) {
    				
    				try {
    					values.put(columnName, AIIDBManager.jsonInterface.toJsonString(o));
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			} else if (fields.get(i).getType().equals(Date.class)) {
    				String time = DbUtils.date2TimeStamp((Date) o);
    				values.put(columnName, time);
    			} else {
    				try {
    					values.put(columnName, AIIDBManager.jsonInterface.toJsonString(o));
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
    		}
    		
    	}
    	String whereClause = null;
    	String[] whereArgs = null;
    	if(uniqueField.size() > 0){
    		int index = 0;
    		StringBuilder sb = new StringBuilder();
    		whereArgs = new String[uniqueField.size()];
    		for (Field field: uniqueField) {
    			if(sb.toString().trim().length() > 0){//如果已经有字段了，就加 AND
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


}
