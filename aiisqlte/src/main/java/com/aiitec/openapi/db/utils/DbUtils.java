package com.aiitec.openapi.db.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.aiitec.openapi.db.AIIDBManager;
import com.aiitec.openapi.db.annotation.Column;
import com.aiitec.openapi.db.annotation.Index;
import com.aiitec.openapi.db.annotation.NotNull;
import com.aiitec.openapi.db.annotation.Table;
import com.aiitec.openapi.db.annotation.Unique;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 数据库工具类
 *
 * @author Anthony
 */
public class DbUtils {

    /**
     * 是否检测了表的map
     * 每检测一个都存一个true
     */
    private static final Map<String, Boolean> checkedTableMap = new HashMap<>();
    final String TAG = "TAG_AII_SQLITE";

    /**
     * 获取表名
     *
     * @param clazz 类
     * @return 表名
     */
    public static String getTableName(Class<?> clazz) {
        Table annotation = clazz.getAnnotation(Table.class);
        //默认取类的名称（不含包）
        String tableName = clazz.getSimpleName();
        if (annotation != null) {
            if (!TextUtils.isEmpty(annotation.value())) {
                tableName = annotation.value();
            } else if (!TextUtils.isEmpty(annotation.name())) {
                tableName = annotation.name();
            }
        }
        return tableName;
    }

