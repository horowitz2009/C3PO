package com.horowitz.seaport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.ProtocolEntry;
import com.horowitz.seaport.model.ShipProtocol;
import com.horowitz.seaport.model.storage.JsonStorage;

public class ProtocolManagerUI extends JPanel {

	private JComboBox<ShipProtocol> _protocolsCB;
	private MapManager _mapManager;
	private ProtocolEditor _editor;

	public ProtocolManagerUI(MapManager mapManager) {
		super();
		_mapManager = mapManager;
		initLayout();
		initLayout2();
		reload();
	}

	private void initLayout() {
		setLayout(new BorderLayout());
		// Box box = Box.createHorizontalBox();
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		// combobox

		_protocolsCB = new JComboBox<ShipProtocol>();
		toolbar.add(_protocolsCB);

		{
			JButton button = new JButton(new AbstractAction("New") {

				@Override
				public void actionPerformed(ActionEvent e) {
					ShipProtocol newProtocol = new ShipProtocol("protocol1");
					newProtocol.setEntries(new ArrayList<ProtocolEntry>(2));
					newProtocol.setSlot(-1);
					_protocolsCB.addItem(newProtocol);
					_protocolsCB.setSelectedItem(newProtocol);
				}
			});
			shrinkFont(button, -1);
			button.setMargin(new Insets(2, 2, 2, 2));

			toolbar.add(button);
		}
		{
			JButton button = new JButton(new AbstractAction("Save") {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub

					save();
				}
			});
			shrinkFont(button, -1);
			button.setMargin(new Insets(2, 2, 2, 2));

			toolbar.add(button);
		}
		{
			JButton button = new JButton(new AbstractAction("Reload") {

				@Override
				public void actionPerformed(ActionEvent e) {
					reload();
				}
			});
			shrinkFont(button, -1);
			button.setMargin(new Insets(2, 2, 2, 2));

			toolbar.add(button);
		}

		add(toolbar, BorderLayout.NORTH);
	}

	private void initLayout2() {
		_editor = new ProtocolEditor(_mapManager);
		add(_editor, BorderLayout.CENTER);

		_protocolsCB.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					ShipProtocol selectedProtocol = (ShipProtocol) e.getItem();
					_editor.applyProtocol(selectedProtocol);
					revalidate();
				}
			}
		});

	}

	private void shrinkFont(Component comp, float size) {
		if (size < 0)
			size = comp.getFont().getSize() - 2;
		comp.setFont(comp.getFont().deriveFont(size));
	}

	private void reload() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					JsonStorage js = new JsonStorage();
					_shipProtocols = js.loadShipProtocols();

					_protocolsCB.removeAllItems();
					ShipProtocol chooseItem = new ShipProtocol("--select--");
					_protocolsCB.addItem(chooseItem);
					Object selected = _protocolsCB.getSelectedItem();
					for (ShipProtocol shipProtocol : _shipProtocols) {
						_protocolsCB.addItem(shipProtocol);
						if (shipProtocol.equals(selected)) {
							_protocolsCB.setSelectedItem(shipProtocol);

						}
					}

					revalidate();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}

	private List<ShipProtocol> _shipProtocols;

	private void save() {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					JsonStorage js = new JsonStorage();
					ShipProtocol editedProtocol = _editor.extractProtocol();
					ShipProtocol originalProtocol = (ShipProtocol) _protocolsCB.getSelectedItem();
					
					originalProtocol.setName(editedProtocol.getName());
					originalProtocol.setSlot(editedProtocol.getSlot());
					originalProtocol.setEntries(editedProtocol.getEntries());
					
					js.saveShipProtocols(_shipProtocols);
					_protocolsCB.setSelectedItem(null);
					_protocolsCB.setSelectedItem(originalProtocol);
					revalidate();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}

	public static void main(String[] args) {
		try {
			JFrame frame = new JFrame("TEST");
			MapManager mapManager = new MapManager(new ScreenScanner(null));
			mapManager.loadData();
			ProtocolManagerUI panel = new ProtocolManagerUI(mapManager);
			frame.getContentPane().add(panel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setBounds(400, 200, 400, 600);

			frame.setVisible(true);
		} catch (HeadlessException | IOException e) {
			e.printStackTrace();
		}

	}
}
