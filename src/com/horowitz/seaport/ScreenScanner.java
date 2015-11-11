package com.horowitz.seaport;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.horowitz.commons.GameLocator;
import com.horowitz.commons.ImageData;
import com.horowitz.commons.ImageMask;
import com.horowitz.commons.Settings;
import com.horowitz.commons.TemplateMatcher;
import com.horowitz.mickey.ImageComparator;
import com.horowitz.mickey.ImageManager;
import com.horowitz.mickey.MouseRobot;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.RobotInterruptedException;
import com.horowitz.mickey.SimilarityImageComparator;
import com.horowitz.mickey.common.MyImageIO;

public class ScreenScanner {

  public final static Logger LOGGER = Logger.getLogger(ScreenScanner.class.getName());
  private static final boolean DEBUG = false;

  private ImageComparator _comparator;
  private TemplateMatcher _matcher;
  private MouseRobot _mouse;

  private Pixel _br = null;
  private Pixel _tl = null;
  private boolean _fullyOptimized = false;

  private ImageData _hooray = null;

  private Rectangle _scanArea = null;

  private Settings _settings;
  private GameLocator _gameLocator;

  private Map<String, ImageData> _imageDataCache;
  private Pixel _safePoint;
  private Pixel _parkingPoint;
  private Rectangle _labelArea;
  private Rectangle _levelArea;
  private Rectangle _popupArea;
  private Rectangle _productionArea3;
  private Rectangle _productionArea2;
  private Rectangle _warehouseArea;;
  private int _labelWidth;
  private Pixel[] _fishes;
  private Pixel[] _shipLocations;
  private Pixel[] _buildingLocations;

  public Pixel[] getShipLocations() {
    return _shipLocations;
  }