    /**
     * 获取字段名
     *
     * @param field 字段
     */
    private static String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        String columnName = field.getName();
        if (column != null) {
            if (!TextUtils.isEmpty(column.value())) {
                columnName = column.value();
            } else if (!TextUtils.isEmpty(column.column())) {
                columnName = column.column();
            }
        }
        return columnName;
    }

    /**
     * 查看表是否存在
     *
     * @param db    db对象
     * @param clazz 类
     *              新对象类
     */
    public static boolean checkTableState(SQLiteDatabase db, Class<?> clazz) {
        String tableName = DbUtils.getTableName(clazz);
        return checkTableState(db, tableName);
    }

    /**
     * 查看表是否存在
     *
     * @param db        db对象
     * @param tableName 表名
     * @return
     */
    public static boolean checkTableState(SQLiteDatabase db, String tableName) {
        boolean isExit = false;
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
     * @param db    db对象
     * @param clazz 类
     * @return 是否更新了字段
     */
    public static boolean updateTable(SQLiteDatabase db, Class<?> clazz) {

        boolean result = false;
        String tableName = DbUtils.getTableName(clazz);
        Boolean isCheck = checkedTableMap.get(tableName);
        if (null != isCheck && isCheck) { // 检测过了就不用检查了，避免浪费资源在检查表结构和表索引上
            return false;
        }
        List<Field> fields = CombinationUtil.getAllFields(clazz);

        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getName().equals("this$0")) {
                // 内部类会出现这个字段，所以我们设计不要有内部类
                continue;
            }

            String columnName = getColumnName(fields.get(i));
            Index index = fields.get(i).getAnnotation(Index.class);
            if (checkColumnExists(db, tableName, columnName)) {
                boolean isUpdateIndex = updateIndex(db, tableName, columnName, index);
                if(isUpdateIndex){
                    result = true;
                }
                continue;
            } else {
                result = true;
            }

            addColume(db, tableName, columnName, fields.get(i));

            updateIndex(db, tableName, columnName, index);

        }
        checkedTableMap.put(tableName, true);
        return result;
    }

    /**
     * 增加字段
     *
     * @param db    数据库
     * @param field 字段
     */
    private static void addColume(SQLiteDatabase db, String tableName, String columnName, Field field) {
        Unique unique = field.getAnnotation(Unique.class);
        NotNull notNull = field.getAnnotation(NotNull.class);

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE '").append(tableName).append("' ADD '")
                .append(columnName).append("'");
        if (field.getType().equals(int.class) || field.getType().equals(Integer.class) ||
                field.getType().equals(long.class) || field.getType().equals(Long.class) ||
                field.getType().equals(float.class) || field.getType().equals(Float.class) ||
                field.getType().equals(double.class) || field.getType().equals(Double.class) ||
                field.getType().equals(byte.class) || field.getType().equals(Byte.class) ||
                field.getType().equals(short.class) || field.getType().equals(Short.class) ||
                field.getType().equals(boolean.class) || field.getType().equals(Boolean.class) ||
                field.getType().equals(Date.class) || field.getType().equals(java.sql.Date.class) // 时间用long来存储
        ) {
            sb.append(" NUMRIC ");
        } else if (field.getType().equals(char.class) || field.getType().equals(String.class)) {
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
        if (AIIDBManager.showLog) {
            Log.i(AIIDBManager.TAG, sb.toString());
        }

        db.execSQL(sb.toString());
    }

    /**
     * 更新索引
     *
     * @param db         数据库对象
     * @param tableName  表名
     * @param columnName 字段名
     * @param index      索引
     * @return 是否有更新
     */
    private static boolean updateIndex(SQLiteDatabase db, String tableName, String columnName, Index index) {

        if (null == index || TextUtils.isEmpty(index.value())) {
            return false;
        }

        // 查询索引是否存在
        String indexSql = findExistsIndex(db, tableName, index.value());
        // 如果索引存在
        if (!TextUtils.isEmpty(indexSql)) {
            // 如果索引已存在，并且没有盖字段，就把当前字段拼进去
            int i = indexSql.lastIndexOf("(");
            int j = indexSql.lastIndexOf(")");
            StringBuilder sb1 = new StringBuilder();

            if (i > 0 && j > 0) {
                String[] split = indexSql.substring(i + 1, j).split(","); // 原本索引已经有的字段
                for (String s : split) {
                    String s1 = s.replaceAll("ASC|asc|desc|DESC", "").trim();
                    if (s1.equals(columnName.trim())) {
                        // 如果这个索引已经有这个字段了
                        return false;
                    }
                }
                sb1.append(indexSql, 0, j).append(",").append(columnName);
                if (TextUtils.isEmpty(index.orderBy())) {
                    sb1.append(" ").append(index.orderBy());
                }
                sb1.append(");");
            }

            // 已存在索引，就删除再增加
            String dropIndexSql = "DROP INDEX " + index.value() + ";";

            // 先删除索引
            if (AIIDBManager.showLog) {
                Log.i(AIIDBManager.TAG, dropIndexSql);
            }
            db.execSQL(dropIndexSql);
            // 再创建修改后的索引
            if (AIIDBManager.showLog) {
                Log.i(AIIDBManager.TAG, sb1.toString());
            }
            db.execSQL(sb1.toString());

        } else {
            // 如果没有这个索引，就创建索引
            String orderBy = "";
            if (!TextUtils.isEmpty(index.orderBy())) {
                orderBy = " " + index.orderBy();
            }
            indexSql = "CREATE INDEX IF NOT EXISTS " + index.value() + " on " + tableName + "(" + columnName + orderBy + ")";
            if (AIIDBManager.showLog) {
                Log.i(AIIDBManager.TAG, indexSql);
            }
            db.execSQL(indexSql);
        }
        return true;
    }


    /**
     * 查找已存在的索引
     *
     * @param db        数据库
     * @param tableName 表名
     * @param indexName 索引名称
     */
    private static String findExistsIndex(SQLiteDatabase db, String tableName, String indexName) {
        String checkIndexExistSql = "SELECT sql FROM sqlite_master WHERE type='index' AND tbl_name = ? AND name = ?";
        Cursor cursor = db.rawQuery(checkIndexExistSql, new String[]{tableName, indexName});
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndexOrThrow("sql"));
        }
        return null;
    }

    /**
     * 检查表中某列是否存在
     *
     * @param db         db对象
     * @param tableName  表名
     * @param columnName 列名
     * @return
     */
    public static boolean checkColumnExists(SQLiteDatabase db,
                                            String tableName, String columnName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            // 方法1
            cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null);
            result = cursor != null && cursor.getColumnIndex(columnName) != -1;
            //方法2
            // cursor = db
            // .rawQuery(
            // "select * from sqlite_master where name = ? and sql like ?",
            // new String[] { tableName, "%" + columnName + "%" });
            // result = null != cursor && cursor.moveToFirst();/**/
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
     * 时间戳转化为日期 yyyy-MM-dd HH:mm:ss 格式  就这点东西不想新建一个类
     *
     * @param timeStamp 时间
     * @return 日期对象
     */
    public static Date timeStamp2Date(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = null;
        try {
            date = format.parse(timeStamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date timeStamp2Date(String timeStamp, String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr, Locale.getDefault());
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
     *
     * @param date 时间
     * @return 格式化后的时间字符串
     */
    public static String date2TimeStamp(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = null;
        timestamp = format.format(date);
        return timestamp;
    }

    public static String date2TimeStamp(Date date, String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr, Locale.getDefault());
        String timestamp = null;
        timestamp = format.format(date);
        return timestamp;
    }

}
