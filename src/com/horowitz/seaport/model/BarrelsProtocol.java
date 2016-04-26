package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import Catalano.Core.IntPoint;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BinaryOpening;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Filters.Difference;
import Catalano.Imaging.Filters.Erosion;
import Catalano.Imaging.Filters.Threshold;
import Catalano.Imaging.Tools.Blob;

import com.horowitz.commons.MotionDetector;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;

public class BarrelsProtocol implements GameProtocol {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private ScreenScanner _scanner;
	private MouseRobot _mouse;

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
			for (int i = 0; i < 3; i++) {
				bis.add(r.createScreenCapture(area));
				Thread.sleep(750);
			}
			
      FastBitmap fb0 = new FastBitmap(bis.get(0));
      fb0.saveAsPNG("IMAGE.PNG");
      
      //ColorFiltering cf = new ColorFiltering(red, green, blue)
      
      
      
      
      
      
      
			MotionDetector motionDetector = new MotionDetector();
			List<Blob> blobs = motionDetector.detect(bis.get(0), bis.get(1), 20*20);
			for (Blob blob : blobs) {
	      System.out.println(blob);
      }
			LOGGER.info("DONE......");

			int i = 5;
			FastBitmap fb = new FastBitmap(bis.get(0));
			FastBitmap fb2 = new FastBitmap(bis.get(1));
			fb.toGrayscale();
			fb2.toGrayscale();

			Difference difference = new Difference(fb);
			Threshold threshold = new Threshold(i);
			Erosion erosion = new Erosion(1);
			BinaryOpening opening = new BinaryOpening(2);

			difference.applyInPlace(fb2);
			threshold.applyInPlace(fb2);
			fb2.saveAsPNG("outputAAA" + i + ".png");
			// ImageIO.write(fb2.toBufferedImage(), "PNG", new File("outputAAA" + i + ".png"));
			// erosion.applyInPlace(fb2);

		} catch (Throwable t) {
			LOGGER.severe("something went wrong in Barrels protocol...");

		}

	}
}
