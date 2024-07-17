import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

@SuppressWarnings("serial")
public class Main extends JFrame {

    private JDesktopPane jDesktopPaneImage;
    private JMenuBar jMenuBar;
    private JMenu jMenuOpen;
    private JMenuItem jMenuItemOpenImage;
    private JMenuItem jMenuItemOpenLandscape;

    private JMenu jMenuComposite;
    private JMenuItem jMenuItemCompositeNormal;
    private JMenuItem jMenuItemCompositeSmooth;

    private BufferedImage selectedPersonImage;
    private BufferedImage selectedLandscapeImage;

    public Main() {
        super("Photo Viewer");
        setSize(1400, 840);

        instantiateComponents();
        addComponents();
        createListeners();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void instantiateComponents() {
        jDesktopPaneImage = new JDesktopPane();
        jMenuBar = new JMenuBar();
        jMenuOpen = new JMenu("Abrir");
        jMenuItemOpenImage = new JMenuItem("Abrir imagem da pessoa");
        jMenuItemOpenLandscape = new JMenuItem("Abrir imagem da paisagem");

        jMenuComposite = new JMenu("Compor");
        jMenuItemCompositeNormal = new JMenuItem("Normal");
        jMenuItemCompositeSmooth = new JMenuItem("Suave");
    }


    private void addComponents() {
        getContentPane().add(jDesktopPaneImage);

        jMenuOpen.add(jMenuItemOpenImage);
        jMenuOpen.add(jMenuItemOpenLandscape);
        jMenuBar.add(jMenuOpen);

        jMenuComposite.add(jMenuItemCompositeNormal);
        jMenuComposite.add(jMenuItemCompositeSmooth);
        jMenuBar.add(jMenuComposite);

        setJMenuBar(jMenuBar);
    }

    private void createListeners() {
        jMenuItemOpenImage.addActionListener((e) -> openPersonImage());
        jMenuItemOpenLandscape.addActionListener((e) -> openLandscapeImage());
        jMenuItemCompositeNormal.addActionListener((e) -> compositePersonInLandscape());
        jMenuItemCompositeSmooth.addActionListener((e) -> compositePersonInLandscapeSmooth());
    }

    private void displayAlertDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void openPersonImage() {
        selectImage(true);
    }

    private void openLandscapeImage() {
        selectImage(false);
    }

    private void compositePersonInLandscape() {
        if (validateImagesAreSelected()) {
            var personRGBMatrix = getRGBMatrix(selectedPersonImage);
            var personTransparentBackground = setTransparentBackground(personRGBMatrix);

            var landscapeRGBMatrix = getRGBMatrix(selectedLandscapeImage);

            var composedRGBAMatrix = compositePersonOverLandscape(personTransparentBackground, landscapeRGBMatrix);
            saveProcessedPersonImage(composedRGBAMatrix, "composed_person_landscape.png");
        }
    }

    private void compositePersonInLandscapeSmooth() {
        if (validateImagesAreSelected()) {
            var personRGBMatrix = getRGBMatrix(selectedPersonImage);
            var personTransparentBackground = setTransparentBackground(personRGBMatrix);

            var landscapeRGBMatrix = getRGBMatrix(selectedLandscapeImage);
        }
    }

    private void saveProcessedPersonImage(int[][][] rgbMatrix, String outputPath) {
        int width = rgbMatrix.length;
        int height = rgbMatrix[0].length;

        BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int r = rgbMatrix[x][y][0];
                int g = rgbMatrix[x][y][1];
                int b = rgbMatrix[x][y][2];
                int a = rgbMatrix[x][y][3];

                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                processedImage.setRGB(x, y, argb);
            }
        }

        try {
            File outputFile = new File(outputPath);
            ImageIO.write(processedImage, "png", outputFile);
            displayAlertDialog("Imagem processada salva em: " + outputFile.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
            displayAlertDialog("Erro ao salvar imagem processada.");
        }
    }

    private boolean validateImagesAreSelected() {
        if (selectedPersonImage == null) {
            displayAlertDialog("Por favor, selecione uma imagem da pessoa");
            return false;
        } else if (selectedLandscapeImage == null) {
            displayAlertDialog("Por favor, selecione uma imagem da paisagem");
            return false;
        }
        return true;
    }

