package com.anluy.datapig.plugin.utils;

import com.anluy.datapig.plugin.core.DataPigException;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-31 15:09
 */

public abstract class Formatter<T> implements IFormatter {
    private String pattern;

    public Formatter(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * 时间对象格式化为字符串
     *
     * @param pattern
     * @return
     */
    public static Formatter DATE_TO_STRING(String pattern) {
        return new Formatter<String>(pattern) {
            @Override
            public String formatter(Object obj) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(this.getPattern());
                try {
                    if (obj != null && obj instanceof Date) {
                        return simpleDateFormat.format(obj);
                    } else if (obj != null) {
                        throw new DataPigException(String.format("不能把数据[%s]格式化为时间", obj.toString()));
                    }
                    return null;
                } catch (Exception e) {
                    throw new DataPigException(String.format("不能把数据[%s]格式化为时间", obj.toString()), e);
                }
            }
        };
    }

    /**
     * 字符串格式化为时间对象
     *
     * @param pattern
     * @return
     */
    public static Formatter STRING_TO_DATE(String pattern) {
        return new Formatter<Date>(pattern) {
            @Override
            public Date formatter(Object obj) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(this.getPattern());
                try {
                    if (obj != null && obj instanceof String) {
                        return simpleDateFormat.parse((String) obj);
                    }
                    if (obj != null && (obj instanceof Integer || obj instanceof Long)) {
                        Date date = new Date();
                        date.setTime((long) obj);
                        return date;
                    } else if (obj != null) {
                        throw new DataPigException(String.format("不能把数据[%s]格式化为时间", obj.toString()));
                    }
                    return null;
                } catch (Exception e) {
                    throw new DataPigException(String.format("不能把数据[%s]格式化为时间", obj.toString()), e);
                }
            }
        };
    }

    /**
     * 将对象拼接成一个字符串
     *
     * @param pattern
     * @return
     */
    public static Formatter JOIN(String pattern) {
        return new Formatter<String>(pattern) {
            @Override
            public String formatter(Object obj) {
                try {
                    int i = 0;
                    if (obj != null && obj instanceof Collection) {
                        StringBuffer sb = new StringBuffer();
                        for (Object o : (Collection) obj) {
                            if (i > 0) {
                                sb.append(pattern);
                            }
                            i++;
                            sb.append(o);
                        }
                        return sb.toString();
                    }
                    if (obj != null && obj instanceof String[]) {
                        StringBuffer sb = new StringBuffer();
                        for (String o : (String[]) obj) {
                            if (i > 0) {
                                sb.append(pattern);
                            }
                            i++;
                            sb.append(o);
                        }
                        return sb.toString();
                    } else if (obj != null) {
                        throw new DataPigException(String.format("不能把数据[%s]按[%s]进行拼接", obj.toString(), this.getPattern()));
                    }
                    return null;
                } catch (Exception e) {
                    throw new DataPigException(String.format("不能把数据[%s]按[%s]进行拼接", obj.toString(), this.getPattern()), e);
                }
            }
        };
    }

    /**
     * 将字符串分割为字符串数组
     *
     * @param pattern
     * @return
     */
    public static Formatter SPLIT(String pattern) {
        return new Formatter<String[]>(pattern) {
            @Override
            public String[] formatter(Object obj) {
                try {
                    if (obj != null && obj instanceof String) {
                        return ((String) obj).split(pattern);
                    } else if (obj != null) {
                        throw new DataPigException(String.format("不能把数据[%s]按[%s]进行分割", obj.toString(), this.getPattern()));
                    }
                    return null;
                } catch (Exception e) {
                    throw new DataPigException(String.format("不能把数据[%s]按[%s]进行分割", obj.toString(), this.getPattern()), e);
                }
            }
        };
    }
}
