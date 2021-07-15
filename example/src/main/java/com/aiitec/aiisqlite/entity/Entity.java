package com.aiitec.aiisqlite.entity;


import com.aiitec.openapi.db.utils.AiiJson;
import com.aiitec.openapi.db.utils.CombinationUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体类基类
 * @author Anthony
 */
public class Entity implements Cloneable {
    /**
     * 如果重写构造函数，记得要重新调用setDefaultNumber()这个方法，或者调用super()
     */
    public Entity() {
//        setDefaultNumber();
    }

    /**
     * 给数字类型赋值默认值,如果值是默认值0，就赋值我们规定的默认值-1，因为0会经常用到，所以不用0作默认值，如果已经赋值，则不再赋值
     * 
     */
    protected void setDefaultNumber() {

        for (Field field : CombinationUtil.getAllFields(getClass())) {
            try {
                // 判断是否是数字类型， 不考虑byte 和short
                if (field.getType().equals(int.class) || field.getType().equals(long.class)
                        || field.getType().equals(float.class) || field.getType().equals(double.class)
                        || field.getType().equals(Integer.class) || field.getType().equals(Long.class)
                        || field.getType().equals(Float.class) || field.getType().equals(Double.class)) {
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (Double.parseDouble(value.toString()) == 0) {
                        field.set(this, -1);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public String toString() {

        String toString = "";
        try {
            toString = new AiiJson().toJsonString(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toString;
    }

    /**
     * 对象的克隆方法， 伪深度克隆，也就是说对象里的除常用数据类型外的属性都是 Entity的子类就没问题，
     * 但是StringBuffer，HashMap这些东西就不行了
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        try {
            Object entity = super.clone();
            for (Field field : CombinationUtil.getAllFields(getClass())) {
                field.setAccessible(true);
                try {
                    // 对象
                    if (Entity.class.isAssignableFrom(field.getType())) {
                        Object value = field.get(this);
                        if (value != null) {
                            field.set(this, ((Entity) value).clone());
                        }
                    } else if (List.class.isAssignableFrom(field.getType())) {
                        List<Object> datas = new ArrayList<Object>();
                        List<Object> oldLists = (List<Object>) field.get(entity);
                        if (oldLists != null) {
                            for (Object childEntity : oldLists) {
                                if (childEntity != null) {
                                    if (Entity.class.isAssignableFrom(childEntity.getClass())) {
                                        if (childEntity != null) {
                                            datas.add(((Entity) childEntity).clone());
                                        }
                                    } else {
                                        datas.add(childEntity);
                                    }
                                }
                            }
                            field.set(this, datas);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return entity;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 两个相同属性的类的克隆 比如 Data1 和 Data2的属性完全一样，那么就可以这样 Data2 data2 =
     * data1.cloneTo(Data2.class) 当然这里考虑的常用的数据类型 和
     * 继承至这个类的对象，特殊的数据类型可能不支持，但是已经达到我的要求了 这个最好写在一个所有对象的父类里，要不然又有其他问题
     * 
     * @param clazz
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public <T> T cloneTo(Class<T> clazz) {
        T newObj = null;
        try {
            Object entity = super.clone();
            if (entity.getClass().equals(clazz)) {
                newObj = (T) entity;
            } else {
                newObj = clazz.newInstance();
                for (Field field : CombinationUtil.getAllFields(getClass())) {
                    field.setAccessible(true);
                    for (Field field2 : CombinationUtil.getAllFields(clazz)) {
                        if (field2.getName().equals(field.getName())) {
                            copyValue(field, field2, entity, newObj);
                        }
                    }
                }
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return newObj;
    }

    @SuppressWarnings("unchecked")
    private <T> void copyValue(Field field, Field field2, Object entity, T newObj) {
        try {
            field2.setAccessible(true);
            // Entity是当前类，也就是所有能互相复制的父类
            if (Entity.class.isAssignableFrom(field.getType())) {
                Entity child = (Entity) field.get(entity);
                if (child != null) {
                    field2.set(newObj, child.cloneTo(field2.getType()));
                }
            } else if (List.class.isAssignableFrom(field.getType())) {
                field.getType();
                Class<?> childClass2 = null;
                try {
                    ParameterizedType type = (ParameterizedType) field.getGenericType();
                    if (type.getActualTypeArguments() != null && type.getActualTypeArguments().length > 0) {
                        childClass2 = (Class<?>) type.getActualTypeArguments()[0];
                    }
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
                List<Object> datas = new ArrayList<Object>();
                List<Object> oldLists = (List<Object>) field.get(entity);
                if (oldLists != null && oldLists.size() > 0) {
                    for (Object childEntity : oldLists) {
                        if (childEntity != null) {
                            if (Entity.class.isAssignableFrom(childEntity.getClass())) {
                                datas.add(((Entity) childEntity).cloneTo(childClass2));
                            } else {
                                datas.add(childEntity);
                            }

                        }
                    }
                }
                field2.set(newObj, datas);
            } else {
                field2.set(newObj, field.get(entity));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }



}
