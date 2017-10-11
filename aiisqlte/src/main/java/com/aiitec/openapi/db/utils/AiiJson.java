package com.aiitec.openapi.db.utils;

import com.aiitec.openapi.db.JsonInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用自己写的json解析包
 *
 * @author Anthony
 */
public class AiiJson implements JsonInterface {

    @Override
    public String toJsonString(Object t) {
        try {
            Class clazz = Class.forName("com.aiitec.openapi.json.JSON");
            if (clazz != null) {
                Method method = clazz.getMethod("toJsonString", Object.class);
                if (method != null) {
                    String jsonString = (String) method.invoke(null, t);
                    return jsonString;
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return defaultToString(t);
    }

    @Override
    public <T> List<T> parseArray(String json, Class<T> entityClazz) {
        try {
            Class clazz = Class.forName("com.aiitec.openapi.json.JSON");
            if (clazz != null) {
                Method method = clazz.getMethod("parseArray", String.class, Class.class);
                if (method != null) {
                    List<T> object = (List<T>) method.invoke(null, json, entityClazz);
                    return object;
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        try {
            List<T> data = new ArrayList();
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

    @Override
    public <T> T parseObject(String json, Class<T> clazz) {
        try {
            Class jsonClazz = Class.forName("com.aiitec.openapi.json.JSON");
            if (clazz != null) {
                Method method = jsonClazz.getMethod("parseObject", String.class, Class.class);
                if (method != null) {
                    return (T) method.invoke(null, json, clazz);
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defaultParseObject(jsonObject, clazz);
    }

    private <T> T defaultParseObject(JSONObject jsonObject, Class<T> clazz) {
        if (jsonObject == null) return null;
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
    private String defaultToString(Object t) {
        if (t.getClass() == String.class || t.getClass().isPrimitive()) {
            return t.toString();
        }
        StringBuffer sb = new StringBuffer();
        if(List.class.isAssignableFrom(t.getClass())){
            sb.append("[");

            for (Object obj : (List)t){
                sb.append(defaultToString(obj)).append(",");
            }
            if(sb.toString().endsWith(",")){
                sb.deleteCharAt(sb.length()-1);
            }
            sb.append("]");
        } else {
            sb.append("{");
            for (Field field : t.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(t);
                    if (value != null) {
                        sb.append("\"").append(field.getName()).append("\"").append(":");
                        if (CombinationUtil.isCommonField(field.getType())) {
                            sb.append("\"").append(value).append("\"");
                        }
                        else if(List.class.isAssignableFrom(field.getType())){
                            sb.append("[");
                            List list = (List) value;
                            for (Object obj : list){
                                sb.append(defaultToString(obj)).append(",");
                            }
                            if(sb.toString().endsWith(",")){
                                sb.deleteCharAt(sb.length()-1);
                            }
                            sb.append("]");
                        }
                        else {
                            sb.append(value);
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
