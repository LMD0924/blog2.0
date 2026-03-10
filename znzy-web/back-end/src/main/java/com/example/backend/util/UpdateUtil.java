package com.example.backend.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

/**
 * 对象更新工具类
 * 用于在修改数据时，忽略源对象中为null或空字符串的字段，只复制有实际内容的字段到目标对象
 */
public class UpdateUtil {

    /**
     * 过滤掉不可写的属性（如 class）
     */
    private static String[] filterReadOnlyProperties(Object source, Set<String> propertyNames) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        Set<String> filteredNames = new HashSet<>();

        for (String propertyName : propertyNames) {
            // 检查属性是否可写
            if (src.isWritableProperty(propertyName)) {
                filteredNames.add(propertyName);
            }
        }
        return filteredNames.toArray(new String[0]);
    }

    /**
     * 获取对象中值为null或空字符串的字段名称
     * @param source 源对象
     * @return 字段名称数组
     */
    public static String[] getNullOrEmptyPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            // 跳过 class 属性
            if ("class".equals(pd.getName())) {
                continue;
            }

            String fieldName = pd.getName();
            Object srcValue = src.getPropertyValue(fieldName);

            // 如果是null，忽略
            if (srcValue == null) {
                emptyNames.add(fieldName);
            }
            // 如果是空字符串，也忽略
            else if (srcValue instanceof String && !StringUtils.hasText((String) srcValue)) {
                emptyNames.add(fieldName);
            }
        }
        return filterReadOnlyProperties(source, emptyNames);
    }

    /**
     * 复制非空属性 - 只复制source中不为null且不为空字符串的字段到target
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyNonNullProperties(Object source, Object target) {
        String[] ignoreProperties = getNullOrEmptyPropertyNames(source);
        BeanUtils.copyProperties(source, target, ignoreProperties);
    }

    /**
     * 获取有实际值的字段（非null且非空字符串）
     * @param source 源对象
     * @return 有实际值的字段名数组
     */
    public static String[] getNonEmptyPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> nonEmptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            // 跳过 class 属性
            if ("class".equals(pd.getName())) {
                continue;
            }

            String fieldName = pd.getName();
            Object srcValue = src.getPropertyValue(fieldName);

            // 如果有实际值（非null且非空字符串）
            if (srcValue != null) {
                if (srcValue instanceof String) {
                    if (StringUtils.hasText((String) srcValue)) {
                        nonEmptyNames.add(fieldName);
                    }
                } else {
                    nonEmptyNames.add(fieldName);
                }
            }
        }
        return nonEmptyNames.toArray(new String[0]);
    }

    /**
     * 判断对象是否所有字段都为空（null或空字符串）
     * @param object 检查的对象
     * @return 是否全为空
     */
    public static boolean isAllFieldsEmpty(Object object) {
        final BeanWrapper src = new BeanWrapperImpl(object);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        for (PropertyDescriptor pd : pds) {
            String fieldName = pd.getName();
            if ("class".equals(fieldName)) {
                continue;
            }

            Object srcValue = src.getPropertyValue(fieldName);

            // 如果有实际值（非null且非空字符串），返回false
            if (srcValue != null) {
                if (srcValue instanceof String) {
                    if (StringUtils.hasText((String) srcValue)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取有实际值的字段及其值（用于调试）
     * @param source 源对象
     * @return 非空字段的Map
     */
    public static java.util.Map<String, Object> getNonEmptyValues(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        java.util.Map<String, Object> values = new java.util.HashMap<>();

        for (PropertyDescriptor pd : pds) {
            String fieldName = pd.getName();
            if ("class".equals(fieldName)) {
                continue;
            }

            Object srcValue = src.getPropertyValue(fieldName);

            if (srcValue != null) {
                if (srcValue instanceof String) {
                    if (StringUtils.hasText((String) srcValue)) {
                        values.put(fieldName, srcValue);
                    }
                } else {
                    values.put(fieldName, srcValue);
                }
            }
        }
        return values;
    }
}