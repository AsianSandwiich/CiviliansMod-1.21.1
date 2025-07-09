package net.asian.civiliansmod.custom_skins;

import net.asian.civiliansmod.util.FolderUtil;
import java.io.File;
import java.io.IOException;


public class SkinFolderManager {
    public static void openFolder(NPCModel subFolderName) {
        // Determine the base folder name based on the subFolderName
        String baseFolderName = subFolderName == NPCModel.WIDE ? FolderUtil.WIDE_SKIN_PATH.toString() : FolderUtil.SLIM_SKIN_PATH.toString();

        // Fetch the actual folder
        File folderToOpen = new File(baseFolderName);

        try {
            // Open the folder based on the OS
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                new ProcessBuilder("explorer", folderToOpen.getAbsolutePath()).start();
            } else if (osName.contains("mac")) {
                new ProcessBuilder("open", folderToOpen.getAbsolutePath()).start();
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                new ProcessBuilder("xdg-open", folderToOpen.getAbsolutePath()).start();
            } else {
                System.err.println("Unknown operating system. Cannot open folder.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void register(){

    }

    public enum NPCModel {
        SLIM,
        WIDE
    }
}