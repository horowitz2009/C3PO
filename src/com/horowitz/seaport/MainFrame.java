package com.horowitz.seaport;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.horowitz.bigbusiness.macros.Macros;
import com.horowitz.bigbusiness.macros.RawMaterialsMacros;
import com.horowitz.bigbusiness.model.BasicElement;
import com.horowitz.bigbusiness.model.Building;
import com.horowitz.bigbusiness.model.Contract;
import com.horowitz.bigbusiness.model.Product;
import com.horowitz.bigbusiness.model.ProductionProtocol;
import com.horowitz.bigbusiness.model.ProductionProtocol.Entry;
import com.horowitz.bigbusiness.model.storage.JsonStorage;
import com.horowitz.commons.ImageData;
import com.horowitz.commons.Settings;
import com.horowitz.commons.TemplateMatcher;
import com.horowitz.mickey.Location;
import com.horowitz.mickey.MouseRobot;
import com.horowitz.mickey.MyLogger;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.RobotInterruptedException;
import com.horowitz.mickey.ocr.OCRB;

public class MainFrame extends JFrame {

  private static final long serialVersionUID = -4827959393249146870L;

  private final static Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

  private static final String APP_TITLE = "Seaport v0.011";

  private Settings _settings;
  private MouseRobot _mouse;
  private ScreenScanner _scanner;

  private JLabel _mouseInfoLabel;

  private CaptureDialog captureDialog;

  private boolean _stopAllThreads;

  private JTextField _findThisTF;

  private TemplateMatcher _matcher;

  private List<Pixel> _fishes;
  private List<Pixel> _shipLocations;
  private List<Pixel> _buildingLocations;

  private Pixel _rock;

  private JToggleButton _shipsToggle;

  private JToggleButton _industriesToggle;

