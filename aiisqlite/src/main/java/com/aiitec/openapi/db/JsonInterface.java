package com.aiitec.openapi.db;

import java.util.List;

/**
 * the object in another object is be a json to save, so you can use gson or other json parse tool to parse,
 * but i think not necessary, because you can use the default parse: AiiJson, this interface just for extend
 * 对象中的对象是用json保存的，这个接口是为了扩展使用第三方json 解析，一般只要用默认的AiiJson就行了
 */
public interface JsonInterface {
	
	public String toJsonString(Object t);
    public <T> T parseObject(String json, Class<T> clazz);

   
    /**
     * 把JSON组成List集合
     * 
     * @param json json字符串
     * @param entityClazz 集合的子选项类 比如要转换成List&#60;User&#62; 那么就得传User.class， 而不是List.class
     * @return 转换后的List
     *
     */
    public <T> List<T> parseArray(String json, Class<T> entityClazz);
       
}
