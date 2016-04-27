package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.horowitz.commons.ImageManager;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;

import Catalano.Core.IntPoint;
import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Blur;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Filters.Or;
import Catalano.Imaging.Filters.Threshold;
import Catalano.Imaging.Filters.Xor;
import Catalano.Imaging.Tools.Blob;
import Catalano.Imaging.Tools.BlobDetection;

public class BarrelsProtocol implements GameProtocol {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private ScreenScanner _scanner;
	
	private MouseRobot _mouse;
	
	private int cnt;

	public BarrelsProtocol(ScreenScanner scanner, MouseRobot mouse) throws IOException {
		_scanner = scanner;
		_mouse = mouse;
	}

	@Override
	public boolean preExecute() throws AWTException, IOException, RobotInterruptedException {
		return _scanner.ensureHome();
	}

	@Override
	public void update() {
		Pixel rock = _scanner.getRock();

	}

	@Override
	public void execute() throws RobotInterruptedException {
		// TODO HUNT BARRELS
		LOGGER.info("Barrels...");
		try {
			Rectangle area = new Rectangle(_scanner.getBarrelsArea());
			_scanner.captureArea(_scanner.getBarrelsArea(), "barrelsArea.png", false);

			Robot r = new Robot();
			List<BufferedImage> bis = new ArrayList<BufferedImage>(3);
			for (int i = 0; i < 1; i++) {
				bis.add(r.createScreenCapture(area));
				Thread.sleep(750);
			}

			FastBitmap fb = new FastBitmap(bis.get(0));
			
			//fb.saveAsPNG("IMAGE.PNG");

			//FILTER BROWN
			ColorFiltering colorFiltering = new ColorFiltering(new IntRange(40, 250), new IntRange(65, 175),
			    new IntRange(0, 110));
			colorFiltering.applyInPlace(fb);

			if (fb.isRGB())
				fb.toGrayscale();
			Threshold thr = new Threshold(70);// was 80
			thr.applyInPlace(fb);

			//fb.saveAsPNG("IMAGE2.png");
			
			////

			BlobDetection bd = new BlobDetection(BlobDetection.Algorithm.EightWay);
			bd.setFilterBlob(true);
			bd.setMinArea(15 * 20);
			bd.setMaxArea(28 * 32);

			//BLUR
			Blur blur = new Blur();
			blur.applyInPlace(fb);
			//fb.saveAsPNG("IMAGE2blurred.png");
			////
			
			
			//OR
			FastBitmap fbLand = new FastBitmap(ImageIO.read(ImageManager.getImageURL("LAND.bmp")));
			fbLand.toGrayscale();
			Or or = new Or(fbLand);
			or.applyInPlace(fb);
			//fb.saveAsPNG("IMAGE2OR.png");
			////
			
			
			Threshold th = new Threshold(10);
			th.applyInPlace(fb);
			//fb.saveAsPNG("IMAGE2blurredTH.png");
			
			Xor xor = new Xor(fbLand);
			xor.applyInPlace(fb);
			//fb.saveAsPNG("IMAGE2blurredTHXOR.png");

			System.out.println("111111111111");
			List<Blob> blobs = bd.ProcessImage(fb);
			for (Blob blob : blobs) {
				cnt++;
				IntPoint c = blob.getCenter();
				System.out.println(c);
				Pixel p = new Pixel(c.y + area.x + 0, c.x + area.y + 0 );
//				Pixel p = new Pixel(c.y + area.x + _scanner.getTopLeft().x, c.x + area.y + _scanner.getTopLeft().y );
//				Pixel p = new Pixel(c.y + _scanner.getTopLeft().x, c.x + _scanner.getTopLeft().y );
				LOGGER.info("BARREL: " + p);
				_mouse.click(p);
				_mouse.delay(300);
			}
			LOGGER.info("BARRELS CNT: " + cnt);



			/*
			 * 
			 * MotionDetector motionDetector = new MotionDetector(); List<Blob> blobs = motionDetector.detect(bis.get(0), bis.get(1), 20*20); for (Blob blob : blobs) { System.out.println(blob); }
			 * 
			 * int i = 5; FastBitmap fb = new FastBitmap(bis.get(0)); FastBitmap fb2 = new FastBitmap(bis.get(1)); fb.toGrayscale(); fb2.toGrayscale();
			 * 
			 * Difference difference = new Difference(fb); Threshold threshold = new Threshold(i); Erosion erosion = new Erosion(1); BinaryOpening opening = new BinaryOpening(2);
			 * 
			 * difference.applyInPlace(fb2); threshold.applyInPlace(fb2); fb2.saveAsPNG("outputAAA" + i + ".png"); // ImageIO.write(fb2.toBufferedImage(), "PNG", new File("outputAAA" + i + ".png")); //
			 * erosion.applyInPlace(fb2);
			 */
			LOGGER.info("DONE......");
		} catch (Throwable t) {
			LOGGER.severe("something went wrong in Barrels protocol...");
			t.printStackTrace();

		}

	}
}
