/* File: SeaPortProgram.java
 * Date: 11/30/2019
 * Author: Zachary Finnegan
 * Purpose: Creates a JFileChooser for the user to select an appropriate data file.
 * The data file is parsed using other classes. Once parsed the data is displayed to 
 * the user in a GUI created by this program. The GUI allows for searching and sorting 
 * of the data based on several different filters and a search box.
 */

package seaPortProject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.text.Format;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

public class SeaPortProgram extends JFrame {	
	private static final long serialVersionUID = 1L;
	protected JPanel treePanel, tablePanel;
	protected JTable jobsTable, resourceTable;
	protected JTree tree;
	private JLabel searchL, sortL;
	private JTextField searchF;
	private JTextArea contentA; 
	protected JSplitPane splitPane;
	protected static JTextArea resourceRequestLog = new JTextArea(); 
	protected static JTextArea resourceReturnLog = new JTextArea(); 
	protected static JTextArea shipMovementText = new JTextArea();
	private JScrollPane resourceTableScroll, resourceTextScroll, resourceReturnScroll;
	private JButton searchB, sortB;
	private JCheckBox reverseCheck, queCheck;
	private JComboBox<String> searchBox, sortBox, skillsBox, portBox;
	protected String[] searchBoxList = {"Name", "Index", "Skill", "All Ships", "Docked Ships", "Queued Ships", "All Docks", "Occupied Docks", "Vacant Docks", "People"};
	protected String[] sortBoxList = {"Name", "Skill", "Ship Name", "Ship Draft", "Ship Weight", "Ship Length", "Ship Width"};
	protected String[] portBoxList;
	protected String[] skillBoxList;
	protected static World world;
	protected DefaultMutableTreeNode root;
	protected static DefaultTreeModel treeModel;
	protected GridBagLayout gridy = new GridBagLayout();
	
