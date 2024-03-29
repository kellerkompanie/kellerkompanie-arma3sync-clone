package fr.soe.a3s.ui.repository.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import fr.soe.a3s.dao.DataAccessConstants;
import fr.soe.a3s.service.CommonService;
import fr.soe.a3s.ui.AbstractDialog;
import fr.soe.a3s.ui.Facade;
import fr.soe.a3s.ui.repository.DownloadPanel;
import net.jimmc.jshortcut.JShellLink;

public class ReportDialog extends AbstractDialog implements DataAccessConstants {

	private final DownloadPanel downloadPanel;
	private JTextArea textArea;
	// Data
	private String downloadReport;

	public ReportDialog(Facade facade, DownloadPanel downloadPanel) {
		super(facade, "Download", true);
		this.downloadPanel = downloadPanel;

		{
			buttonOK.setText("Export");
			buttonCancel.setText("Close");
			getRootPane().setDefaultButton(buttonCancel);
			buttonCancel.setPreferredSize(buttonOK.getPreferredSize());
		}
		{
			textArea = new JTextArea();
			textArea.setLineWrap(true);
			textArea.setEditable(false);
			JScrollPane scrollpane = new JScrollPane();
			scrollpane.setViewportView(textArea);
			scrollpane.setBorder(BorderFactory
					.createEtchedBorder(BevelBorder.LOWERED));
			this.add(scrollpane, BorderLayout.CENTER);
		}

		this.pack();
	}

	public void init(String downloadReport) {

		this.downloadReport = downloadReport;
		this.downloadPanel.getButtonDownloadReport().setEnabled(false);
		if (downloadReport == null) {
			this.setMinimumSize(new Dimension(300, 300));
			String message = "Download report is not available.";
			textArea.setText(message);
			Font fontTextField = UIManager.getFont("Label.font");
			textArea.setFont(fontTextField.deriveFont(Font.ITALIC));
			buttonOK.setEnabled(false);
		} else {
			this.setMinimumSize(new Dimension(500, 550));
			String message = downloadReport;
			textArea.setText(message);
			Font fontTextField = UIManager.getFont("TextField.font");
			textArea.setFont(fontTextField);
			if (textArea.getFont().getSize() < 12) {
				textArea.setFont(textArea.getFont().deriveFont(new Float(12)));
			}
			textArea.setCaretPosition(0);
			buttonOK.setEnabled(true);
		}
		this.setLocationRelativeTo(facade.getMainPanel());
	}

	@Override
	protected void buttonOKPerformed() {
		
		try {
			String osName = System.getProperty("os.name");
			CommonService commonService = new CommonService();
			if (osName.toLowerCase().contains("windows")) {
				String path = JShellLink.getDirectory("desktop") + File.separator + LOG_FILE_NAME;
				commonService.exportLogFile(downloadReport, path);
				JOptionPane.showMessageDialog(facade.getMainPanel(), "Log file has been exported to desktop",
						"ArmA3Sync", JOptionPane.INFORMATION_MESSAGE);
			} else {
				String path = System.getProperty("user.home") + File.separator + LOG_FILE_NAME;
				commonService.exportLogFile(downloadReport, path);
				JOptionPane.showMessageDialog(facade.getMainPanel(), "Log file has been exported to home directory",
						"ArmA3Sync", JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(
					facade.getMainPanel(),
					"Failed to export log file" + "\n"
							+ e1.getMessage(), "Report",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	protected void buttonCancelPerformed() {
		this.dispose();
		this.downloadPanel.getButtonDownloadReport().setEnabled(true);
	}

	@Override
	protected void menuExitPerformed() {
		this.dispose();
		this.downloadPanel.getButtonDownloadReport().setEnabled(true);
	}
}
