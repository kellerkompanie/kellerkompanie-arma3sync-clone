package fr.soe.a3s.ui.main;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.xml.parsers.ParserConfigurationException;

import net.jimmc.jshortcut.JShellLink;

import org.xml.sax.SAXException;

import fr.soe.a3s.constant.CheckRepositoriesFrequency;
import fr.soe.a3s.constant.DefaultProfileName;
import fr.soe.a3s.constant.GameExecutables;
import fr.soe.a3s.constant.MinimizationType;
import fr.soe.a3s.constant.ProtocolType;
import fr.soe.a3s.dao.DataAccessConstants;
import fr.soe.a3s.domain.AbstractProtocole;
import fr.soe.a3s.domain.Ftp;
import fr.soe.a3s.domain.configration.LauncherOptions;
import fr.soe.a3s.dto.configuration.PreferencesDTO;
import fr.soe.a3s.exception.CheckException;
import fr.soe.a3s.exception.LoadingException;
import fr.soe.a3s.exception.WritingException;
import fr.soe.a3s.service.CommonService;
import fr.soe.a3s.service.ConfigurationService;
import fr.soe.a3s.service.ConnectionService;
import fr.soe.a3s.service.LaunchService;
import fr.soe.a3s.service.PreferencesService;
import fr.soe.a3s.service.ProfileService;
import fr.soe.a3s.service.RepositoryService;
import fr.soe.a3s.ui.Facade;
import fr.soe.a3s.ui.UIConstants;
import fr.soe.a3s.ui.help.AboutDialog;
import fr.soe.a3s.ui.help.AutoConfigExportDialog;
import fr.soe.a3s.ui.help.AutoConfigImportDialog;
import fr.soe.a3s.ui.help.PreferencesDialog;
import fr.soe.a3s.ui.help.ProxyConfigurationDialog;
import fr.soe.a3s.ui.main.dialogs.WelcomeDialog;
import fr.soe.a3s.ui.main.tasks.TaskCheckRepositories;
import fr.soe.a3s.ui.main.tasks.TasksManager;
import fr.soe.a3s.ui.profiles.ProfileSelectionDialog;
import fr.soe.a3s.ui.repository.RepositoryPanel;
import fr.soe.a3s.ui.tools.acre2.FirstPageACRE2InstallerDialog;
import fr.soe.a3s.ui.tools.aia.AiaInstallerDialog;
import fr.soe.a3s.ui.tools.bikey.BiKeyExtractorDialog;
import fr.soe.a3s.ui.tools.rpt.RptViewerPanel;
import fr.soe.a3s.ui.tools.tfar.FirstPageTFARInstallerPanel;

public class MainPanel extends JFrame implements UIConstants {

	private final Facade facade;

	/* UI */
	private static final String TAB_TITLE_ADDONS = "Addons";
	private static final String TAB_TITLE_ADDON_OPTIONS = "Addon Options";
	private static final String TAB_TITLE_LAUNCH_OPTIONS = "Launcher Options";
	private static final String TAB_TITLE_ONLINE = "Online";
	private static final String TAB_TITLE_EXTENAL_APPS = "External Apps";
	private static final String TAB_TITLE_SYNC = "Repositories";
	private JMenuBar menuBar;
	private JMenu menuProfiles, menuGroups, menuHelp, menuTools, menuItemAutoConfig;
	private JMenuItem menuItemEdit, menuItemHelp, menuItemuUpdates, menuItemAbout, menuItemPreferences,
			menuItemACRE2wizard, menuItemRPTviewer, menuItemeExportAsShortcut, menuItemAiAwizard, menuItemBISforum,
			menuItemAutoConfigImport, menuItemAutoConfigExport, menuItemBikeyExtractor, menuItemConfigureProxy;
	private JTabbedPane tabbedPane;
	private JPanel infoPanel, launchPanel;
	private PopupMenu popup;
	private MenuItem launchItem, exitItem;
	private final Container contenu;
	private JMenuItem menuItemAddGroup, menuItemDuplicateGroup, menuItemRenameGroup, menuItemRemoveGroup,
			menuItemTFARwizard, menuDonate;

	/* System tray */
	private SystemTray tray;
	private TrayIcon trayIcon;

	/* Services */
	private final ConfigurationService configurationService = new ConfigurationService();
	private final ProfileService profileService = new ProfileService();
	private final CommonService commonService = new CommonService();
	private final PreferencesService preferencesService = new PreferencesService();
	private final RepositoryService repositoryService = new RepositoryService();
	private final LaunchService launchService = new LaunchService();

	/* Manager */
	private final DynamicTabManager dynamicTabManager = new DynamicTabManager();

	public MainPanel(Facade facade) {

		this.facade = facade;
		this.facade.setMainPanel(this);
		setTitle(APPLICATION_NAME);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setResizable(true);
		setIconImage(ICON);
		contenu = this.getContentPane();
		this.setLocationRelativeTo(null);
	}

