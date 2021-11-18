/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author balis
 */
public class ImageHelper {

    public static void saveFile(File src) {
        File dir = new File("photo", src.getName());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            Path source = Paths.get(src.getAbsolutePath());
            Path desination = Paths.get(dir.getAbsolutePath());
            Files.copy(source, desination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ImageIcon readFile(String filename, JLabel label) {
        File path = new File("photo", filename);
        return new ImageIcon(new ImageIcon(path.getAbsolutePath()).getImage().getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH));
    }
}
