package com.horowitz.seaport.test;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import Catalano.Imaging.FastBitmap;

import com.horowitz.commons.ImageData;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.Settings;
import com.horowitz.seaport.ScreenScanner;

public class MatchTest {

	private ScreenScanner _scanner;
	private Settings _settings;

	@Before
	public void setup() {
		System.err.println("before");
		_settings = Settings.createSettings("seaport.properties");
		_scanner = new ScreenScanner(_settings);
	}

	@Test
	public void test() {
		System.err.println("test");

		try {
			ImageData id = _scanner.generateImageData("ships/HMSAssuranceF.bmp");
			BufferedImage screen = new FastBitmap("hmsass.bmp").toBufferedImage();
			Pixel p = _scanner.getMatcher().findMatch(id.getImage(), screen, Color.RED);
			assertTrue(p != null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}

	}

}
