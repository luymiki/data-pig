import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-20 19:28
 */


public class Ognl {
    public Ognl() {
    }

    public static boolean isNotNull(Object o) throws IllegalArgumentException {
        if(o == null) {
            return false;
        }
        return true;
    }
    public static boolean isEmpty(Object o) throws IllegalArgumentException {
        if(o == null) {
            return true;
        } else {
            if(o instanceof String) {
                if(((String)o).length() == 0) {
                    return true;
                }
            } else if(o instanceof Collection) {
                if(((Collection)o).isEmpty()) {
                    return true;
                }
            } else if(o.getClass().isArray()) {
                if(Array.getLength(o) == 0) {
                    return true;
                }
            } else {
                if(!(o instanceof Map)) {
                    return false;
                }

                if(((Map)o).isEmpty()) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean isNotEmpty(Object o) {
        return !isEmpty(o);
    }

    public static boolean isNotBlank(Object o) {
        return !isBlank(o);
    }

    public static boolean isNumber(Object o) {
        if(o == null) {
            return false;
        } else if(o instanceof Number) {
            return true;
        } else if(o instanceof String) {
            String str = (String)o;
            if(str.length() == 0) {
                return false;
            } else if(str.trim().length() == 0) {
                return false;
            } else {
                try {
                    Double.parseDouble(str);
                    return true;
                } catch (NumberFormatException var3) {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public static boolean isBlank(Object o) {
        if(o == null) {
            return true;
        } else if(o instanceof String) {
            String str = (String)o;
            return isBlank(str);
        } else {
            return false;
        }
    }

    public static boolean isBlank(String str) {
        if(str != null && str.length() != 0) {
            for(int i = 0; i < str.length(); ++i) {
                if(!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean equals(String v1, String v2) {
        return isEmpty(v1)?false:v1.equals(v2);
    }

    public static boolean equalsIgnoreCase(String v1, String v2) {
        return isEmpty(v1)?false:v1.equalsIgnoreCase(v2);
    }

    public static boolean isNotEquals(String v1, String v2) {
        return !equals(v1, v2);
    }
}