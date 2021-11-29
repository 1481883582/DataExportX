package com.bx.change.tool;

import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.ClsEnumConstantImpl;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link PsiAnnotationMemberValue}工具
 * @author xiehai
 * @date 2021/06/25 19:30
 * @Copyright (c) wisewe co.,ltd
 */
public class PsiAnnotationMemberValueUtil {
    private PsiAnnotationMemberValueUtil() {

    }

    /**
     * 获取注解属性
     * @param annotation 注解
     * @param attribute  属性名
     * @return 属性值
     */
    public static Object value(PsiAnnotation annotation, String attribute) {
        if (Objects.isNull(annotation)) {
            return null;
        }

        return value(annotation.findAttributeValue(attribute));
    }

    public static Object value(PsiAnnotationMemberValue v) {
        if (Objects.isNull(v)) {
            return null;
        }

        if (v instanceof PsiArrayInitializerMemberValue) {
            return
                Arrays.stream(((PsiArrayInitializerMemberValue) v).getInitializers())
                    .map(PsiAnnotationMemberValueUtil::value)
                    .toArray();
        }

        if (v instanceof PsiExpression) {
            return value((PsiExpression) v);
        }

        return v.getText();
    }

    static Object value(PsiExpression value) {
        if (value instanceof PsiLiteralExpression) {
            return ((PsiLiteralExpression) value).getValue();
        }

        if (value instanceof PsiReferenceExpression) {
            PsiElement resolve = ((PsiReferenceExpression) value).resolve();
            if (resolve instanceof PsiField) {
                return Optional.ofNullable(((PsiField) resolve).computeConstantValue())
                    .orElseGet(resolve::getText);
            }

            return value((PsiExpression) resolve);
        }

        return value.getText();
    }

    public static Object getArrayFirstValue(PsiAnnotation annotation, String attribute) {
        Object value = value(annotation, attribute);
        if (Objects.nonNull(value) && value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            if (array.length > 0) {
                return format(array[0]);
            }

            return null;
        }

        return format(value);
    }

    static Object format(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String || value.getClass().isPrimitive()) {
            return value;
        }

        if (value instanceof ClsEnumConstantImpl) {
            return ((ClsEnumConstantImpl) value).getName();
        }

        // 注解类型忽略
        return null;
    }
}
