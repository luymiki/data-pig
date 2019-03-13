package com.anluy.datapig.dao;

import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-20 19:22
 */


public class PropertyUtils {
    public PropertyUtils() {
    }

    private static void handleException(Exception e) {
        ReflectionUtils.handleReflectionException(e);
    }

    public static void clearDescriptors() {
        org.apache.commons.beanutils.PropertyUtils.clearDescriptors();
    }

    public static void copyProperties(Object dest, Object orig) {
        try {
            org.apache.commons.beanutils.PropertyUtils.copyProperties(dest, orig);
        } catch (Exception var3) {
            handleException(var3);
        }

    }

    public static Map describe(Object bean) {
        if(bean instanceof Map) {
            return (Map)bean;
        } else {
            try {
                return org.apache.commons.beanutils.PropertyUtils.describe(bean);
            } catch (Exception var2) {
                handleException(var2);
                return null;
            }
        }
    }

    public static Object getIndexedProperty(Object bean, String name, int index) {
        try {
            return org.apache.commons.beanutils.PropertyUtils.getIndexedProperty(bean, name, index);
        } catch (Exception var4) {
            handleException(var4);
            return null;
        }
    }

    public static Object getIndexedProperty(Object bean, String name) {
        try {
            return org.apache.commons.beanutils.PropertyUtils.getIndexedProperty(bean, name);
        } catch (Exception var3) {
            handleException(var3);
            return null;
        }
    }

    public static Object getMappedProperty(Object bean, String name, String key) {
        try {
            return org.apache.commons.beanutils.PropertyUtils.getMappedProperty(bean, name, key);
        } catch (Exception var4) {
            handleException(var4);
            return null;
        }
    }

    public static Object getMappedProperty(Object bean, String name) {
        try {
            return org.apache.commons.beanutils.PropertyUtils.getMappedProperty(bean, name);
        } catch (Exception var3) {
            handleException(var3);
            return null;
        }
    }

    public static Object getNestedProperty(Object bean, String name) {
        try {
            return org.apache.commons.beanutils.PropertyUtils.getNestedProperty(bean, name);
        } catch (Exception var3) {
            handleException(var3);
            return null;
        }
    }

    public static Object getProperty(Object bean, String name) {
        try {
            return org.apache.commons.beanutils.PropertyUtils.getProperty(bean, name);
        } catch (Exception var3) {
            handleException(var3);
            return null;
        }
    }

    public static PropertyDescriptor getPropertyDescriptor(Object bean, String name) {
        try {
            return org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptor(bean, name);
        } catch (Exception var3) {
            handleException(var3);
            return null;
        }
    }

    public static PropertyDescriptor[] getPropertyDescriptors(Class beanClass) {
        return org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptors(beanClass);
    }

    public static PropertyDescriptor[] getPropertyDescriptors(Object bean) {
        return org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptors(bean);
    }

    public static Class getPropertyEditorClass(Object bean, String name) {
        try {
            return org.apache.commons.beanutils.PropertyUtils.getPropertyEditorClass(bean, name);
        } catch (Exception var3) {
            handleException(var3);
            return null;
        }
    }

    public static Class getPropertyType(Object bean, String name) {
        try {
            return org.apache.commons.beanutils.PropertyUtils.getPropertyType(bean, name);
        } catch (Exception var3) {
            handleException(var3);
            return null;
        }
    }

    public static Method getReadMethod(PropertyDescriptor descriptor) {
        return org.apache.commons.beanutils.PropertyUtils.getReadMethod(descriptor);
    }

    public static Object getSimpleProperty(Object bean, String name) {
        try {
            return org.apache.commons.beanutils.PropertyUtils.getSimpleProperty(bean, name);
        } catch (Exception var3) {
            handleException(var3);
            return null;
        }
    }

    public static Method getWriteMethod(PropertyDescriptor descriptor) {
        return org.apache.commons.beanutils.PropertyUtils.getWriteMethod(descriptor);
    }

    public static boolean isReadable(Object bean, String name) {
        return org.apache.commons.beanutils.PropertyUtils.isReadable(bean, name);
    }

    public static boolean isWriteable(Object bean, String name) {
        return org.apache.commons.beanutils.PropertyUtils.isWriteable(bean, name);
    }

    public static void setIndexedProperty(Object bean, String name, int index, Object value) {
        try {
            org.apache.commons.beanutils.PropertyUtils.setIndexedProperty(bean, name, index, value);
        } catch (Exception var5) {
            handleException(var5);
        }

    }

    public static void setIndexedProperty(Object bean, String name, Object value) {
        try {
            org.apache.commons.beanutils.PropertyUtils.setIndexedProperty(bean, name, value);
        } catch (Exception var4) {
            handleException(var4);
        }

    }

    public static void setMappedProperty(Object bean, String name, Object value) {
        try {
            org.apache.commons.beanutils.PropertyUtils.setMappedProperty(bean, name, value);
        } catch (Exception var4) {
            handleException(var4);
        }

    }

    public static void setMappedProperty(Object bean, String name, String key, Object value) {
        try {
            org.apache.commons.beanutils.PropertyUtils.setMappedProperty(bean, name, key, value);
        } catch (Exception var5) {
            handleException(var5);
        }

    }

    public static void setNestedProperty(Object bean, String name, Object value) {
        try {
            org.apache.commons.beanutils.PropertyUtils.setNestedProperty(bean, name, value);
        } catch (Exception var4) {
            handleException(var4);
        }

    }

    public static void setProperty(Object bean, String name, Object value) {
        try {
            org.apache.commons.beanutils.PropertyUtils.setProperty(bean, name, value);
        } catch (Exception var4) {
            handleException(var4);
        }

    }

    public static void setSimpleProperty(Object bean, String name, Object value) {
        try {
            org.apache.commons.beanutils.PropertyUtils.setSimpleProperty(bean, name, value);
        } catch (Exception var4) {
            handleException(var4);
        }

    }
}