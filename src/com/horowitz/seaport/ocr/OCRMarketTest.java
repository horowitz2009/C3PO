package com.horowitz.seaport.ocr;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.horowitz.commons.SimilarityImageComparator;
import com.horowitz.ocr.OCRB;

import Catalano.Imaging.FastBitmap;

public class OCRMarketTest {
	private OCRMarket ocrMarket;

	@Before
	public void setUp() throws Exception {
		ocrMarket = new OCRMarket(new SimilarityImageComparator(0.04, 2000));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testScanImage() {
		assertTrue(scanImage("264"));
		assertTrue(scanImage("288"));
		assertTrue(scanImage("330"));
		assertTrue(scanImage("276"));
	}
	
	private boolean scanImage(String number) {
		FastBitmap fb = new FastBitmap("ocr/ocrm/test/"+number+".bmp");
		String res = ocrMarket.scanImage(fb.toBufferedImage());
		System.err.println(res);
		return number.equals(res);
	}

}
