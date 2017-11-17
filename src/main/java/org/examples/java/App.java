package org.examples.java;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("prop: " + System.getProperty("GREETING") + System.getProperty("NAME"));
        System.out.println("env: " + System.getenv("GREETING") + System.getenv("NAME"));
    }
}
