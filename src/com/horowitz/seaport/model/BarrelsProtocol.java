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

	private Rectangle _barrelsArea;

	private FastBitmap _landFB;

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
	}

	@Override
	public void execute() throws RobotInterruptedException {
		// TODO HUNT BARRELS

		try {

			Pixel rock = _scanner.getRock();
			Rectangle area = new Rectangle();
			int x1 = rock.x - 114;
			int x2 = _scanner.getBottomRight().x - 28;

			int y1 = rock.y - 225;
			int y1a = _scanner.getTopLeft().y;
			y1 = Math.max(y1, y1a);

			area.width = x2 - x1;
			area.x = x1;
			area.y = y1;
			area.height = 350;
			_barrelsArea = new Rectangle(area);

			int rockOffset = rock.y - y1;

			BufferedImage image = ImageIO.read(ImageManager.getImageURL("LAND4.bmp"));

			int xx = 166 - 114;
			int yy = 263 - rockOffset;
			image = image.getSubimage(xx, yy, area.width, area.height);

			_landFB = new FastBitmap(image);
			if (_landFB.isRGB())
				_landFB.toGrayscale();
			//_landFB.saveAsBMP("LANDCUT.BMP");

			LOGGER.info("Barrels...");
			area = new Rectangle(_barrelsArea);
			//_scanner.captureArea(_barrelsArea, "barrelsArea.png", false);

			Robot r = new Robot();
			List<BufferedImage> bis = new ArrayList<BufferedImage>(3);
			for (int i = 0; i < 1; i++) {
				bis.add(r.createScreenCapture(area));
				//Thread.sleep(750);
			}

			FastBitmap fb = new FastBitmap(bis.get(0));

			//fb.saveAsPNG("IMAGE.PNG");

			// FILTER BROWN
			ColorFiltering colorFiltering = new ColorFiltering(new IntRange(40, 250), new IntRange(65, 175), new IntRange(0,
			    110));
			colorFiltering.applyInPlace(fb);

			if (fb.isRGB())
				fb.toGrayscale();
			Threshold thr = new Threshold(70);// was 80
			thr.applyInPlace(fb);

			//fb.saveAsPNG("IMAGE2.png");

			// //

			BlobDetection bd = new BlobDetection(BlobDetection.Algorithm.EightWay);
			bd.setFilterBlob(true);
			bd.setMinArea(20 * 20);
			bd.setMaxArea(28 * 32);

			// // BLUR
			// Blur blur = new Blur();
			// blur.applyInPlace(fb);
			// fb.saveAsPNG("IMAGE2blurred.png");
			// // //

			// OR
			// FastBitmap fbLand = new FastBitmap(ImageIO.read(ImageManager.getImageURL("LAND.bmp")));
			// _landFB.toGrayscale();
			Or or = new Or(_landFB);
			or.applyInPlace(fb);
			//fb.saveAsPNG("IMAGE2OR.png");
			// //

			Threshold th = new Threshold(10);
			th.applyInPlace(fb);
			//fb.saveAsPNG("IMAGE2blurredTH.png");

			Xor xor = new Xor(_landFB);
			xor.applyInPlace(fb);
			//fb.saveAsPNG("IMAGE2blurredTHXOR.png");

			// BLUR
			Blur blur = new Blur();
			blur.applyInPlace(fb);
			//fb.saveAsPNG("IMAGE2blurredTHXORRRRRRRRRR.png");
			// //
			th = new Threshold(10);
			th.applyInPlace(fb);
			//fb.saveAsPNG("IMAGE2blurredTHHHHHHHHHHHHH.png");

			System.out.println("111111111111");
			List<Blob> blobs = bd.ProcessImage(fb);
			for (Blob blob : blobs) {
				cnt++;
				IntPoint c = blob.getCenter();
				System.out.println(c);
				Pixel p = new Pixel(c.y + area.x + 0, c.x + area.y + 0);
				// Pixel p = new Pixel(c.y + area.x + _scanner.getTopLeft().x, c.x + area.y + _scanner.getTopLeft().y );
				// Pixel p = new Pixel(c.y + _scanner.getTopLeft().x, c.x + _scanner.getTopLeft().y );
				LOGGER.info("BARREL: " + p);
				_mouse.click(p);
				_mouse.delay(300);
			}
			LOGGER.info("BARRELS CNT: " + cnt);

			LOGGER.info("DONE......");
		} catch (Throwable t) {
			LOGGER.severe("something went wrong in Barrels protocol...");
			t.printStackTrace();

		}

	}
}
