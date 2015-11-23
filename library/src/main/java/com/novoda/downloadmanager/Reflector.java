package com.novoda.downloadmanager;

import java.lang.reflect.Field;

final class Reflector {

    private static final String CLASS_NAME = "com.novoda.downloadmanager.Authority";
    private static final String CONSTANT_FULL_NAME = CLASS_NAME + ".AUTHORITY";

    private static String cachedAuthority;

    private Reflector() {
        // static helper class
    }

    static String reflectAuthority() {
        if (cachedAuthority == null) {
            try {
                Class<?> authorityClass = Class.forName(CLASS_NAME);
                Field authorityField = authorityClass.getDeclaredField("AUTHORITY");
                Object o = authorityField.get(authorityClass);
                cachedAuthority = (String) o;
            } catch (ClassNotFoundException e) {
                throwNoAuthorityClass();
            } catch (NoSuchFieldException e) {
                throwNoAuthorityField();
            } catch (IllegalAccessException e) {
                throwHiddenFieldName();
            } catch (ClassCastException e) {
                throwNotAString();
            }
        }
        return cachedAuthority;
    }

    private static void throwNotAString() {
        throw new IllegalArgumentException(CONSTANT_FULL_NAME + " is not a string");
    }

    private static void throwHiddenFieldName() {
        throw new IllegalStateException(CONSTANT_FULL_NAME + " is not a visible field");
    }

    private static void throwNoAuthorityField() {
        throw new NullPointerException(CONSTANT_FULL_NAME + " was not defined");
    }

    private static void throwNoAuthorityClass() {
        throw new NullPointerException(CLASS_NAME + " is not defined. See the documentation.");
    }
}