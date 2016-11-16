package com.horowitz.seaport.ocr;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Threshold;

public class ImageFilter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		filter("images/ships/new/Adventure1");
		filter("images/ships/new/BonaEsperanza1");
		filter("images/ships/new/Girmand1");
		filter("images/ships/new/MeagDelight1");
		filter("images/ships/new/HMSAssurance1");
		filter("images/ships/new/SanEsteban1");
		filter("images/ships/new/Santiago1");
		filter("images/ships/new/SaoCristovao1");
		filter("images/ships/new/Squirrel1");
		System.out.println("done.");
	}
	
	private static void filter(String filename) {
		
		try {
	    FastBitmap fb = new FastBitmap(filename + ".bmp");
	    if (fb.isRGB())
	    	fb.toGrayscale();
	    Threshold t = new Threshold(200);
	    t.applyInPlace(fb);
	    fb.toRGB();
	    fb.saveAsBMP(filename + "b.bmp");
    } catch (Exception e) {
	    e.printStackTrace();
    }
		
	}

}
