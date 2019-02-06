package com.horowitz.seaport.optimize;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.horowitz.commons.Settings;

public class COFrame extends JFrame {
	private final static Logger LOGGER = Logger.getLogger("MAIN");

	PropertyChangeSupport support;

	private Settings settings;

	public COFrame(Settings settings) throws HeadlessException {
		super();
		support = new PropertyChangeSupport(this);
		this.settings = settings;
		initLayout();
	}

	private void initLayout() {
		setTitle("Contract Optimizer");

		int w = 450;
		int h = 700;
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x, y;
		x = screenSize.width - w - 290;
		y = 200;
		setBounds(x, y, w, h);

		getContentPane().setLayout(new BorderLayout());
		JPanel toolbars = new JPanel(new GridLayout(0, 1));
		getContentPane().add(toolbars, BorderLayout.NORTH);
		JToolBar toolbar1 = createToolbar1();
		toolbars.add(toolbar1);
		JToolBar toolbar2 = createToolbar2();
		toolbars.add(toolbar2);

		main = new JPanel(new BorderLayout());
		getContentPane().add(main);

	}

	private JTextField limitTF;
	private JTextField minTF;
	private JTextField maxTF;
	private JTextField destTF;
	private JTextField dest2TF;
	private JTextField goalTF;
	private JScrollPane scrollPane;

	private JToolBar createToolbar1() {

		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		minTF = new JTextField(5);

		maxTF = new JTextField(5);
		destTF = new JTextField(5);
		dest2TF = new JTextField(5);
		goalTF = new JTextField(10);
		limitTF = new JTextField(5);
		toolbar.add(new JLabel("from "));
		toolbar.add(minTF);
		toolbar.add(new JLabel("to "));
		toolbar.add(maxTF);
		toolbar.add(new JLabel(" d1 "));
		toolbar.add(destTF);
		toolbar.add(new JLabel(" d2 "));
		toolbar.add(dest2TF);
		toolbar.add(new JLabel(" GOAL "));
		toolbar.add(goalTF);
		reload();
		// Calculate
		{
			AbstractAction action = new AbstractAction("Calc1") {
				public void actionPerformed(ActionEvent e) {
					Thread myThread = new Thread(new Runnable() {
						@Override
						public void run() {
							calculate1(false);
						}

					});

					myThread.start();
				}
			};
			toolbar.add(action);
		}
		{
			AbstractAction action = new AbstractAction("Calc2") {
				public void actionPerformed(ActionEvent e) {
					Thread myThread = new Thread(new Runnable() {
						@Override
						public void run() {
							calculate1(true);
						}

					});

					myThread.start();
				}
			};
			toolbar.add(action);
		}
		toolbar.add(new JLabel("    "));
		return toolbar;
	}

	private JToolBar createToolbar2() {

		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		// limitTF = new JTextField(5);

		toolbar.add(new JLabel("limit "));
		toolbar.add(limitTF);
		{
			AbstractAction action = new AbstractAction("CalcF") {
				public void actionPerformed(ActionEvent e) {
					Thread myThread = new Thread(new Runnable() {
						@Override
						public void run() {
							calculateFast(false);
						}

					});

					myThread.start();
				}
			};
			toolbar.add(action);
		}

		return toolbar;
	}

	private void reload() {
		minTF.setText(settings.getProperty("contract.optimizer.min", "210"));
		maxTF.setText(settings.getProperty("contract.optimizer.max", "700"));
		destTF.setText(settings.getProperty("contract.optimizer.dest", "E"));
		dest2TF.setText(settings.getProperty("contract.optimizer.dest2", "G"));
		limitTF.setText(settings.getProperty("contract.optimizer.limit", "7"));

	}