    private int[][][] getRGBMatrix(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][][] rgbMatrix = new int[width][height][3];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                rgbMatrix[x][y][0] = (rgb >> 16) & 0xFF;
                rgbMatrix[x][y][1] = (rgb >> 8) & 0xFF;
                rgbMatrix[x][y][2] = rgb & 0xFF;
            }
        }

        return rgbMatrix;
    }

    private int[][][] setTransparentBackground(int[][][] rgbMatrix) {
        int width = rgbMatrix.length;
        int height = rgbMatrix[0].length;

        int[][][] newRGBMatrix = new int[width][height][4];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int r = rgbMatrix[x][y][0];
                int g = rgbMatrix[x][y][1];
                int b = rgbMatrix[x][y][2];

                if (r > 200 && g > 200 && b > 200) {
                    newRGBMatrix[x][y][0] = 0;
                    newRGBMatrix[x][y][1] = 0;
                    newRGBMatrix[x][y][2] = 0;
                    newRGBMatrix[x][y][3] = 0;
                } else {
                    newRGBMatrix[x][y][0] = r;
                    newRGBMatrix[x][y][1] = g;
                    newRGBMatrix[x][y][2] = b;
                    newRGBMatrix[x][y][3] = 255;
                }
            }
        }
        return newRGBMatrix;
    }

    private int[][][] compositePersonOverLandscape(int[][][] personRGBMatrix, int[][][] landscapeRGBMatrix) {
        int width = landscapeRGBMatrix.length;
        int height = landscapeRGBMatrix[0].length;

        // Criar uma nova matriz RGBA para a imagem composta
        int[][][] composedRGBAMatrix = new int[width][height][4];

        // Copiar a paisagem para a matriz composta
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                composedRGBAMatrix[x][y][0] = landscapeRGBMatrix[x][y][0]; // R
                composedRGBAMatrix[x][y][1] = landscapeRGBMatrix[x][y][1]; // G
                composedRGBAMatrix[x][y][2] = landscapeRGBMatrix[x][y][2]; // B
                composedRGBAMatrix[x][y][3] = 255; // A (totalmente opaco)
            }
        }

        // Sobrepor a imagem da pessoa com fundo transparente na matriz composta
        int personWidth = personRGBMatrix.length;
        int personHeight = personRGBMatrix[0].length;

        // Calcular a posição centralizada para a pessoa na paisagem
        int startX = (width - personWidth) / 2;
        int startY = (height - personHeight) / 2;

        for (int x = 0; x < personWidth; x++) {
            for (int y = 0; y < personHeight; y++) {
                // Copiar os pixels da pessoa para a matriz composta, mantendo a transparência
                if (personRGBMatrix[x][y][3] > 0) { // Verificar se o pixel da pessoa não é transparente
                    composedRGBAMatrix[startX + x][startY + y][0] = personRGBMatrix[x][y][0]; // R
                    composedRGBAMatrix[startX + x][startY + y][1] = personRGBMatrix[x][y][1]; // G
                    composedRGBAMatrix[startX + x][startY + y][2] = personRGBMatrix[x][y][2]; // B
                    composedRGBAMatrix[startX + x][startY + y][3] = personRGBMatrix[x][y][3]; // A (transparência)
                }
            }
        }

        return composedRGBAMatrix;
    }

    private void selectImage(boolean isPersonImage) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }

        String path = fileChooser.getSelectedFile().getAbsolutePath();

        try {
            BufferedImage selectedImage = ImageIO.read(new File(path));
            if (isPersonImage) {
                selectedPersonImage = selectedImage;
                displaySelectedImage(selectedPersonImage, "Selected Person Image");
            } else {
                selectedLandscapeImage = selectedImage;
                displaySelectedImage(selectedLandscapeImage, "Selected Landscape Image");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void displaySelectedImage(BufferedImage image, String title) {
        JInternalFrame frame = new JInternalFrame(title, true, true, true, true);
        ImagePanel panel = new ImagePanel(image);
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        frame.pack();
        jDesktopPaneImage.add(frame);
        frame.setVisible(true);
    }

    static class ImagePanel extends JPanel {

        private final BufferedImage image;

        public ImagePanel(BufferedImage image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return image != null ? new Dimension(image.getWidth(), image.getHeight()) : super.getPreferredSize();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }
}