	public void drawGUI() {

		/* Toolbar */
		menuBar = new JMenuBar();
		menuProfiles = new JMenu("Profiles");
		menuBar.add(menuProfiles);
		menuItemEdit = new JMenuItem("Edit", new ImageIcon(EDIT));
		menuItemeExportAsShortcut = new JMenuItem("Shortcut", new ImageIcon(SHORTCUT));
		JSeparator s = new JSeparator();
		menuProfiles.add(menuItemEdit);
		menuProfiles.add(menuItemeExportAsShortcut);
		menuProfiles.add(s);

		menuGroups = new JMenu("Groups");
		menuItemAddGroup = new JMenuItem("Add");
		menuItemDuplicateGroup = new JMenuItem("Duplicate");
		menuItemRenameGroup = new JMenuItem("Rename");
		menuItemRemoveGroup = new JMenuItem("Remove");
		menuGroups.add(menuItemAddGroup);
		menuGroups.add(menuItemDuplicateGroup);
		menuGroups.add(menuItemRenameGroup);
		menuGroups.add(menuItemRemoveGroup);
		// menuBar.add(menuGroups);

		menuTools = new JMenu("Tools");
		menuBar.add(menuTools);
		menuItemACRE2wizard = new JMenuItem("ACRE 2 installer", new ImageIcon(ACRE2_SMALL));
		// menuTools.add(menuItemACRE2wizard);
		menuItemTFARwizard = new JMenuItem("TFAR installer", new ImageIcon(TFAR_SMALL));
		// menuTools.add(menuItemTFARwizard);
		menuItemAiAwizard = new JMenuItem("AiA tweaker", new ImageIcon(AIA_SMALL));
		menuTools.add(menuItemAiAwizard);
		menuItemRPTviewer = new JMenuItem("RPT viewer", new ImageIcon(RPT_SMALL));
		menuTools.add(menuItemRPTviewer);
		menuItemBikeyExtractor = new JMenuItem("Bikey extractor", new ImageIcon(BIKEY_SMALL));
		menuTools.add(menuItemBikeyExtractor);
		menuHelp = new JMenu("Help");
		menuItemHelp = new JMenuItem("Online Help", new ImageIcon(HELP));
		menuHelp.add(menuItemHelp);
		menuItemBISforum = new JMenuItem("BIS Forum", new ImageIcon(BIS));
		menuHelp.add(menuItemBISforum);
		JSeparator s1 = new JSeparator();
		menuHelp.add(s1);
		menuItemPreferences = new JMenuItem("Preferences", new ImageIcon(PREFERENCES));
		menuHelp.add(menuItemPreferences);
		menuItemAutoConfig = new JMenu("Auto-config");
		menuHelp.add(menuItemAutoConfig);
		menuItemAutoConfigImport = new JMenuItem("Import");
		menuItemAutoConfig.add(menuItemAutoConfigImport);
		menuItemAutoConfigExport = new JMenuItem("Export");
		menuItemAutoConfig.add(menuItemAutoConfigExport);
		menuItemConfigureProxy = new JMenuItem("Configure proxy");
		menuHelp.add(menuItemConfigureProxy);
		menuItemuUpdates = new JMenuItem("Check for Updates", new ImageIcon(UPDATE));
		menuHelp.add(menuItemuUpdates);
		JSeparator s2 = new JSeparator();
		menuHelp.add(s2);
		menuDonate = new JMenuItem("Donate");
		menuHelp.add(menuDonate);
		JSeparator s3 = new JSeparator();
		menuHelp.add(s3);
		menuItemAbout = new JMenuItem("About", new ImageIcon(ABOUT));
		menuHelp.add(menuItemAbout);
		menuBar.add(menuHelp);
		setJMenuBar(menuBar);

		/* Info panel */
		infoPanel = new InfoPanel(facade);
		contenu.add(infoPanel, BorderLayout.NORTH);

		/* Tab panel */
		tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.addTab(TAB_TITLE_ADDONS, new AddonsPanel(facade));
		tabbedPane.addTab(TAB_TITLE_ADDON_OPTIONS, new AddonOptionsPanel(facade));
		tabbedPane.addTab(TAB_TITLE_LAUNCH_OPTIONS, new LauncherOptionsPanel(facade));
		tabbedPane.addTab(TAB_TITLE_ONLINE, new OnlinePanel(facade));
		tabbedPane.addTab(TAB_TITLE_EXTENAL_APPS, new ExternalApplicationsPanel(facade));
		tabbedPane.addTab(TAB_TITLE_SYNC, new SyncPanel(facade));
		contenu.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setFocusable(false);

		JPanel sidePanel1 = new JPanel();
		contenu.add(sidePanel1, BorderLayout.EAST);
		JPanel sidePanel2 = new JPanel();
		contenu.add(sidePanel2, BorderLayout.WEST);

		/* Launch panel */
		launchPanel = new LaunchPanel(facade);
		contenu.add(launchPanel, BorderLayout.SOUTH);

		/* Tray Icon */
		if (SystemTray.isSupported()) {
			trayIcon = new TrayIcon(TRAYICON, "ArmA3Sync");
			tray = SystemTray.getSystemTray();
			popup = new PopupMenu();
			trayIcon.setPopupMenu(popup);
			launchItem = new MenuItem("ArmA3Sync");
			exitItem = new MenuItem("Exit");
			popup.add(launchItem);
			popup.addSeparator();
			popup.add(exitItem);
		} else {
			System.out.println("System Tray is not supported by your system.");
		}

		menuItemEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				menuItemEditPerformed();
			}
		});
		menuItemeExportAsShortcut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						menuItemExportAsShortcutPerformed();
					}
				});
			}
		});
		menuItemACRE2wizard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						menuItemACRE2wizardPerformed();
					}
				});
			}
		});

		menuItemTFARwizard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						menuItemTFARwizardPerformed();
					}
				});
			}
		});
		menuItemAiAwizard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						menuItemAiAwizardPerformed();
					}
				});
			}
		});
		menuItemRPTviewer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				menuItemRPTviewerPerformed();
			}
		});
		menuItemBikeyExtractor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				menuItemBikeyExtractorPerformed();
			}
		});
		menuItemHelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				menuItemHelpPerformed();
			}
		});
		menuItemBISforum.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				menuItemBISforumPerformed();
			}
		});
		menuItemPreferences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuItemPreferencesPerformed();
			}
		});
		menuItemAutoConfigImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuItemAutoConfigImportPerformed();
			}
		});
		menuItemAutoConfigExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuItemAutoConfigExportPerformed();
			}
		});
		menuItemConfigureProxy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuItemConfigureProxyPerformed();
			}
		});
		menuItemuUpdates.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				menuItemuUpdatesPerformed();
			}
		});
		menuDonate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuDonatePerformed();
			}
		});
		menuItemAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				menuItemAboutPerformed();
			}
		});
		if (trayIcon != null) {
			trayIcon.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					trayIconPerformed();
				}
			});
		}
		if (launchItem != null) {
			launchItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					launchTrayItemPerformed();
				}
			});
		}
		if (exitItem != null) {
			exitItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					exitTrayItemPerformed();
				}
			});
		}
		menuItemAddGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuItemAddGroupPerformed();
			}
		});
		menuItemDuplicateGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuItemDuplicateGroupPerformed();
			}
		});
		menuItemRenameGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuItemRenameGroupPerformed();
			}
		});
		menuItemRemoveGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuItemRemoveGroupPerformed();
			}
		});

		// Add Windows Listeners
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowDeiconified(WindowEvent arg0) {
				trayIconPerformed();
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowActivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				menuExitPerformed();
			}

			@Override
			public void windowIconified(WindowEvent e) {
				menuIconifiedPerformed();
			}

			@Override
			public void windowOpened(WindowEvent e) {
			}
		});
	}

	public void init() {

		/* Load data */
		try {
			configurationService.read();
		} catch (LoadingException e1) {
			System.out.println(e1.getMessage());
		}

		try {
			profileService.readAll();
		} catch (LoadingException e2) {
			System.out.println(e2.getMessage());
		}

		try {
			repositoryService.readAll();
		} catch (LoadingException e3) {
			System.out.println(e3.getMessage());
		}

		/* Set proxy configuration */
		configurationService.loadProxy();

		/* Ensure profile with name profileName really exists */
		String profileName = configurationService.getProfileName();
		if (profileName == null) {
			configurationService.setProfileName(DefaultProfileName.DEFAULT.getDescription());
		} else {
			List<String> profileNames = profileService.getProfileNames();
			if (!profileNames.contains(profileName)) {
				configurationService.setProfileName(DefaultProfileName.DEFAULT.getDescription());
			}
		}

		/* Sets app Dimensions */
		this.pack();
		int appWidth = this.getBounds().width;
		int appHeight = this.getBounds().height;
		if (infoPanel.getBounds().height < 30) {
			infoPanel.setPreferredSize(new Dimension(appWidth, 30));
		}
		if (launchPanel.getBounds().height < 45) {
			launchPanel.setPreferredSize(new Dimension(appWidth, 45));
		}

		this.pack();

		appWidth = this.getBounds().width;

		int minHeight = this.facade.getInfoPanel().getPreferredSize().height
				+ this.facade.getLaunchOptionsPanel().getPreferredSize().height
				+ this.facade.getLaunchPanel().getPreferredSize().height + 80;

		this.setMinimumSize(new Dimension(appWidth, minHeight));
		this.setPreferredSize(new Dimension(appWidth, minHeight));

		int height = configurationService.getHeight();
		int width = configurationService.getWidth();
		if (height != 0 && width != 0) {
			this.setPreferredSize(new Dimension(width, height));
		}

		this.pack();

		/* Center app into the middle of the screen */
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((screenDimension.getWidth() - this.getPreferredSize().getWidth()) / 2);
		int y = (int) ((screenDimension.getHeight() - this.getPreferredSize().getHeight()) / 2);
		this.setLocation(x, y);

		/* Copy old Addons search directories to profile */
		Set<String> set = configurationService.getAddonSearchDirectoryPaths();
		if (set != null) {
			Iterator iter = set.iterator();
			while (iter.hasNext()) {
				profileService.addAddonSearchDirectoryPath((String) iter.next());
			}
			configurationService.resetAddonSearchDirectoryPaths();
		}

		/* Copy old launcher options to profile */
		LauncherOptions oldLps = configurationService.getLauncherOptions();
		if (oldLps != null) {
			profileService.setArmA3ExePath(oldLps.getArma3ExePath());
			profileService.setCheckBoxAutoRestart(oldLps.isAutoRestart());
			profileService.setCheckBoxCheckSignatures(oldLps.isCheckSignatures());
			profileService.setCheckBoxFilePatching(oldLps.isFilePatching());
			profileService.setCheckBoxNoPause(oldLps.isNoPause());
			profileService.setCheckBoxShowScriptErrors(oldLps.isShowScriptErrors());
			profileService.setCheckBoxWindowMode(oldLps.isWindowMode());
			profileService.setCpuCount(Integer.toString(oldLps.getCpuCountSelection()));
			profileService.setDefaultWorld(oldLps.isDefaultWorld());
			profileService.setEnableHT(oldLps.isEnableHT());
			profileService.setHugePages(oldLps.isHugePages());
			profileService.setExThreads(oldLps.getExThreadsSelection());
			profileService.setGameProfile(oldLps.getGameProfile());
			profileService.setMalloc(oldLps.getMallocSelection());
			profileService.setMaxMemory(oldLps.getMaxMemorySelection());
			profileService.setNoLogs(oldLps.isNologs());
			profileService.setNoSplashScreen(oldLps.isNoSplashScreen());
			configurationService.resetLauncherOptions();
		}

		/* Show GUI */
		if (facade.isRunMode()) {
			setToTray();
		} else {
			setVisible(true);
			/* Check ArmA3 Executable location */
			checkArmA3ExeLocation();
		}

		/* Init active views */
		updateTabs(OP_PROFILE_CHANGED);
	}

	public void initBackGround() {

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				/* Check for updates */
				checkForUpdates(false);
				/* Check repositories for updates */
				checkRepositories();
			}
		});
		t.start();
	}

	/*  */

	public void updateTabs(int flag) {

		if (flag == OP_PROFILE_CHANGED) {
			updateProfilesMenu();
		}

		this.facade.getInfoPanel().update(flag);
		this.facade.getAddonsPanel().update(flag);
		this.facade.getAddonOptionsPanel().update(flag);
		this.facade.getLaunchOptionsPanel().update(flag);
		this.facade.getExternalApplicationsPanel().update(flag);
		this.facade.getSyncPanel().update(flag);
		this.facade.getOnlinePanel().update(flag);
		this.facade.getLaunchPanel().update(flag);
	}

	private void updateProfilesMenu() {

		int numberMenuItems = menuProfiles.getItemCount();

		for (int i = numberMenuItems - 1; i > 2; i--) {
			JMenuItem menuItem = menuProfiles.getItem(i);
			menuProfiles.remove(menuItem);
		}

		List<String> profileNames = profileService.getProfileNames();
		String initProfileName = configurationService.getProfileName();
		assert (initProfileName != null);
		for (int i = 0; i < profileNames.size(); i++) {
			final String profileName = profileNames.get(i);
			JCheckBoxMenuItem menuItemProfile = new JCheckBoxMenuItem(profileName);
			menuProfiles.add(menuItemProfile);
			if (profileName.equals(initProfileName)) {
				menuItemProfile.setSelected(true);
			}
			menuItemProfile.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent evt) {
					menuItemProfilePerformed(evt);
				}
			});
		}
	}

	private void menuItemProfilePerformed(ActionEvent e) {

		int numberMenuItems = menuProfiles.getItemCount();

		for (int i = numberMenuItems - 1; i > 2; i--) {
			JCheckBoxMenuItem checkBoxItem = (JCheckBoxMenuItem) menuProfiles.getItem(i);
			checkBoxItem.setSelected(false);
		}

		JCheckBoxMenuItem menuItemProfile = (JCheckBoxMenuItem) e.getSource();
		menuItemProfile.setSelected(true);
		String profileName = menuItemProfile.getText();
		configurationService.setProfileName(profileName);
		updateTabs(OP_PROFILE_CHANGED);
	}

	/* Menu Actions */

	private void menuItemAddGroupPerformed() {
		tabbedPane.setSelectedIndex(0);
		facade.getAddonsPanel().getGroupManager().addGroup();
	}

	private void menuItemDuplicateGroupPerformed() {
		tabbedPane.setSelectedIndex(0);
		facade.getAddonsPanel().getGroupManager().duplicateGroup();
	}

	private void menuItemRenameGroupPerformed() {
		tabbedPane.setSelectedIndex(0);
		facade.getAddonsPanel().getGroupManager().renameGroup();
	}

	private void menuItemRemoveGroupPerformed() {
		tabbedPane.setSelectedIndex(0);
		facade.getAddonsPanel().getGroupManager().removeGroup();
	}

	private void menuItemEditPerformed() {

		ProfileSelectionDialog profilePanel = new ProfileSelectionDialog(facade);
		profilePanel.init();
		profilePanel.toFront();
		profilePanel.setVisible(true);
	}

	private void menuItemExportAsShortcutPerformed() {

		/* Windows only */
		String osName = System.getProperty("os.name");
		if (!osName.toLowerCase().contains("windows")) {
			JOptionPane.showMessageDialog(this, "This feature is not available for your system.",
					"Export profile as shortcut", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		String profileName = configurationService.getProfileName();
		assert (profileName != null);
		if (profileName == null) {
			return;// unexpected
		}

		String exePath = profileService.getArma3ExePath();

		if (exePath == null || "".equals(exePath)) {
			String message = "ArmA 3 Executable location is missing for profile name " + profileName + "." + "\n"
					+ "Please checkout Launcher Options panel.";
			JOptionPane.showMessageDialog(this, message, "Export profile as shortcut", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		try {
			List<String> list = launchService.determineRunParameters();
			String arguments = "";
			for (String stg : list) {
				arguments = arguments + " " + stg;
			}
			JShellLink link = new JShellLink();
			String path = JShellLink.getDirectory("desktop");
			link.setFolder(path);
			link.setName(profileName);
			link.setPath(exePath);
			link.setArguments(arguments);
			link.save();
			String message = "Shortcut has been created on desktop for profile " + profileName + ".";
			JOptionPane.showMessageDialog(this, message, "Export profile as shortcut", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to create shortcut" + "\n" + e.getMessage(),
					"Export profile as shortcut", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void menuItemACRE2wizardPerformed() {

		FirstPageACRE2InstallerDialog firstPage = new FirstPageACRE2InstallerDialog(facade);
		firstPage.init();
		firstPage.setVisible(true);
	}

	private void menuItemTFARwizardPerformed() {

		FirstPageTFARInstallerPanel firstPage = new FirstPageTFARInstallerPanel(facade);
		firstPage.init();
		firstPage.setVisible(true);
	}

	private void menuItemAiAwizardPerformed() {

		AiaInstallerDialog aiaInstallerPanel = new AiaInstallerDialog(facade);
		aiaInstallerPanel.setVisible(true);
		aiaInstallerPanel.init();
	}

	private void menuItemRPTviewerPerformed() {

		RptViewerPanel rptViewerPanel = new RptViewerPanel(facade);
		rptViewerPanel.setVisible(true);
	}

	private void menuItemBikeyExtractorPerformed() {

		BiKeyExtractorDialog biKeyExtactorPanel = new BiKeyExtractorDialog(facade);
		biKeyExtactorPanel.init();
		biKeyExtactorPanel.setVisible(true);
	}

	private void menuItemHelpPerformed() {

		CommonService commonService = new CommonService();
		String urlValue = commonService.getWiki();
		try {
			URI url = new java.net.URI(urlValue);
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(url);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Can't open system web browser.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void menuItemBISforumPerformed() {

		CommonService commonService = new CommonService();
		String urlValue = commonService.getBIS();
		try {
			URI url = new java.net.URI(urlValue);
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(url);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Can't open system web browser.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void menuItemPreferencesPerformed() {
		PreferencesDialog preferencesPanel = new PreferencesDialog(facade);
		preferencesPanel.init();
		preferencesPanel.setVisible(true);
	}

	private void menuItemAutoConfigImportPerformed() {
		AutoConfigImportDialog autoConfigImportPanel = new AutoConfigImportDialog(facade);
		autoConfigImportPanel.setVisible(true);
	}

	private void menuItemAutoConfigExportPerformed() {

		AutoConfigExportDialog autoConfigExportPanel = new AutoConfigExportDialog(facade);
		autoConfigExportPanel.init();
		autoConfigExportPanel.setVisible(true);
	}

	private void menuItemConfigureProxyPerformed() {

		ProxyConfigurationDialog proxyConfigurationDialog = new ProxyConfigurationDialog(facade);
		proxyConfigurationDialog.init();
		proxyConfigurationDialog.setVisible(true);
	}

	private void menuItemuUpdatesPerformed() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				checkForUpdates(true);
			}
		});
	}

	private void menuItemAboutPerformed() {
		AboutDialog about = new AboutDialog(facade);
		about.setVisible(true);
	}

	private void menuDonatePerformed() {

		String urlValue = commonService.getPayPal();
		try {
			URI url = new java.net.URI(urlValue);
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(url);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Can't open system web browser.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void menuIconifiedPerformed() {

		PreferencesDTO preferencesDTO = preferencesService.getPreferences();
		MinimizationType type = preferencesDTO.getLaunchPanelMinimized();
		if (type.equals(MinimizationType.TASK_BAR)) {
			setToTaskBar();
		} else if (type.equals(MinimizationType.TRAY)) {
			setToTray();
		}
	}

	public void menuExitPerformed() {

		int close = 0;
		try {
			commonService.save(getHeight(), getWidth());
		} catch (WritingException e) {
			close = JOptionPane.showConfirmDialog(facade.getMainPanel(),
					e.getMessage() + "\n" + "Exit ArmA3Sync anyway?", "Error", JOptionPane.YES_NO_OPTION,
					JOptionPane.ERROR_MESSAGE);
		} finally {
			if (close == 0) {
				dispose();
				System.exit(0);
			}
		}
	}

	private void exitTrayItemPerformed() {
		if (SystemTray.isSupported()) {
			tray.remove(trayIcon);
		}
		menuExitPerformed();
	}

	private void trayIconPerformed() {
		if (SystemTray.isSupported()) {
			tray.remove(trayIcon);
		}
		this.setState(JFrame.NORMAL);
		this.setVisible(true);
		if (facade.getInfoUpdatedRepositoryPanel() != null) {
			facade.getMainPanel().showSyncPanel();
			facade.getInfoUpdatedRepositoryPanel().setVisible(true);
		} else {
			this.toFront();
		}
	}

	private void launchTrayItemPerformed() {
		if (SystemTray.isSupported()) {
			tray.remove(trayIcon);
		}
		this.setState(JFrame.NORMAL);
		this.setVisible(true);
		if (facade.getInfoUpdatedRepositoryPanel() != null) {
			facade.getMainPanel().showSyncPanel();
			facade.getInfoUpdatedRepositoryPanel().setVisible(true);
		} else {
			this.toFront();
		}
	}

	// Main Panel front
	public void setToFront() {
		if (SystemTray.isSupported()) {
			tray.remove(trayIcon);
		}
		this.setState(JFrame.NORMAL);
		this.setVisible(true);
		this.toFront();
	}

	// Current panel to front
	public void recoverFromTray() {
		if (SystemTray.isSupported()) {
			tray.remove(trayIcon);
		}
		this.setState(JFrame.NORMAL);
		this.setVisible(true);
	}

	public void setToTaskBar() {
		this.setState(JFrame.ICONIFIED);
	}

	public void setToTray() {
		if (SystemTray.isSupported()) {
			try {
				tray.add(trayIcon);
				this.setVisible(false);
			} catch (AWTException e) {
				System.out.println("TrayIcon could not be added.");
				this.setState(JFrame.ICONIFIED);
				return;
			}
		}
	}

	public boolean isToTray() {
		if (SystemTray.isSupported()) {
			if (tray.getTrayIcons().length != 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void displayMessageToSystemTray(String message) {
		if (SystemTray.isSupported()) {
			if (tray.getTrayIcons().length != 0) {
				trayIcon.displayMessage("ArmA3Sync", message, TrayIcon.MessageType.INFO);
			}
		}
	}

	/*
	 * class ProfilesMenuListener implements MenuListener {
	 * 
	 * @Override public void menuSelected(MenuEvent evt) {
	 * facade.getAddonsPanel().saveAddonGroups();
	 * facade.getLaunchOptionsPanel().setAdditionalParameters(); ProfilePanel
	 * profilePanel = new ProfilePanel(facade); profilePanel.toFront();
	 * profilePanel.setVisible(true); }
	 * 
	 * @Override public void menuDeselected(MenuEvent e) { //
	 * System.out.println("menuDeselected");
	 * 
	 * }
	 * 
	 * @Override public void menuCanceled(MenuEvent e) { //
	 * System.out.println("menuCanceled"); } }
	 */
	/**/

	private void checkForUpdates(final boolean withInfoMessage) {

		System.out.println("Checking for updates...");

		String url = DataAccessConstants.UPDTATE_REPOSITORY_ADRESS;
		String port = Integer.toString(DataAccessConstants.UPDTATE_REPOSITORY_PORT);
		String login = DataAccessConstants.UPDTATE_REPOSITORY_LOGIN;
		String password = DataAccessConstants.UPDTATE_REPOSITORY_PASS;
		ProtocolType protocolType = ProtocolType.FTP;

		AbstractProtocole protocol = new Ftp(url, port, login, password, protocolType);

		String availableVersion = null;
		try {
			ConnectionService connectionService = new ConnectionService(protocol);
			availableVersion = connectionService.checkForUpdates(facade.isDevMode(), protocol);
		} catch (CheckException | IOException | ParserConfigurationException | SAXException e) {
			System.out.println(e.getMessage());
			if (withInfoMessage) {
				JOptionPane.showMessageDialog(facade.getMainPanel(), e.getMessage(), "Update",
						JOptionPane.ERROR_MESSAGE);
			}
			return;
		}

		if (availableVersion != null) {
			displayMessageToSystemTray("A new update is available");
			int response = JOptionPane.showConfirmDialog(facade.getMainPanel(),
					"A new update is available. Proceed update?", "Update", JOptionPane.OK_CANCEL_OPTION);

			if (response == 0) {
				// Proceed with update
				String command = "java -jar -Djava.net.preferIPv4Stack=true ArmA3Sync-Updater.jar";
				if (facade.isDevMode()) {
					command = command + " -dev";
				}
				try {
					Runtime.getRuntime().exec(command);
					System.exit(0);
				} catch (IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(facade.getMainPanel(), ex.getMessage(), "Update",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (withInfoMessage) {
			JOptionPane.showMessageDialog(facade.getMainPanel(), "No new update available.", "Update",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void checkArmA3ExeLocation() {

		boolean invalid = false;
		String path = profileService.getArma3ExePath();
		if (path == null) {
			invalid = true;
		} else if ("".equals(path)) {
			invalid = true;
		} else if (!(new File(path)).exists()) {
			invalid = true;
		}

		if (invalid) {
			String arma3FolderPath = configurationService.determineArmA3Path();
			if (arma3FolderPath != null) {
				File arma3ExeFile = new File(arma3FolderPath, GameExecutables.GAME_x64.getDescription());
				if (arma3ExeFile.exists()) {
					profileService.setArmA3ExePath(arma3ExeFile.getAbsolutePath());
					invalid = false;
				}
			}
		}

		if (invalid) {
			WelcomeDialog wellcomePanel = new WelcomeDialog(facade);
			wellcomePanel.toFront();
			wellcomePanel.setVisible(true);
		}
	}

	private void checkRepositories() {

		CheckRepositoriesFrequency checkRepositoriesFrequency = preferencesService.getPreferences()
				.getCheckRepositoriesFrequency();
		TaskCheckRepositories task = new TaskCheckRepositories(facade);
		TasksManager tasksManager = TasksManager.getInstance();
		if (checkRepositoriesFrequency.equals(CheckRepositoriesFrequency.DISABLED)) {
			tasksManager.addTask(task, 0);// execute now, no repetition
		} else {
			int frequency = checkRepositoriesFrequency.getFrequency();// min
			long period = (long) (frequency * 60 * Math.pow(10, 3));// ms
			// long period = (long) (10* Math.pow(10, 3));
			tasksManager.addTask(task, 0, period);// execute now + repetition
		}
	}

	public void showSyncPanel() {
		tabbedPane.setSelectedIndex(5);
	}

	public RepositoryPanel openRepository(final String repositoryName, final String eventName, final boolean select,
			final boolean asAdmin) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, eventName);

		String title = null;
		if (eventName != null) {
			title = eventName;
		} else {
			title = repositoryName;
		}

		if (dynamicTabFound == null) {

			RepositoryPanel newRepositoryPanel = new RepositoryPanel(facade);

			DynamicTab newDynamicTab = new DynamicTab();
			newDynamicTab.setEventName(eventName);
			newDynamicTab.setRepositoryName(repositoryName);
			newDynamicTab.setTitle(title);
			newDynamicTab.setAsAdmin(asAdmin);

			addClosableTab(newRepositoryPanel, newDynamicTab);
			final int index = tabbedPane.getTabCount() - 1;
			newDynamicTab.setTabIndex(index);
			dynamicTabManager.add(newDynamicTab);

			if (select) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						tabbedPane.setSelectedIndex(index);
					}
				});
			}
			return newRepositoryPanel;

		} else {
			if (dynamicTabFound.isAsAdmin() == asAdmin) {
				final int index = dynamicTabFound.getTabIndex();
				if (select) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							tabbedPane.setSelectedIndex(index);
						}
					});
				}
				return null;
			} else {
				boolean closed = closeRepository(repositoryName, eventName, false);
				if (closed) {
					RepositoryPanel newRepositoryPanel = new RepositoryPanel(facade);

					DynamicTab newDynamicTab = new DynamicTab();
					newDynamicTab.setEventName(eventName);
					newDynamicTab.setRepositoryName(repositoryName);
					newDynamicTab.setTitle(title);
					newDynamicTab.setAsAdmin(asAdmin);

					addClosableTab(newRepositoryPanel, newDynamicTab);
					final int index2 = tabbedPane.getTabCount() - 1;
					newDynamicTab.setTabIndex(index2);
					dynamicTabManager.add(newDynamicTab);

					if (select) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								tabbedPane.setSelectedIndex(index2);
							}
						});
					}
					return newRepositoryPanel;
				} else {
					final int index2 = dynamicTabFound.getTabIndex();
					if (select) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								tabbedPane.setSelectedIndex(index2);
							}
						});
					}
					return null;
				}
			}
		}

		/*
		 * if (mapTabIndexes.containsKey(repositoryName)) { final int index =
		 * mapTabIndexes.get(repositoryName); RepositoryPanel repositoryPanel =
		 * (RepositoryPanel) tabbedPane.getComponentAt(index); if
		 * (!repositoryPanel.getName().equals(title)) { boolean isClose =
		 * closeRepository(repositoryName, false); if (isClose) { RepositoryPanel
		 * newRepositoryPanel = new RepositoryPanel(facade);
		 * addClosableTab(newRepositoryPanel, title, repositoryName); final int index2 =
		 * tabbedPane.getTabCount() - 1; mapTabIndexes.put(repositoryName, index2); if
		 * (select) { SwingUtilities.invokeLater(new Runnable() {
		 * 
		 * @Override public void run() { tabbedPane.setSelectedIndex(index2); } }); }
		 * return newRepositoryPanel; } else { final int index2 =
		 * mapTabIndexes.get(repositoryName); if (select) {
		 * SwingUtilities.invokeLater(new Runnable() {
		 * 
		 * @Override public void run() { tabbedPane.setSelectedIndex(index2); } }); }
		 * return null; } } else { final int index2 = mapTabIndexes.get(repositoryName);
		 * if (select) { SwingUtilities.invokeLater(new Runnable() {
		 * 
		 * @Override public void run() { tabbedPane.setSelectedIndex(index2); } }); }
		 * return null; } } else { RepositoryPanel newRepositoryPanel = new
		 * RepositoryPanel(facade); addClosableTab(newRepositoryPanel, title,
		 * repositoryName); final int index = tabbedPane.getTabCount() - 1;
		 * mapTabIndexes.put(repositoryName, index); if (select) {
		 * SwingUtilities.invokeLater(new Runnable() {
		 * 
		 * @Override public void run() { tabbedPane.setSelectedIndex(index); } }); }
		 * return newRepositoryPanel; }
		 */
	}

	public void addClosableTab(final JComponent c, final DynamicTab dynamicTab) {

		// Add the tab to the pane without any label
		tabbedPane.addTab(null, c);
		// c.setName(dynamicTab.getTitle());
		int pos = tabbedPane.indexOfComponent(c);

		// Now assign the component for the tab
		tabbedPane.setTabComponentAt(pos, new CloseableTabComponent(tabbedPane, dynamicTab));
	}

	// A component for the custom tabs with a closer button
	private class CloseableTabComponent extends JPanel {

		private JTabbedPane tabbedPane = null; // the tabbed pane this component
												// belongs to
		private JLabel titleLabel = null; // the title of the tab
		private JButton closeButton = null; // the closer button on the right
											// side of the tab
		private Font defaultFont = null; // the default font of the title label
		private Font selectedFont = null; // the font of the title label if tab
											// is selected
		private Color selectedColor = null; // the foreground color of the title
											// lable if tab is selected

		public CloseableTabComponent(JTabbedPane aTabbedPane, final DynamicTab dynamicTab) {

			FlowLayout f = new FlowLayout(FlowLayout.CENTER, 5, 0);
			this.setLayout(f);

			tabbedPane = aTabbedPane;
			setOpaque(false);

			// setup the controls of this tab component
			titleLabel = new JLabel(dynamicTab.getTitle());
			titleLabel.setOpaque(false);
			// get the defaults for rendering the title label
			defaultFont = titleLabel.getFont();
			selectedFont = titleLabel.getFont();
			selectedColor = UIManager.getColor("TabbedPane.selectedForeground");
			if (selectedColor == null) {
				selectedColor = tabbedPane.getForeground();
			}
			closeButton = new CloseButton();
			closeButton.setBorder(null);
			closeButton.setFocusable(false);
			closeButton.setOpaque(false);
			add(titleLabel);
			add(closeButton);
			/*
			 * Add a thin border to keep the image below the top edge of the tab when the
			 * tab is selected
			 */
			setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

			// Add the listener that removes the tab
			ActionListener listener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					/*
					 * The component parameter must be declared "final" so that it can be referenced
					 * in the anonymous listener class like this.
					 */
					closeRepository(dynamicTab.getRepositoryName(), dynamicTab.getEventName(), false);
				}
			};
			closeButton.addActionListener(listener);
		}

		// calculate the tab index of this tab component
		private int getTabIndex() {
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				if (this.equals(tabbedPane.getTabComponentAt(i))) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public void updateUI() {
			super.updateUI();
			// if look and feel changes we have to set the new defaults for
			// rendering the title label
			if (titleLabel != null) {
				defaultFont = titleLabel.getFont().deriveFont(~Font.BOLD);
				selectedFont = titleLabel.getFont().deriveFont(Font.BOLD);
				selectedColor = UIManager.getColor("TabbedPane.selectedForeground");
				if (selectedColor == null) {
					selectedColor = tabbedPane.getForeground();
				}
			}
		}

		// We have to override paint to handle the rendering of the title label,
		// because we want
		// the title to be painted different when tab is selected.
		@Override
		public void paint(Graphics g) {
			int tabIndex = getTabIndex();
			if (tabIndex >= 0) {
				if (tabIndex == tabbedPane.getSelectedIndex()) {
					titleLabel.setFont(selectedFont);
					if (tabbedPane.getForegroundAt(tabIndex) instanceof ColorUIResource) {
						titleLabel.setForeground(selectedColor);
					} else {
						titleLabel.setForeground(tabbedPane.getForegroundAt(tabIndex));
					}
				} else {
					titleLabel.setFont(defaultFont);
					titleLabel.setForeground(tabbedPane.getForegroundAt(tabIndex));
				}
			}
			super.paint(g);
		}
	}

	// A closer button for the custom tab components
	private class CloseButton extends JButton {

		private final ImageIcon CLOSER_ICON = new ImageIcon(CLOSE_GRAY);
		private final ImageIcon CLOSER_ROLLOVER_ICON = new ImageIcon(CLOSE_RED);
		private final ImageIcon CLOSER_PRESSED_ICON = new ImageIcon(CLOSE_RED);

		private Dimension prefSize = new Dimension(10, 10);

		public CloseButton() {
			super("");
			// setup the button
			setIcon(CLOSER_ICON);
			setRolloverIcon(CLOSER_ROLLOVER_ICON);
			setPressedIcon(CLOSER_PRESSED_ICON);
			setContentAreaFilled(false);
			setBorder(BorderFactory.createEmptyBorder());
			setFocusable(false);
			// the preferrd size of this button is the size of the closer image
			prefSize = new Dimension(CLOSER_ICON.getIconWidth(), CLOSER_ICON.getIconHeight());
		}

		@Override
		public Dimension getPreferredSize() {
			return prefSize;
		}
	}

	public boolean closeRepository(final String repositoryName, final String eventName, final boolean silent) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, eventName);
		if (dynamicTabFound != null) {
			String message = dynamicTabManager.canClose(dynamicTabFound);
			if (message == null) {
				tabbedPane.remove(dynamicTabFound.getTabIndex());
				dynamicTabManager.remove(dynamicTabFound);
				return true;
			} else {
				if (!silent) {
					JOptionPane.showMessageDialog(facade.getMainPanel(), message, dynamicTabFound.getTitle(),
							JOptionPane.INFORMATION_MESSAGE);
				}
				return false;
			}
		} else {
			return true;
		}
	}

	public void selectRepository(final String repositoryName, final String eventName) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, eventName);
		if (dynamicTabFound != null) {
			final int index = dynamicTabFound.getTabIndex();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					tabbedPane.setSelectedIndex(index);
				}
			});
		}
	}

	public String getRepositoryTitle(String repositoryName, String eventName) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, eventName);
		if (dynamicTabFound != null) {
			return dynamicTabFound.getTitle();
		}
		return "";
	}

	public boolean isCheckingForAddons(final String repositoryName, final String eventName) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, eventName);
		if (dynamicTabFound != null) {
			return dynamicTabFound.isCheckingForAddons();
		} else {
			return false;
		}
	}

	public void setCheckingForAddons(final String repositoryName, final String eventName, final boolean value) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, eventName);
		if (dynamicTabFound != null) {
			dynamicTabFound.setCheckingForAddons(value);
		}
	}

	public boolean isDownloading(final String repositoryName, final String eventName) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, eventName);
		if (dynamicTabFound != null) {
			return dynamicTabFound.isDownloading();
		} else {
			return false;
		}
	}

	public void setDownloading(final String repositoryName, final String eventName, final boolean value) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, eventName);
		if (dynamicTabFound != null) {
			dynamicTabFound.setDownloading(value);
		}
	}

	public boolean isDownloading() {
		return dynamicTabManager.isDownloading();
	}

	public boolean isBuilding(final String repositoryName) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, null);
		if (dynamicTabFound != null) {
			return dynamicTabFound.isBuilding();
		} else {
			return false;
		}
	}

	public void setBuilding(final String repositoryName, final boolean value) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, null);
		if (dynamicTabFound != null) {
			dynamicTabFound.setBuilding(value);
		}
	}

	public boolean isUploading(final String repositoryName) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, null);
		if (dynamicTabFound != null) {
			return dynamicTabFound.isUploading();
		} else {
			return false;
		}
	}

	public void setUploading(final String repositoryName, final boolean value) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, null);
		if (dynamicTabFound != null) {
			dynamicTabFound.setUploading(value);
		}
	}

	public boolean isChecking(final String repositoryName) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, null);
		if (dynamicTabFound != null) {
			return dynamicTabFound.isChecking();
		} else {
			return false;
		}
	}

	public void setChecking(final String repositoryName, final boolean value) {

		DynamicTab dynamicTabFound = dynamicTabManager.find(repositoryName, null);
		if (dynamicTabFound != null) {
			dynamicTabFound.setChecking(value);
		}
	}

	private class DynamicTabManager {

		private final List<DynamicTab> dynamicTabs = new LinkedList<DynamicTab>();

		public DynamicTab find(final String repositoryName, String eventName) {

			DynamicTab dynamicTabFound = null;
			for (int i = 0; i < dynamicTabs.size(); i++) {
				DynamicTab dynamicTab = dynamicTabs.get(i);
				if (dynamicTab.getRepositoryName().equals(repositoryName)) {
					if (dynamicTab.getEventName() == null && eventName == null) {
						dynamicTabFound = dynamicTab;
					} else if (dynamicTab.getEventName() != null && eventName != null) {
						if (dynamicTab.getEventName().equals(eventName)) {
							dynamicTabFound = dynamicTab;
						}
					}
				}
				if (dynamicTabFound != null) {
					break;
				}
			}
			return dynamicTabFound;
		}

		public void add(DynamicTab newDynamicTab) {
			dynamicTabs.add(newDynamicTab);
		}

		public String canClose(DynamicTab dynamicTabFound) {

			if (dynamicTabFound.isCheckingForAddons()) {
				return "Tab can't be closed.\nFiles are being checked.";
			} else if (dynamicTabFound.isDownloading()) {
				return "Tab can't be closed.\nFiles are being downloaded.";
			} else if (dynamicTabFound.isBuilding()) {
				return "Tab can't be closed.\nFiles are being proceeded.";
			} else if (dynamicTabFound.isUploading()) {
				return "Tab can't be closed.\nFiles are being uploaded.";
			} else if (dynamicTabFound.isChecking()) {
				return "Tab can't be closed.\nFiles are being checked.";
			} else {
				return null;
			}
		}

		public void remove(DynamicTab dynamicTabFound) {

			dynamicTabs.remove(dynamicTabFound);
			int index = 6;// First Index
			for (int i = 0; i < dynamicTabs.size(); i++) {
				DynamicTab dynamicTab = dynamicTabs.get(i);
				dynamicTab.setTabIndex(index);
				index++;
			}
		}

		public boolean isDownloading() {

			for (int i = 0; i < dynamicTabs.size(); i++) {
				DynamicTab dynamicTab = dynamicTabs.get(i);
				if (dynamicTab.isDownloading()) {
					return true;
				}
			}
			return false;
		}
	}

	private class DynamicTab {

		private int tabIndex = 0;
		private String title = "";
		private String repositoryName = null;
		private String eventName = null;
		private boolean asAdmin = false;
		private boolean isCheckingForAddons = false;
		private boolean isDownloading = false;
		private boolean isBuilding = false;
		private boolean isUploading = false;
		private boolean isChecking = false;

		public int getTabIndex() {
			return tabIndex;
		}

		public void setTabIndex(int tabIndex) {
			this.tabIndex = tabIndex;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getRepositoryName() {
			return repositoryName;
		}

		public void setRepositoryName(String repositoryName) {
			this.repositoryName = repositoryName;
		}

		public String getEventName() {
			return eventName;
		}

		public void setEventName(String eventName) {
			this.eventName = eventName;
		}

		public boolean isAsAdmin() {
			return asAdmin;
		}

		public void setAsAdmin(boolean asAdmin) {
			this.asAdmin = asAdmin;
		}

		public boolean isCheckingForAddons() {
			return isCheckingForAddons;
		}

		public void setCheckingForAddons(boolean value) {
			this.isCheckingForAddons = value;
		}

		public boolean isDownloading() {
			return isDownloading;
		}

		public void setDownloading(boolean value) {
			this.isDownloading = value;
		}

		public boolean isBuilding() {
			return isBuilding;
		}

		public void setBuilding(boolean isBuilding) {
			this.isBuilding = isBuilding;
		}

		public boolean isUploading() {
			return isUploading;
		}

		public void setUploading(boolean isUploading) {
			this.isUploading = isUploading;
		}

		public boolean isChecking() {
			return this.isChecking;
		}

		public void setChecking(boolean value) {
			this.isChecking = value;
		}
	}
}
