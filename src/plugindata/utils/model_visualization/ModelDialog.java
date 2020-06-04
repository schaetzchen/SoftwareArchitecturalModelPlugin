package plugindata.utils.model_visualization;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ModelDialog extends DialogWrapper {

    private String pathToImage;
    private String interfaceData;

    public ModelDialog(String pathToImage, String interfaceData) {
        super(true);
        this.pathToImage = pathToImage;
        this.interfaceData = interfaceData;
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        try {
            BufferedImage image = ImageIO.read(new File(pathToImage));
            JLabel picLabel = new JLabel(new ImageIcon(image));
            picLabel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            panel.add(picLabel, BorderLayout.WEST);

            JTextPane textPane = new JTextPane();
            textPane.setText(interfaceData);
            textPane.setPreferredSize(new Dimension(250, 300));
            panel.add(textPane, BorderLayout.EAST);

            panel.setPreferredSize(new Dimension(picLabel.getWidth() + textPane.getWidth() + 5,
                    picLabel.getHeight() + textPane.getHeight()));
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        return panel;
    }
}
