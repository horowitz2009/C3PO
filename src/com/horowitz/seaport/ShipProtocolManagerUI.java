package com.horowitz.seaport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.ProtocolEntry;
import com.horowitz.seaport.model.ShipProtocol;
import com.horowitz.seaport.model.storage.JsonStorage;

public class ShipProtocolManagerUI extends JPanel {

	private JList<ShipProtocol> _protocolsCB;
	private MapManager _mapManager;
	private ProtocolEditor _editor;

	public ShipProtocolManagerUI(MapManager mapManager) {
		super();
		_mapManager = mapManager;
		initLayout();
		initLayout2();
		reload();
	}

	class MyListModel extends DefaultListModel<ShipProtocol> {

		@Override
		public ShipProtocol remove(int index) {
			// TODO Auto-generated method stub
			return super.remove(index);
		}
	}

	private void initLayout() {
		setLayout(new BorderLayout());
		Box box = Box.createVerticalBox();
		JPanel headerPanel = new JPanel(new BorderLayout());
		JToolBar toolbar = new JToolBar();
		box.add(toolbar);
		headerPanel.add(box, BorderLayout.EAST);
		toolbar.setFloatable(false);

		// THE LIST
		_model = new MyListModel();
		_protocolsCB = new JList<ShipProtocol>(_model);
		_protocolsCB.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_protocolsCB.setVisibleRowCount(3);
		// toolbar.add(new JScrollPane(_protocolsCB));
		headerPanel.add(new JScrollPane(_protocolsCB), BorderLayout.CENTER);
		{
			JButton button = new JButton(new AbstractAction("New") {

				@Override
				public void actionPerformed(ActionEvent e) {
					ShipProtocol newProtocol = new ShipProtocol("protocol1");
					newProtocol.setEntries(new ArrayList<ProtocolEntry>(2));
					newProtocol.setSlot(-1);
					_model.addElement(newProtocol);

					// _protocolsCB.getSelectionModel().setSelectionInterval(_model.getSize() - 1, _model.getSize() - 1);
					_protocolsCB.setSelectedValue(newProtocol, true);

				}
			});
			shrinkFont(button, -1);
			button.setMargin(new Insets(2, 2, 2, 2));

			toolbar.add(button);
		}
		{
			JButton button = new JButton(new AbstractAction("Delete") {

				@Override
				public void actionPerformed(ActionEvent e) {
					delete();
				}
			});
			shrinkFont(button, -1);
			button.setMargin(new Insets(2, 2, 2, 2));

			toolbar.add(button);
		}

		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		box.add(toolbar);

		{
			JButton button = new JButton(new AbstractAction("Save") {

				@Override
				public void actionPerformed(ActionEvent e) {
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

		add(headerPanel, BorderLayout.NORTH);
	}

	private void initLayout2() {
		_editor = new ProtocolEditor(_mapManager);
		add(_editor, BorderLayout.CENTER);

		_protocolsCB.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					ShipProtocol p = _protocolsCB.getSelectedValue();
					_editor.applyProtocol(p);
					revalidate();
				}
				// TODO Auto-generated method stub
				// if (e.getFirstIndex() == ItemEvent.SELECTED) {
				// ShipProtocol selectedProtocol = (ShipProtocol) e.getItem();
				// }

			}
		});

	}

	private void shrinkFont(Component comp, float size) {
		if (size < 0)
			size = comp.getFont().getSize() - 2;
		comp.setFont(comp.getFont().deriveFont(size));
	}

	private MyListModel _model;

	private void reload() {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					JsonStorage js = new JsonStorage();
					List<ShipProtocol> shipProtocols = js.loadShipProtocols();
					_model.clear();

					// Object selected = _protocolsCB.getSelectedItem();
					for (ShipProtocol shipProtocol : shipProtocols) {
						_model.addElement(shipProtocol);
						// if (shipProtocol.equals(selected)) {
						// _protocolsCB.setSelectedItem(shipProtocol);
						//
						// }
					}

					revalidate();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}

	private void save() {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					JsonStorage js = new JsonStorage();
					ShipProtocol editedProtocol = _editor.extractProtocol();
					ShipProtocol originalProtocol = (ShipProtocol) _protocolsCB.getSelectedValue();

					originalProtocol.setName(editedProtocol.getName());
					originalProtocol.setSlot(editedProtocol.getSlot());
					originalProtocol.setEntries(editedProtocol.getEntries());

					Enumeration<ShipProtocol> elements = _model.elements();
					List<ShipProtocol> newList = new ArrayList<ShipProtocol>();
					while (elements.hasMoreElements()) {
						ShipProtocol shipProtocol = (ShipProtocol) elements.nextElement();
						newList.add(shipProtocol);
					}

					js.saveShipProtocols(newList);
					// _protocolsCB.setSelectedValue(null, false);
					_protocolsCB.setSelectedValue(originalProtocol, true);
					revalidate();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}

	private void delete() {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					JsonStorage js = new JsonStorage();
					int index = _protocolsCB.getSelectedIndex();
					_model.remove(index);

					Enumeration<ShipProtocol> elements = _model.elements();
					List<ShipProtocol> newList = new ArrayList<ShipProtocol>();
					while (elements.hasMoreElements()) {
						ShipProtocol shipProtocol = (ShipProtocol) elements.nextElement();
						newList.add(shipProtocol);
					}

					js.saveShipProtocols(newList);
					// _protocolsCB.setSelectedValue(null, false);
					// _protocolsCB.setSelectedValue(originalProtocol, true);
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
			ShipProtocolManagerUI panel = new ShipProtocolManagerUI(mapManager);
			frame.getContentPane().add(panel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setBounds(400, 200, 400, 600);

			frame.setVisible(true);
		} catch (HeadlessException | IOException e) {
			e.printStackTrace();
		}

	}

	public ShipProtocol getSelectedShipProtocol() {
		return _protocolsCB.getSelectedValue();
	}

	public void addListSelectionListener(ListSelectionListener listener) {
		_protocolsCB.addListSelectionListener(listener);
	}

}
