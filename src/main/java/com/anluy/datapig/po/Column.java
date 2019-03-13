package com.anluy.datapig.po;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD})
public abstract @interface Column {
    // Method descriptor #5 ()Ljava/lang/String;
    public abstract String name() default "";

    // Method descriptor #9 ()Z
    public abstract boolean unique() default false;

    // Method descriptor #9 ()Z
    public abstract boolean nullable() default true;

    // Method descriptor #9 ()Z
    public abstract boolean insertable() default true;

    // Method descriptor #9 ()Z
    public abstract boolean updatable() default true;

    // Method descriptor #5 ()Ljava/lang/String;
    public abstract String columnDefinition() default "";

    // Method descriptor #5 ()Ljava/lang/String;
    public abstract String table() default "";

    // Method descriptor #18 ()I
    public abstract int length() default (int) 255;

    // Method descriptor #18 ()I
    public abstract int precision() default (int) 0;

    // Method descriptor #18 ()I
    public abstract int scale() default (int) 0;
}
