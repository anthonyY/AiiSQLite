package com.aiitec.openapi.db.utils;

import android.annotation.SuppressLint;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CombinationUtil {

	private static HashMap<Field, Class<?>> childClasses = new HashMap<Field, Class<?>>();
	  
	private static HashMap<Class<?>, List<Field>> fieldMap = new HashMap<Class<?>, List<Field>>();
    /**
     * 获取当前类和父类所有字段 ，递归遍历
     * 
     * @param clazz
     *            遍历的类，不一定是哪个类
     * @param allFields
     *            记录所有字段的集合
     * @return 当前类和祖宗类的所有字段（不重复，包括注解名字一样的也变成一个）
     */
    private static List<Field> getFields(Class<?> clazz, List<Field> allFields) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            /*
             * Android Studio 会增加一个 $change 的变量 是增加了Instant
             * Run特性导致的，那就把此特性关闭就可以了(默认是开启的) 关闭方法：Settings> Build, Execution,
             * Deployment> Instant Run> Enable Instant Run to hot swap
             * code/resource changes on deploy(default enabled)（取消此选项）
             * 但是不是每个人都会去设置，所以还是代码处理
             */
            if (field.isSynthetic()) {
                continue;
            }
            if (field.getName().equalsIgnoreCase("serialVersionUID")) {
                continue;
            }
            if (field.getName().equals("CREATOR")) {
                continue;
            }

            // 变量名
            String filedName = field.getName();
            
            boolean isSame = false;
            for (Field field2 : allFields) {
                if ((field2.getName().equals(filedName))) {// 已经有了
                    isSame = true;
                    break;
                }
            }
            if (!isSame) {
                allFields.add(field);
            }
        }
        Class<?> parent = clazz.getSuperclass();

        if (parent != null && !parent.equals(Object.class) && !Enum.class.isAssignableFrom(parent)) {
            getFields(parent, allFields);
        }
        return allFields;
    }

    /**
     * 获取类的所以变量， 包括父类，爷爷类，到Object为止
     * 
     * @param clazz
     * @return
     */
    @SuppressLint("NewApi") 
    public static List<Field> getAllFields(Class<?> clazz) {

       
        List<Field> allFields = fieldMap.get(clazz);
        if(allFields == null){
        	 allFields = new ArrayList<Field>();
        	 allFields = getFields(clazz, allFields);
//        	 if(fieldMap.size() > 50){//存储的数据不要太大，否则浪费内存
//        		 fieldMap.remove(clazz);//多了就移除掉一条，保持最高50条
//        	 }
        	 fieldMap.put(clazz, allFields);
        }
        
        return allFields;
    }
    public static Class<?> getChildClass(Field field){
    	Class<?> childClass = childClasses.get(field);
    	if(childClass == null){
    		ParameterizedType type = (ParameterizedType) field.getGenericType();
            if (type.getActualTypeArguments() != null && type.getActualTypeArguments().length > 0) {
                childClass = (Class<?>) type.getActualTypeArguments()[0];
                if(childClass != null){
                	childClasses.put(field, childClass);
                }
            }
    	}
        return childClass; 
    }

    /**
     * 是否是常用数据类型，包括常用类的包装类Integer等和String
     *
     * @param classType
     *            需要比较的类
     * @return 是否是常用数据类型
     */
    public static boolean isCommonField(Class<?> classType) {
        boolean isCommonField = (classType.equals(int.class) || classType.equals(Integer.class)
                || classType.equals(float.class) || classType.equals(Float.class) || classType.equals(double.class)
                || classType.equals(Double.class) || classType.equals(long.class) || classType.equals(Long.class)
                || classType.equals(char.class) || classType.equals(String.class) || classType.equals(boolean.class) || classType
                .equals(Boolean.class));
        return isCommonField;
    }

}
