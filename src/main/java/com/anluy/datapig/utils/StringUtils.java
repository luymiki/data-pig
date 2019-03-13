package com.anluy.datapig.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils extends org.apache.commons.lang3.StringUtils {
    public static String join(Object[] array, String sep) {
        return join(array, sep, null);
    }

    public static String join(Collection list, String sep) {
        return join(list, sep, null);
    }

    public static String join(Collection list, String sep, String prefix) {
        Object[] array = (list == null) ? null : list.toArray();
        return join(array, sep, prefix);
    }

    public static String join(Object[] array, String sep, String prefix) {
        if (array == null) {
            return "";
        }
        int arraySize = array.length;
        if (arraySize == 0) {
            return "";
        }
        if (sep == null) {
            sep = "";
        }
        if (prefix == null) {
            prefix = "";
        }
        StringBuilder buf = new StringBuilder(prefix);
        for (int i = 0; i < arraySize; ++i) {
            if (i > 0) {
                buf.append(sep);
            }
            buf.append((array[i] == null) ? "" : array[i]);
        }
        return buf.toString();
    }

    public static String jsonJoin(String[] array) {
        int arraySize = array.length;
        int bufSize = arraySize * (array[0].length() + 3);
        StringBuilder buf = new StringBuilder(bufSize);
        for (int i = 0; i < arraySize; ++i) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append('"');
            buf.append(array[i]);
            buf.append('"');
        }
        return buf.toString();
    }

    public static boolean isNullOrEmpty(String s) {
        return ((s == null) || ("".equals(s)));
    }

    public static boolean inStringArray(String s, String[] array) {
        for (String x : array) {
            if (x.equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 集合对象转成json字符串
     *
     * @param obj
     * @return
     * @author yuan.lei
     */
    public static String toJSONArrStr(Object obj) {
        if (obj != null) {
            return JSONArray.toJSONString(obj);
        }
        return "";
    }

    /**
     * 非集合对象转成json字符串
     *
     * @param obj
     * @return
     * @author yuan.lei
     */
    public static String toJSONObjStr(Object obj) {
        if (obj != null) {
            return JSONObject.toJSONString(obj);
        }
        return "";
    }

    public static String createGivenLenStr(String oldString, int givenLen) {
        String tmpStr = "";
        int offset = givenLen - oldString.length();
        if (offset >= 0) {
            for (int i = 0; i < offset; i++) {
                tmpStr += "0";
            }
            tmpStr += oldString;
        } else {
            tmpStr = oldString.substring(0, givenLen);
        }
        return trim(tmpStr);
    }

    /**
     * @param value
     * @return
     * @Description 判断指定值是否存在
     * @Author wanghongbo 2017-12-12
     */
    public static boolean isSet(Object value) {
        return value != null && !value.equals("");
    }

    /**
     * @param array
     * @return
     * @Description 判断指定集合是否存在
     * @Author wanghongbo 2017-12-12
     */
    public static boolean isSet(Collection<?> array) {
        return (array != null && !array.isEmpty());
    }

    /**
     * @param value
     * @param compare
     * @return
     * @Description 判断两个值是否相等
     * @Author wanghongbo 2017-12-12
     */
    public static boolean isSet(Object value, Object compare) {
        return value != null && value.equals(compare);
    }

    /**
     * 对象转数组
     *
     * @param obj
     * @return
     * @author wanghongbo 2017-12-12
     */
    public byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /**
     * 数组转对象
     *
     * @param bytes
     * @return
     * @author wanghongbo 2017-12-12
     */
    public Object toObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    /**
     * 将字节数组输出为文件
     *
     * @param bytes
     * @param outputFile
     * @return
     * @author wanghongbo 2017-12-12
     */
    public File getFileFromBytes(byte[] bytes, String outputFile) {
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    /**
     * 文件转对象
     *
     * @param inputFile
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @author wanghongbo 2017-12-12
     */
    public Object fileToObject(File inputFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(inputFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object obj = ois.readObject();
        return obj;
    }

    /**
     * 截取指定长度的字符串内容，超出截取长度的以..补位
     *
     * @param str
     * @param toCount
     * @return
     */
    public static String subSimpleNavTitle(String str, int toCount) {
        int reInt = 0;
        String reStr = "";
        if (str == null) {
            return "";
        }
        char[] tempChar = str.toCharArray();
        for (int kk = 0; (kk < tempChar.length && toCount > reInt); kk++) {
            String s1 = str.valueOf(tempChar[kk]);
            byte[] b = s1.getBytes();
            reInt += b.length;
            if (toCount < reInt) {
                break;
            }
            reStr += tempChar[kk];
        }
        if (tempChar.length > toCount) {
            return reStr + "...";
        } else {
            return reStr;
        }
    }

    /**
     * 判断字符串是否可以转double数值
     *
     * @param str--字符串
     * @return
     * @author wanghongbo 2018-4-18
     */
    public static boolean canStrToDouble(String str) {
        boolean flag = true;
        if (StringUtils.isBlank(str.trim())) {
            return false;
        }
        try {
            Double.parseDouble(str);
            flag = true;
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 判断字符串是否可以转long数值
     *
     * @param str--字符串
     * @return
     * @author wanghongbo 2018-4-18
     */
    public static boolean canStrToLong(String str) {
        boolean flag = true;
        if (StringUtils.isBlank(str.trim())) {
            return false;
        }
        try {
            Long.parseLong(str);
            flag = true;
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 判断字符串是否可以转int数值
     *
     * @param str--字符串
     * @return
     * @author wanghongbo 2018-4-18
     */
    public static boolean canStrToInteger(String str) {
        boolean flag = true;
        if (StringUtils.isBlank(str.trim())) {
            return false;
        }
        try {
            Integer.parseInt(str);
            flag = true;
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * @author heyanwei
     * @description 首字母大写
     * @date 2018/5/25
     */
    public static String firstWordToUpper(String source) {
        if (isEmpty(source)) {
            return source;
        }
        if (source.length() == 1) {
            return source.substring(0, 1).toUpperCase();
        }
        return source.substring(0, 1).toUpperCase() + source.substring(1, source.length());

    }

    /**
     * @desc 判断字符串是否包含中文
     * @author yuan.lei
     * @date 2018/7/16
     */
    public static boolean isContainsChinese(String str) {
        Pattern pat = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher matcher = pat.matcher(str);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    /**
     * @param str
     * @return
     * @desc字符串去空格
     * @author wanghongbo
     * @date 2018/9/19
     */
    public static String trimString(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        return str.trim();
    }

    public static void main(String[] args) {
        String msg = "1测试2截取aa功b能是否有效，无效怎么办啊。？";
        String reMsg = subSimpleNavTitle(msg, 18);
        System.out.println(reMsg);
    }
}