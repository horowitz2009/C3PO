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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

public class COFrame extends JFrame {

	PropertyChangeSupport support;

	public COFrame() throws HeadlessException {
		super();
		support = new PropertyChangeSupport(this);

		initLayout();
	}

	private void initLayout() {
		setTitle("Contract Optimizer");

		int w = 400;
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

		main = new JPanel(new BorderLayout());
		getContentPane().add(main);

	}

	private JTextField minTF;
	private JTextField maxTF;
	private JTextField destTF;
	private JTextField goalTF;
	private JScrollPane scrollPane;

	private JToolBar createToolbar1() {

		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		minTF = new JTextField(5);

		maxTF = new JTextField(5);
		destTF = new JTextField(5);
		goalTF = new JTextField(10);
		toolbar.add(new JLabel("from "));
		toolbar.add(minTF);
		toolbar.add(new JLabel("to "));
		toolbar.add(maxTF);
		toolbar.add(new JLabel(" dest "));
		toolbar.add(destTF);
		toolbar.add(new JLabel(" GOAL "));
		toolbar.add(goalTF);
		reload();
		// Calculate
		{
			AbstractAction action = new AbstractAction("Calculate") {
				public void actionPerformed(ActionEvent e) {
					Thread myThread = new Thread(new Runnable() {
						@Override
						public void run() {
							calculate();
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

	private void reload() {
		minTF.setText("100");
		maxTF.setText("400");
		destTF.setText("E");

	}

	private void calculate() {
		try {
			ContractOptimizer co = new ContractOptimizer(Integer.parseInt(minTF.getText()), Integer.parseInt(maxTF.getText()), 500);
			co.init();
			co.loadShipsLog();

			List<Solution> solutions = co.getSolutionFor(Integer.parseInt(goalTF.getText()));
			co.printSolutions(solutions);
			solutions = sortByPrecission(solutions, 5);
			for (Solution s : solutions) {
				s.destination = destTF.getText();
			}
			displaySolutions(solutions);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	JPanel main = null;

	private void displaySolutions(List<Solution> solutions) {
		JPanel solutionPanel = new JPanel(new GridLayout(0, 1, 5, 5));

		for (Solution solution : solutions) {
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

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		support.addPropertyChangeListener(propertyName, listener);
	}
}
