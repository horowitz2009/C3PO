package com.horowitz.seaport;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.KeyEventDispatcher;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.horowitz.commons.ImageData;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.MyLogger;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.Settings;
import com.horowitz.commons.TemplateMatcher;
import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.BasicElement;
import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.Task;
import com.horowitz.seaport.model.storage.JsonStorage;

public class MainFrame extends JFrame {

  private static final long serialVersionUID = -4827959393249146870L;

  private final static Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

  private static String APP_TITLE = "Seaport v0.019";

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
  private List<Pixel> _buildingLocationsREF = new ArrayList<>();
  private List<Pixel> _buildingLocationsABS = new ArrayList<>();
  private List<Building> _buildings = new ArrayList<>();
  private MapManager _mapManager;

  private Pixel _rock;
  private Pixel _marketPos;

  private JToggleButton _shipsToggle;

  private JToggleButton _industriesToggle;
  private JToggleButton _fullScreenToggle;

  private List<Task> _tasks;

  private Task _scanTask;

  private Task _fishTask;

  private Task _shipsTask;

  private Task _buildingsTask;

  private boolean _testMode;

  private JToggleButton _slowToggle;

  public static void main(String[] args) {

    try {
      boolean isTestmode = false;
      if (args.length > 0) {
        for (String arg : args) {
          System.err.println(arg);
          if (arg.equalsIgnoreCase("test")) {
            isTestmode = true;
            break;
          }
        }
      }
      MainFrame frame = new MainFrame(isTestmode);
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

    // LOADING DATA
    try {
      _settings = Settings.createSettings("seaport.properties");
      _scanner = new ScreenScanner(_settings);
      _scanner.setDebugMode(_testMode);
      _mouse = _scanner.getMouse();
      _mapManager = new MapManager(_scanner);
      _mapManager.loadDestinations();
      _market = _mapManager.getMarket();

      _buildings = new JsonStorage().loadBuildings();
      _tasks = new ArrayList<Task>();
      _scanTask = new Task("Scan", 1);
      _fishTask = new Task("Fish", 1);
      _shipsTask = new Task("Ships", 2);
      _buildingsTask = new Task("Buildings", 1);
      _tasks.add(_scanTask);
      _tasks.add(_fishTask);
      _tasks.add(_shipsTask);
      _tasks.add(_buildingsTask);

      _matcher = _scanner.getMatcher();

      _stopAllThreads = false;

    } catch (IOException e1) {
      System.err.println("Something went wrong!");
      e1.printStackTrace();
      System.exit(1);
    }

    initLayout();

  }

  private void initLayout() {
    if (_testMode)
      APP_TITLE += " TEST";
    setTitle(APP_TITLE);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setAlwaysOnTop(true);

    JPanel rootPanel = new JPanel(new BorderLayout());
    getContentPane().add(rootPanel, BorderLayout.CENTER);

    // CONSOLE
    rootPanel.add(buildConsole(), BorderLayout.CENTER);

    // TOOLBARS
    JToolBar mainToolbar1 = createToolbar1();
    JToolBar mainToolbar2 = createToolbar2();
    List<JToolBar> mainToolbars3 = createToolbars3();
    JToolBar mainToolbar4 = createToolbar4();

    JPanel toolbars = new JPanel(new GridLayout(0, 1));
    toolbars.add(mainToolbar1);
    toolbars.add(mainToolbar2);
    for (JToolBar jToolBar : mainToolbars3) {
      toolbars.add(jToolBar);
    }
    toolbars.add(mainToolbar4);

    Box north = Box.createVerticalBox();
    north.add(toolbars);

    if (_testMode) {

      JToolBar testToolbar = createTestToolbar();
      toolbars.add(testToolbar);
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
                Pixel p = _scanner.scanOneFast(filename, null, true);
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

    }
    _mouseInfoLabel = new JLabel(" ");
    north.add(_mouseInfoLabel);
    rootPanel.add(north, BorderLayout.NORTH);

    // //KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new
    // MyKeyEventDispatcher());
  }

  private Container buildConsole() {
    final JTextArea outputConsole = new JTextArea(8, 14);

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
    return new JScrollPane(outputConsole);
  }

  private void clearBuildings() {
    _buildingLocationsABS.clear();
  }

  private void addNewBuilding() {
    try {
      Point p = _mouse.getCurrentPosition();
      Pixel pp = new Pixel(p.x, p.y);
      LOGGER.info("Position: " + pp);

      _buildingLocationsABS.add(pp);

      _buildingLocationsREF.clear();

      for (Pixel pAbs : _buildingLocationsABS) {
        int xRelative = _rock.x - pAbs.x;
        int yRelative = _rock.y - pAbs.y;

        _buildingLocationsREF.add(new Pixel(xRelative, yRelative));
      }

      LOGGER.info("saving building locations...");
      // new JsonStorage().saveBuildings(_buildingLocationsREF);
      LOGGER.info("done");

      recalcPositions(false, 1);

    } catch (Exception e) {
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      e.printStackTrace();
    }
  }

  private void recalcPositions(boolean click, int attempt) throws RobotInterruptedException {
    Pixel goodRock = new Pixel(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2 + 93,
        _scanner.getTopLeft().y + 254);

    try {
      if (!_scanner.isOptimized()) {
        scan();
      }

      if (_scanner.isOptimized()) {
        _mouse.click(_scanner.getSafePoint());
        _mouse.delay(200);
        _mouse.mouseMove(_scanner.getParkingPoint());

        boolean needRecalc = true;
        if (_rock == null) {
          _rock = _scanner.findRock();
          LOGGER.info("rock is found for the first time.");
          needRecalc = true;
        } else {
          Pixel newRock = _scanner.findRockAgain(_rock);
          needRecalc = !_rock.equals(newRock);
          _rock = newRock;
          if (!needRecalc) {
            LOGGER.info("rock is found in the same place.");
            LOGGER.info("Skipping recalc...");
          }
        }
      }

      if (_rock != null) {

        if (Math.abs(_rock.x - goodRock.x) > 5 && Math.abs(_rock.x - goodRock.y) > 5) {
          // need adjusting
          _mouse.drag(_rock.x, _rock.y, goodRock.x, goodRock.y);
          _mouse.delay(2000);
          _rock = _scanner.findRockAgain(goodRock);
        }

        LOGGER.info("Recalc positions... ");
        Pixel[] fishes = _scanner.getFishes();
        _fishes = new ArrayList<Pixel>();
        for (Pixel fish : fishes) {
          Pixel goodFish = new Pixel(_rock.x + fish.x, _rock.y + fish.y);
          _fishes.add(goodFish);
        }

        Pixel[] shipLocations = _scanner.getShipLocations();
        _shipLocations = new ArrayList<Pixel>();
        for (Pixel p : shipLocations) {
          Pixel goodP = new Pixel(_rock.x + p.x, _rock.y + p.y);
          _shipLocations.add(goodP);
        }

        // NEWEST
        _buildingLocationsABS.clear();
        for (Building b : _buildings) {
          if (b.isEnabled()) {
            Pixel p = b.getPosition();
            Pixel goodP = new Pixel(_rock.x + p.x, _rock.y + p.y);
            _buildingLocationsABS.add(goodP);
          }
        }
      } else {
        LOGGER.info("CAN'T FIND THE ROCK!!!");
        handlePopups(false);
        if (attempt <= 2)
          recalcPositions(false, ++attempt);
        else
          _rock = null; // reset the hell
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

  private boolean isRunning(String threadName) {
    boolean isRunning = false;
    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
    for (Iterator<Thread> it = threadSet.iterator(); it.hasNext();) {
      Thread thread = it.next();
      if (thread.getName().equals(threadName)) {
        isRunning = true;
        break;
      }
    }
    return isRunning;
  }

  private final class MyKeyEventDispatcher implements KeyEventDispatcher {

    public boolean dispatchKeyEvent(KeyEvent e) {
      if (!e.isConsumed()) {
        // LOGGER.info("pressed " + e.getKeyCode());
        // e.consume();

        if (e.getKeyCode() == 119 || e.getKeyCode() == 65) {// F8 or a
          LOGGER.info("pressed " + e.getKeyCode());
          if (!isRunning("HMM")) {
            Thread t = new Thread(new Runnable() {
              public void run() {
                addNewBuilding();
              }
            }, "HMM");
            t.start();
          }
        }

        if (e.getKeyCode() == 88) {// X
          // massClick(1, (int) (_scanner.getXOffset() * 1.6), true);
        }
        if (e.getKeyCode() == 67) {// C
          // massClick(1, (int) (_scanner.getXOffset() * 3), true);
        }

        if (e.getKeyCode() == 65 || e.getKeyCode() == 18) {// A or Alt
          // massClick(2, true);
        }
        if (e.getKeyCode() == 83) {// S
          // massClick(2, (int) (_scanner.getXOffset() * 1.6), true);
        }
        if (e.getKeyCode() == 68) {// D
          // massClick(2, (int) (_scanner.getXOffset() * 3), true);
        }

        if (e.getKeyCode() == 81 || e.getKeyCode() == 32) {// Q or Space
          // massClick(4, true);
        }
        if (e.getKeyCode() == 87) {// W
          // massClick(4, (int) (_scanner.getXOffset() * 1.6), true);
        }
        if (e.getKeyCode() == 69) {// E
          // massClick(4, (int) (_scanner.getXOffset() * 3), true);
        }

        if (e.getKeyCode() == 77) {// M for MAILS
          // massClick(1, (int) (_scanner.getXOffset() / 2), true);
        }

        // LOGGER.info("key pressed: " + e.getExtendedKeyCode() + " >>> " +
        // e.getKeyCode());
        e.consume();
      }
      return false;
    }
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

    // RESET BUILDINGS
    {
      AbstractAction action = new AbstractAction("Reset") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              clearBuildings();
              // try {
              // if (!_scanner.isOptimized()) {
              // scan();
              // }
              //
              // if (_scanner.isOptimized()) {
              // _mouse.savePosition();
              // locateIndustries();
              // _mouse.restorePosition();
              // } else {
              // LOGGER.info("I need to know where the game is!");
              // }
              // } catch (RobotInterruptedException e) {
              // LOGGER.log(Level.WARNING, e.getMessage());
              // e.printStackTrace();
              // } catch (IOException e) {
              // LOGGER.log(Level.WARNING, e.getMessage());
              // e.printStackTrace();
              // } catch (AWTException e) {
              // LOGGER.log(Level.WARNING, e.getMessage());
              // e.printStackTrace();
              // }
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

      _fullScreenToggle = new JToggleButton("Full screen");
      // //toolbar.add(_fullScreenToggle);
      _slowToggle = new JToggleButton("Slow");
      _slowToggle.addItemListener(new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
          boolean b = e.getStateChange() == ItemEvent.SELECTED;
          LOGGER.info("Slow mode: " + (b ? "on" : "off"));
          if (b) {
            _mouse.setDelayBetweenActions(500);
          } else {
            _mouse.setDelayBetweenActions(0);
          }
        }
      });
      toolbar.add(_slowToggle);
    }
    return toolbar;
  }

  private Destination _dest = null;

  private Destination _market;

  @SuppressWarnings("serial")
  private List<JToolBar> createToolbars3() {
    List<JToolBar> toolbars = new ArrayList<>();
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    // DESTINATIONS GO HERE
    ButtonGroup bg = new ButtonGroup();
    int itemsPerRow = 3;
    int n = 0;
    for (final Destination destination : _mapManager.getDestinations()) {

      JToggleButton toggle = new JToggleButton(new AbstractAction(destination.getName()) {

        @Override
        public void actionPerformed(ActionEvent e) {
          LOGGER.info("Destination: " + destination.getName());
          _dest = destination;
        }
      });
      bg.add(toggle);

      n++;
      if (n > itemsPerRow) {
        toolbars.add(toolbar);
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        n = 0;
      }
      toolbar.add(toggle);

    }
    return toolbars;
  }

  @SuppressWarnings("serial")
  private JToolBar createToolbar4() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    // DESTINATIONS GO HERE
    for (final Building b : _buildings) {
      final JToggleButton toggle = new JToggleButton(b.getName());
      toggle.setSelected(b.isEnabled());
      toggle.addItemListener(new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
          b.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
          LOGGER.info("Building " + b.getName() + " is now " + (b.isEnabled() ? "on" : "off"));
        }
      });
      //
      toolbar.add(toggle);
    }
    return toolbar;
  }

  @SuppressWarnings("serial")
  private JToolBar createTestToolbar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    {
      Action action = new AbstractAction("Test") {

        @Override
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              LOGGER.info("Testing...");
              if (!_scanner.isOptimized()) {
                try {
                  scan();
                } catch (RobotInterruptedException e) {
                  e.printStackTrace();
                }
              }

              if (_scanner.isOptimized()) {
                // DO THE JOB
                test();
              } else {
                LOGGER.info("I need to know where the game is!");
              }
            }
          });

          myThread.start();
        }
      };

      toolbar.add(action);
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
      LOGGER.info("Loading data...");

      // _buildingLocationsREF = new JsonStorage().loadBuildings();

      LOGGER.info("Scanning...");
      setTitle(APP_TITLE + " ...");
      boolean found = _scanner.locateGameArea(_fullScreenToggle.isSelected());
      if (found) {

        _mapManager.deserializeDestinations();
        _marketPos = null;

        LOGGER.info("Coordinates: " + _scanner.getTopLeft() + " - " + _scanner.getBottomRight());

        _scanner.zoomOut();

        // locate the rock and recalc
        // recalcPositions(false);

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

        LOGGER.info("GAME FOUND! INSOMNIA READY!");
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

  private Rectangle generateMiniArea(Pixel p) {
    return new Rectangle(p.x - 2 - 18, p.y - 50 + 35, 44, 60);
  }

  public MainFrame(boolean isTestmode) throws HeadlessException, AWTException {
    super();
    _testMode = isTestmode;

    setupLogger();

    init();
  }

  private void test() {
    setTitle(APP_TITLE + " testing");

    try {
      handlePopups(true);
      recalcPositions(false, 1);

      Pixel mapP = _scanner.scanOneFast("mapButton.bmp", null, true);
      if (mapP != null) {
        _mouse.delay(2000);

        Destination market = null;
        Pixel marketPos = null;
        for (Destination destination : _mapManager.getDestinations()) {
          if (destination.getName().equals("Market"))
            market = destination;

          ImageData id = destination.getImageData();
          Pixel p = _scanner.scanOneFast(id, null, false);
          if (p != null) {
            LOGGER.info("found " + destination.getName());
            destination.setRelativePosition(p);
          } else {
            LOGGER.info("didn't found " + destination.getName());
          }
          // _mouse.delay(1000);
        }
        LOGGER.info("done destinations");

        if (market != null) {
          Pixel pm = market.getRelativePosition();
          for (Destination destination : _mapManager.getDestinations()) {
            if (!destination.getName().equals("Market")) {
              Pixel pp = destination.getRelativePosition();
              pp.x = -pm.x + pp.x;
              pp.y = -pm.y + pp.y;
            }
          }
          _mapManager.saveDestinations();
          _mouse.drag(pm.x, pm.y, pm.x + 60, pm.y);
          _mouse.delay(2000);
          pm = _scanner.scanOneFast(market.getImageData(), null, false);
          market.setRelativePosition(new Pixel(0, 0));
          if (pm != null) {
            for (Destination destination : _mapManager.getDestinations()) {
              Pixel pp = destination.getRelativePosition();
              LOGGER.info(destination.getName() + ": " + destination.getRelativePosition());
              Pixel absP = new Pixel(pm.x + pp.x, pm.y + pp.y);
              LOGGER.info(destination.getName() + ": " + absP);
              _mouse.mouseMove(absP);
              _mouse.delay(2000);
            }
          }
        }
      }
      // TODO more

      LOGGER.info("TEST DONE");
      setTitle(APP_TITLE + " DONE");

    } catch (RobotInterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    }

  }

  private void doMagic() {
    assert _scanner.isOptimized();
    setTitle(APP_TITLE + " RUNNING");
    _stopAllThreads = false;

    try {
      long start = System.currentTimeMillis();
      do {
        // 1. SCAN
        handlePopups(false);
        recalcPositions(false, 1);

        // 2. FISH
        LOGGER.info("Fish...");
        // long now = System.currentTimeMillis();
        // if (now - start > 11*60000) {
        doFishes();
        // start = System.currentTimeMillis();
        // }

        // 3. SHIPS
        if (_shipsToggle.isSelected()) {
          LOGGER.info("Ships...");
          doShips();
          // handlePopups(true);//TEMPORARY
          // recalcPositions(false, 1);
        }

        // 4. BUILDINGS
        if (_industriesToggle.isSelected()) {
          LOGGER.info("Buildings...");
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

  private void handlePopups(boolean fast) throws RobotInterruptedException {
    try {
      LOGGER.info("Popups...");
      boolean found = false;
      Pixel p = null;

      found = _scanner.scanOneFast("anchor.bmp", null, true) != null;

      if (found)
        return;
      // reload
      long start = System.currentTimeMillis();
      long now, t1 = 0, t2 = 0, t3, t4;
      if (!fast) {
        Rectangle area = _scanner.generateWindowedArea(412, 550);
        p = _scanner.scanOneFast("reload.bmp", area, true);
        now = System.currentTimeMillis();

        t1 = now - start;
        t2 = now;
        if (p == null) {
          p = _scanner.scanOneFast("reload2.bmp", area, true);
          now = System.currentTimeMillis();
          t2 = now - t2;
        }

        found = p != null;
        if (found) {
          LOGGER.info("Game crashed. Reloading...");
          _mouse.delay(8000);
          return;
        } else {
          _mouse.delay(150);
        }
      }

      t3 = now = System.currentTimeMillis();
      found = _scanner.scanOneFast("buildings/x.bmp", null, true) != null;
      now = System.currentTimeMillis();
      t3 = now - t3;
      _mouse.delay(150);
      t4 = now = System.currentTimeMillis();
      found = _scanner.scanOneFast("anchor.bmp", null, true) != null;
      now = System.currentTimeMillis();
      t4 = now - t4;
      if (found)
        _mouse.delay(450);
      now = System.currentTimeMillis();
      LOGGER.info("[" + t1 + ",  " + t2 + ",  " + t3 + ",  " + t4 + "], TOTAL: " + (now - start) + " - " + found);
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
      LOGGER.info("Fishes empty! Why?");
    }
  }

  private int doShips() throws RobotInterruptedException {
    int numShipsSent = 0;
    try {
      if (_shipLocations != null && !_shipLocations.isEmpty() && _dest != null) {
        for (Pixel pixel : _shipLocations) {

          boolean shipSent = false;
          _mouse.click(pixel);
          _mouse.delay(250);

          Destination dest = _dest;
          // check for menu
          Rectangle miniArea = new Rectangle(pixel.x - 15, pixel.y + 65, 44, 44);
          Pixel pin = _scanner.scanOneFast(_scanner.getImageData("pin.bmp"), miniArea, false);
          if (pin != null) {
            // scan the name
            Rectangle nameArea = new Rectangle(pin.x - 75, pin.y - 67, 150, 40);
            Pixel nameP = _scanner.scanOne("ships/MaryRoseS.bmp", nameArea, false);
            if (nameP != null) {
              // it's Mary Rose
              dest = _mapManager.getDestination("Coastline");
              LOGGER.info("Mary Rose ...");
            }

            _mouse.click(pin);
            _mouse.delay(50);
            _mouse.mouseMove(_scanner.getParkingPoint());
            _mouse.delay(500);

            // now choose destination
            // 1. check is map open
            Pixel anchor = _scanner.scanOneFast("anchor.bmp", null, false);
            if (anchor != null) {
              // MAP ZONE
              if (_marketPos == null) {
                LOGGER.info("Looking for market for the first time");

                Rectangle smallerArea = _scanner.generateWindowedArea(260, 500);
                smallerArea.x += 50 + _scanner.getTopLeft().x;
                smallerArea.width = 260;
                smallerArea.y = _scanner.getTopLeft().y + _scanner.getGameHeight() / 2;
                smallerArea.height = _scanner.getGameHeight() / 2;
                _marketPos = _scanner.scanOneFast(_market.getImageData(), smallerArea, false);
                if (_marketPos == null) {
                  _marketPos = _scanner.scanOneFast(_market.getImageData(), null, false);
                  LOGGER.info("DAMMMMMMMMMMMMMMMMMMMMMMMMN");
                } else {
                  LOGGER.info("BINGO");
                }
              } else {
                // int x = oldRock.x - rockData.get_xOff();
                // int y = oldRock.y - rockData.get_yOff();

                Rectangle areaSpec = new Rectangle(_marketPos.x - 10, _marketPos.y - 10, _market.getImageData()
                    .getImage().getWidth() + 20, _market.getImageData().getImage().getHeight() + 20);

                Pixel newMarketPos = _scanner.scanOneFast(_market.getImageData(), areaSpec, false);
                if (newMarketPos == null)
                  newMarketPos = _scanner.scanOneFast(_market.getImageData(), null, false);

                _marketPos = newMarketPos;
                if (_marketPos.equals(newMarketPos)) {
                  LOGGER.info("Market found in the same place.");
                }
              }

              Pixel destP = null;
              shipSent = sendShip(dest, true);
              if (shipSent)
                numShipsSent++;
            }

          }
          if (shipSent) {
            _mouse.delay(300);
            _scanner.scanOneFast("anchor.bmp", null, true);
            _mouse.delay(300);
            _mouse.mouseMove(_scanner.getParkingPoint());
            _mouse.delay(1250);
          }
          // next slot
          _mouse.delay(750);
          handlePopups(true);

        }
      } else {
        LOGGER.info("UHOH doing ships");
      }

    } catch (AWTException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return numShipsSent;
  }

  private boolean sendShip(Destination dest, boolean redirect) throws AWTException, RobotInterruptedException,
      IOException {
    boolean success = false;
    Pixel destP;
    if (!dest.getName().equals("Market") && _marketPos != null) {
      int x = _marketPos.x + dest.getRelativePosition().x - _market.getImageData().get_xOff() - 25;
      int y = _marketPos.y + dest.getRelativePosition().y - _market.getImageData().get_yOff() - 25;
      Rectangle destArea = new Rectangle(x, y, 153 + 20 + 30, 25 + 30);
      LOGGER.info("Using custom area for " + dest.getImage());
      destP = _scanner.scanOneFast(dest.getImageData(), destArea, false);
    } else {
      if (_marketPos != null)
        destP = _marketPos;
      else
        destP = _scanner.scanOneFast(dest.getImageData(), null, false);
    }

    if (destP != null) {
      LOGGER.info("Sending to " + dest.getName() + "...");
      _mouse.click(destP);
      _mouse.mouseMove(_scanner.getParkingPoint());
      _mouse.delay(400);

      Pixel destTitle = _scanner.scanOneFast(dest.getImageDataTitle(), null, false);

      if (destTitle != null) {
        LOGGER.info("SEND POPUP OPEN...");
        _mouse.mouseMove(_scanner.getParkingPoint());
        _mouse.delay(100);

        if (dest.getName().equals("Market")) {

        }

        Pixel destButton = _scanner.scanOneFast("dest/setSail.bmp", null, false);
        if (destButton != null) {
          // nice. we can continue
          if (dest.getName().equals("Market")) {
            LOGGER.info("Market! I choose coins...");
            Pixel coins = new Pixel(destTitle);
            coins.y += 228;
            _mouse.click(coins);
            _mouse.delay(250);
            success = true;
          }

          _mouse.click(destButton);
        } else {
          if (redirect)
            if (dest.getName().equals("Market")) {
              Pixel destButtonGray = _scanner.scanOneFast("dest/setSailGray.bmp", null, false);
              if (destButtonGray != null) {
                LOGGER.info("Material not available! Go to buy it...");
                boolean found = _scanner.scanOneFast("buildings/x.bmp", null, true) != null;
                success = sendShip(_mapManager.getDestination("Cocoa Plant"), false);
                // SEND to Cocoa plant (for now)

              }
            }
          LOGGER.info("Destination unreachable! Skipping...");
          _mouse.delay(300);
        }

      }
    } else {
      LOGGER.info("Destination: UNKNOWN!!!");
    }
    return success;
  }

  private void locateIndustries() throws IOException, AWTException {
    try {
      LOGGER.info("Locating buildings. Please wait!");

      _buildingLocationsREF.clear();

      Pixel p = null;

      List<Pixel> whites = _scanner.scanMany("buildings/whiteArrow.bmp", null, false);

      LOGGER.info("buildings found: " + whites.size());

      for (Pixel pAbs : whites) {
        int xRelative = _rock.x - pAbs.x;
        int yRelative = _rock.y - pAbs.y;

        _buildingLocationsREF.add(new Pixel(xRelative, yRelative));
      }

      LOGGER.info("saving building locations...");
      // new JsonStorage().saveBuildings(_buildingLocationsREF);
      LOGGER.info("done");

    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      LOGGER.info("interrupted");
    }
  }

  private void doIndustriesOld() throws RobotInterruptedException {
    try {
      if (_rock != null && _buildingLocationsABS != null && !_buildingLocationsABS.isEmpty()) {
        for (Pixel p1 : _buildingLocationsABS) {
          _mouse.click(p1);
          _mouse.delay(100);
          _mouse.mouseMove(_scanner.getParkingPoint());
          Rectangle area = new Rectangle(p1.x - 80, p1.y + 41, 160, 55);
          Pixel gears = _scanner.scanOneFast("buildings/gears2.bmp", area, true);
          if (gears != null) {
            _mouse.mouseMove(_scanner.getParkingPoint());
            LOGGER.info("GEARS...");
            Pixel produceButton = _scanner.scanOne("buildings/produce.bmp", null, true);
            if (produceButton != null) {
              // nice. we can continue
              // shipSent = true;
            } else {
              // try if gray
              produceButton = _scanner.scanOne("buildings/produceGray.bmp", null, false);
              if (produceButton != null) {
                LOGGER.info("Production not possible...");
                area = _scanner.generateWindowedArea(400, 550);
                _scanner.scanOne("buildings/x.bmp", area, true);
              } else {
                LOGGER.info("Building not ready...");
                _mouse.delay(300);
                _mouse.click(_scanner.getSafePoint());
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

  private void doIndustries() throws RobotInterruptedException {
    try {
      if (_rock != null && _buildingLocationsABS != null && !_buildingLocationsABS.isEmpty()) {
        for (Pixel p1 : _buildingLocationsABS) {
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
            // if (gears == null) {
            // LOGGER.info("click again...");
            // _mouse.click(p1);
            // _mouse.delay(800);
            // _mouse.mouseMove(_scanner.getParkingPoint());
            // gears = _scanner.scanOneFast("buildings/gears2.bmp", area, true);
            // }
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