	private void calculateFast(boolean minusMeansLast) {

		try {
			List<Solution> solutions = new ArrayList<Solution>(0);
			ContractOptimizer co = new ContractOptimizer(Integer.parseInt(minTF.getText()), Integer.parseInt(maxTF.getText()),
			    5);
			co.setMinusMeansLast(minusMeansLast);
			co.init();
			co.loadShipsLog();
			co.setSolutionsLimit2(20);
			List<Solution> solutions2 = co.getSolutionForFAST(Integer.parseInt(goalTF.getText()),
			    Integer.parseInt(limitTF.getText()));

			co.printSolutions(solutions2);
			solutions2 = sortByTime(solutions2, 5);
			for (Solution s : solutions2) {
				s.destination = destTF.getText();
				s.destination2 = dest2TF.getText();
			}
			solutions.addAll(solutions2);

			displaySolutions(solutions);
			LOGGER.info("solutions: " + solutions.size());
		} catch (IOException e) {
			LOGGER.info("Error finding solutions: " + e.getMessage());
			e.printStackTrace();
		}

	}

	private void calculate1(boolean minusMeansLast) {

		try {
			List<Solution> solutions = new ArrayList<Solution>(0);
			ContractOptimizer co = new ContractOptimizer(Integer.parseInt(minTF.getText()), Integer.parseInt(maxTF.getText()),
			    5);
			co.setMinusMeansLast(minusMeansLast);
			co.init();
			co.loadShipsLog();
			solutions = co.getSolutionForFAST(Integer.parseInt(goalTF.getText()), 0);
			co.printSolutions(solutions);
			solutions = sortByPrecission(solutions, 5);
			for (Solution s : solutions) {
				s.destination = destTF.getText();
				s.destination2 = dest2TF.getText();
			}
			if (solutions.isEmpty()) {
				co.setSolutionsLimit2(20);
				List<Solution> solutions2 = co.getSolutionForFAST(Integer.parseInt(goalTF.getText()),
				    Integer.parseInt(limitTF.getText()));

				co.printSolutions(solutions2);
				solutions2 = sortByPrecission(solutions2, 5);
				for (Solution s : solutions2) {
					s.destination = destTF.getText();
					s.destination2 = dest2TF.getText();
				}
				solutions.addAll(solutions2);
			}

			displaySolutions(solutions);
			LOGGER.info("solutions: " + solutions.size());
		} catch (IOException e) {
			LOGGER.info("Error finding solutions: " + e.getMessage());
			e.printStackTrace();
		}

	}

	JPanel main = null;

	private void displaySolutions(List<Solution> solutions) {
		JPanel solutionPanel = new JPanel(new GridLayout(0, 1, 5, 5));

		for (Solution solution : solutions) {
			// solution.combine();
			SolutionView solutionView = new SolutionView(solution);
			solutionPanel.add(solutionView);
			solutionView.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					support.firePropertyChange(evt);
				}
			});
		}
		main.removeAll();
		scrollPane = null;

		if (scrollPane != null) {
			getContentPane().remove(scrollPane);
			scrollPane.removeAll();
			scrollPane.add(solutionPanel);
			scrollPane.invalidate();
		} else {
			scrollPane = new JScrollPane(solutionPanel);
		}
		main.add(scrollPane, BorderLayout.CENTER);
		revalidate();
	}

	private List<Solution> sortByPrecission(List<Solution> solutions, int limit) {
		Collections.sort(solutions, new Comparator<Solution>() {
			@Override
			public int compare(Solution s1, Solution s2) {
				return s1.goal - s2.goal;
			}
		});

		return solutions.stream().limit(limit).collect(Collectors.toList());
	}
	
	private List<Solution> sortByTime(List<Solution> solutions, int limit) {
		Collections.sort(solutions, new Comparator<Solution>() {
			@Override
			public int compare(Solution s1, Solution s2) {
				return (int)(s1.latest - s2.latest);
			}
		});
		
		return solutions.stream().limit(limit).collect(Collectors.toList());
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		support.addPropertyChangeListener(propertyName, listener);
	}

	public void setGoal(int need) {
		goalTF.setText("" + need);
	}
}
