package com.anluy.datapig.utils;


import com.anluy.datapig.po.Column;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

/**
 * 类加载工具
 * @author hc.zeng 2017-2-26
 * @version 1.0
 * @see
 * @since Copyright &copy; 2016 <i>xinghuo.com</i>. All Rights Reserved
 */
public class ClassUtils
{
    private static Map<String,Field[]> CLASS_MAP = new Hashtable<String,Field[]>();

    private ClassUtils(){}
    
    /**
     * 根据类名称，加载类对象
     * @author hc.zeng 2017-2-26
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> loadClass(String className) throws ClassNotFoundException
    {
        return Class.forName(className);
    }
    
    /**
     * 根据类对象，加载属性
     * @author hc.zeng 2017-2-26
     * @param clazz
     * @return
     */
    private static Field[] loadClassFields(Class<?> clazz)
    {
        Field[] fields = clazz.getDeclaredFields();
        ArrayList<Field> fs = new ArrayList<Field>();
        for (Field field : fields)
        {
            if(!field.getName().startsWith("_"))
            {
                field.setAccessible(true);
                fs.add(field);
            }
        }
        return fs.toArray(new Field[fs.size()]);
    }
    
    
    /**
     * 根据类对象，加载属性
     * @author hc.zeng 2017-2-26
     * @param clazz
     * @return
     */
    public static Field[] getFields(Class<?> clazz)
    {
        if(CLASS_MAP.containsKey(clazz.getName())) {
            return CLASS_MAP.get(clazz.getName());
        } else
        {
            Field[] f = loadClassFields(clazz);
            CLASS_MAP.put(clazz.getName(), f);
            return f;
        }
    }
    /**
     * 根据对象，加载属性
     * @author hc.zeng 2017-2-26
     * @param o
     * @return
     */
    public static Field[] getFields(Object o)
    {
        return getFields(o.getClass());
    }
    
    /**
     * 根据类对象路径，加载属性
     * @author hc.zeng 2017-2-26
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public static Field[] getFields(String className) throws ClassNotFoundException
    {
        return getFields(Class.forName(className));
    }

    public static Column getColumn(Class<?> clazz, String fieldName)
    {
        String methodName = "get"+(fieldName.substring(0,1).toUpperCase()+fieldName.substring(1,fieldName.length()));
        Method method;
        try
        {
            method = clazz.getMethod(methodName);
            if(method==null ) {
                return null;
            }
            return method.getAnnotation(Column.class);
        }
        catch (NoSuchMethodException e)
        {
        }
        catch (SecurityException e)
        {
        }
        return null;
    }
}
