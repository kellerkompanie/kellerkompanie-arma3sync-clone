package fr.soe.a3s.ui.repository.workers;

import java.awt.Color;
import java.util.List;

import javax.swing.SwingUtilities;

import fr.soe.a3s.controller.ObserverCountInt;
import fr.soe.a3s.controller.ObserverError;
import fr.soe.a3s.dao.DataAccessConstants;
import fr.soe.a3s.service.administration.RepositoryCheckProcessor;
import fr.soe.a3s.ui.Facade;
import fr.soe.a3s.ui.repository.AdminPanel;

public class RepositoryChecker extends Thread implements DataAccessConstants {

	private final Facade facade;
	private final AdminPanel adminPanel;
	/* Data */
	private final String repositoryName;
	/* Tests */
	private boolean canceled;
	/* Services */
	private RepositoryCheckProcessor repositoryCheckProcessor;
	/* observers */
	private ObserverError observerEnd;
	private ObserverError observerError;

	public RepositoryChecker(Facade facade, String repositoryName, AdminPanel adminPanel) {
		this.facade = facade;
		this.adminPanel = adminPanel;
		this.repositoryName = repositoryName;
	}

	@Override
	public void run() {

		System.out.println("Starting checking repository content: " + repositoryName);

		// Init AdminPanel for start checking
		initAdminPanelForStartCheck();
		canceled = false;

		this.adminPanel.getCheckProgressBar().setIndeterminate(true);

		repositoryCheckProcessor = new RepositoryCheckProcessor(repositoryName);
		repositoryCheckProcessor.addObserverCountProgress(new ObserverCountInt() {
			@Override
			public void update(int value) {
				executeUpdateCountProgress(value);
			}
		});
		repositoryCheckProcessor.addObserverCountErrors(new ObserverCountInt() {
			@Override
			public void update(int value) {
				executeUpdateCountErrors(value);
			}
		});
		repositoryCheckProcessor.addObserverEnd(new ObserverError() {
			@Override
			public void error(List<Exception> errors) {
				executeEnd(errors);
			}
		});
		repositoryCheckProcessor.addObserverError(new ObserverError() {
			@Override
			public void error(List<Exception> errors) {
				executeError(errors);
			}
		});

		repositoryCheckProcessor.run();
	}

	private void initAdminPanelForStartCheck() {

		this.adminPanel.getButtonSelectRepositoryfolderPath().setEnabled(false);
		this.adminPanel.getButtonBuild().setEnabled(false);
		this.adminPanel.getButtonBuildOptions().setEnabled(false);
		this.adminPanel.getButtonUpload().setEnabled(false);
		this.adminPanel.getButtonUploadOptions().setEnabled(false);
		this.adminPanel.getButtonCopyAutoConfigURL().setEnabled(false);
		this.adminPanel.getButtonCheck().setText("Stop");
		this.adminPanel.getCheckProgressBar().setString("Checking remote files...");
		this.adminPanel.getCheckProgressBar().setStringPainted(true);
		this.adminPanel.getCheckProgressBar().setMaximum(100);
		this.adminPanel.getCheckProgressBar().setMinimum(0);
		this.adminPanel.getCheckErrorLabel().setForeground(new Color(45, 125, 45));
		this.adminPanel.getCheckErrorLabelValue().setText("0");
		this.adminPanel.getCheckErrorLabelValue().setForeground(new Color(45, 125, 45));
		this.adminPanel.getCheckInformationBox().setVisible(true);
	}

	private void initAdminPanelForEndCheck() {

		this.adminPanel.getButtonSelectRepositoryfolderPath().setEnabled(true);
		this.adminPanel.getButtonBuild().setEnabled(true);
		this.adminPanel.getButtonBuildOptions().setEnabled(true);
		this.adminPanel.getButtonUpload().setEnabled(true);
		this.adminPanel.getButtonUploadOptions().setEnabled(true);
		this.adminPanel.getButtonCopyAutoConfigURL().setEnabled(true);
		this.adminPanel.getButtonCheck().setText("Check");
		this.adminPanel.getCheckProgressBar().setString("");
		this.adminPanel.getCheckProgressBar().setStringPainted(false);
		this.adminPanel.getCheckProgressBar().setMaximum(0);
		this.adminPanel.getCheckProgressBar().setMinimum(0);
		this.adminPanel.getCheckErrorLabel().setForeground(new Color(45, 125, 45));
		this.adminPanel.getCheckErrorLabelValue().setText("0");
		this.adminPanel.getCheckErrorLabelValue().setForeground(new Color(45, 125, 45));
		this.adminPanel.getCheckInformationBox().setVisible(false);
	}

	private void executeUpdateCountProgress(final int value) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				adminPanel.getCheckProgressBar().setIndeterminate(false);
				adminPanel.getCheckProgressBar().setValue(value);
			}
		});
	}

	private void executeUpdateCountErrors(int value) {

		adminPanel.getCheckErrorLabelValue().setText(Integer.toString(value));
		adminPanel.getCheckErrorLabel().setForeground(Color.RED);
		adminPanel.getCheckErrorLabelValue().setForeground(Color.RED);
	}

	private void executeEnd(List<Exception> errors) {

		adminPanel.getCheckProgressBar().setIndeterminate(false);

		if (!canceled) {

			canceled = true;

			if (errors.isEmpty()) {
				System.out.println("Repository: " + repositoryName + " - repository is synchronized.");
			} else {
				System.out.println("Repository: " + repositoryName + " - repository is not synchronized.");
				for (Exception e : errors) {
					System.out.println(e.getMessage());
				}
			}

			this.adminPanel.getCheckProgressBar().setValue(100);
			this.adminPanel.getCheckProgressBar().setString("100%");

			initAdminPanelForEndCheck();
			terminate();
			
			// Admin panel
			observerEnd.error(errors);
		}
	}

	private void executeError(List<Exception> errors) {

		adminPanel.getCheckProgressBar().setIndeterminate(false);

		if (!canceled) {

			canceled = true;

			System.out.println("Repository: " + repositoryName + " - synchronization finished with error.");

			this.adminPanel.getCheckProgressBar().setString("Error!");

			initAdminPanelForEndCheck();
			terminate();
			
			// Admin panel
			observerError.error(errors);
		}
	}

	private void terminate() {

		repositoryCheckProcessor.cancel();
		System.gc();// Required for unlocking files!
	}

	public void cancel() {

		this.canceled = true;
		initAdminPanelForEndCheck();
		terminate();
	}

	public void addObserverEnd(ObserverError obs) {
		this.observerEnd = obs;
	}

	public void addObserverError(ObserverError obs) {
		this.observerError = obs;
	}
}
