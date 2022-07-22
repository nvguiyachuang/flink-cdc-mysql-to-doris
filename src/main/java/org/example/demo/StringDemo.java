package org.example.demo;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class StringDemo {
    public static void main(String[] args) {
        String[] arr = {"a", "b", "c", "d", "e"};
        String[] strings = Arrays.copyOf(arr, 3);
        System.out.println(StringUtils.join(strings, ","));
    }
}
