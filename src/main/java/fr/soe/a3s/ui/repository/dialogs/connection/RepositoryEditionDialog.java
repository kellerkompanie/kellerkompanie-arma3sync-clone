package fr.soe.a3s.ui.repository.dialogs.connection;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import fr.soe.a3s.constant.ProtocolType;
import fr.soe.a3s.dao.DataAccessConstants;
import fr.soe.a3s.dto.ProtocolDTO;
import fr.soe.a3s.dto.RepositoryDTO;
import fr.soe.a3s.exception.CheckException;
import fr.soe.a3s.exception.WritingException;
import fr.soe.a3s.exception.repository.RepositoryException;
import fr.soe.a3s.exception.repository.RepositoryNotFoundException;
import fr.soe.a3s.service.ProfileService;
import fr.soe.a3s.service.RepositoryService;
import fr.soe.a3s.ui.AbstractDialog;
import fr.soe.a3s.ui.Facade;
import fr.soe.a3s.ui.repository.dialogs.progress.ProgressSynchronizationDialog;

public class RepositoryEditionDialog extends AbstractDialog implements DataAccessConstants {

	private DescriptionPanel descriptionPanel;
	private ProtocolPanel protocolPanel;
	private ConnectionPanel connectionPanel;
	/* Data */
	private String initialRepositoryName = null;
	private DefaultComboBoxModel comboBoxProtocolModel = null;
	/* Services */
	private final RepositoryService repositoryService = new RepositoryService();
	private final ProfileService profileService = new ProfileService();

	public RepositoryEditionDialog(Facade facade) {
		super(facade, "Repository", true);
		this.setResizable(false);

		{
			buttonOK.setPreferredSize(buttonCancel.getPreferredSize());
			getRootPane().setDefaultButton(buttonOK);
		}
		{
			Box vBox = Box.createVerticalBox();
			this.add(vBox, BorderLayout.CENTER);
			{
				descriptionPanel = new DescriptionPanel(this);
				connectionPanel = new ConnectionPanel();
				protocolPanel = new ProtocolPanel(connectionPanel);
				vBox.add(descriptionPanel);
				vBox.add(protocolPanel);
				vBox.add(connectionPanel);
			}
		}

		this.pack();
		int height = this.getBounds().height;
		int width = this.getBounds().width;
		if (width < 510) {
			this.setPreferredSize(new Dimension(510, height));
		}
		this.pack();
		this.setLocationRelativeTo(facade.getMainPanel());
	}

	public void init() {

		this.setTitle("New repository");

		/* Init Protocol Section */
		comboBoxProtocolModel = new DefaultComboBoxModel(new String[] { ProtocolType.FTP.getDescription(),
				ProtocolType.HTTP.getDescription(), ProtocolType.HTTPS.getDescription() });
		protocolPanel.init(comboBoxProtocolModel);

		/* Init Connection Section */
		connectionPanel.init(ProtocolType.FTP);
	}

	public void init(String repositoryName) {

		this.setTitle("Edit repository");
		this.initialRepositoryName = repositoryName;

		/* Init Repository Section */
		descriptionPanel.init(repositoryName);

		/* Init Protocol Section */
		comboBoxProtocolModel = new DefaultComboBoxModel(new String[] { ProtocolType.FTP.getDescription(),
				ProtocolType.HTTP.getDescription(), ProtocolType.HTTPS.getDescription() });
		protocolPanel.init(comboBoxProtocolModel);

		try {
			/* Init Repository and Connection Section */
			RepositoryDTO repositoryDTO = repositoryService.getRepository(repositoryName);
			ProtocolDTO protocoleDTO = repositoryDTO.getProtocoleDTO();
			protocolPanel.init(protocoleDTO);
			connectionPanel.init(protocoleDTO);
		} catch (RepositoryException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(facade.getMainPanel(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	protected void buttonOKPerformed() {

		try {
			String newRepositoryName = descriptionPanel.getRepositoryName();
			ProtocolType protocolType = ProtocolType.getEnum((String) comboBoxProtocolModel.getSelectedItem());
			String url = connectionPanel.getUrl();
			String port = connectionPanel.getPort();
			String login = connectionPanel.getLogin();
			String password = connectionPanel.getPassword();
			boolean validateSSLCertificate = protocolPanel.getCheckBoxValidateSSLCertificate().isSelected();

			if (initialRepositoryName != null) {// Edit Repository
				if (initialRepositoryName.equals(newRepositoryName)) {
					repositoryService.setRepository(initialRepositoryName, url, port, login, password, protocolType,validateSSLCertificate);
				} else {
					repositoryService.renameRepository(initialRepositoryName, newRepositoryName);
					repositoryService.setRepository(newRepositoryName, url, port, login, password, protocolType,validateSSLCertificate);
				}
				repositoryService.resetRepositoryUploadProtocol(newRepositoryName);
			} else {// New Repository
				repositoryService.createRepository(newRepositoryName, url, port, login, password, protocolType,
						validateSSLCertificate);
				// Set default download path
				List<String> addonSearchDirectories = profileService.getAddonSearchDirectoryPaths();
				if (!addonSearchDirectories.isEmpty()) {
					repositoryService.setDefaultDownloadLocation(newRepositoryName, null,
							addonSearchDirectories.get(0));
				}
			}

			repositoryService.write(newRepositoryName);
			this.dispose();

			ProgressSynchronizationDialog synchronizingPanel = new ProgressSynchronizationDialog(facade);
			synchronizingPanel.setVisible(true);
			synchronizingPanel.init(newRepositoryName);
		} catch (CheckException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
		} catch (RepositoryNotFoundException | WritingException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	protected void menuExitPerformed() {
		connectionPanel.clearPassword();
		this.dispose();
	}

	@Override
	protected void buttonCancelPerformed() {
		connectionPanel.clearPassword();
		this.dispose();
	}

	public ConnectionPanel getConnectionPanel() {
		return this.connectionPanel;
	}

	public DefaultComboBoxModel getComboBoxProtocolModel() {
		return this.comboBoxProtocolModel;
	}
}
