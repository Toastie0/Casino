package com.ombremoon.playingcards.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Utility methods for array operations
 */
public class ArrayHelper {

    /**
     * Convert Byte array to primitive byte array
     */
    public static byte[] toPrimitive(Byte[] array) {
        if (array == null) return null;
        if (array.length == 0) return new byte[0];
        
        byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Convert primitive byte array to Byte array
     */
    public static Byte[] toObject(byte[] array) {
        if (array == null) return null;
        if (array.length == 0) return new Byte[0];
        
        Byte[] result = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Shuffle an array using Fisher-Yates algorithm
     */
    public static void shuffle(Object[] array) {
        shuffle(array, new Random());
    }

    /**
     * Shuffle an array using Fisher-Yates algorithm with specified Random
     */
    public static void shuffle(Object[] array, Random random) {
        for (int i = array.length - 1; i > 0; i--) {
            int randomIndex = random.nextInt(i + 1);
            
            // Swap elements
            Object temp = array[i];
            array[i] = array[randomIndex];
            array[randomIndex] = temp;
        }
    }
    
    /**
     * Shuffle a list using Collections.shuffle (more standard approach)
     */
    public static <T> void shuffle(List<T> list) {
        Collections.shuffle(list);
    }
    
    /**
     * Shuffle a list with specified Random
     */
    public static <T> void shuffle(List<T> list, Random random) {
        Collections.shuffle(list, random);
    }
}
