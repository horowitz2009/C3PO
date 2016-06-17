package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

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

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;

public class BarrelsProtocol implements GameProtocol {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private ScreenScanner _scanner;
	private boolean _capture = false;

	private MouseRobot _mouse;

	private int cnt;
	private int blobMin;
	private int blobMax;

	public BarrelsProtocol(ScreenScanner scanner, MouseRobot mouse) throws IOException {
		blobMin = 15 * 20;
		blobMax = 28 * 32;
		_scanner = scanner;
		_mouse = mouse;
	}

	@Override
	public boolean preExecute() throws AWTException, IOException, RobotInterruptedException {
		return _scanner.ensureHome();
	}

	@Override
	public void update() {
	}

	@Override
	public void execute() throws RobotInterruptedException {
		// TODO HUNT BARRELS
		boolean debug = false;
		try {

			BufferedImage image = _scanner.getImageData("LAND5.bmp").getImage();

			Pixel rock = _scanner.getRock();
			int x1 = rock.x - 114;
			int x2 = _scanner.getBottomRight().x - 28;
			int y1 = rock.y - 225 + 32;
			int y1a = _scanner.getTopLeft().y + 32;
			y1 = Math.max(y1, y1a);

			Rectangle area = new Rectangle(x1, y1, Math.min(764, x2 - x1), 318);

			FastBitmap landFB = new FastBitmap(image.getSubimage(0, 263 - rock.y + y1, area.width, area.height));
			if (landFB.isRGB())
				landFB.toGrayscale();
			if (debug)
				landFB.saveAsBMP("LANDCUT.BMP");

			LOGGER.info("Barrels..." + blobMin + " - " + blobMax);
			// _scanner.captureArea(_barrelsArea, "barrelsArea.png", false);

			FastBitmap fb = new FastBitmap(new Robot().createScreenCapture(area));

			if (debug)
				fb.saveAsPNG("IMAGE.PNG");

			// FILTER BROWN
			ColorFiltering colorFiltering = new ColorFiltering(new IntRange(40, 250), new IntRange(65, 175), new IntRange(0,
			    110));
			colorFiltering.applyInPlace(fb);

			if (fb.isRGB())
				fb.toGrayscale();
			Threshold thr = new Threshold(70);// was 80
			thr.applyInPlace(fb);

			if (debug)
				fb.saveAsPNG("IMAGE2.png");

			// //

			BlobDetection bd = new BlobDetection(BlobDetection.Algorithm.EightWay);
			bd.setFilterBlob(true);
			bd.setMinArea(blobMin);
			bd.setMaxArea(blobMax);

			// OR
			Or or = new Or(landFB);
			or.applyInPlace(fb);
			if (debug)
				fb.saveAsPNG("IMAGE2OR.png");
			// //

			Threshold th = new Threshold(10);
			th.applyInPlace(fb);
			if (debug)
				fb.saveAsPNG("IMAGE2blurredTH.png");

			Xor xor = new Xor(landFB);
			xor.applyInPlace(fb);
			if (debug)
				fb.saveAsPNG("IMAGE2blurredTHXOR.png");

			// BLUR
			Blur blur = new Blur();
			blur.applyInPlace(fb);
			if (debug)
				fb.saveAsPNG("IMAGE2blurredTHXORRRRRRRRRR.png");
			// //
			th = new Threshold(10);
			th.applyInPlace(fb);
			if (debug)
				fb.saveAsPNG("IMAGE2blurredTHHHHHHHHHHHHH.png");

			List<Blob> blobs = bd.ProcessImage(fb);
			for (Blob blob : blobs) {
				cnt++;
				IntPoint c = blob.getCenter();
				System.out.println(c);
				Pixel p = new Pixel(c.y + area.x + 0, c.x + area.y + 0);
				LOGGER.info("BARREL: " + p);
				_mouse.click(p);
				_mouse.delay(50);

				p.x += 6;
				p.y += 6;
				_mouse.click(p);
				_mouse.delay(50);

				p.x -= 12;
				p.y -= 6;
				_mouse.click(p);
				_mouse.delay(150);
				if (_capture) {
				  _scanner.captureArea(new Rectangle(p.x - 21, p.y - 120, 51, 120), "barrels/barrels", true);
				}

			}
			LOGGER.info("BARRELS CNT: " + cnt);

		} catch (RobotInterruptedException e) {
			throw new RobotInterruptedException();
		} catch (Exception e) {
			LOGGER.severe("something went wrong in Barrels protocol...");
			e.printStackTrace();

		}

	}

	public boolean isCapture() {
		return _capture;
	}

	public void setCapture(boolean capture) {
		_capture = capture;
	}

	public int getBlobMin() {
		return blobMin;
	}

	public void setBlobMin(int blobMin) {
		this.blobMin = blobMin;
	}

	public int getBlobMax() {
		return blobMax;
	}

	public void setBlobMax(int blobMax) {
		this.blobMax = blobMax;
	}

}
