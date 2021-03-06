package com.aiitec.openapi.db.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用自己写的json解析包
 *
 * @author Anthony
 */
public class AiiJson {

    public static String toJsonString(Object t) {
        return defaultToString(t);
    }

    public static <T> List<T> parseArray(String json, Class<T> entityClazz) {
        try {
            List<T> data = new ArrayList<T>();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                if (CombinationUtil.isCommonField(entityClazz)) {
                    data.add((T) array.get(i));
                } else {
                    JSONObject obj = array.getJSONObject(i);
                    T t = defaultParseObject(obj, entityClazz);
                    if (t != null) {
                        data.add(t);
                    }
                }
            }
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultParseObject(jsonObject, clazz);
    }

    private static <T> T defaultParseObject(JSONObject jsonObject, Class<T> clazz) {
        if (jsonObject == null) {
            return null;
        }
        try {
            T t = clazz.newInstance();
            if (t != null) {
                Iterator<String> iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    Object value = jsonObject.opt(key);
                    if (value == null) {
                        continue;
                    }

                    Field field = clazz.getDeclaredField(key);
                    if (field != null) {
                        field.setAccessible(true);
                        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                            String stringValue = value.toString();
                            if (stringValue.equals("1") || stringValue.equalsIgnoreCase("true")) {
                                value = true;
                            } else if (stringValue.equals("0") || stringValue.equals("2")
                                    || stringValue.equalsIgnoreCase("false")) {
                                value = false;
                            }
                            field.set(t, value);
                        }
                        else if(field.getType().equals(int.class) || field.getType().equals(Integer.class)){
                            field.set(t, jsonObject.optInt(key));
                        }
                        else if(field.getType().equals(float.class) || field.getType().equals(Float.class)){
                            field.set(t, (float)jsonObject.optDouble(key));
                        }
                        else if(field.getType().equals(double.class) || field.getType().equals(Double.class)){
                            field.set(t, jsonObject.optDouble(key));
                        }
                        else if(field.getType().equals(long.class) || field.getType().equals(Long.class)){
                            field.set(t, jsonObject.optLong(key));
                        } else if(field.getType().equals(String.class)){
                            field.set(t, jsonObject.optString(key));
                        }
                        else {
                            try {//这里会有各种各样的异常，所以要try catch Exception
                                JSONObject obj = jsonObject.optJSONObject(key);
                                defaultParseObject(obj, field.getType());
                            } catch (Exception e) {
//                                e.printStackTrace();
                            }
                        }
                    }
                }
                return t;
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 默认的toStrng 只读取当前类常用属性
     *
     * @param t 要转Json String 的对象
     * @return String 结果
     */
    private static String defaultToString(Object t) {
        if (t.getClass() == String.class || t.getClass().isPrimitive()) {
            return t.toString();
        }
        StringBuilder sb = new StringBuilder();
        if(List.class.isAssignableFrom(t.getClass())){
            sb.append("[");

            for (Object obj : (List)t){
                if(CombinationUtil.isCommonField(obj.getClass())){
                    if(Number.class.isAssignableFrom(obj.getClass())){
                        sb.append(obj).append(",");
                    } else {
                        sb.append("\"").append(obj).append("\"").append(",");
                    }
                } else {
                    sb.append(defaultToString(obj)).append(",");
                }
            }
            if(sb.toString().endsWith(",")){
                sb.deleteCharAt(sb.length()-1);
            }
            sb.append("]");
        } else {
            sb.append("{");
            List<Field> fields = CombinationUtil.getAllFields(t.getClass());
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(t);
                    if (value != null /* && !value.toString().equals("-1") && !value.toString().equals("-1.0") */) {
                        sb.append("\"").append(field.getName()).append("\"").append(":");
                        if (CombinationUtil.isCommonField(field.getType())) {
                            sb.append("\"").append(value).append("\"");
                        }
                        else if(List.class.isAssignableFrom(field.getType())){
                            sb.append("[");
                            List list = (List) value;
                            for (Object obj : list){
                                if(CombinationUtil.isCommonField(obj.getClass())){
                                    if(Number.class.isAssignableFrom(obj.getClass())){
                                        sb.append(obj).append(",");
                                    } else {
                                        sb.append("\"").append(obj).append("\"").append(",");
                                    }
                                } else {
                                    sb.append(defaultToString(obj)).append(",");
                                }
                            }
                            if(sb.toString().endsWith(",")){
                                sb.deleteCharAt(sb.length()-1);
                            }
                            sb.append("]");
                        }
                        else {
                            if(value.toString().trim().startsWith("{")){
                                sb.append(value);
                            } else {
                                sb.append("\"").append(value).append("\"");
                            }
                        }
                        sb.append(",");
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (sb.toString().endsWith(",")) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("}");
        }

        return sb.toString();
    }

}
