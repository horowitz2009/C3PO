package com.horowitz.seaport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.builder.CompareToBuilder;

import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.ProtocolEntry;
import com.horowitz.seaport.model.Ship;
import com.horowitz.seaport.model.ShipProtocol;
import com.horowitz.seaport.model.storage.JsonStorage;

public class ProtocolEditor extends JPanel {

	private static final long serialVersionUID = -7306243578379329501L;
	private Box _box;
	private MapManager _mapManager;
	private JTextField _titleTF;

	public ProtocolEditor(MapManager mapManager) {
		super(new BorderLayout());
		_mapManager = mapManager;
		initLayout();
	}

	private void initLayout() {
		JPanel mainRoot = new JPanel(new BorderLayout());
		JPanel headerPanel = new JPanel(new GridBagLayout());
		headerPanel.setBackground(Color.LIGHT_GRAY);
		GridBagConstraints gbc = new GridBagConstraints();

		// NAME
		_titleTF = new JTextField();
		_titleTF.setFont(_titleTF.getFont().deriveFont(18f));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.gridwidth = 4;
		gbc.insets = new Insets(3, 3, 2, 2);

		headerPanel.add(_titleTF, gbc);

		//gbc.gridx++;
		gbc.gridy = 1;
		//gbc.gridheight = 2;
		//gbc.gridwidth = 1;
		JLabel agenda = new JLabel(
		    "<html>S - Small Town  C - Coastline   G - Gulf<br>CP - Cocoa Plant   MC - Market COINS   MX - Market COINS</html>");
		agenda.setFont(agenda.getFont().deriveFont(9f));
		headerPanel.add(agenda, gbc);

//		// fake label
//		gbc.gridy++;
//		gbc.gridx++;
//		gbc.weightx = 1.0;
//		gbc.weighty = 1.0;
//		headerPanel.add(new JLabel(""), gbc);
		mainRoot.add(headerPanel, BorderLayout.NORTH);

		// //////////
		// CENTER
		// //////////
		JPanel root = new JPanel(new BorderLayout());
		mainRoot.add(root, BorderLayout.CENTER);

		_box = Box.createVerticalBox();

		root.add(_box, BorderLayout.NORTH);

		// ADD BUTTON
		Box tempBar = Box.createHorizontalBox();

		JButton addButton = new JButton(new AddAction());
		addButton.setMargin(new Insets(2, 2, 2, 2));
		shrinkFont(addButton, 9f);

		tempBar.setBorder(new EmptyBorder(3, 3, 3, 3));
		tempBar.add(addButton);
		tempBar.add(Box.createHorizontalStrut(6));

		JButton saveButton = new JButton(new AbstractAction("save") {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ShipProtocol extractProtocol = extractProtocol();
					List<ShipProtocol> shipProtocols = new ArrayList<ShipProtocol>();
					shipProtocols.add(extractProtocol);
					new JsonStorage().saveShipProtocols(shipProtocols);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		shrinkFont(saveButton, 9f);
		saveButton.setMargin(new Insets(2, 2, 2, 2));
		// tempBar.add(saveButton);
		// tempBar.add(Box.createHorizontalStrut(3));

		JButton loadButton = new JButton(new AbstractAction("load") {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						ShipProtocol sp = loadShipProtocol();
						applyProtocol(sp);
						revalidate();

					}
				});
			}
		});
		shrinkFont(loadButton, 9f);
		loadButton.setMargin(new Insets(2, 2, 2, 2));
		// tempBar.add(loadButton);

		root.add(tempBar, BorderLayout.SOUTH);

		add(mainRoot, BorderLayout.NORTH);

		addRow();
	}

	public ShipProtocol extractProtocol() {
		int n = _box.getComponentCount();
		ShipProtocol sp = new ShipProtocol();
		List<ProtocolEntry> entries = new ArrayList<ProtocolEntry>();
		for (int i = 0; i < n; i++) {
			ProtocolEntryView pev = (ProtocolEntryView) _box.getComponent(i);
			Ship ship = (Ship) pev._shipFieldCB.getSelectedItem();
			String destChainStr = pev._destField.getText();
			ProtocolEntry pe = new ProtocolEntry();
			pe.setShip(ship);
			pe.setChainStr(destChainStr);
			entries.add(pe);
		}
		sp.setEntries(entries);
		sp.setName(_titleTF.getText());
		return sp;
	}

	public void applyProtocol(ShipProtocol sp) {
		_box.removeAll();
		if (sp != null && sp.getEntries() != null) {
			setVisible(true);
			_titleTF.setText(sp.getName());
			for (ProtocolEntry e : sp.getEntries()) {
				ProtocolEntryView pev = new ProtocolEntryView();
				ComboBoxModel<Ship> m = pev._shipFieldCB.getModel();
				int n = m.getSize();
				for (int i = 0; i < n; i++) {
					if (m.getElementAt(i).getName().equals(e.getShipName())) {
						pev._shipFieldCB.setSelectedIndex(i);
					}
				}
				pev._destField.setText(e.getChainStr());
				_box.add(pev);
			}
		} else {
			setVisible(false);
		}
	}

	private void addRow() {
		_box.add(new ProtocolEntryView());
	}

	void shrinkFont(Component comp, float size) {
		if (size < 0)
			size = comp.getFont().getSize() - 2;
		comp.setFont(comp.getFont().deriveFont(size));
	}

	class ProtocolEntryView extends Box {

		private static final long serialVersionUID = -3644832831205433822L;
		RemoveAction _removeAction;
		JComboBox<Ship> _shipFieldCB;
		JTextField _destField;

		public ProtocolEntryView() {
			super(BoxLayout.LINE_AXIS);
			// remove
			_removeAction = new RemoveAction(ProtocolEntryView.this);
			JButton removeButton = new JButton(_removeAction);
			shrinkFont(removeButton, -1);
			removeButton.setMargin(new Insets(2, 2, 2, 2));
			add(removeButton);
			add(Box.createHorizontalStrut(6));
			// ship
			List<Ship> ships = new ArrayList<Ship>(_mapManager.getShips());
			Collections.sort(ships, new Comparator<Ship>() {
				@Override
				public int compare(Ship o1, Ship o2) {
					return new CompareToBuilder().append(o2.getCapacity(), o1.getCapacity()).toComparison();
				}
			});
			//Ship select = new Ship("-- choose ship --");
			Ship all = new Ship("<ALL>");
			Ship rest = new Ship("<Rest>");
			Ship unknown = new Ship("<Unknown>");
			ships.add(0, all);
			//ships.add(0, select);
			
			ships.add(rest);
			ships.add(unknown);
			_shipFieldCB = new JComboBox<Ship>(ships.toArray(new Ship[0]));
			shrinkFont(_shipFieldCB, -1);
			add(_shipFieldCB);
			add(Box.createHorizontalStrut(6));

			// dest
			_destField = new JTextField(15);
			add(_destField);
			add(Box.createHorizontalStrut(6));

			setBorder(new EmptyBorder(3, 3, 3, 0));
		}

	}

	class AddAction extends AbstractAction {

		private static final long serialVersionUID = 3079504856636198124L;

		public AddAction() {
			super("  +  ");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					addRow();
					revalidate();
				}
			});

		}
	}

	class RemoveAction extends AbstractAction {
		private static final long serialVersionUID = -632649060095568382L;
		Component _comp;

		public RemoveAction(Component comp) {
			super("  -  ");
			_comp = comp;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_box.remove(_comp);
					revalidate();
				}
			});

		}
	}

	private ShipProtocol loadShipProtocol() {
		try {
			List<ShipProtocol> shipProtocols = new JsonStorage().loadShipProtocols();

			for (ShipProtocol shipProtocol : shipProtocols) {
				if (shipProtocol.getName().equals("noname"))
					return shipProtocol;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {

		try {
			JFrame frame = new JFrame("TEST");
			MapManager mapManager = new MapManager(new ScreenScanner(null));
			mapManager.loadData();
			ProtocolEditor panel = new ProtocolEditor(mapManager);
			frame.getContentPane().add(panel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setBounds(400, 200, 500, 600);

			frame.setVisible(true);
		} catch (HeadlessException | IOException e) {
			e.printStackTrace();
		}

	}

	class NoneSelectedButtonGroup extends ButtonGroup {

		@Override
		public void setSelected(ButtonModel model, boolean selected) {

			if (selected) {

				super.setSelected(model, selected);

			} else {

				clearSelection();
			}
		}
	}

}
