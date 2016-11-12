package com.horowitz.seaport.ocr;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.horowitz.commons.SimilarityImageComparator;
import com.horowitz.ocr.OCRB;

import Catalano.Imaging.FastBitmap;

public class OCRMarketBonusTest {
	private OCRMarketBonus ocrMarketBonus;

	@Before
	public void setUp() throws Exception {
		ocrMarketBonus = new OCRMarketBonus(new SimilarityImageComparator(0.04, 2000));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testScanImage() {
		assertTrue(scanImage("1"));
		assertTrue(scanImage("2"));
		assertTrue(scanImage("4"));
		assertTrue(scanImage("6"));
	}
	
	private boolean scanImage(String number) {
		FastBitmap fb = new FastBitmap("ocr/ocrb/test/"+number+".bmp");
		String res = ocrMarketBonus.scanImage(fb.toBufferedImage());
		System.err.println(res);
		return number.equals(res);
	}

}
