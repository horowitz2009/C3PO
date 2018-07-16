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

public class ImageBarrelsProtocol extends AbstractGameProtocol implements IBarrelsProtocol {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private ScreenScanner _scanner;
	private boolean _capture = false;

	private MouseRobot _mouse;

	private int cnt;

	private Settings _settings;

	public ImageBarrelsProtocol(ScreenScanner scanner, MouseRobot mouse, Settings settings) throws IOException {
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
			Rectangle area = new Rectangle(rock.x - 116, rock.y - 85, 835, 185+30);
			
			LOGGER.info("Barrels...");
			List<Pixel> m = _scanner.scanMany("market/barrel1.bmp", area, true);
			LOGGER.info("BARRELS CNT1: " + m.size());
			m = _scanner.scanMany("market/barrel2.bmp", area, true);
			LOGGER.info("BARRELS CNT2: " + m.size());
			m = _scanner.scanMany("market/barrel3.bmp", area, true);
			LOGGER.info("BARRELS CNT3: " + m.size());
			m = _scanner.scanMany("market/barrel4.bmp", area, true);
			LOGGER.info("BARRELS CNT4: " + m.size());

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

	@Override
	public void setBlobMax(int blobMax) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getBlobMax() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setBlobMin(int blobMin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getBlobMin() {
		// TODO Auto-generated method stub
		return 0;
	}

}
