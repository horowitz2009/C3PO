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
import com.horowitz.commons.Settings;
import com.horowitz.seaport.ScreenScanner;

public class BarrelsProtocol extends AbstractGameProtocol implements IBarrelsProtocol {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private ScreenScanner _scanner;
	private boolean _capture = false;

	private MouseRobot _mouse;

	private int cnt;
	private int blobMin;
	private int blobMax;

	private Settings _settings;

	public BarrelsProtocol(ScreenScanner scanner, MouseRobot mouse, Settings settings) throws IOException {
		blobMin = 15 * 20;
		blobMax = 28 * 32;
		_scanner = scanner;
		_mouse = mouse;
		_settings = settings;
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

			BufferedImage image = _scanner.getImageData("ships/LAND_VINTAGE.bmp").getImage();// was LAND5.bmp

			Pixel rock = _scanner.getRock();
			int topY = _scanner.getTopLeft().y;
			// int yy = rock.y - topY;
			int yy1 = rock.y - 263;

			int xx1 = rock.x - 114;
			int xx2 = _scanner.getBottomRight().x - 28;

			// if (yy < 263) {//outside the game area => need to cut off the land5
			// int toCut = 263 - yy;
			// image = image.getSubimage(0, toCut, image.getWidth(), image.getHeight() - toCut);
			// }

			image = image.getSubimage(0, 0, Math.min(image.getWidth(), xx2 - xx1), image.getHeight());

			Rectangle area = new Rectangle(xx1, yy1, image.getWidth(), image.getHeight());

			FastBitmap landFB = new FastBitmap(image);
			if (landFB.isRGB())
				landFB.toGrayscale();
			if (debug)
				landFB.saveAsBMP("LANDCUT.BMP");

			LOGGER.info("Barrels..." + blobMin + " - " + blobMax);
			// _scanner.captureArea(_barrelsArea, "barrelsArea.png", false);

			BufferedImage capture = new Robot().createScreenCapture(area);
			FastBitmap fb = new FastBitmap(capture);

			if (debug) {
				fb.saveAsPNG("IMAGEa.PNG");
			}
			// FILTER BROWN
			// ColorFiltering colorFiltering = new ColorFiltering(new IntRange(40, 255), new IntRange(30, 255), new IntRange(0,
			// 110));
			// FILTER AMPHORAS
			int rmin = _settings.getInt("barrels.rmin", 98);
			int rmax = _settings.getInt("barrels.rmax", 255);
			int gmin = _settings.getInt("barrels.gmin", 148);
			int gmax = _settings.getInt("barrels.gmax", 250);
			int bmin = _settings.getInt("barrels.bmin", 178);
			int bmax = _settings.getInt("barrels.bmax", 242);

			ColorFiltering colorFiltering = new ColorFiltering(new IntRange(rmin, rmax), new IntRange(gmin, gmax),
			    new IntRange(bmin, bmax));
			colorFiltering.applyInPlace(fb);

			if (fb.isRGB())
				fb.toGrayscale();
			Threshold thr = new Threshold(_settings.getInt("barrels.threshold", 70));
			thr.applyInPlace(fb);
			FastBitmap fb2 = null;

			if (_settings.getBoolean("barrels2.use", false)) {
				fb2 = new FastBitmap(capture);
				int rmin2 = _settings.getInt("barrels2.rmin", 98);
				int rmax2 = _settings.getInt("barrels2.rmax", 255);
				int gmin2 = _settings.getInt("barrels2.gmin", 148);
				int gmax2 = _settings.getInt("barrels2.gmax", 250);
				int bmin2 = _settings.getInt("barrels2.bmin", 178);
				int bmax2 = _settings.getInt("barrels2.bmax", 242);

				ColorFiltering colorFiltering2 = new ColorFiltering(new IntRange(rmin2, rmax2), new IntRange(gmin2, gmax2),
				    new IntRange(bmin2, bmax2));
				colorFiltering2.applyInPlace(fb2);

				if (fb2.isRGB())
					fb2.toGrayscale();
				Threshold thr2 = new Threshold(_settings.getInt("barrels2.threshold", 20));
				thr2.applyInPlace(fb2);
				if (debug) {
					fb2.saveAsPNG("IMAGE2b.png");
				}
			}
			if (debug) {
				fb.saveAsPNG("IMAGE2a.png");
			}

			// //

			BlobDetection bd = new BlobDetection(BlobDetection.Algorithm.EightWay);
			bd.setFilterBlob(true);
			bd.setMinArea(blobMin);
			bd.setMaxArea(blobMax);

			// OR
			Or or = new Or(landFB);
			or.applyInPlace(fb);

			if (fb2 != null) {
				or = new Or(fb2);
				or.applyInPlace(fb);
			}
			
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
				fb.saveAsPNG("IMAGE2blurredTHHHHHHHHHHHHH-LAST.png");

			List<Blob> blobs = bd.ProcessImage(fb);
			for (Blob blob : blobs) {
				if (isNotInterrupted()) {
					cnt++;
					IntPoint c = blob.getCenter();
					Pixel p = new Pixel(c.y + area.x + 0, c.x + area.y + 0);
					LOGGER.fine("BARREL: " + p);
					// SHIP WITH CHEST
					// if (((p.x > _scanner.getBottomRight().x - 381 && p.y > _scanner.getTopLeft().y + 167)
					// || (p.x <= _scanner.getBottomRight().x - 381 && p.y > _scanner.getTopLeft().y + 69))
					// && p.y < _scanner.getRock().y + 126) {//to avoid store ship icon was 72
					if (p.y > _scanner.getTopLeft().y + 69 && p.y < _scanner.getRock().y + 126) {
						//_mouse.click(p);
						//_mouse.delay(50);

						p.x += _settings.getInt("barrels.xOff1", 6);
						p.y += _settings.getInt("barrels.yOff1", 6);
						_mouse.click(p);
						_mouse.delay(50);

						p.x += _settings.getInt("barrels.xOff2", -12);
						p.y += _settings.getInt("barrels.yOff2", -6);
						_mouse.click(p);
						_mouse.delay(100);
						if (_capture) {
							_scanner.captureArea(new Rectangle(p.x - 21, p.y - 120, 51, 120), "barrels/barrels", true);
						}
					}
				}

			}
			_scanner.handlePopups();
			
			if (isNotInterrupted()) {
				// additional clicks
				int n = _settings.getInt("barrels.additional", 5);
				int x = _scanner.getBottomRight().x;
				int y = _scanner.getTopLeft().y;
				for (int i = 0; i < n; i++) {
					int x1 = _settings.getInt("barrels.x" + (i + 1), 0);
					int y1 = _settings.getInt("barrels.y" + (i + 1), 0);
					if (x1 != 0 && y1 != 0) {
						_mouse.click(x + x1, y + y1);
					}
				}
				_mouse.delay(100);
				_scanner.handlePopups();
				_mouse.delay(100);
			}
			LOGGER.info("BARRELS CNT: " + cnt);

		} catch (RobotInterruptedException e) {
			throw new RobotInterruptedException();
		} catch (Exception e) {
			LOGGER.severe("something went wrong in Barrels protocol...");
			e.printStackTrace();

		}

	}

	@Override
	public boolean isCapture() {
		return _capture;
	}

	@Override
	public void setCapture(boolean capture) {
		_capture = capture;
	}

	@Override
	public int getBlobMin() {
		return blobMin;
	}

	@Override
	public void setBlobMin(int blobMin) {
		this.blobMin = blobMin;
	}

	@Override
	public int getBlobMax() {
		return blobMax;
	}

	@Override
	public void setBlobMax(int blobMax) {
		this.blobMax = blobMax;
	}

}