  public static void main(String[] args) {

    try {
      MainFrame frame = new MainFrame();

      frame.pack();
      frame.setSize(new Dimension(frame.getSize().width + 8, frame.getSize().height + 8));
      int w = frame.getSize().width;
      int h = frame.getSize().height;
      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int x = screenSize.width - w;
      int y = (screenSize.height - h) / 2;
      frame.setBounds(x, y, w, h);

      frame.setVisible(true);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private void createLabelImageData(BasicElement element) throws IOException {
    element.setLabelImage(_scanner.getImageData("labels/" + element.getName() + ".bmp", _scanner.getLabelArea(), 0, 0));
  }

  private void createPictureImageData(BasicElement element, String folder) throws IOException {
    element.setPictureImage(_scanner.getImageData(folder + "/" + element.getName() + ".bmp", _scanner.getScanArea(), 0,
        0));
  }

  @SuppressWarnings("serial")
  private void init() throws AWTException {
    setTitle(APP_TITLE);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setAlwaysOnTop(true);

    _settings = Settings.createSettings("bbgun.properties");
    _scanner = new ScreenScanner(_settings);
    _mouse = _scanner.getMouse();

    JPanel rootPanel = new JPanel(new BorderLayout());
    getContentPane().add(rootPanel, BorderLayout.CENTER);

    final JTextArea outputConsole = new JTextArea(8, 14);

    rootPanel.add(new JScrollPane(outputConsole), BorderLayout.CENTER);

    Handler handler = new Handler() {

      @Override
      public void publish(LogRecord record) {
        String text = outputConsole.getText();
        if (text.length() > 3000) {
          outputConsole.setText("");
        }
        outputConsole.append(record.getMessage());
        outputConsole.append("\n");
        outputConsole.setCaretPosition(outputConsole.getDocument().getLength());
        // outputConsole.repaint();
      }

      @Override
      public void flush() {
        outputConsole.repaint();
      }

      @Override
      public void close() throws SecurityException {
        // do nothing

      }
    };
    LOGGER.addHandler(handler);
    ScreenScanner.LOGGER.addHandler(handler);

    JToolBar mainToolbar1 = createToolbar1();
    JToolBar mainToolbar2 = createToolbar2();
    mainToolbar2.setFloatable(false);

    JPanel toolbars = new JPanel(new GridLayout(9, 1));
    toolbars.add(mainToolbar1);
    toolbars.add(mainToolbar2);

    Box north = Box.createVerticalBox();
    north.add(toolbars);

    _findThisTF = new JTextField();
    Box box = Box.createHorizontalBox();
    box.add(_findThisTF);
    JButton findButton = new JButton(new AbstractAction("Find") {

      @Override
      public void actionPerformed(ActionEvent ae) {
        LOGGER.info("scan for " + _findThisTF.getText());
        final String filename = _findThisTF.getText();
        new Thread(new Runnable() {
          public void run() {
            try {

              _scanner.getImageData(filename);
              Pixel p = _scanner.scanOne(filename, null, true);
              if (p != null) {
                LOGGER.info("found it: " + p);
              } else {
                LOGGER.info(filename + " not found");
                LOGGER.info("trying with redused threshold");
                double old = _matcher.getSimilarityThreshold();
                _matcher.setSimilarityThreshold(0.91d);
                p = _scanner.scanOne(filename, null, true);
                if (p != null) {
                  LOGGER.info("found it: " + p);
                } else {
                  LOGGER.info(filename + " not found");
                }
                _matcher.setSimilarityThreshold(old);

              }
            } catch (RobotInterruptedException e) {
              LOGGER.log(Level.WARNING, e.getMessage());
              e.printStackTrace();
            } catch (IOException e) {
              LOGGER.log(Level.WARNING, e.getMessage());
              e.printStackTrace();
            } catch (AWTException e) {
              LOGGER.log(Level.WARNING, e.getMessage());
              e.printStackTrace();
            }

          }
        }).start();

      }
    });
    box.add(findButton);
    north.add(box);
    _mouseInfoLabel = new JLabel(" ");
    north.add(_mouseInfoLabel);
    rootPanel.add(north, BorderLayout.NORTH);

  }

  @SuppressWarnings("serial")
  private JToolBar createToolbar1() {
    JToolBar mainToolbar1 = new JToolBar();
    mainToolbar1.setFloatable(false);

    // SCAN
    {
      AbstractAction action = new AbstractAction("Scan") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                scan();
              } catch (RobotInterruptedException e) {
                e.printStackTrace();
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }
    // RUN MAGIC
    {
      AbstractAction action = new AbstractAction("Run") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              LOGGER.info("Let's get rolling...");
              if (!_scanner.isOptimized()) {
                try {
                  scan();
                } catch (RobotInterruptedException e) {
                  e.printStackTrace();
                }
              }

              if (_scanner.isOptimized()) {
                // DO THE JOB
                doMagic();
              } else {
                LOGGER.info("I need to know where the game is!");
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }
    // STOP MAGIC
    {
      AbstractAction action = new AbstractAction("Stop") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {

            @Override
            public void run() {
              LOGGER.info("Stopping BB Gun");
              _stopAllThreads = true;
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    // RECORD
    {
      AbstractAction action = new AbstractAction("R") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              record();
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    // COINS
    {
      AbstractAction action = new AbstractAction("F") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                if (!_scanner.isOptimized()) {
                  scan();
                }

                if (_scanner.isOptimized()) {
                  _mouse.savePosition();
                  _scanner.scanOne("tags/medical.bmp", null, true);
                  _scanner.scanOne("tags/fire.bmp", null, true);
                  _mouse.restorePosition();
                } else {
                  LOGGER.info("I need to know where the game is!");
                }
              } catch (RobotInterruptedException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (AWTException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    // COINS
    {
      AbstractAction action = new AbstractAction("Coins") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                if (!_scanner.isOptimized()) {
                  scan();
                }

                if (_scanner.isOptimized()) {

                  // scanCoins();
                  LOGGER.info("Scan for coins...");
                  _mouse.savePosition();
                  _scanner.scanMany("tags/coins.bmp", null, true);
                  _mouse.restorePosition();
                  LOGGER.info("Done");

                } else {
                  LOGGER.info("I need to know where the game is!");
                }
              } catch (RobotInterruptedException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (AWTException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    // Houses
    {
      AbstractAction action = new AbstractAction("Houses") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {

                if (!_scanner.isOptimized()) {
                  scan();
                }

                if (_scanner.isOptimized()) {
                  LOGGER.info("Scan for houses...");
                  _mouse.savePosition();

                  if (!_scanner.scanMany("tags/houses.bmp", null, true).isEmpty()) {
                    _mouse.delay(200);
                  }
                  _mouse.click(_scanner.getSafePoint());
                  _mouse.delay(100);
                  _mouse.click(_scanner.getSafePoint());
                  _mouse.restorePosition();
                  LOGGER.info("Done.");

                } else {
                  LOGGER.info("I need to know where the game is!");
                }
              } catch (RobotInterruptedException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (AWTException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }
    return mainToolbar1;
  }

  @SuppressWarnings("serial")
  private JToolBar createToolbar2() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    // SCAN
    {
      _shipsToggle = new JToggleButton("Ships");
      toolbar.add(_shipsToggle);
      _industriesToggle = new JToggleButton("Industries");
      _industriesToggle.setSelected(true);
      toolbar.add(_industriesToggle);
    }
    return toolbar;
  }

  private void setupLogger() {
    try {
      MyLogger.setup();
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Problems with creating the log files");
    }
  }

  @SuppressWarnings("serial")
  private static class CaptureDialog extends JFrame {
    Point _startPoint;
    Point _endPoint;
    Rectangle _rect;
    boolean inDrag;

    public CaptureDialog() {
      super("hmm");
      setUndecorated(true);
      getRootPane().setOpaque(false);
      getContentPane().setBackground(new Color(0, 0, 0, 0.05f));
      setBackground(new Color(0, 0, 0, 0.05f));

      _startPoint = null;
      _endPoint = null;
      inDrag = false;

      // events

      addMouseListener(new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent e) {
          inDrag = true;

        }

        @Override
        public void mouseClicked(MouseEvent e) {

          if (e.getButton() == MouseEvent.BUTTON1) {
            if (_startPoint == null) {
              LOGGER.info("clicked once " + e.getButton() + " (" + e.getX() + ", " + e.getY() + ")");
              _startPoint = e.getPoint();
              repaint();
            } else {
              _endPoint = e.getPoint();
              // LOGGER.info("clicked twice " + e.getButton() +
              // " (" + e.getX() + ", " + e.getY() + ")");
              setVisible(false);
              LOGGER.info("AREA: " + _rect);
            }
          } else if (e.getButton() == MouseEvent.BUTTON3) {
            _startPoint = null;
            _endPoint = null;
            repaint();
          }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          // LOGGER.info("REL:"+e);

          if (inDrag && _endPoint != null && _startPoint != null) {
            // LOGGER.info("end of drag " + e.getButton() + " (" +
            // e.getX() + ", " + e.getY() + ")");
            inDrag = false;
            setVisible(false);
            LOGGER.info("AREA: " + _rect);
            // HMM
            dispose();
          }

        }

      });

      addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
          // LOGGER.info("move " + e.getPoint());
          _endPoint = e.getPoint();
          repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
          if (_startPoint == null) {
            _startPoint = e.getPoint();
          }
          _endPoint = e.getPoint();
          repaint();
          // LOGGER.info("DRAG:" + e);
        }

      });

    }

    @Override
    public void paint(Graphics g) {
      super.paint(g);
      if (_startPoint != null && _endPoint != null) {
        g.setColor(Color.RED);
        int x = Math.min(_startPoint.x, _endPoint.x);
        int y = Math.min(_startPoint.y, _endPoint.y);
        int w = Math.abs(_startPoint.x - _endPoint.x);
        int h = Math.abs(_startPoint.y - _endPoint.y);
        _rect = new Rectangle(x, y, w, h);

        g.drawRect(x, y, w, h);

        // g.setColor(Color.GRAY);
        // g.drawString("[" + w + ", " + h + "]", w / 2 - 13, h / 2 -
        // 3);
        g.setColor(Color.RED);
        g.drawString(x + ", " + y + ", [" + w + ", " + h + "]", x + 3, y + 13);
      }
    }
  }

  private void scan() throws RobotInterruptedException {
    try {
      LOGGER.info("Scanning...");
      setTitle(APP_TITLE + " ...");
      boolean found = _scanner.locateGameArea();
      if (found) {

        LOGGER.info("GAME FOUND! Seaport READY.");
        // fixTheGame();
        /*
        _protocol = new ProductionProtocol();
        _buildingLocations = new JsonStorage().loadBuildings();
        postDeserialize(_buildingLocations);
        recalcPositions(false);

        Product milk = new Product("Milk");
        milk.setPosition(1);
        milk.setTime(2);
        milk.setBuildingName("Ranch");
        milk.setLevelRequired(1);
        createLabelImageData(milk);
        _protocol.addEntry(milk, 1, 2, 8);
        {
          Product grain = new Product("Grain");
          createLabelImageData(grain);
          grain.setPosition(1);
          grain.setTime(2);
          grain.setBuildingName("Farm");
          grain.setLevelRequired(1);
          _protocol.addEntry(grain, 1, 3, 6);
        }
        {
          Product product = new Product("Polyethylene");
          createLabelImageData(product);
          product.setPosition(1);
          product.setTime(2);
          product.setBuildingName("PaperMill");
          product.setLevelRequired(1);
          _protocol.addEntry(product, 1, 4, 8);
        }
        */

        setTitle(APP_TITLE + " READY");
      } else {
        LOGGER.info("CAN'T FIND THE GAME!");
        setTitle(APP_TITLE);
      }
    } catch (Exception e1) {
      LOGGER.log(Level.WARNING, e1.getMessage());
      e1.printStackTrace();
    }

  }

  private void recalcPositions(boolean click) throws RobotInterruptedException {
    try {
      _mouse.click(_scanner.getSafePoint());
      _mouse.delay(100);
      _mouse.mouseMove(_scanner.getParkingPoint());

      Pixel rock = _scanner.findRock();
      if (rock != null) {
        _rock = rock;
        LOGGER.info("The rock is found: " + rock);

        Pixel[] fishes = _scanner.getFishes();
        _fishes = new ArrayList<Pixel>();
        for (Pixel fish : fishes) {
          Pixel goodFish = new Pixel(rock.x + fish.x, rock.y + fish.y);
          _fishes.add(goodFish);
        }

        Pixel[] shipLocations = _scanner.getShipLocations();
        _shipLocations = new ArrayList<Pixel>();
        for (Pixel p : shipLocations) {
          Pixel goodP = new Pixel(rock.x + p.x, rock.y + p.y);
          _shipLocations.add(goodP);
        }

        Pixel[] bLocations = _scanner.getBuildingLocations();
        _buildingLocations = new ArrayList<Pixel>();
        for (Pixel p : bLocations) {
          Pixel goodP = new Pixel(rock.x + p.x, rock.y + p.y);
          _buildingLocations.add(goodP);
        }

        // for (Building building : _buildingLocations) {
        // Pixel rel = building.getRelativePosition();
        // Pixel newAbsPos = new Pixel(rock.x - rel.x, rock.y - rel.y);
        // building.setPosition(newAbsPos);
        // if (click) {
        // _mouse.mouseMove(newAbsPos);
        // LOGGER.info(building.getName() + " - " + newAbsPos);
        // _mouse.delay(2000);
        // _mouse.checkUserMovement();
        // _mouse.click(_scanner.getSafePoint());
        // _mouse.delay(200);
        // _mouse.checkUserMovement();
        // }
        //
        // }
      } else {
        LOGGER.info("CAN'T FIND THE ROCK!!!");
        // throw new RobotInterruptedException();
        handlePopups();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  private void record() {
    try {
      LOGGER.info("Recording the mouse movement (for now)");

      captureDialog = new CaptureDialog();
      if (_scanner.isOptimized()) {
        captureDialog.setBounds(_scanner.getTopLeft().x, _scanner.getTopLeft().y, _scanner.getGameWidth(),
            _scanner.getGameHeight());
      } else {
        captureDialog.setBounds(0, 0, 1679, 1009);
      }
      captureDialog.setVisible(true);
      try {
        while (true) {
          Point loc = MouseInfo.getPointerInfo().getLocation();
          // LOGGER.info("location: " + loc.x + ", " + loc.y);
          _mouseInfoLabel.setText("location: " + loc.x + ", " + loc.y);
          _mouse.delay(250, false);

        }
      } catch (RobotInterruptedException e) {
        LOGGER.info("interrupted");
      }
    } catch (Exception e1) {
      LOGGER.log(Level.WARNING, e1.getMessage());
      e1.printStackTrace();
    }

  }

  private Rectangle generateMiniArea(Pixel p) {
    return new Rectangle(p.x - 2 - 18, p.y - 50 + 35, 44, 60);
  }

  public MainFrame() throws HeadlessException, AWTException {
    super();
    setupLogger();
    init();

    _matcher = _scanner.getMatcher();

    _stopAllThreads = false;

  }

  private void doMagic() {
    assert _scanner.isOptimized();
    setTitle(APP_TITLE + " RUNNING");
    _stopAllThreads = false;

    try {

      recalcPositions(false);

      do {
        handlePopups();

        // 1. FISH
        LOGGER.info("Fish...");
        doFishes();

        // 2. SHIPS
        if (_shipsToggle.isSelected()) {
          LOGGER.info("Ships...");
          doShips();
        }

        // 3. INDUSTRIES
        if (_industriesToggle.isSelected()) {
          LOGGER.info("industries...");
          doIndustries();
        }

        _mouse.mouseMove(_scanner.getParkingPoint());

        _mouse.delay(200);

      } while (!_stopAllThreads);

    } catch (RobotInterruptedException e) {
      LOGGER.info("interrupted");
      setTitle(APP_TITLE);
      // e.printStackTrace();
    }
  }

  private void handlePopups() throws RobotInterruptedException {
    try {
      LOGGER.info("popups...");
      boolean popup = false;
      // reload
      Rectangle area = _scanner.generateWindowedArea(412, 550);
      Pixel p = _scanner.scanOneFast("reload.bmp", area, true);
      if (p == null)
        p = _scanner.scanOneFast("reload2.bmp", area, true);
      popup = p != null;
      if (popup) {
        LOGGER.info("Game crashed. Reloading...");
        _mouse.delay(8000);
      } else {
        _mouse.delay(150);
      }

      _scanner.scanOneFast("buildings/x.bmp", null, true);
      _mouse.delay(150);
      Pixel anchor = _scanner.scanOneFast("anchor.bmp", null, true);
      _mouse.delay(450);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    }

  }

  private void rescan() {
    // TODO Auto-generated method stub

  }

  private void doFishes() throws RobotInterruptedException {
    if (_fishes != null && !_fishes.isEmpty()) {
      for (Pixel pixel : _fishes) {

        _mouse.click(pixel);
        _mouse.delay(200);
      }
    } else {
      LOGGER.info("UHOH");
    }
  }

  private void doShips() throws RobotInterruptedException {
    try {
      boolean shipSent = false;
      if (_shipLocations != null && !_shipLocations.isEmpty()) {
        for (Pixel pixel : _shipLocations) {
          _mouse.click(pixel);
          _mouse.delay(50);
          _mouse.click(pixel);
          _mouse.delay(250);

          // check for menu
          Rectangle miniArea = new Rectangle(pixel.x - 15, pixel.y + 65, 44, 44);
          Pixel fp = _scanner.scanOneFast(_scanner.getImageData("pin.bmp"), miniArea, false);
          if (fp != null) {
            LOGGER.info("SHIP to be sent ...");
            _mouse.click(fp);
            _mouse.delay(1250);

            // now choose destination
            // 1. check is map open
            Pixel anchor = _scanner.scanOneFast("anchor.bmp", null, false);
            if (anchor != null) {
              // it is open
              // TODO get destination from buttons
              Pixel dest = _scanner.scanOne("dest/smallTown.bmp", null, true);
              if (dest != null) {
                LOGGER.info("Sending to Small Town...");
                _mouse.click(dest);
                _mouse.delay(350);
                Pixel destTitle = _scanner.scanOne("dest/smallTownTitle.bmp", null, false);
                if (destTitle != null) {
                  LOGGER.info("SEND POPUP OPEN...");
                  Pixel destButton = _scanner.scanOne("dest/setSail.bmp", null, true);
                  if (destButton != null) {
                    // nice. we can continue
                    shipSent = true;
                  } else {
                    LOGGER.info("Destination unreachable! Skipping...");
                    _mouse.delay(300);
                    _mouse.click(anchor);
                  }

                }
                // throw new RobotInterruptedException();
                if (!shipSent) {
                  _mouse.delay(300);
                  _mouse.click(anchor);
                  _mouse.delay(1300);
                }
              } else {
                LOGGER.info("Destination: UNKNOWN!!!");
                _mouse.delay(300);
                _mouse.click(anchor);
                _mouse.delay(1300);
              }
            }

          }

        }
      } else {
        LOGGER.info("UHOH");
      }

    } catch (AWTException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private void doIndustries() throws RobotInterruptedException {
    try {
      if (_rock != null && _buildingLocations != null && !_buildingLocations.isEmpty()) {
        for (Pixel p1 : _buildingLocations) {
          Rectangle miniArea = new Rectangle(p1.x - 14, p1.y - 12, 28, 12);
          Pixel p = _scanner.scanOneFast("buildings/whiteArrow.bmp", miniArea, false);
          if (p != null) {
            LOGGER.info("Building busy. Moving on...");
          } else {
            _mouse.click(p1);
            _mouse.delay(800);
            _mouse.mouseMove(_scanner.getParkingPoint());

            // check if popup is opened, else click again
            Rectangle area = new Rectangle(p1.x - 80, p1.y + 41, 160, 55);
            Pixel gears = _scanner.scanOneFast("buildings/gears2.bmp", area, true);
            if (gears == null) {
              LOGGER.info("click again...");
              _mouse.click(p1);
              _mouse.delay(800);
              _mouse.mouseMove(_scanner.getParkingPoint());
            }
            gears = _scanner.scanOneFast("buildings/gears2.bmp", area, true);
            if (gears != null) {
              LOGGER.info("GEARS...");
              _mouse.mouseMove(_scanner.getParkingPoint());
              _mouse.delay(1000);

              Pixel produceButton = _scanner.scanOneFast("buildings/produce.bmp", null, true);
              if (produceButton != null) {
                _mouse.delay(2000);
              } else {
                // try if gray
                produceButton = _scanner.scanOneFast("buildings/produceGray.bmp", null, true);
                if (produceButton != null) {
                  LOGGER.info("Production not possible...");
                  _scanner.scanOneFast("buildings/x.bmp", null, true);
                  _mouse.delay(1000);
                } else {
                  LOGGER.info("Building not ready...");
                  _mouse.delay(300);
                  _scanner.scanOneFast("buildings/x.bmp", null, true);

                  _mouse.delay(300);
                  _mouse.click(_scanner.getSafePoint());
                }
                

              }

            }

          }

          _mouse.delay(1000);// reduce later
          _mouse.click(_scanner.getSafePoint());
          _mouse.delay(1000);

        }
      }

    } catch (AWTException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
