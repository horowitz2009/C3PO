package com.horowitz.seaport;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.horowitz.commons.ImageData;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.MyLogger;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.Service;
import com.horowitz.commons.Settings;
import com.horowitz.commons.TemplateMatcher;
import com.horowitz.monitor.GameHealthMonitor;
import com.horowitz.ocr.OCRB;
import com.horowitz.seaport.dest.BuildingManager;
import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.AbstractGameProtocol;
import com.horowitz.seaport.model.BalancedShipProtocolExecutor;
import com.horowitz.seaport.model.BarrelsProtocol;
import com.horowitz.seaport.model.Building;
import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.DispatchEntry;
import com.horowitz.seaport.model.FishingProtocol;
import com.horowitz.seaport.model.IBarrelsProtocol;
import com.horowitz.seaport.model.ImageBarrelsProtocol;
import com.horowitz.seaport.model.ManualBuildingsProtocol;
import com.horowitz.seaport.model.ProtocolEntry;
import com.horowitz.seaport.model.Ship;
import com.horowitz.seaport.model.ShipProtocol;
import com.horowitz.seaport.model.Task;
import com.horowitz.seaport.model.storage.JsonStorage;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.ColorFiltering;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -4827959393249146870L;

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private static String APP_TITLE = "Seaport v153";

	private Settings _settings;
	private Stats _stats;
	private MouseRobot _mouse;
	private ScreenScanner _scanner;

	private JLabel _mouseInfoLabel;

	private CaptureDialog captureDialog;

	private boolean _stopAllThreads;

	private JTextField _findThisTF;

	private TemplateMatcher _matcher;

	private MapManager _mapManager;
	private BuildingManager _buildingManager;

	private JToggleButton _fishToggle;
	private JToggleButton _shipsToggle;

	private JToggleButton _industriesToggle;
	// private JToggleButton _barrelsAToggle;
	private JToggleButton _barrelsSToggle;
	// private JToggleButton _xpToggle;

	private List<Task> _tasks;

	private Task _fishTask;

	private Task _shipsTask;

	private Task _buildingsTask;
	private Task _barrelsTask;

	private boolean _testMode;

	private JToggleButton _autoRefreshToggle;
	private JToggleButton _slowToggle;

	private ShipProtocolManagerUI _shipProtocolManagerUI;

	private OCRB _ocr;

	private boolean _doOCR;

	private IBarrelsProtocol _barrelsProtocol;

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

			frame.setVisible(true);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("serial")
	private void init() throws AWTException {

		try {

			_filesTracked = new Hashtable<String, Long>();
			File f = new File("data/destinationsNEW.json");
			_filesTracked.put("data/destinationsNEW.json", f.lastModified());
			f = new File("data/ships.json");
			_filesTracked.put("data/ships.json", f.lastModified());
			f = new File("data/shipProtocols.json");
			_filesTracked.put("data/shipProtocols.json", f.lastModified());

			_ocr = new OCRB("ocr/digit");
			_ocr.setErrors(1);

			// LOADING DATA
			_settings = Settings.createSettings("seaport.properties");
			if (!_settings.containsKey("fish")) {
				setDefaultSettings();
			}

			_doOCR = _settings.getBoolean("doOCR", true);
			_stats = new Stats();
			_scanner = new ScreenScanner(_settings);
			_scanner.setDebugMode(_testMode);
			_matcher = _scanner.getMatcher();
			_mouse = _scanner.getMouse();
			_mapManager = new MapManager(_scanner);
			_mapManager.loadData();

			_buildingManager = new BuildingManager(_scanner);
			_buildingManager.loadData();

			_tasks = new ArrayList<Task>();

			// SCAN TASK - scanning and fixing game could be something out of tasks
			// list
			// _scanTask = new Task("Scan", 1);
			// _tasks.add(_scanTask);

			// FISHING TASK
			_fishTask = new Task("Fish", 1);
			FishingProtocol fishingProtocol = new FishingProtocol(_scanner, _mouse);
			_fishTask.setProtocol(fishingProtocol);
			_tasks.add(_fishTask);

			// SHIPS TASK
			_shipsTask = new Task("Ships", 2);
			_shipsTask.setEnabled(true);
			_shipProtocolExecutor = new BalancedShipProtocolExecutor(_scanner, _mouse, _mapManager, _settings);
			_shipProtocolExecutor.addPropertyChangeListener(new StatsListener());
			_mapManager.addPropertyChangeListener("TRIP_REGISTERED", new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					loadStats();
				}
			});

			_shipsTask.setProtocol(_shipProtocolExecutor);
			_tasks.add(_shipsTask);

			// BUILDING TASK
			_buildingsTask = new Task("Buildings", 1);

			ManualBuildingsProtocol buildingsProtocol = new ManualBuildingsProtocol(_scanner, _mouse, _buildingManager);
			_buildingsTask.setProtocol(buildingsProtocol);
			_tasks.add(_buildingsTask);

			_barrelsTask = new Task("Barrels", 1);
			if (_settings.getBoolean("barrels.image", true)) {
				_barrelsProtocol = new ImageBarrelsProtocol(_scanner, _mouse, _settings);
			} else {
				_barrelsProtocol = new BarrelsProtocol(_scanner, _mouse, _settings);
			}
			_barrelsProtocol.setBlobMin(_settings.getInt("barrels.blobMin", 15 * 20));
			_barrelsProtocol.setBlobMax(_settings.getInt("barrels.blobMax", 28 * 32));
			_barrelsTask.setProtocol((AbstractGameProtocol)_barrelsProtocol);
			
