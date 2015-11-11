package com.horowitz.seaport;

import Catalano.Imaging.Color;

public class ColorTest {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    Color c = Color.Black;
    System.out.println(c.r);
    System.out.println(c.g);
    System.out.println(c.b);
    c = new Color(0);
    System.out.println(c.r);
    System.out.println(c.g);
    System.out.println(c.b);
    
    System.out.println(c.r);
    System.out.println(c.g);
    System.out.println(c.b);
    
    
//    int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
//        * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
//        + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
//        * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
//        + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF))
//        * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));

  }

}