	public static void main(String[] args) {
		/*
		 * Runs the JFileChooser for selecting the data file.
		 * This file is then used to create a new world class where
		 * the file is parsed. Once the data file is parsed the world 
		 * is used as the parameter in the SeaPortProgram constructor.
		 */
		JFileChooser fc = new JFileChooser(".");
		int returnVal = fc.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this file: " + fc.getSelectedFile().getName());
			try {
				world = new World(fc.getSelectedFile());
				new SeaPortProgram(world);
			} catch (FileNotFoundException e1) {
				JOptionPane.showMessageDialog(null, "File not found.");
			} catch (NoSuchFieldException e2) {
				JOptionPane.showMessageDialog(null, e2.getMessage());
			} catch (NullPointerException e3) {}
		}
	}
	
	public SeaPortProgram(World world) {
		/*
		 * Creates the core GUI. Displays the information in the input data file
		 * and allows searching of the input data and then displays that data.
		 */
		// basic frame setup
		this.setTitle("Sea Port Program");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		//Basic Frame Tabbed panel	
		//Left most panel layout
		//bottom of left panel
		setupSplitPane();

		//Search, sort and hire panel adding to left most panel
		JPanel topP = new JPanel();		
		JPanel theLeftPanel = new JPanel(new BorderLayout());
		theLeftPanel.add(searchSortHirePanelSetup(topP), BorderLayout.PAGE_START);
		
		//ResourcePanel setup
		JPanel resourcePanel = new JPanel(new BorderLayout());
		JPanel resourceRequestPanel = new JPanel(new BorderLayout());
		JPanel superResourcePanel = new JPanel(new BorderLayout());
		resourcePanel.add(setupResourceTablePanel(), BorderLayout.LINE_START);
		resourcePanel.add(setupResourceReturnPanel(), BorderLayout.CENTER);
		resourceRequestPanel.add(setupResourceTextPanel(), BorderLayout.CENTER);
		superResourcePanel.add(resourcePanel, BorderLayout.PAGE_START);
		superResourcePanel.add(resourceRequestPanel, BorderLayout.CENTER);
		
		//TabbedPane Setup adds 
		JTabbedPane tabby = new JTabbedPane();
		tabby.add("File Data", splitPane);
		tabby.add("Resource Tracking", superResourcePanel);
		tabby.add("Arrivals and Departures", shipMovementPanelSetup());
		theLeftPanel.add(tabby, BorderLayout.LINE_START);
		tabby.setPreferredSize(new Dimension(700, 600));
		
		//JTable 
		jobsTable = new JTable(world.createTable());
		jobTableSetup();
		tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(jobsTable, BorderLayout.CENTER);
		JScrollPane jobsScroll = new JScrollPane(tablePanel);
		jobsScroll.getVerticalScrollBar().setUnitIncrement(16);
		jobsScroll.setPreferredSize(new Dimension(900, 600));
		jobsScroll.setColumnHeaderView(jobsTable.getTableHeader());
		jobsScroll.setBorder(BorderFactory.createTitledBorder("Job Progress Table"));
		
		JPanel theRightPanel = new JPanel(new BorderLayout());
		theRightPanel.add(jobsScroll, BorderLayout.CENTER);
		
		//Action listeners. Rather long but simplified. Method calls are based on
		//the input from the comboboxes, textfields and checkboxes. From those inputs
		//a string will be built and output to the textarea contentA. Switch functions
		//are used for differentiating between the combobox options.
		sortB.addActionListener((ActionEvent e) -> {
			if(e.getSource() == sortB) {
				String pick = (String) sortBox.getSelectedItem();
				boolean isReverse = reverseCheck.isSelected();
				boolean isQue = queCheck.isSelected();
				String reverseSt = "";
				String queSt = "";
				if(isReverse) {
					reverseSt += "reverse ";
				}
				if(isQue) {
					queSt += "Queued Ships Only\n";
				}
				String st = ">>>>The World " + reverseSt + "sorted by: " + pick + "\n";
				switch(pick) {
					case "Name":
						world.NameSort(isReverse);
						contentA.setText(world.toString("Name"));
						break;
					case "Skill":
						contentA.setText(st + world.SkillSort(isReverse));
						break;
					case "Ship Name":
						if(isReverse) {
							contentA.setText(st + queSt + world.ShipNameSort(isQue, new NameComparator().reversed()));
						}else {
							contentA.setText(st + queSt + world.ShipNameSort(isQue, new NameComparator()));
						}
						break;
					case "Ship Weight":
						if(isReverse) {
							contentA.setText(st + queSt + world.ShipSortByInput(isQue, new ShipComparatorByWeight().reversed()));
						} else {
							contentA.setText(st + queSt + world.ShipSortByInput(isQue, new ShipComparatorByWeight()));
						}
						break;
					case "Ship Width":
						if(isReverse) {
							contentA.setText(st + queSt + world.ShipSortByInput(isQue, new ShipComparatorByWidth().reversed()));
						} else {
							contentA.setText(st + queSt + world.ShipSortByInput(isQue, new ShipComparatorByWidth()));
						}
						break;
					case "Ship Draft":
						if(isReverse) {
							contentA.setText(st + queSt + world.ShipSortByInput(isQue, new ShipComparatorByDraft().reversed()));
						} else {
							contentA.setText(st + queSt + world.ShipSortByInput(isQue, new ShipComparatorByDraft()));
						}
						break;
					case "Ship Length":
						if(isReverse) {
							contentA.setText(st + queSt + world.ShipSortByInput(isQue, new ShipComparatorByLength().reversed()));
						} else {
							contentA.setText(st + queSt + world.ShipSortByInput(isQue, new ShipComparatorByLength()));
						}
						break;
				}
			}
		});
		
		searchB.addActionListener((ActionEvent e2) -> {
			if(e2.getSource() == searchB) {
				String pick = (String) searchBox.getSelectedItem();
				switch(pick) {
					case "Index":
						if(!searchF.getText().isEmpty()) {
							if(isInteger(searchF.getText())) {
								int index = Integer.parseInt(searchF.getText());
								contentA.setText(world.findByIndex(index));
							}
						}
						break;
					case "Name":
						if(!searchF.getText().isEmpty()) {
							String name = searchF.getText();
							contentA.setText(world.findByName(name));
						}
						break;
					case "Skill":
						if(!searchF.getText().isEmpty()) {
							String skill = searchF.getText();
							contentA.setText(world.findBySkill(skill));
						}
						break;
					case "All Ships":
						contentA.setText(world.getShips("All"));
						break;
					case "Docked Ships":
						contentA.setText(world.getShips("Docked"));
						break;
					case "Queued Ships":
						contentA.setText(world.getShips("Queued"));
						break;
					case "All Docks":
						contentA.setText(world.getDocks("All"));
						break;
					case "Occupied Docks":
						contentA.setText(world.getDocks("Occupied"));
						break;
					case "Vacant Docks":
						contentA.setText(world.getDocks("Vacant"));
						break;
					case "People":
						contentA.setText(world.getPeople());
						break;
				}
			}
		});

		
		

		JPanel thePanel = new JPanel(new BorderLayout());
		thePanel.add(theLeftPanel, BorderLayout.LINE_START);
		thePanel.add(theRightPanel, BorderLayout.CENTER);
		this.add(thePanel);
		this.pack();
		this.setVisible(true);
	}
	
	private JPanel shipMovementPanelSetup() {
		JPanel shipMovePanel = new JPanel(new BorderLayout());
		shipMovementText.setFont(new Font("Serif", Font.PLAIN, 18));
		JScrollPane shipScrolly = new JScrollPane(shipMovementText);
		shipMovePanel.add(shipScrolly, BorderLayout.CENTER);
		return shipMovePanel;
		
	}
	private JPanel searchSortHirePanelSetup(JPanel topPanel) {
		searchL = new JLabel("Search: ");
		sortL = new JLabel("Sort: ");
		searchF = new JTextField("", 12);
		searchB = new JButton("Search");
		sortB = new JButton("Sort");
		searchBox = new JComboBox<>(searchBoxList);
		searchBox.setBackground(Color.white);
		sortBox = new JComboBox<>(sortBoxList);
		sortBox.setBackground(Color.white);
		reverseCheck = new JCheckBox("Reverse");
		queCheck = new JCheckBox("Queued");
		
		JPanel searchP = new JPanel();
		JPanel checkP = new JPanel();
		JPanel sortP = new JPanel();
		JPanel topP = new JPanel();
		JPanel hirePeoplePanel = new JPanel();
		hirePanelSetup(hirePeoplePanel);
		
		searchP.setLayout(gridy);
		checkP.setLayout(gridy);
		sortP.setLayout(gridy);
		topP.setLayout(gridy);
		topP.setBorder(BorderFactory.createTitledBorder("Search, Sort and Hire Panel"));
		
		addComp(checkP, reverseCheck, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addComp(checkP, queCheck, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		
		addComp(searchP, searchL, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addComp(searchP, searchF, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addComp(searchP, searchB, 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addComp(searchP, searchBox, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);

		addComp(sortP, sortL, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addComp(sortP, sortBox, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addComp(sortP, checkP, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addComp(sortP, sortB, 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		
		addComp(topP, searchP, 0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE);
		addComp(topP, sortP, 1, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE);
		addComp(topP, hirePeoplePanel, 2, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE);

		return topP;
	}
	
	private JPanel hirePanelSetup(JPanel hirePeoplePanel) {
		JPanel portPanel = new JPanel();
		JPanel skillPanel = new JPanel();
		hirePeoplePanel.setLayout(gridy);
		portPanel.setLayout(gridy);
		skillPanel.setLayout(gridy);
		JButton hireButton = new JButton("Hire");
		JLabel skillsLabel = new JLabel("Needed Skills:");
		JLabel portLabel = new JLabel("Port to Hire:");
		String[] skillArray = {"               "};
		skillsBox = new JComboBox<String>(skillArray);
		skillsBox.setBackground(Color.white);
		portBox = new JComboBox<String>(world.getPortNames());
		portBox.setBackground(Color.white);
		portBox.addActionListener((ActionEvent e) -> {
			if(e.getSource() == portBox) {
				updateSkillsBox();
			}
		});
		
		hireButton.addActionListener((ActionEvent e) -> {
			if(e.getSource() == hireButton) {
				try {
					if(!portBox.getSelectedItem().equals(null) && !skillsBox.getSelectedItem().equals(null))
						world.hirePerson((String) portBox.getSelectedItem(), (String) skillsBox.getSelectedItem());
						updateSkillsBox();
				}catch(NullPointerException e1) {}
			}
		});
		
		addComp(hirePeoplePanel, portLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addComp(hirePeoplePanel, portBox, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addComp(hirePeoplePanel, skillsLabel, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addComp(hirePeoplePanel, skillsBox, 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		addComp(hirePeoplePanel, hireButton, 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		
		return hirePeoplePanel;
	}
	
	private void setupSplitPane() {
		contentA = new JTextArea();
		contentA.setText(world.toString());
		JScrollPane sp = new JScrollPane(contentA, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setMinimumSize(new Dimension(200, 50));

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jTreeAndPanelSetup(), sp);
		splitPane.setDividerLocation(200);
		splitPane.setPreferredSize(new Dimension(700, 600));
	}
	
	private JPanel jTreeAndPanelSetup() {
		treePanel = new JPanel(new BorderLayout());
		treePanel.setMinimumSize(new Dimension(100, 50));
		root = new DefaultMutableTreeNode("The World");
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setEditable(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		drawTree();
		tree.expandRow(0);
		JScrollPane treeScroll = new JScrollPane(tree);
		treePanel.add(treeScroll);
		return treePanel;
	}
	
	private void jobTableSetup() {
		jobsTable.addMouseListener(new MouseListener(jobsTable));
		jobsTable.setDefaultRenderer(JProgressBar.class, new CustomCellRenderer(jobsTable));
		jobsTable.setDefaultRenderer(JButton.class, new CustomCellRenderer());
		jobsTable.setDefaultRenderer(JLabel.class, new CustomCellRenderer(jobsTable));
		jobsTable.setFont(new Font("Serif", Font.PLAIN, 12));
		jobsTable.setRowHeight(jobsTable.getRowHeight()+10);
		setJobColumnWidth();
	}
	
	private void setJobColumnWidth() {
		jobsTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		jobsTable.getColumnModel().getColumn(1).setPreferredWidth(40);
		jobsTable.getColumnModel().getColumn(2).setPreferredWidth(35);
		jobsTable.getColumnModel().getColumn(3).setPreferredWidth(200);
		jobsTable.getColumnModel().getColumn(4).setPreferredWidth(50);
		jobsTable.getColumnModel().getColumn(5).setPreferredWidth(50);
		jobsTable.getColumnModel().getColumn(6).setPreferredWidth(40);
	}
	
	private JScrollPane setupResourceTextPanel() {
		resourceRequestLog.setVisible(true);
		JPanel reReqPanel = new JPanel(new BorderLayout());
		reReqPanel.add(resourceRequestLog);
		resourceTextScroll = new JScrollPane(reReqPanel);
		resourceTextScroll.getVerticalScrollBar().setUnitIncrement(16);
		resourceTextScroll.setPreferredSize(new Dimension(600, 200));
		resourceRequestLog.setCaretPosition(resourceRequestLog.getDocument().getLength());
		resourceTextScroll.setBorder(BorderFactory.createTitledBorder("Resource Request Log"));
		return resourceTextScroll;
	}
	
	public JScrollPane setupResourceTablePanel() {
		JPanel resourceTablePanel = new JPanel(new BorderLayout());
		resourceTable = new JTable(world.createResourceTableModel(resourceTablePanel));
		resourceTable.setFont(new Font("Serif", Font.BOLD, 15));
		resourceTable.setRowHeight(resourceTable.getRowHeight()+4);
		resourceTable.setDefaultRenderer(JLabel.class, new CustomCellRenderer(resourceTable));
		resourceTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		resourceTable.getColumnModel().getColumn(1).setPreferredWidth(20);
		resourceTable.getColumnModel().getColumn(2).setPreferredWidth(20);
		resourceTablePanel.add(resourceTable, BorderLayout.CENTER);
		resourceTableScroll = new JScrollPane(resourceTablePanel);
		resourceTableScroll.setPreferredSize(new Dimension(250, 20));
		resourceTableScroll.setColumnHeaderView(resourceTable.getTableHeader());
		resourceTableScroll.setBorder(BorderFactory.createTitledBorder("Workers"));
		
		return resourceTableScroll;
	}
	
	public JScrollPane setupResourceReturnPanel() {
		resourceReturnLog.setVisible(true);
		resourceReturnScroll = new JScrollPane(resourceReturnLog);
		resourceReturnScroll.getVerticalScrollBar().setUnitIncrement(16);
		resourceReturnScroll.setPreferredSize(new Dimension(450, 200));
		resourceReturnLog.setCaretPosition(resourceReturnLog.getDocument().getLength());
		resourceReturnScroll.setBorder(BorderFactory.createTitledBorder("Resource Return and Addition Log"));
		return resourceReturnScroll;
	}
	
	public void updateSkillsBox() {
		skillsBox.removeAllItems();
		String selection = (String) portBox.getSelectedItem();
		for(SeaPort port: world.ports) {
			if(selection.equalsIgnoreCase(port.name)) {
				Set<String> tempSet = new HashSet<String>(port.getMissingSkillsList());
				for(String skill: tempSet) {
					skillsBox.addItem(skill);
				}
			}
		}
	}
	
	public static void updateResourceRequestLog(String newLog) {
		resourceRequestLog.append(newLog);
		resourceRequestLog.setCaretPosition(resourceRequestLog.getDocument().getLength());
	}
	
	public static void updateResourceReturnLog(String newLog) {
		resourceReturnLog.append(newLog);
		resourceReturnLog.setCaretPosition(resourceReturnLog.getDocument().getLength());
	}
	
	public static void updateArrivalsAndDepartures(String newMovement) {
		shipMovementText.append(newMovement);
		shipMovementText.setCaretPosition(shipMovementText.getDocument().getLength());
	}
	
	// method for adding components to panels using the gridbaglayout
	private void addComp(JPanel thePanel, JComponent comp, int xPos, int yPos, int compWidth, int compHeight,
			int anchor, int stretch) {
		GridBagConstraints gBag = new GridBagConstraints();
		gBag.gridx = xPos;
		gBag.gridy = yPos;
		gBag.gridwidth = compWidth;
		gBag.gridheight = compHeight;
		gBag.insets = new Insets(5, 5, 5, 5);
		gBag.weightx = 100;
		gBag.weighty = 100;
		gBag.anchor = anchor;
		gBag.fill = stretch;

		thePanel.add(comp, gBag);

	}
	
	public boolean isInteger(String s) {
	    try { 
	       Integer.parseInt(s); 
	   } catch(NumberFormatException e) { 
	       return false; 
	   } catch(NullPointerException e) {
	       return false;
	   }
	   // only got here if we didn't return false
	   return true;
	}

	private void drawTree() {
		for(SeaPort port: world.ports) {
			DefaultMutableTreeNode portNode = new DefaultMutableTreeNode(port.getName());
			root.add(portNode);
			DefaultMutableTreeNode dockParent = new DefaultMutableTreeNode("Docks");
			DefaultMutableTreeNode shipParent = new DefaultMutableTreeNode("Ships");
			DefaultMutableTreeNode queuedParent = new DefaultMutableTreeNode("Queued Ships");
			DefaultMutableTreeNode peopleParent = new DefaultMutableTreeNode("People");
			portNode.add(dockParent);
			portNode.add(shipParent);
			portNode.add(queuedParent);
			portNode.add(peopleParent);
			
			for(Dock dock: port.docks) {
				DefaultMutableTreeNode dockChild = new DefaultMutableTreeNode(dock.getName());
				dockParent.add(dockChild);
				DefaultMutableTreeNode dockedShipNode = new DefaultMutableTreeNode(dock.getShip().getName());
				dockChild.add(dockedShipNode);
			}
			
			for(Ship ship: port.ships) {
				DefaultMutableTreeNode shipChild = new DefaultMutableTreeNode(ship.getName());
				shipParent.add(shipChild);
				DefaultMutableTreeNode jobParent = new DefaultMutableTreeNode("Jobs");
				shipChild.add(jobParent);
				for(Job job: ship.jobs) {
					DefaultMutableTreeNode jobChild = new DefaultMutableTreeNode(job.getName());
					jobParent.add(jobChild);
				}
			}
			for(Ship ship: port.que) {
				DefaultMutableTreeNode shipChild = new DefaultMutableTreeNode(ship.getName());
				queuedParent.add(shipChild);
				DefaultMutableTreeNode jobParent = new DefaultMutableTreeNode("Jobs");
				shipChild.add(jobParent);
				for(Job job: ship.jobs) {
					DefaultMutableTreeNode jobChild = new DefaultMutableTreeNode(job.getName());
					jobParent.add(jobChild);
				}
			}
			for(Person person: port.persons) {
				DefaultMutableTreeNode personChild = new DefaultMutableTreeNode(person.getName());
				peopleParent.add(personChild);
				DefaultMutableTreeNode skillNode = new DefaultMutableTreeNode(person.getSkill());
				personChild.add(skillNode);
			}
		}
		validate();
	}

}
