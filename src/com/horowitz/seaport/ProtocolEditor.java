package com.horowitz.seaport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
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
	private JLabel _tl;
	private MapManager _mapManager;

	public ProtocolEditor(MapManager mapManager) {
		super(new BorderLayout());
		_mapManager = mapManager;
		initLayout();
	}

	private void initLayout() {
		JPanel root = new JPanel(new BorderLayout());

		_box = Box.createVerticalBox();

		root.add(_box, BorderLayout.NORTH);

		// ADD BUTTON
		JButton addButton = new JButton(new AddAction());
		JPanel addPanel = new JPanel(new BorderLayout());
		addPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
		addPanel.add(addButton, BorderLayout.WEST);

		JToolBar tempBar = new JToolBar();
		tempBar.add(new AbstractAction("save") {

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

		tempBar.add(new AbstractAction("load") {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	ShipProtocol sp = loadShipProtocol();
	        	uploadToView(sp);
	        	revalidate();
		        
	        }
        });
			}
		});
		addPanel.add(tempBar, BorderLayout.CENTER);

		root.add(addPanel, BorderLayout.SOUTH);

		add(root, BorderLayout.NORTH);
		JPanel testPanel = new JPanel();
		_tl = new JLabel("TEST GOES HERE");
		testPanel.add(_tl);
		add(testPanel);

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
		sp.setSlot(-1);
		sp.setName("noname");
		return sp;
	}

	private void addRow() {
		_box.add(new ProtocolEntryView());
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
			add(new JButton(_removeAction));
			add(Box.createHorizontalStrut(6));
			// ship
			List<Ship> ships = new ArrayList<Ship>(_mapManager.getShips());
			Collections.sort(ships, new Comparator<Ship>() {
				@Override
				public int compare(Ship o1, Ship o2) {
					return new CompareToBuilder().append(o2.getCapacity(), o1.getCapacity()).toComparison();
				}
			});
			Ship select = new Ship("-- choose ship --");
			Ship rest = new Ship("<Rest>");
			Ship unknown = new Ship("<Unknown>");
			ships.add(0, select);
			ships.add(rest);
			ships.add(unknown);
			_shipFieldCB = new JComboBox<Ship>(ships.toArray(new Ship[0]));
			// _shipField.setMaximumSize(new Dimension(120, 40));
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

	private void uploadToView(ShipProtocol sp) {
		_box.removeAll();
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
}
