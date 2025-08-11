package com.toastie01.casino.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Utility methods for array and collection operations.
 * Provides type conversion and shuffling functionality for the Casino mod.
 */
public final class ArrayHelper {

    private ArrayHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Convert Byte array to primitive byte array.
     * 
     * @param array the Byte array to convert
     * @return primitive byte array, or null if input is null
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
     * Convert primitive byte array to Byte array.
     * 
     * @param array the primitive byte array to convert
     * @return Byte array, or null if input is null
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
     * Shuffle an array using Fisher-Yates algorithm.
     * 
     * @param array the array to shuffle in place
     */
    public static void shuffle(Object[] array) {
        shuffle(array, new Random());
    }

    /**
     * Shuffle an array using Fisher-Yates algorithm with specified Random.
     * 
     * @param array the array to shuffle in place
     * @param random the Random instance to use
     */
    public static void shuffle(Object[] array, Random random) {
        if (array == null || array.length <= 1) return;
        
        for (int i = array.length - 1; i > 0; i--) {
            int randomIndex = random.nextInt(i + 1);
            
            // Swap elements
            Object temp = array[i];
            array[i] = array[randomIndex];
            array[randomIndex] = temp;
        }
    }
    
    /**
     * Shuffle a list using Collections.shuffle.
     * 
     * @param list the list to shuffle
     * @param <T> the type of elements in the list
     */
    public static <T> void shuffle(List<T> list) {
        Collections.shuffle(list);
    }
    
    /**
     * Shuffle a list with specified Random.
     * 
     * @param list the list to shuffle
     * @param random the Random instance to use
     * @param <T> the type of elements in the list
     */
    public static <T> void shuffle(List<T> list, Random random) {
        Collections.shuffle(list, random);
    }
}