  public ScreenScanner(Settings settings) {
    _settings = settings;
    _comparator = new SimilarityImageComparator(0.04, 2000);
    _matcher = new TemplateMatcher();
    try {
      _mouse = new MouseRobot();
    } catch (AWTException e1) {
      e1.printStackTrace();
    }
    _gameLocator = new GameLocator();
    _imageDataCache = new Hashtable<String, ImageData>();

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle area = new Rectangle(20, 340, screenSize.width - 20 - 404, screenSize.height - 340 - 110);
    try {

      _tl = new Pixel(0, 0);
      _br = new Pixel(screenSize.width - 3, screenSize.height - 3);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }
  
  public Rectangle generateWindowedArea(int width, int height) {
    int xx = (getGameWidth() - width) / 2;
    int yy = (getGameHeight() - height) / 2;
    return new Rectangle(_tl.x + xx, _tl.y + yy, width, height);
  }

  private void setKeyAreas() throws IOException {

    _fullyOptimized = true;
    Rectangle area;
    int xx;
    int yy;

    _scanArea = new Rectangle(_tl.x + 125, _tl.y, getGameWidth() - 125 - 125, getGameHeight());

    _fishes = new Pixel[] { new Pixel(-169, -13), new Pixel(-223, -49), new Pixel(-282, -89), new Pixel(-354, -120) };

    _shipLocations = new Pixel[] { new Pixel(103, 83), new Pixel(103, 187), new Pixel(103, 278) };
    _buildingLocations = new Pixel[] { new Pixel(57, -59), new Pixel(141, -76) };

    // label area
    _labelWidth = 380;
    xx = (getGameWidth() - _labelWidth) / 2;
    _labelArea = new Rectangle(_tl.x + xx, _tl.y + 71, _labelWidth, 66);
    _levelArea = new Rectangle(_tl.x + xx, _tl.y + 360, _labelWidth, 35);

    //_popupArea = generateWindowedArea(324, 516);
    _popupArea = generateWindowedArea(328, 520);
    _safePoint = new Pixel(_br.x - 15, _br.y - 15);
    _parkingPoint = new Pixel(_br);

    getImageData("rockEdge.bmp", _scanArea, -45, 26);
    getImageData("pin.bmp", _scanArea, 8, 8);
    
    area = new Rectangle(_br.x-110, _br.y -  75, 60, 40);
    getImageData("anchor.bmp", area, 20, 7);
    getImageData("dest/smallTown.bmp", _scanArea, 41, 45);
    getImageData("dest/smallTownTitle.bmp", _popupArea, 0, 0);
    //getImageData("dest/missing.bmp", _scanArea, 41, 45);
    getImageData("dest/setSail.bmp", _popupArea, 30, 6);

    ImageData gear2 = getImageData("buildings/gears2.bmp", _popupArea, 0, 0);
    gear2.setColorToBypass(Color.BLACK);
    
    getImageData("buildings/produce.bmp", _popupArea, 0, 0);
    getImageData("buildings/produceGray.bmp", _popupArea, 0, 0);
    getImageData("buildings/x.bmp", _popupArea, 10, 10);
    
    /*
    _hooray = new ImageData("Hooray.bmp", area, _comparator, 23, 6);

    getImageData("tags/zzz.bmp", _scanArea, 0, 7);
    getImageData("tags/coins.bmp", _scanArea, 0, 9);
    getImageData("tags/houses.bmp", _scanArea, 0, 9);
    getImageData("tags/fire.bmp", _scanArea, 0, 7);
    getImageData("tags/medical.bmp", _scanArea, 14, 9);
    getImageData("tags/greenDown.bmp", _scanArea, 18, -35);
    getImageData("buildings/Warehouse.bmp", _scanArea, 35, 0);

    area = new Rectangle(_br.x - 264, _tl.y, 264, 35);
    getImageData("populationRed.bmp", area, 0, 0);
    getImageData("populationBlue.bmp", area, 0, 0);
    */
  }

  public Pixel[] getBuildingLocations() {
    return _buildingLocations;
  }

  public Pixel[] getFishes() {
    return _fishes;
  }

  public Pixel getParkingPoint() {
    return _parkingPoint;
  }

  public Rectangle getProductionArea3() {
    return _productionArea3;
  }

  public Rectangle getProductionArea2() {
    return _productionArea2;
  }

  public Rectangle getWarehouseArea() {
    return _warehouseArea;
  }

  public Rectangle getScanArea() {
    return _scanArea;
  }

  public ImageData getImageData(String filename) throws IOException {
    return getImageData(filename, _scanArea, 0, 0);
  }

  public ImageData getImageData(String filename, Rectangle defaultArea, int xOff, int yOff) throws IOException {
    // if (!new File(filename).exists())
    // return null;

    if (_imageDataCache.containsKey(filename)) {
      return _imageDataCache.get(filename);
    } else {
      ImageData imageData = null;
      try {
        imageData = new ImageData(filename, defaultArea, _comparator, xOff, yOff);
      } catch (IOException e) {
        System.err.println(e);
        return null;
      }
      if (imageData != null)
        _imageDataCache.put(filename, imageData);
      return imageData;
    }
  }

  public boolean locateGameArea() throws AWTException, IOException, RobotInterruptedException {
    LOGGER.fine("Locating game area ... ");

    boolean found = _gameLocator.locateGameArea(new ImageData("topLeft.bmp", null, _comparator, -38, -12),
        new ImageData("bottomRight.bmp", null, _comparator, 45 + 53, 64), false);
    if (found) {
      _tl = _gameLocator.getTopLeft();
      _br = _gameLocator.getBottomRight();
      setKeyAreas();
      return true;
    } else {
      _tl = new Pixel(0, 0);
      _br = new Pixel(1600, 1000);
    }
    return false;
  }

  public Pixel findRock() throws IOException, AWTException, RobotInterruptedException {
    Rectangle area = new Rectangle(_tl.x + 450, _tl.y + 43, 760, 450);

    Pixel p = scanOne("rockEdge.bmp", area, false);
    // writeImage(area, "admArea1.png");
    if (p == null) {
      LOGGER.info("Rock try 2 ...");
      p = scanOne("rockEdge.bmp", getScanArea(), false);
    }
    return p;
  }

  public void writeImage(Rectangle rect, String filename) {
    try {
      writeImage(new Robot().createScreenCapture(rect), filename);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public void writeImage2(Rectangle rect, String filename) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("MM-dd  HH-mm-ss-SSS");
      String date = sdf.format(Calendar.getInstance().getTime());
      String filename2 = filename + " " + date + ".png";

      writeImage(new Robot().createScreenCapture(rect), filename2);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public void writeImage(BufferedImage image, String filename) {

    try {
      int ind = filename.lastIndexOf("/");
      if (ind > 0) {
        String path = filename.substring(0, ind);
        File f = new File(path);
        f.mkdirs();
      }
      File file = new File(filename);
      MyImageIO.write(image, filename.substring(filename.length() - 3).toUpperCase(), file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void captureGame() {
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd  HH-mm-ss-SSS");
    String date = sdf.format(Calendar.getInstance().getTime());
    String filename = "popup " + date + ".png";
    captureGame(filename);
  }

  public void captureGame(String filename) {
    writeImage(new Rectangle(new Point(_tl.x, _tl.y), new Dimension(getGameWidth(), getGameHeight())), filename);
  }

  public Pixel locateImageCoords(String imageName, Rectangle[] area, int xOff, int yOff) throws AWTException,
      IOException, RobotInterruptedException {

    final Robot robot = new Robot();
    final BufferedImage image = ImageIO.read(ImageManager.getImageURL(imageName));
    Pixel[] mask = new ImageMask(imageName).getMask();
    BufferedImage screen;
    int turn = 0;
    Pixel resultPixel = null;
    // MouseRobot mouse = new MouseRobot();
    // mouse.saveCurrentPosition();
    while (turn < area.length) {

      screen = robot.createScreenCapture(area[turn]);
      List<Pixel> foundEdges = findEdge(image, screen, _comparator, null, mask);
      if (foundEdges.size() >= 1) {
        // found
        // AppConsole.print("found it! ");
        int y = area[turn].y;
        int x = area[turn].x;
        resultPixel = new Pixel(foundEdges.get(0).x + x + xOff, foundEdges.get(0).y + y + yOff);
        // System.err.println("AREA: [" + turn + "] " + area[turn]);
        break;
      }
      turn++;
    }
    // mouse.checkUserMovement();
    // AppConsole.println();
    return resultPixel;
  }

  public boolean isOptimized() {
    return _fullyOptimized && _br != null && _tl != null;
  }

  private List<Pixel> findEdge(final BufferedImage targetImage, final BufferedImage area, ImageComparator comparator,
      Map<Integer, Color[]> colors, Pixel[] indices) {
    if (DEBUG)
      try {
        MyImageIO.write(area, "PNG", new File("C:/area.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    List<Pixel> result = new ArrayList<Pixel>(8);
    for (int i = 0; i < (area.getWidth() - targetImage.getWidth()); i++) {
      for (int j = 0; j < (area.getHeight() - targetImage.getHeight()); j++) {
        final BufferedImage subimage = area.getSubimage(i, j, targetImage.getWidth(), targetImage.getHeight());
        if (DEBUG)
          try {
            MyImageIO.write(subimage, "PNG", new File("C:/subimage.png"));
          } catch (IOException e) {
            e.printStackTrace();
          }
        if (comparator.compare(targetImage, subimage, colors, indices)) {
          // System.err.println("FOUND: " + i + ", " + j);
          result.add(new Pixel(i, j));
          if (result.size() > 0) {// increase in case of trouble
            break;
          }
        }
      }
    }
    return result;
  }

  public void scan() {
    try {
      Robot robot = new Robot();

      BufferedImage screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
      if (DEBUG)
        MyImageIO.write(screenshot, "PNG", new File("screenshot.png"));
    } catch (HeadlessException | AWTException | IOException e) {

      e.printStackTrace();
    }

  }

  public void compare(String imageName1, String imageName2) throws IOException {
    final BufferedImage image1 = ImageIO.read(ImageManager.getImageURL(imageName1));
    Pixel[] mask1 = new ImageMask(imageName1).getMask();
    final BufferedImage image2 = ImageIO.read(ImageManager.getImageURL(imageName2));
    Pixel[] mask2 = new ImageMask(imageName2).getMask();

    List<Pixel> res = compareImages(image1, image2, _comparator, mask2);

    // System.err.println(res);
  }

  public List<Pixel> compareImages(final BufferedImage image1, final BufferedImage image2, ImageComparator comparator,
      Pixel[] indices) {
    if (DEBUG)
      try {
        ImageIO.write(image2, "PNG", new File("area.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    List<Pixel> result = new ArrayList<Pixel>(8);
    for (int i = 0; i <= (image2.getWidth() - image1.getWidth()); i++) {
      for (int j = 0; j <= (image2.getHeight() - image1.getHeight()); j++) {
        final BufferedImage subimage = image2.getSubimage(i, j, image1.getWidth(), image1.getHeight());
        if (DEBUG)
          try {
            MyImageIO.write(subimage, "PNG", new File("subimage.png"));
          } catch (IOException e) {
            e.printStackTrace();
          }

        boolean b = comparator.compare(image1, image2, null, indices);
        // System.err.println("equal: " + b);
        indices = null;
        b = comparator.compare(image1, image2, null, indices);
        // System.err.println("equal2: " + b);
        List<Pixel> list = comparator.findSimilarities(image1, subimage, indices);
        // System.err.println("FOUND: " + list);
      }
    }
    return result;
  }

  public Pixel getBottomRight() {
    return _br;
  }

  public Pixel getTopLeft() {
    return _tl;
  }

  public int getGameWidth() {
    int width = isOptimized() ? _br.x - _tl.x : Toolkit.getDefaultToolkit().getScreenSize().width;
    return width != 0 ? width : Toolkit.getDefaultToolkit().getScreenSize().width;
  }

  public int getGameHeight() {
    if (isOptimized()) {
      return _br.y - _tl.y == 0 ? Toolkit.getDefaultToolkit().getScreenSize().height : _br.y - _tl.y;
    } else {
      return Toolkit.getDefaultToolkit().getScreenSize().height;
    }
  }

  public void addHandler(Handler handler) {
    LOGGER.addHandler(handler);
  }

  public ImageData generateImageData(String imageFilename) throws IOException {
    return new ImageData(imageFilename, null, _comparator, 0, 0);
  }

  public ImageData setImageData(String imageFilename) throws IOException {
    return getImageData(imageFilename, _scanArea, 0, 0);
  }

  public ImageData generateImageData(String imageFilename, int xOff, int yOff) throws IOException {
    return new ImageData(imageFilename, null, _comparator, xOff, yOff);
  }

  public ImageComparator getComparator() {
    return _comparator;
  }

  public Pixel getSafePoint() {
    return _safePoint;
  }

  public Rectangle getPopupArea() {
    return _popupArea;
  }

  public Rectangle getLabelArea() {
    return _labelArea;
  }

  public Rectangle getLevelArea() {
    return _levelArea;
  }

  public ImageComparator getImageComparator() {
    return _comparator;
  }

  public List<Pixel> scanMany(String filename, BufferedImage screen, boolean click) throws RobotInterruptedException,
      IOException, AWTException {

    ImageData imageData = getImageData(filename);
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    return scanMany(imageData, screen, click);
  }

  public List<Pixel> scanMany(ImageData imageData, BufferedImage screen, boolean click)
      throws RobotInterruptedException, IOException, AWTException {
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    Rectangle area = imageData.getDefaultArea();
    if (screen == null)
      screen = new Robot().createScreenCapture(area);
    List<Pixel> matches = _matcher.findMatches(imageData.getImage(), screen);
    if (!matches.isEmpty()) {
      Collections.sort(matches);
      Collections.reverse(matches);

      // filter similar
      if (matches.size() > 1) {
        for (int i = matches.size() - 1; i > 0; --i) {
          for (int j = i - 1; j >= 0; --j) {
            Pixel p1 = matches.get(i);
            Pixel p2 = matches.get(j);
            if (Math.abs(p1.x - p2.x) <= 3 && Math.abs(p1.y - p2.y) <= 3) {
              // too close to each other
              // remove one
              matches.remove(j);
              --i;
            }
          }
        }
      }

      for (Pixel pixel : matches) {
        pixel.x += (area.x + imageData.get_xOff());
        pixel.y += (area.y + imageData.get_yOff());
        if (click)
          _mouse.click(pixel.x, pixel.y);
      }
    }
    return matches;
  }

  public Pixel scanOne(ImageData imageData, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException {
    if (area == null) {
      area = imageData.getDefaultArea();
    }
    BufferedImage screen = new Robot().createScreenCapture(area);
    Pixel pixel = _matcher.findMatch(imageData.getImage(), screen);
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.info("found: " + pixel);
      if (click) {
        _mouse.click(pixel.x, pixel.y);
      }
    }
    return pixel;

  }

  public Pixel scanOne(String filename, Rectangle area, boolean click) throws RobotInterruptedException, IOException,
      AWTException {
    ImageData imageData = getImageData(filename);
    if (imageData == null)
      return null;
    if (area == null)
      area = imageData.getDefaultArea();

    BufferedImage screen = new Robot().createScreenCapture(area);
    Pixel pixel = _matcher.findMatch(imageData.getImage(), screen);
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.info("found: " + pixel);
      if (click) {
        _mouse.click(pixel.x, pixel.y);
        _mouse.delay(100);
      }
    }
    return pixel;
  }

  public Pixel scanOneFast(ImageData imageData, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException {
    if (area == null) {
      area = imageData.getDefaultArea();
    }
    BufferedImage screen = new Robot().createScreenCapture(area);
    Pixel pixel = _comparator.findImage(imageData.getImage(), screen);
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.info("found: " + pixel);
      if (click) {
        _mouse.click(pixel.x, pixel.y);
      }
    }
    return pixel;

  }

  public Pixel scanOneFast(String filename, Rectangle area, boolean click) throws RobotInterruptedException,
      IOException, AWTException {
    ImageData imageData = getImageData(filename);
    if (imageData == null)
      return null;
    if (area == null)
      area = imageData.getDefaultArea();

    BufferedImage screen = new Robot().createScreenCapture(area);
    
    Pixel pixel = _comparator.findImage(imageData.getImage(), screen, imageData.getColorToBypass());
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.info("found: " + pixel);
      if (click) {
        _mouse.click(pixel.x, pixel.y);
        _mouse.delay(100);
      }
    }
    return pixel;
  }

  public TemplateMatcher getMatcher() {
    return _matcher;
  }

  public void setMatcher(TemplateMatcher matcher) {
    _matcher = matcher;
  }

  public MouseRobot getMouse() {
    return _mouse;
  }

  public void reduceThreshold() {
    _matcher.setSimilarityThreshold(.85d);
  }

  public void restoreThreshold() {
    _matcher.setSimilarityThreshold(.95d);

  }

}
