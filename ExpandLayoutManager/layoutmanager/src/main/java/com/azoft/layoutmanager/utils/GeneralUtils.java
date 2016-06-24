package com.azoft.layoutmanager.utils;

public final class GeneralUtils {

    private GeneralUtils() {
    }

    public static boolean equals(final Object object1, final Object object2) {
        return null == object1 ? null == object2 : object1.equals(object2);
    }
}