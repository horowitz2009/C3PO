package com.horowitz.seaport.dest;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Threshold;

public class Collect {
	public static void main(String[] args) {
		{
			FastBitmap fb1 = new FastBitmap("temp/collect_contract_new.bmp");
			fb1.toGrayscale();
			Threshold t = new Threshold(255);
			t.applyInPlace(fb1);
			fb1.toRGB();
			fb1.saveAsBMP("temp/collect_result.bmp");
			System.out.println("done.");
		}
		{
			FastBitmap fb1 = new FastBitmap("images/dest/collect_friend.bmp");
			fb1.toGrayscale();
			Threshold t = new Threshold(255);
			t.applyInPlace(fb1);
			fb1.toRGB();
			fb1.saveAsBMP("temp/collect_result2.bmp");
			System.out.println("done.");
		}
	}
}
