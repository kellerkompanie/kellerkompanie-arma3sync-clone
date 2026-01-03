package fr.soe.a3s.console;

import java.io.File;
import java.util.List;

import fr.soe.a3s.controller.ObserverEnd;
import fr.soe.a3s.dto.RepositoryDTO;
import fr.soe.a3s.exception.LoadingException;
import fr.soe.a3s.exception.repository.RepositoryException;
import fr.soe.a3s.service.RepositoryService;

/**
 * edited by Schwaggot
 */
public class CommandLine extends CommandGeneral {

    public void build(String repositoryName) {

        RepositoryService repositoryService = new RepositoryService();

        /* Load Repositories */

        try {
            repositoryService.readAll();
            repositoryService.getRepository(repositoryName);
        } catch (LoadingException | RepositoryException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        /* Proceed with command */

        ObserverEnd observerEndBuild = new ObserverEnd() {
            @Override
            public void end() {
                System.exit(0);
            }
        };

        super.build(repositoryName, observerEndBuild);
    }

    public void buildAll() {

        RepositoryService repositoryService = new RepositoryService();

        /* Load Repositories */

        try {
            repositoryService.readAll();
        } catch (LoadingException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        /* Proceed with command */

        ObserverEnd observerEndBuild = () -> {
        };

        List<RepositoryDTO> repositories = repositoryService.getRepositories();
        for (RepositoryDTO repo : repositories) {
            super.build(repo.getName(), observerEndBuild);
        }

        System.exit(0);
    }

    public void check(String repositoryName) {

        RepositoryService repositoryService = new RepositoryService();

        /* Load Repositories */

        try {
            repositoryService.readAll();
            repositoryService.getRepository(repositoryName);
        } catch (LoadingException | RepositoryException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        /* Proceed with command */

        ObserverEnd observerEndCheck = new ObserverEnd() {
            @Override
            public void end() {
                System.exit(0);
            }
        };

        super.check(repositoryName, observerEndCheck);
    }

    public void sync(final String repositoryName, String destinationFolderPath,
                     String withExactMath) {

        assert (repositoryName != null);
        assert (destinationFolderPath != null);

        RepositoryService repositoryService = new RepositoryService();

        /* Load Repositories */

        try {
            repositoryService.readAll();
            repositoryService.getRepository(repositoryName);
        } catch (LoadingException | RepositoryException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        // Set parameters
        if (!new File(destinationFolderPath).exists()
                || !new File(destinationFolderPath).isDirectory()) {
            String message = "Error: destination folder path "
                    + destinationFolderPath + " does not exist!";
            System.out.println(message);
            System.exit(0);
        }
        if (!(withExactMath.equalsIgnoreCase("true") || withExactMath
                .equalsIgnoreCase("false"))) {
            String message = "Unrecognized exact math parameter (true/false).";
            System.out.println(message);
            System.exit(0);
        }

        /* Proceed with command */

        repositoryService.setDefaultDownloadLocation(repositoryName, null,
                destinationFolderPath);
        repositoryService.setExactMatch(Boolean.parseBoolean(withExactMath),
                repositoryName);
        repositoryService.setConnectionTimeout(repositoryName, "0");
        repositoryService.setReadTimeout(repositoryName, "0");

        ObserverEnd observerEnd = new ObserverEnd() {
            @Override
            public void end() {
                System.exit(0);
            }
        };

        super.sync(repositoryName, observerEnd);
    }

    @Override
    public void extractBikeys(String sourceDirectoryPath,
                              String targetDirectoryPath) {

        super.extractBikeys(sourceDirectoryPath, targetDirectoryPath);

        System.exit(0);
    }

    public void checkForUpdates() {

        super.checkForUpdates(false);

        System.exit(0);
    }

    /* Modset Commands */

    public void modsetList(String repositoryName) {
        super.modsetList(repositoryName);
        System.exit(0);
    }

    public void modsetShow(String repositoryName, String modsetName) {
        super.modsetShow(repositoryName, modsetName);
        System.exit(0);
    }

    public void modsetCreate(String repositoryName, String modsetName) {
        super.modsetCreate(repositoryName, modsetName);
        System.exit(0);
    }

    public void modsetDelete(String repositoryName, String modsetName) {
        super.modsetDelete(repositoryName, modsetName);
        System.exit(0);
    }

    public void modsetRename(String repositoryName, String oldName, String newName) {
        super.modsetRename(repositoryName, oldName, newName);
        System.exit(0);
    }

    public void modsetSetDescription(String repositoryName, String modsetName, String description) {
        super.modsetSetDescription(repositoryName, modsetName, description);
        System.exit(0);
    }

    public void modsetAddAddon(String repositoryName, String modsetName, String addonName, boolean optional) {
        super.modsetAddAddon(repositoryName, modsetName, addonName, optional);
        System.exit(0);
    }

    public void modsetRemoveAddon(String repositoryName, String modsetName, String addonName) {
        super.modsetRemoveAddon(repositoryName, modsetName, addonName);
        System.exit(0);
    }

    public void modsetListAddons(String repositoryName) {
        super.modsetListAddons(repositoryName);
        System.exit(0);
    }
}
