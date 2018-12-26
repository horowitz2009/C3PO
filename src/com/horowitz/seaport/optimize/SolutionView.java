package com.horowitz.seaport.optimize;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.horowitz.commons.DateUtils;
import com.horowitz.seaport.model.DispatchEntry;

public class SolutionView extends JPanel {

	private PropertyChangeSupport support;

	public SolutionView(Solution solution) {
		super();
		setLayout(new GridBagLayout());
		JLabel goalLabel = new JLabel("" + solution.goal);
		GridBagConstraints gbc = new GridBagConstraints();
		support = new PropertyChangeSupport(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.ipadx = 5;
		gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		gbc.weightx = 2.0;

		// now the list of ships
		// List<DispatchEntry> des = new ArrayList<>(solution.ships);
		// Collections.sort(des, new Comparator<DispatchEntry>() {
		// @Override
		// public int compare(DispatchEntry de1, DispatchEntry de2) {
		// return (int) (de1.willArriveAt() - de2.willArriveAt());
		// }
		// });
		long now = System.currentTimeMillis();
		for (DispatchEntry de : solution.ships) {
			JLabel deLabel = new JLabel(de.getShipObj().getCount() + "x " + de.getShipObj().getCapacity() + "  "
			    + de.getShip() + "  " + DateUtils.fancyTime2(de.willArriveAt() - now));
			add(deLabel, gbc);
			gbc.gridy++;
		}

		gbc.gridy--;

		gbc.weightx = 1.0;
		gbc.gridx = 0;
		// gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		add(goalLabel, gbc);

		gbc.gridx = 2;
		// gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.SOUTHEAST;

		JButton button = new JButton(new AbstractAction("Choose") {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.err.println("BOO");
				support.firePropertyChange("SOLUTION", null, solution);
			}
		});
		gbc.gridy = 0;
		gbc.gridheight = solution.ships.size();
		add(button, gbc);

		gbc.gridx++;
		gbc.gridy = gbc.gridheight + 1;
		gbc.gridheight = 1;
		add(new JLabel(" "), gbc);

	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		support.addPropertyChangeListener(propertyName, listener);
	}

}