//			ImageBarrelsProtocol imageBarrelsProtocol = new ImageBarrelsProtocol(_scanner, _mouse, _settings);
//			_barrelsTask.setProtocol(imageBarrelsProtocol);
			_tasks.add(_barrelsTask);

			_stopAllThreads = false;

		} catch (IOException e1) {
			System.err.println("Something went wrong!");
			e1.printStackTrace();
			System.exit(1);
		}

		initLayout();
		loadStats();

		reapplySettings();

		runSettingsListener();
		_monitor = new GameHealthMonitor(_settings);
		// _monitor.setRunnable(new Runnable() {
		// public void run() {
		// try {
		// _scanner.handleErrorPopups();
		// } catch (RobotInterruptedException | GameErrorException e) {
		// _monitor.triggerAlert();
		// }
		// }
		// });
		_monitor.addPropertyChangeListener("NO_ACTIVITY", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				bounce();
			}
		});
		_mapManager.addPropertyChangeListener("TRIP_REGISTERED", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				_monitor.pingActivity();
			}
		});

		// _monitor.startMonitoring();

	}

	private void setDefaultSettings() {
		_settings.setProperty("fish", "true");
		_settings.setProperty("ships", "true");
		_settings.setProperty("industries", "true");
		_settings.setProperty("slow", "false");
		_settings.setProperty("autoSailors", "false");
		_settings.setProperty("barrelsS", "false");
		_settings.setProperty("Buildings.SawMill1", "false");
		_settings.setProperty("Buildings.SawMill2", "true");
		_settings.setProperty("Buildings.Quarry", "true");
		_settings.setProperty("Buildings.Foundry", "true");
		_settings.setProperty("ShipProtocol", "SINGLE");
		_settings.setProperty("autoSailors.speedProtocol", "SINGLE");
		_settings.setProperty("autoSailors.defaultProtocol", "DEFAULT");
		_settings.setProperty("autoSailors.upperThreshold", "1000");
		_settings.setProperty("autoSailors.lowerThreshold", "600");
		_settings.saveSettingsSorted();
	}

	private void initLayout() {

		if (_testMode)
			APP_TITLE += " TEST";
		setTitle(APP_TITLE);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setAlwaysOnTop(true);

		JPanel rootPanel = new JPanel(new BorderLayout());
		getContentPane().add(rootPanel, BorderLayout.CENTER);

		_console = buildConsole();
		rootPanel.add(_console, BorderLayout.CENTER);

		_mainToolbar1 = createToolbar1();
		_mainToolbar2 = createToolbar2();
		_mainToolbar4 = createToolbar4();

		JPanel toolbars = new JPanel(new GridLayout(0, 1));
		// JPanel toolbars = new JPanel(new FlowLayout(FlowLayout.LEFT));
		toolbars.add(_mainToolbar1);
		toolbars.add(_mainToolbar2);
		// for (JToolBar jToolBar : mainToolbars3) {
		// toolbars.add(jToolBar);
		// }
		toolbars.add(_mainToolbar4);

		// toolbars.add(createToolbar5());

		Box north = Box.createVerticalBox();
		north.add(toolbars);
		_statsPanel = createStatsPanel();
		_shipProtocolManagerPanel = createShipProtocolManagerPanel();
		north.add(_statsPanel);
		north.add(_shipProtocolManagerPanel);

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

		_shipLog = new JTextArea(5, 10);
		rootPanel.add(new JScrollPane(_shipLog), BorderLayout.SOUTH);

		_mapManager.addPropertyChangeListener("TRIP_REGISTERED", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String text = _shipLog.getText();
				if (text.length() > 3000) {
					int ind = text.indexOf("\n", 2000);
					if (ind <= 0)
						ind = 2000;
					text = text.substring(ind);
					_shipLog.setText(text);
				}

				DispatchEntry de = (DispatchEntry) evt.getNewValue();
				Calendar now = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				_shipLog.append(sdf.format(now.getTime()) + "  " + de.getDest() + "  " + de.getShip() + "\n");
				_shipLog.setCaretPosition(_shipLog.getDocument().getLength());

			}
		});

		// //KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new
		// MyKeyEventDispatcher());
	}

	private Map<String, JLabel> _labels = new HashMap<String, JLabel>();

	private Component createStatsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagConstraints gbc2 = new GridBagConstraints();
		JLabel l;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc2.gridx = 2;
		gbc2.gridy = 1;

		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.WEST;
		gbc2.insets = new Insets(2, 4, 2, 2);
		gbc2.anchor = GridBagConstraints.EAST;

		// S
		panel.add(new JLabel("S:"), gbc);
		l = new JLabel(" ");
		_labels.put("S", l);
		panel.add(l, gbc2);

		// C
		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("C:"), gbc);
		l = new JLabel(" ");
		_labels.put("C", l);
		panel.add(l, gbc2);

		// G
		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("G:"), gbc);
		l = new JLabel(" ");
		_labels.put("G", l);
		panel.add(l, gbc2);

		// BB
		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("BB:"), gbc);
		l = new JLabel(" ");
		_labels.put("BB", l);
		panel.add(l, gbc2);

		gbc.insets = new Insets(2, 12, 2, 2);
		gbc.gridx = 3;
		gbc2.gridx = 4;
		gbc.gridy = 0;
		gbc2.gridy = 0;

		// BS
		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("BS:"), gbc);
		l = new JLabel(" ");
		_labels.put("BS", l);
		panel.add(l, gbc2);

		// CA
		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("CA:"), gbc);
		l = new JLabel(" ");
		_labels.put("CA", l);
		panel.add(l, gbc2);

		// MX
		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("MX:"), gbc);
		l = new JLabel(" ");
		_labels.put("MX", l);
		panel.add(l, gbc2);

		// F
		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("F:"), gbc);
		l = new JLabel(" ");
		_labels.put("F", l);
		panel.add(l, gbc2);

		gbc.insets = new Insets(2, 12, 2, 2);
		gbc.gridx = 5;
		gbc2.gridx = 6;
		gbc.gridy = 0;
		gbc2.gridy = 0;

		// E
		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("E:"), gbc);
		l = new JLabel(" ");
		_labels.put("E", l);
		panel.add(l, gbc2);

		// IS
		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("IS:"), gbc);
		l = new JLabel(" ");
		_labels.put("IS", l);
		panel.add(l, gbc2);

		// A
		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("A:"), gbc);
		l = new JLabel(" ");
		_labels.put("A", l);
		panel.add(l, gbc2);

		gbc.gridx = 7;
		gbc2.gridx = 8;
		gbc.gridy = 0;
		gbc2.gridy = 0;

		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("R:"), gbc);
		l = new JLabel(" ");
		_labels.put("R", l);
		panel.add(l, gbc2);

		gbc.gridy++;
		gbc2.gridy++;
		panel.add(new JLabel("N:"), gbc);
		l = new JLabel(" ");
		_labels.put("N", l);
		panel.add(l, gbc2);

		// gbc.gridy++;
		// gbc2.gridy++;
		// panel.add(new JLabel("RM:"), gbc);
		// l = new JLabel(" ");
		// _labels.put("RM", l);
		// panel.add(l, gbc2);
		//
		// gbc.gridy++;
		// gbc2.gridy++;
		// panel.add(new JLabel("IM:"), gbc);
		// l = new JLabel(" ");
		// _labels.put("IM", l);
		// panel.add(l, gbc2);

		// FAKE
		gbc2.gridx++;
		gbc2.gridy++;
		gbc2.weightx = 1.0f;
		gbc2.weighty = 1.0f;
		panel.add(new JLabel(""), gbc2);

		return panel;
	}

	private ShipProtocol _shipProtocol;
	private String _lastProtocolName;
	private BalancedShipProtocolExecutor _shipProtocolExecutor;

	private JToggleButton _autoSailorsToggle;

	private JToolBar _buildingsToolbar;

	private JToggleButton _pingToggle;
	private JToggleButton _ping2Toggle;
	private JToggleButton _ping3Toggle;

	public long _lastTime;

	private JToggleButton _fullScreenToggle;
	private JToggleButton _miniLayoutToggle;

	private JPanel createShipProtocolManagerPanel() {
		_shipProtocolManagerUI = new ShipProtocolManagerUI(_mapManager);
		_shipProtocolManagerUI.addListSelectionListener(new ListSelectionListener() {

			@SuppressWarnings("rawtypes")
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					JList list = (JList) e.getSource();
					_shipProtocol = (ShipProtocol) list.getSelectedValue();
					_shipProtocolExecutor.setShipProtocol(_shipProtocol);
					if (_shipProtocol != null) {
						_settings.setProperty("ShipProtocol", _shipProtocol.getName());
						_settings.saveSettingsSorted();
					}
				}
			}
		});
		return _shipProtocolManagerUI;
	}

	private Container buildConsole() {
		final JTextArea outputConsole = new JTextArea(8, 14);

		Handler handler = new Handler() {

			@Override
			public void publish(LogRecord record) {
				String text = outputConsole.getText();
				if (text.length() > 3000) {
					int ind = text.indexOf("\n", 2000);
					if (ind <= 0)
						ind = 2000;
					text = text.substring(ind);
					outputConsole.setText(text);
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

		return new JScrollPane(outputConsole);
	}

	/**
	 * 
	 * @param click
	 * @param attempt
	 * @throws RobotInterruptedException
	 * @throws GameErrorException
	 * @deprecated
	 */
	private void recalcPositions(boolean click, int attempt) throws RobotInterruptedException, GameErrorException {
		try {
			if (!_scanner.isOptimized()) {
				scan();
			}

			if (_scanner.isOptimized()) {
				_mouse.click(_scanner.getSafePoint());
				_mouse.delay(200);
				_mouse.mouseMove(_scanner.getParkingPoint());

				_scanner.checkAndAdjustRock();
			}
			Pixel r = _scanner.getRock();
			if (r != null) {

				LOGGER.info("Recalc positions... ");
				for (Task task : _tasks) {
					task.update();
				}
			} else {
				LOGGER.info("CAN'T FIND THE ROCK!!!");
				handlePopups();
				if (attempt <= 2)
					recalcPositions(false, ++attempt);
				else
					r = null; // reset the hell
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	private void reload() {
		LOGGER.info("Reloading data...");

		try {
			_mapManager.loadData();
			_mapManager.update();
			// _buildingManager.loadData();
		} catch (IOException | RobotInterruptedException e) {
			LOGGER.warning("Error loading data: " + e.getMessage());
			e.printStackTrace();
		}

		_shipProtocolManagerUI.reload();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// reapplySettings();
				String sp = _settings.getProperty("ShipProtocol", "DEFAULT");
				if (!sp.equals(_shipProtocol != null ? _shipProtocol.getName() : "")) {
					setProtocol(sp);
				}

			}
		});

		LOGGER.info("Reloading protocols DONE");

	}

	private void reset() {
		try {
			_stats.clear();
			_mapManager.resetDispatchEntries();
			loadStats();
		} catch (IOException e1) {
			LOGGER.info("Failed to reset entries!");
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

	private final class StatsListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("SHIP_SENT")) {
				Destination dest = (Destination) evt.getOldValue();
				Ship ship = (Ship) evt.getNewValue();
				_stats.registerShip(ship);
				_stats.registerDestination(dest);

				_lastTime = System.currentTimeMillis();
				LOGGER.info("SHIPS SENT: " + _stats.getTotalShipsSent());
			}

		}
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
								// //addNewBuilding();
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
					runMagic();
				}

			};
			mainToolbar1.add(action);
		}
		// SCAN AND RUN MAGIC
		{
			AbstractAction action = new AbstractAction("SRun") {
				public void actionPerformed(ActionEvent e) {
					_mapManager.reset();
					_scanner.reset();
					runMagic();
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
							LOGGER.info("Stopping all processes...");
							_stopAllThreads = true;
							_monitor.stopMonitoring();
						}
					});

					myThread.start();
				}
			};
			mainToolbar1.add(action);
		}

		// RECORD
		{
			AbstractAction action = new AbstractAction("Reload") {
				public void actionPerformed(ActionEvent e) {
					Thread myThread = new Thread(new Runnable() {
						@Override
						public void run() {
							reload();
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
							reset();
						}

					});

					myThread.start();
				}
			};
			mainToolbar1.add(action);
		}
		// TEST Button
		{
			AbstractAction action = new AbstractAction("T") {
				public void actionPerformed(ActionEvent e) {
					Thread myThread = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								//testMap();
								_scanner.findRockAgain(_scanner.getRock());
							} catch (Exception e) {
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
			// SHIPS
			_fishToggle = new JToggleButton("Fish");
			// _fishToggle.setSelected(true);
			_fishToggle.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					_fishTask.setEnabled(b);
					_settings.setProperty("fish", "" + b);
					_settings.saveSettingsSorted();
				}
			});
			// toolbar.add(_fishToggle);

			// SHIPS
			_shipsToggle = new JToggleButton("Ships");
			// _shipsToggle.setSelected(true);
			_shipsToggle.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					_shipsTask.setEnabled(b);
					_settings.setProperty("ships", "" + b);
					_settings.saveSettingsSorted();
				}
			});
			toolbar.add(_shipsToggle);

			// BUILDINGS
			_industriesToggle = new JToggleButton("Industries");
			// _industriesToggle.setSelected(true);
			_industriesToggle.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					_buildingsTask.setEnabled(b);
					_settings.setProperty("industries", "" + b);
					_settings.saveSettingsSorted();

				}
			});
			// toolbar.add(_industriesToggle);

			// BARRELS
			_barrelsSToggle = new JToggleButton("BS");
			// _industriesToggle.setSelected(true);
			_barrelsSToggle.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					// if (b && _barrelsAToggle.isSelected())
					// _barrelsAToggle.setSelected(false);

					_barrelsTask.setEnabled(b);
					_settings.setProperty("barrelsS", "" + b);
					_settings.saveSettingsSorted();

				}
			});
			toolbar.add(_barrelsSToggle);

			// _barrelsAToggle = new JToggleButton("BA");
			// // _industriesToggle.setSelected(true);
			// _barrelsAToggle.addItemListener(new ItemListener() {
			// @Override
			// public void itemStateChanged(ItemEvent e) {
			// boolean b = e.getStateChange() == ItemEvent.SELECTED;
			// if (b && _barrelsSToggle.isSelected())
			// _barrelsSToggle.setSelected(false);
			// _settings.setProperty("barrelsA", "" + b);
			// _settings.saveSettingsSorted();
			//
			// }
			// });
			// toolbar.add(_barrelsAToggle);

			_fullScreenToggle = new JToggleButton("FS");
			_fullScreenToggle.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					LOGGER.info("Full Screen mode: " + (b ? "on" : "off"));
					_settings.setProperty("fullScreen", "" + b);
					_settings.saveSettingsSorted();
				}
			});
			// _slowToggle.setSelected(false);
			toolbar.add(_fullScreenToggle);

			_miniLayoutToggle = new JToggleButton("ML");
			_miniLayoutToggle.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					LOGGER.info("Mini Layout mode: " + (b ? "on" : "off"));
					_settings.setProperty("miniLayout", "" + b);
					_settings.saveSettingsSorted();
					transformLayout();
				}
			});
			// _slowToggle.setSelected(false);
			toolbar.add(_miniLayoutToggle);

			_autoRefreshToggle = new JToggleButton("AR");
			_autoRefreshToggle.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					LOGGER.info("Auto Refresh mode: " + (b ? "on" : "off"));
					_settings.setProperty("autoRefresh", "" + b);
					_settings.saveSettingsSorted();
				}
			});
			// _slowToggle.setSelected(false);
			toolbar.add(_autoRefreshToggle);

			_autoSailorsToggle = new JToggleButton("AS");
			// _autoSailorsToggle.setSelected(false);
			_autoSailorsToggle.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					LOGGER.info("AutoSailors mode: " + (b ? "on" : "off"));
					if (!b) {
						_speedTime = null;
					}
					_settings.setProperty("autoSailors", "" + b);
					_settings.saveSettingsSorted();

				}
			});

			// toolbar.add(_autoSailorsToggle);

			// /////////////

			_slowToggle = new JToggleButton("SL");
			_slowToggle.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					if (b)
						_mouse.setMode(MouseRobot.SLOW);
					else
						_mouse.setMode(MouseRobot.NORMAL);

					LOGGER.info("Slow mode: " + (b ? "on" : "off"));
					_settings.setProperty("slow", "" + b);
					_settings.saveSettingsSorted();
				}
			});
			// _slowToggle.setSelected(false);
			toolbar.add(_slowToggle);

			// /////////////

			_pingToggle = new JToggleButton("P1");
			// _autoSailorsToggle.setSelected(false);
			_pingToggle.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					LOGGER.info("Ping: " + (b ? "on" : "off"));
					_settings.setProperty("ping", "" + b);
					_settings.saveSettingsSorted();

				}
			});

			toolbar.add(_pingToggle);

			_ping2Toggle = new JToggleButton("P2");
			// _autoSailorsToggle.setSelected(false);
			_ping2Toggle.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					//_barrelsProtocol.setCapture(false);// TODO OFF FOR GOOD!!!! move it to settings, motherfucker!
					LOGGER.info("Ping2: " + (b ? "on" : "off"));
					_settings.setProperty("ping2", "" + b);
					_settings.saveSettingsSorted();

				}
			});

			toolbar.add(_ping2Toggle);

			_ping3Toggle = new JToggleButton("P3");
			// _autoSailorsToggle.setSelected(false);
			_ping3Toggle.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean b = e.getStateChange() == ItemEvent.SELECTED;
					LOGGER.info("Ping3: " + (b ? "on" : "off"));
					_settings.setProperty("ping3", "" + b);
					_settings.saveSettingsSorted();

				}
			});

			toolbar.add(_ping3Toggle);

			// _xpToggle = new JToggleButton("XP");
			// _xpToggle.setSelected(_mapManager.getMarketStrategy().equals("XP"));
			// _xpToggle.addItemListener(new ItemListener() {
			//
			// @Override
			// public void itemStateChanged(ItemEvent e) {
			// boolean b = e.getStateChange() == ItemEvent.SELECTED;
			// String strategy = b ? "XP" : "COINS";
			// LOGGER.info("MARKET STRATEGY: " + strategy);
			// _mapManager.setMarketStrategy(strategy);
			// }
			// });
			// toolbar.add(_xpToggle);

		}
		return toolbar;
	}

	protected void transformLayout() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				boolean vis = !_settings.getBoolean("miniLayout", false);

				_mainToolbar4.setVisible(vis);
				_shipLog.setVisible(vis);
				_statsPanel.setVisible(vis);
				_shipProtocolManagerPanel.setVisible(vis);
				_console.setVisible(vis);
				resizeFrame(vis);
				if (_settings.getBoolean("miniLayout", false)) {
				} else {
				}
			}
		});
		t.start();
	}

	@SuppressWarnings("serial")
	private List<JToolBar> createToolbars3() {
		List<JToolBar> toolbars = new ArrayList<>();
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		// DESTINATIONS GO HERE
		ButtonGroup bg = new ButtonGroup();

		JToggleButton toggle;

		// // COCOA 1
		// toggle = new JToggleButton("Cocoa 1.0");
		// toggle.addItemListener(new ItemListener() {
		//
		// @Override
		// public void itemStateChanged(ItemEvent e) {
		// LOGGER.info("Cocoa Protocol 1.0: ");
		// LOGGER.info("Send all ships to Cocoa plant, then all to market");
		// LOGGER.info("Time: 2h 30m ");
		// _shipsTask.setProtocol(_cocoaProtocol1);
		// _shipsTask.getProtocol().update();
		// }
		// });
		//
		// bg.add(toggle);
		// toolbar.add(toggle);

		// // COCOA 2
		// toggle = new JToggleButton("Cocoa 2.0");
		// JToggleButton selected = toggle;
		// toggle.addItemListener(new ItemListener() {
		//
		// @Override
		// public void itemStateChanged(ItemEvent e) {
		// LOGGER.info("Cocoa Protocol 2.0: ");
		// LOGGER.info("Send half ships to Cocoa plant");
		// LOGGER.info("One specific ship sells cocoa.");
		// LOGGER.info("The rest go to Gulf.");
		// LOGGER.info("Time: 2h");
		// _shipsTask.setProtocol(_cocoaProtocol2);
		// _shipsTask.getProtocol().update();
		// }
		// });
		//
		// bg.add(toggle);
		// toolbar.add(toggle);

		// // MANUAL SHIP PROTOCOL
		// toolbars.add(toolbar);
		// toolbar = new JToolBar();
		// toolbar.setFloatable(false);
		//
		// int itemsPerRow = 3;
		// int n = 0;
		// for (final Destination destination : _mapManager.getDestinations()) {
		//
		// toggle = new JToggleButton(destination.getName());
		// toggle.addItemListener(new ItemListener() {
		//
		// @Override
		// public void itemStateChanged(ItemEvent e) {
		// LOGGER.info("Simple protocol: ");
		// LOGGER.info("Send all ships to: " + destination.getName());
		// LOGGER.info("Time: " + destination.getTime());// TODO format time
		// _manualShipsProtocol.setDestination(destination);
		// _shipsTask.setProtocol(_manualShipsProtocol);
		// _shipsTask.getProtocol().update();
		// }
		// });
		// bg.add(toggle);
		// // toggle.setSelected(destination.getName().equals("Coastline"));
		//
		// n++;
		// if (n > itemsPerRow) {
		// toolbars.add(toolbar);
		// toolbar = new JToolBar();
		// toolbar.setFloatable(false);
		// n = 0;
		// }
		// toolbar.add(toggle);
		//
		// }
		//
		// selected.setSelected(true);
		return toolbars;
	}

	@SuppressWarnings("serial")
	private JToolBar createToolbar4() {
		_buildingsToolbar = new JToolBar();
		_buildingsToolbar.setFloatable(false);

		// BUILDINGS GO HERE

		try {
			for (final Building b : _buildingManager.getBuildings()) {
				final JToggleButton toggle = new JToggleButton(b.getName());
				toggle.setActionCommand(b.getName().replace(" ", ""));

				toggle.addItemListener(new ItemListener() {

					@Override
					public void itemStateChanged(ItemEvent e) {

						boolean val = e.getStateChange() == ItemEvent.SELECTED;
						b.setEnabled(val);
						LOGGER.info("Building " + b.getName() + " is now " + (b.isEnabled() ? "on" : "off"));

						_settings.setProperty("Buildings." + toggle.getActionCommand(), "" + val);
						_settings.saveSettingsSorted();

					}
				});
				//
				// toggle.setSelected(b.isEnabled());
				_buildingsToolbar.add(toggle);
			}
		} catch (IOException e) {
			LOGGER.warning("Failed to load buildings");
		}
		return _buildingsToolbar;
	}

	private JToolBar createToolbar5() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		// Temp bar for custom protocol
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
								try {
									test();
								} catch (GameErrorException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
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
			_mouse.savePosition();
			_mapManager.reset();
			_scanner.reset();
			LOGGER.info("Scanning...");
			setTitle(APP_TITLE + " ...");
			boolean fullScreen = _settings.getBoolean("fullScreen", false);
			boolean found = _scanner.locateGameArea(false);
			if (found) {
				_scanner.zoomOut();

				_scanner.checkAndAdjustRock();
				_mapManager.update();
				_buildingManager.update();

				LOGGER.info("Coordinates: " + _scanner.getTopLeft() + " - " + _scanner.getBottomRight());

				_scanner.zoomOut();

				LOGGER.info("GAME FOUND! INSOMNIA READY!");
				setTitle(APP_TITLE + " READY");

				loadStats();
				_mouse.restorePosition();
			} else {
				LOGGER.info("CAN'T FIND THE GAME!");
				setTitle(APP_TITLE);
			}
		} catch (Exception e1) {
			LOGGER.log(Level.WARNING, e1.getMessage());
			e1.printStackTrace();
		}

	}

	private void loadStats() {
		try {

			Iterator<String> i = _labels.keySet().iterator();
			while (i.hasNext()) {
				String key = (String) i.next();
				_labels.get(key).setText("" + 0);
			}

			List<DispatchEntry> des = new JsonStorage().loadDispatchEntries();
			for (DispatchEntry de : des) {
				String dest = de.getDest();
				if (dest.startsWith("M"))
					dest = "MX";
				JLabel l = _labels.get(dest);
				if (l != null) {
					l.setText("" + (Integer.parseInt(l.getText()) + de.getTimes()));
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		resizeFrame(true);
	}

	private void resizeFrame(boolean big) {
		int w = 290;// frame.getSize().width;
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x, y, h;
		if (big) {
			pack();
			// setSize(new Dimension(getSize().width + 8, getSize().height + 8));
			h = (int) (screenSize.height * 0.9);
			x = screenSize.width - w;
			y = (screenSize.height - h) / 2;
		} else {
			h = 99;
			x = 150;
			y = screenSize.height - h - 1;
		}
		setBounds(x, y, w, h);

	}

	private void test() throws GameErrorException {
		setTitle(APP_TITLE + " testing");

		try {
			handlePopups();
			recalcPositions(false, 1);

			Pixel mapP = _scanner.scanOneFast("mapButton.bmp", null, true);
			if (mapP != null) {
				_mouse.delay(2000);

				Destination market = null;
				Pixel marketPos = null;
				for (Destination destination : _mapManager.getDestinations()) {
					if (destination.getName().startsWith("Market"))
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

				if (false) {
					if (market != null) {
						Pixel pm = market.getRelativePosition();
						for (Destination destination : _mapManager.getDestinations()) {
							if (!destination.getName().startsWith("Market")) {
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

	private void doMagic() throws IOException, AWTException, RobotInterruptedException {
		assert _scanner.isOptimized();
		setTitle(APP_TITLE + " RUNNING");
		_stopAllThreads = false;
		_monitor.pingActivity();
		try {
			long start = System.currentTimeMillis();
			// initial recalc
			// recalcPositions(false, 2);
			for (Task task : _tasks) {
				task.getProtocol().reset();
				if (task.isEnabled())
					task.update();
			}

			_mouse.saveCurrentPosition();
			long fstart = System.currentTimeMillis();
			_lastTime = fstart;
			int turn = 0;

			do {

				turn++;
				if (turn == 4)
					turn = 1;

				long mandatoryRefresh = _settings.getInt("autoRefresh.mandatoryRefresh", 45) * 60 * 1000;
				long now = System.currentTimeMillis();

				_mouse.checkUserMovement();
				// 1. SCAN
				// if (turn % 3 == 0)
				handlePopups();

				// 2. REFRESH
				LOGGER.fine("refresh ? " + _settings.getBoolean("autoRefresh", false) + " - " + mandatoryRefresh + " < "
				    + (now - fstart));
				long minTime = getInactivityTimeAllowed();
				LOGGER.fine("time since no ship sent: " + ((now - _lastTime) / 60000) + " < " + minTime / 60000);

				if (_settings.getBoolean("autoRefresh", false)) {

					boolean r = false;
					if (mandatoryRefresh > 0 && now - fstart >= mandatoryRefresh) {
						LOGGER.info("mandatory refresh time...");
						r = true;
						// } else if (now - _lastTime >= minTime) {
						// LOGGER.info("INACTIVITY REFRESH...");
						// _scanner.captureScreen("INACTIVITY ", true);
						// r = true;
					}
					if (r) {
						try {
							refresh(false);
						} catch (AWTException e) {
							LOGGER.info("FAILED TO refresh: " + e.getMessage());
						} catch (IOException e) {
							LOGGER.info("FAILED TO refresh: " + e.getMessage());
						}
						_lastTime = fstart = System.currentTimeMillis();
					}
				}

				_mouse.checkUserMovement();
				if (_autoSailorsToggle.isSelected())
					scanSailors();
				// recalcPositions(false, 1);

				// 3. DO TASKS
				// long now = System.currentTimeMillis();
				// if (now - start > 11*60000) {
				for (Task task : _tasks) {
					if (task.isEnabled() && !_stopAllThreads) {
						try {
							_mouse.checkUserMovement();
							task.preExecute();
							_mouse.checkUserMovement();
							if (!_stopAllThreads)
								task.execute();
						} catch (AWTException e) {
							LOGGER.info("FAILED TO execute task: " + task.getName());
						} catch (IOException e) {
							LOGGER.info("FAILED TO execute task: " + task.getName());
						}
					}
				}

				// 4. PING
				if (turn % 3 == 0 && !_stopAllThreads) {
					_mouse.checkUserMovement();
					if (_pingToggle.isSelected()) {
						ping();
					}

					if (_ping2Toggle.isSelected()) {
						ping2New();
					}

					if (_ping3Toggle.isSelected()) {
						ping3();
					}
				}

				_mouse.mouseMove(_scanner.getParkingPoint());

				_mouse.delay(200);

			} while (!_stopAllThreads);

		} catch (GameErrorException e) {
			if (e.getCode() > 1) {
				bounce();
			}
		}
	}

	private boolean refresh(boolean bookmark) throws AWTException, IOException, RobotInterruptedException {
		_scanner.deleteOlder("refresh", 5);
		LOGGER.info("Time to refresh...");

		Pixel p;
		if (bookmark || !_scanner.isOptimized()) {
			p = _scanner.scanOne("seaportBookmark.bmp", null, false);
			if (p == null)
				p = new Pixel(79, 76);
			_mouse.click(p.x, p.y);
		} else {
			if (_scanner.isFullScreen()) {
				Robot robot = new Robot();
				robot.keyPress(KeyEvent.VK_ESCAPE);
				robot.keyRelease(KeyEvent.VK_ESCAPE);
				_mouse.delay(2000);
				_scanner.locateGameArea(false);
			}
			p = _scanner.getBottomRight();
			p.x -= 10;
			p.y += 3;
			_mouse.click(p.x, p.y);
			try {
				Robot robot = new Robot();
				robot.keyPress(KeyEvent.VK_F5);
				robot.keyRelease(KeyEvent.VK_F5);
			} catch (AWTException e) {
			}

		}
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
		}
		_scanner.reset();

		boolean done = false;
		for (int i = 0; i < 14 && !done && !_stopAllThreads; i++) {
			LOGGER.info("after refresh recovery try " + (i + 1) + " " + _stopAllThreads);
			// check for popups first, like offers (damn it)
			boolean f = _scanner.scanOneFast("buildings/x.bmp", null, true) != null;
			if (f)
				_mouse.delay(500);
			f = _scanner.scanOneFast("buildings/x.bmp", null, true) != null;
			if (f)
				_mouse.delay(500);
			f = _scanner.scanOneFast("buildings/x.bmp", null, true) != null;
			if (f)
				_mouse.delay(500);

			// LOCATE THE GAME
			if (_scanner.locateGameArea(false)) {
				_scanner.checkAndAdjustRock();
				if (_scanner.getRock() != null) {
					_mapManager.update();
					_buildingManager.update();

					LOGGER.info("Game located successfully!");
					done = true;
				}
			} else {
				processRequests();
			}
			if (i > 8) {
				_scanner.captureScreen("refresh trouble ", true);
			}
		}
		if (_stopAllThreads) {
			LOGGER.info("Refresh stopped...");
			_scanner.captureScreen("refresh stopped ", true);
		} else {
			if (done) {
				// runMagic();
				_scanner.captureScreen("refresh done ", true);
				// not sure why shipsTasks gets off after refresh
				reapplySettings();
				return true;
			} else {
				// try bookmark
				if (!bookmark)
					return refresh(true);
			}
		}
		return false;
	}

	private Long _lastPing = System.currentTimeMillis();
	private Long _lastPing2 = System.currentTimeMillis();
	private Long _lastPing3 = System.currentTimeMillis();

	private void ping() throws RobotInterruptedException {
		if (System.currentTimeMillis() - _lastPing > _settings.getInt("ping.time", 120) * 1000) {
			LOGGER.info("ping1...");
			if (_scanner.getScoreBoard() != null) {
				_mouse.click(_scanner.getScoreBoard());
				_mouse.delay(200);
				_mouse.mouseMove(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2, _scanner.getTopLeft().y + 49);
				_mouse.delay(2000);
				if (_mouse.getMode() == MouseRobot.SLOW)
					_mouse.delay(_settings.getInt("slow.delay", 500));
			}
			_scanner.captureScreen(null, true);
			if (_scanner.getScoreBoard() != null) {
				_mouse.click(_scanner.getBottomRight().x - 23, _scanner.getTopLeft().y + 70);
				_mouse.delay(200);
			}
			_lastPing = System.currentTimeMillis();
		}

	}

	private void ping3() throws RobotInterruptedException, IOException, AWTException, GameErrorException {
		if (System.currentTimeMillis() - _lastPing3 > _settings.getInt("ping3.time", 300) * 1000) {
			LOGGER.info("ping3...");
			_mouse.click(_scanner.getBottomRight().x - 80, _scanner.getBottomRight().y - 53);
			_mouse.delay(2000);
			if (_mouse.getMode() == MouseRobot.SLOW)
				_mouse.delay(_settings.getInt("slow.delay", 500) + 500);

			_scanner.captureScreen("ping map ", true);
			try {
				_mapManager.ensureMap();
			} catch (Exception e) {
				LOGGER.warning("ensureMap failed... Moving forward!");
			}

			// contractors
			String contractors = _settings.getProperty("ping3.contractors", "");
			String[] s = contractors.split(",");
			for (String abbr : s) {
				if (!_stopAllThreads) {
					Destination dest = _mapManager.getDestinationByAbbr(abbr);
					if (dest != null) {
						Pixel smallTownPos = _mapManager.getSmallTownPos();
						if (smallTownPos == null) {
							_mapManager.ensureMap();
							smallTownPos = _mapManager.getSmallTownPos();
						}
						if (smallTownPos != null) {
							// TODO ensure dest
							int x = smallTownPos.x + dest.getRelativePosition().x;
							int y = smallTownPos.y + dest.getRelativePosition().y;
							_mouse.click(x, y);
							_mouse.delay(750);
							_scanner.captureScreen("ping " + dest.getAbbr() + " ", true);

							if (_scanner.scanOneFast("buildings/x.bmp", null, true) != null)
								_mouse.delay(200);
						}
					}
				}
			}

			_scanner.scanOne(_scanner.getAnchorButton(), null, true);
			_lastPing3 = System.currentTimeMillis();
		}

	}

	private void testMap() throws RobotInterruptedException {
		_mapManager.setSmallTownPos(null);
		_mouse.click(_scanner.getBottomRight().x - 80, _scanner.getBottomRight().y - 53);
		_mouse.delay(2000);

		try {
			Pixel pp = _scanner.scanOneFast("ships/fleetOverviewX.bmp", null, false);
			if (pp != null) {
				_mouse.click(pp.x + 7, pp.y + 7);
				_mouse.delay(1000);
			}
			Pixel smallTownPos = _mapManager.getSmallTownPos();
			if (smallTownPos == null) {
				_mapManager.ensureMap();
				smallTownPos = _mapManager.getSmallTownPos();
				LOGGER.info("smallown: " + smallTownPos);
			}

			_mapManager.ensureDestination(_mapManager.getSmallTown().getRelativePosition(), true);

			Pixel p;
			// EAST
			Pixel p1 = new Pixel(861, -576);
			p1 = new Pixel(861, 709);
			LOGGER.info("explore SE");
			_mapManager.ensureDestination(p1, false);
			p = _scanner.scanOne("ships/explore.bmp", null, false);

			if (p != null)
				_mouse.mouseMove(p);

		} catch (AWTException | IOException | GameErrorException e) {
			e.printStackTrace();
		}
	}

	private void ping2New() throws RobotInterruptedException, IOException, AWTException {
		if (System.currentTimeMillis() - _lastPing2 > _settings.getInt("ping2.time", 300) * 1000) {
			LOGGER.info("ping2...");
			BufferedImage image = _scanner.scanStorage();
			if (image != null) {
				try {
					_scanner.writeImageTS(image, "ping storage");
					_mouse.delay(1300);
					Pixel good = _scanner.scanOneFast("buildings/x.bmp", null, true);
					if (good == null) {
						LOGGER.info("cound't find the x...");
						_scanner.handlePopupsFast();
					}
				} finally {
					_lastPing2 = System.currentTimeMillis();
				}
			}
			// LOGGER.info("ping2 done");
		}
	}

	private void ping2() throws RobotInterruptedException, IOException, AWTException {
		if (_scanner.getScoreBoard() != null) {
			if (System.currentTimeMillis() - _lastPing2 > _settings.getInt("ping2.time", 60) * 1000) {
				LOGGER.info("ping2...");
				_mouse.click(_scanner.getScoreBoard());
				_mouse.delay(2000);
				Pixel tr = new Pixel(_scanner.getBottomRight().x, _scanner.getTopLeft().y);

				// click the coins tab
				_mouse.click(tr.x - 53, tr.y + 106);
				_mouse.delay(1000);

				// look for Carlos
				// carlosFB.bmp
				Rectangle area = new Rectangle(tr.x - 269, tr.y + 122, 25, 437);
				Pixel p = null;
				int pages = 0;
				do {
					p = _scanner.scanOne("carlosFB.bmp", area, false);
					pages++;
					if (p == null) {
						_mouse.click(tr.x - 125, tr.y + 575);
						_mouse.delay(750);
					}
				} while (p == null && pages < 4);

				Rectangle carlosArea;
				if (p != null)
					carlosArea = new Rectangle(p.x - 47, p.y, 300, 19);
				else
					carlosArea = new Rectangle(tr.x - 313, tr.y + 121, 301, 436);
				_scanner.captureArea(carlosArea, "carlos ", true);

				// close the scoreboard
				_mouse.click(tr.x - 17, tr.y + 68);
				_mouse.delay(200);

				_lastPing2 = System.currentTimeMillis();
			}
		}
	}

	private Long _speedTime = null;

	//private BarrelsProtocol _barrelsProtocol;

	private void scanSailors() {
		Pixel sailorsPos = _scanner.getSailorsPos();
		if (sailorsPos != null) {

			if (_speedTime == null) {
				try {
					Rectangle miniArea = new Rectangle(sailorsPos.x + 20, sailorsPos.y + 4, 76, 15);
					BufferedImage image = new Robot().createScreenCapture(miniArea);

					FastBitmap fb = new FastBitmap(image);
					// COLOR FILTERING
					ColorFiltering colorFiltering = new ColorFiltering(new IntRange(255, 255), new IntRange(255, 255),
					    new IntRange(255, 255));
					colorFiltering.applyInPlace(fb);

					String sailorsStr = _ocr.scanImage(fb.toBufferedImage());
					LOGGER.info("sailors:" + sailorsStr);

					try {
						int sailors = Integer.parseInt(sailorsStr);
						if (sailors > _settings.getInt("autoSailors.upperThreshold", 1000)) {
							String newProtocolName = _shipProtocol != null ? _shipProtocol.getName() : "DEFAULT";
							String speedProtocolName = _settings.getProperty("autoSailors.speedProtocol", "SINGLE");
							if (!newProtocolName.equals(speedProtocolName)) {
								_lastProtocolName = newProtocolName;
								setProtocol(speedProtocolName);
								_speedTime = System.currentTimeMillis();
								LOGGER.info("Switching to speed protocol: " + speedProtocolName);
							}
						}
					} catch (NumberFormatException e) {
					}
				} catch (AWTException e) {
					e.printStackTrace();
				}
			} else {
				if (System.currentTimeMillis() - _speedTime >= _settings.getInt("autoSailors.speedTime", 120) * 60000) {
					_speedTime = null;
					String newProtocolName = _lastProtocolName != null ? _lastProtocolName : "DEFAULT";
					LOGGER.info("Going back to protocol: " + newProtocolName);

					try {
						_mapManager.resetDispatchEntries();
					} catch (IOException e1) {
						LOGGER.info("Failed to reset entries!");
					}
					setProtocol(newProtocolName);
				} else {
					long time = System.currentTimeMillis() - _speedTime - _settings.getInt("autoSailors.speedTime", 120) * 60000;
					time = -time / 60000;
					LOGGER.info("Time to switch back to normal protocol: " + time + " minutes");
				}
			}
		}
	}

	private void setProtocol(String shipProtocolName) {
		_shipProtocolManagerUI.setShipProtocol(shipProtocolName);
	}

	private void handlePopups() throws RobotInterruptedException, GameErrorException {
		try {
			LOGGER.info("Popups...");
			boolean found = false;
			Pixel p = null;
			if (_scanner.isOptimized()) {
				_mouse.click(_scanner.getSafePoint());
				_mouse.delay(130);
			}
			found = _scanner.scanOneFast(_scanner.getAnchorButton(), null, true) != null;

			if (found) {
				return;
			}

			found = _scanner.handlePopups();
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

	private void reapplySettings() {

		// toggles

		boolean fish = "true".equalsIgnoreCase(_settings.getProperty("fish"));
		if (fish != _fishToggle.isSelected()) {
			_fishToggle.setSelected(fish);
		}

		boolean ships = "true".equalsIgnoreCase(_settings.getProperty("ships"));
		//if (ships != _shipsToggle.isSelected()) {
		_shipsToggle.setSelected(ships);
		_shipsTask.setEnabled(ships);
		//}

		boolean industries = "true".equalsIgnoreCase(_settings.getProperty("industries"));
		if (industries != _industriesToggle.isSelected()) {
			_industriesToggle.setSelected(industries);
		}

		boolean barrels = "true".equalsIgnoreCase(_settings.getProperty("barrelsS"));
		if (barrels != _barrelsSToggle.isSelected()) {
			_barrelsSToggle.setSelected(barrels);
		}

		// barrels = "true".equalsIgnoreCase(_settings.getProperty("barrelsA"));
		// if (barrels != _barrelsAToggle.isSelected()) {
		// _barrelsAToggle.setSelected(barrels);
		// }

		boolean ar = "true".equalsIgnoreCase(_settings.getProperty("autoRefresh"));
		if (ar != _autoRefreshToggle.isSelected()) {
			_autoRefreshToggle.setSelected(ar);
		}

		boolean fs = "true".equalsIgnoreCase(_settings.getProperty("fullScreen"));
		if (fs != _fullScreenToggle.isSelected()) {
			_fullScreenToggle.setSelected(ar);
		}

		boolean ml = "true".equalsIgnoreCase(_settings.getProperty("miniLayout"));
		if (ml != _miniLayoutToggle.isSelected()) {
			_miniLayoutToggle.setSelected(ar);
		}

		boolean slow = "true".equalsIgnoreCase(_settings.getProperty("slow"));
		if (slow)
			_mouse.setMode(MouseRobot.SLOW);
		else
			_mouse.setMode(MouseRobot.NORMAL);

		if (slow != _slowToggle.isSelected()) {
			_slowToggle.setSelected(slow);
		}

		boolean autoSailors = "true".equalsIgnoreCase(_settings.getProperty("autoSailors"));
		if (autoSailors != _autoSailorsToggle.isSelected()) {
			_autoSailorsToggle.setSelected(autoSailors);
		}

		boolean ping = "true".equalsIgnoreCase(_settings.getProperty("ping"));
		if (ping != _pingToggle.isSelected()) {
			_pingToggle.setSelected(ping);
		}

		ping = "true".equalsIgnoreCase(_settings.getProperty("ping2"));
		if (ping != _ping2Toggle.isSelected()) {
			_ping2Toggle.setSelected(ping);
		}

		ping = "true".equalsIgnoreCase(_settings.getProperty("ping3"));
		if (ping != _ping3Toggle.isSelected()) {
			_ping3Toggle.setSelected(ping);
		}

		// buildings

		for (int i = 0; i < _buildingsToolbar.getComponentCount(); i++) {
			if (_buildingsToolbar.getComponent(i) instanceof JToggleButton) {
				JToggleButton toggle = (JToggleButton) _buildingsToolbar.getComponent(i);
				String ac = toggle.getActionCommand();
				if (_settings.containsKey("Buildings." + ac)) {
					boolean v = "true".equalsIgnoreCase(_settings.getProperty("Buildings." + ac));
					if (v != toggle.isSelected()) {
						toggle.setSelected(v);
					}

				}
			}
		}

		// ship protocol
		String sp = _settings.getProperty("ShipProtocol", "DEFAULT");
		if (!sp.equals(_shipProtocol != null ? _shipProtocol.getName() : "")) {
			setProtocol(sp);
		}

//		_barrelsProtocol.setBlobMin(_settings.getInt("barrels.blobMin", 15 * 20));
//		_barrelsProtocol.setBlobMax(_settings.getInt("barrels.blobMax", 28 * 32));

	}

	private void stopMagic() {
		_stopAllThreads = true;
		LOGGER.info("Stopping...");
		for (Task task : _tasks) {
			task.getProtocol().interrupt();
		}
		int tries = 10;
		boolean stillRunning = true;
		for (int i = 0; i < tries && stillRunning; ++i) {
			stillRunning = isRunning("MAGIC");
			if (stillRunning) {
				LOGGER.info("Magic still working...");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			} else {
				LOGGER.info("INSOMNIA STOPPED");
				setTitle(APP_TITLE);
			}
		}
		// _stopAllThreads = false;
	}

	private void processRequests() {
		Service service = new Service();

		String[] requests = service.getActiveRequests();
		for (String r : requests) {

			if (r.startsWith("stop")) {
				service.inProgress(r);
				_monitor.stopMonitoring();
				stopMagic();
				_scanner.captureScreen(null, true);

			} else if (r.startsWith("run") || r.startsWith("start")) {
				service.inProgress(r);
				stopMagic();
				_scanner.reset();
				runMagic();
				_scanner.captureScreen(null, true);

			} else if (r.startsWith("click")) {
				service.inProgress(r);
				processClick(r);
			} else if (r.startsWith("refresh")) {
				service.inProgress(r);

				bounce();

			} else if (r.startsWith("ping") || r.startsWith("p")) {
				service.inProgress(r);
				LOGGER.info("Ping...");
				_scanner.captureScreen(null, true);
				service.done(r);
			} else if (r.startsWith("reload")) {
				service.inProgress(r);
				reload();
				service.done(r);
			} else if (r.startsWith("reset")) {
				service.inProgress(r);
				reset();
				service.done(r);
			}
		}

		// service.purgeOld(1000 * 60 * 60);// 1 hour old
	}

	private void bounce() {
		try {
			stopMagic();
			_monitor.startMonitoring();// need to be run before refresh in order to monitor refresh as well
			_stopAllThreads = false;
			refresh(false);
			if (!_stopAllThreads) {
				_scanner.reset();
				runMagic();
			}
		} catch (AWTException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RobotInterruptedException e) {
			e.printStackTrace();
		}
	}

	private void processClick(String r) {
		try {
			String[] ss = r.split("_");
			int x = Integer.parseInt(ss[1]);
			int y = Integer.parseInt(ss[2]);
			_mouse.click(x, y);
			try {
				_mouse.delay(1000);
			} catch (RobotInterruptedException e) {
			}
		} finally {
			new Service().done(r);
		}
	}

	private void runSettingsListener() {
		Thread requestsThread = new Thread(new Runnable() {
			public void run() {
				// new Service().purgeAll();
				boolean stop = false;
				do {
					LOGGER.fine("......");
					try {
						processRequests();
					} catch (Throwable t) {
						// hmm
						t.printStackTrace();
					}

					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					try {
						if (filesChanged()) {
							LOGGER.info("DETECTED FILE CHANGES! RELOADING...");
							reload();
						}
						_settings.loadSettings();
						reapplySettings();
					} catch (Throwable t) {
						// hmm
						t.printStackTrace();
					}
				} while (!stop);
			}

		}, "REQUESTS");

		requestsThread.start();

	}

	private Map<String, Long> _filesTracked;

	private GameHealthMonitor _monitor;

	private JToolBar _mainToolbar1;

	private JToolBar _mainToolbar2;

	private JToolBar _mainToolbar4;

	private JTextArea _shipLog;

	private Component _statsPanel;

	private JPanel _shipProtocolManagerPanel;

	private Container _console;

	protected boolean filesChanged() {
		boolean changed = false;
		for (String key : _filesTracked.keySet()) {
			File f = new File(key);
			long lastModified = f.lastModified();

			if (_filesTracked.get(key) != lastModified) {
				changed = true;
				_filesTracked.put(key, lastModified);
			}
		}

		return changed;
	}

	private void runMagic() {
		_monitor.startMonitoring();
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

					try {
						doMagic();
					} catch (IOException | AWTException e) {
						e.printStackTrace();
						LOGGER.warning("Something went wrong!");
					} catch (RobotInterruptedException e) {
						LOGGER.info("interrupted");
						setTitle(APP_TITLE);
					}
				} else {
					LOGGER.info("I need to know where the game is!");
				}
			}
		}, "MAGIC");

		myThread.start();
	}

	public long getInactivityTimeAllowed() {
		long result = 20; // in minutes

		// Find the min time of current ship protocol
		if (_shipProtocol != null) {
			int minTime = 10 * 60; // 10hours
			for (ProtocolEntry entry : _shipProtocol.getEntries()) {

				String s = entry.getChainStr().toUpperCase();
				String[] ss = s.split(",");

				for (String ds : ss) {
					if (ds.indexOf("-") > 0) {
						ds = ds.split("-")[0];
					}

					Destination dest = _mapManager.getDestinationByAbbr(ds.trim());
					if (dest != null) {
						if (minTime > dest.getTime())
							minTime = dest.getTime();
					}
				}
			}
			result = minTime;
		}

		result += 2;
		// convert it to milliseconds
		result *= 60000;

		return result;
	}

}
