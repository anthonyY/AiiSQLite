package com.aiitec.openapi.db.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.aiitec.openapi.db.annotation.Column;
import com.aiitec.openapi.db.annotation.NotNull;
import com.aiitec.openapi.db.annotation.Table;
import com.aiitec.openapi.db.annotation.Unique;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DbUtils {

    public static String getTableName(Class<?> clazz) {
        Table annotation = clazz.getAnnotation(Table.class);
        String tableName = clazz.getName();
        int index = clazz.getName().lastIndexOf(".");
        if (index != -1) {
            tableName = clazz.getName().substring(index + 1,
                    clazz.getName().length());
        }
        if (annotation != null && !TextUtils.isEmpty(annotation.name())) {
            tableName = annotation.name();
        }
        return tableName;
    }

    /**
     * @param db 
     * @param clazz
     *            新对象类
     */
    public static boolean checkTableState(SQLiteDatabase db, Class<?> clazz) {
        boolean isExit = false;
        String tableName = DbUtils.getTableName(clazz);
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM sqlite_master where type='table' and name='"
                        + tableName + "'", null);
        if (cursor.moveToNext()) {
            int count = cursor.getInt(0);
            if (count > 0) {// 表示该表存在
                isExit = true;
            }
        }
        cursor.close();
        return isExit;
    }

    /**
     * 更新表中的columns
     * 
     * @param db
     * @param clazz
     * @return
     */
    public static boolean updateTable(SQLiteDatabase db, Class<?> clazz) {
        boolean result = false;
        // Field[] fields = clazz.getDeclaredFields();
        List<Field> fields = CombinationUtil.getAllFields(clazz);
        String tableName = DbUtils.getTableName(clazz);

        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getName().equals("this$0")) {// 内部类会出现这个字段，所以我们设计不要有内部类
                continue;
            }

            Column column = fields.get(i).getAnnotation(Column.class);
            String columnName = fields.get(i).getName();
            if (column != null && !TextUtils.isEmpty(column.column())) {
                columnName = column.column();
            }

            if (checkColumnExists(db, tableName, columnName)) {
                continue;
            } else {
                result = true;
            }

            Unique unique = fields.get(i).getAnnotation(Unique.class);
            NotNull notNull = fields.get(i).getAnnotation(NotNull.class);

            StringBuilder sb = new StringBuilder();
            sb.append("ALTER TABLE ").append(tableName).append(" ADD ")
                    .append(columnName);
            if (fields.get(i).getType().equals(int.class)
                    || fields.get(i).getType().equals(long.class)) {
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
            sb.append(";");
            db.execSQL(sb.toString());
        }

        return result;
    }

    /**
     * 检查表中某列是否存在
     * 
     * @param db
     * @param tableName
     *            表名
     * @param columnName
     *            列名
     * @return
     */
    public static boolean checkColumnExists(SQLiteDatabase db,
            String tableName, String columnName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            // 方法1
            cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 1",
                    null);
            result = cursor != null && cursor.getColumnIndex(columnName) != -1;
            // //方法2
            // cursor = db
            // .rawQuery(
            // "select * from sqlite_master where name = ? and sql like ?",
            // new String[] { tableName, "%" + columnName + "%" });
            // result = null != cursor && cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return result;
    }

    /**
     * 
	 * 时间戳转化为日期 yyyy-MM-dd HH:mm:ss 格式  就这点东西不想新建一个类
	 * @param timeStamp 
	 * @return
	 */
	public static Date timeStamp2Date(String timeStamp){
	    SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	    Date date = null;
	    try {
	        date = format.parse(timeStamp);
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    return date;
	}
	/**
	 * Date 转化为时间戳  yyyy-MM-dd HH:mm:ss  格式  就这点东西不想新建一个类
	 * @param date 时间
	 * @return
	 */
	public static String date2TimeStamp(Date date){
	    SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", Locale.getDefault() );
	    String timestamp = null;
	    timestamp = format.format(date);
	    return timestamp;
	}
	
}
