/**
 * Processamento de Imagens
 * Alunos: Isaac Santiago e Marcus Vinícius
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;
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
        super("Composição de imagens");
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

    private int getSmoothIntensity() {
        JSlider slider = new JSlider(1, 21, 5);
        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        int option = JOptionPane.showConfirmDialog(
                this,
                slider,
                "Selecione a intensidade da suavização",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        return option == JOptionPane.OK_OPTION ? slider.getValue() : -1;
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
            displayProcessedPersonImage(composedRGBAMatrix, "Resultado da composição normal");
        }
    }

    private void compositePersonInLandscapeSmooth() {
        if (validateImagesAreSelected()) {
            int intensity = getSmoothIntensity();
            if (intensity == -1) return;

            var personRGBMatrix = getRGBMatrix(selectedPersonImage);
            var personTransparentBackground = setTransparentBackground(personRGBMatrix);

            var landscapeRGBMatrix = getRGBMatrix(selectedLandscapeImage);
            var landscapeSmoothedRGBMatrix = applyEnhancedSmoothFilter(landscapeRGBMatrix, intensity);

            var composedRGBAMatrix = compositePersonOverLandscape(personTransparentBackground, landscapeSmoothedRGBMatrix);
            var title = String.format("Resultado da composição suave (intensidade: %d)", intensity);
            displayProcessedPersonImage(composedRGBAMatrix, title);
        }
    }

    private void displayProcessedPersonImage(int[][][] rgbMatrix, String title) {
        int width = rgbMatrix.length;
        int height = rgbMatrix[0].length;

        BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        IntStream.range(0, width).forEach(x ->
                IntStream.range(0, height).forEach(y -> {
                    int[] pixel = rgbMatrix[x][y];
                    int r = pixel[0];
                    int g = pixel[1];
                    int b = pixel[2];
                    int a = pixel[3];

                    int argb = (a << 24) | (r << 16) | (g << 8) | b;
                    processedImage.setRGB(x, y, argb);
                })
        );

        displaySelectedImage(processedImage, title);
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

        IntStream.range(0, width).forEach(x ->
                IntStream.range(0, height).forEach(y -> {
                    int rgb = image.getRGB(x, y);
                    rgbMatrix[x][y][0] = (rgb >> 16) & 0xFF;
                    rgbMatrix[x][y][1] = (rgb >> 8) & 0xFF;
                    rgbMatrix[x][y][2] = rgb & 0xFF;
                })
        );
        return rgbMatrix;
    }

    private int[][][] setTransparentBackground(int[][][] rgbMatrix) {
        int width = rgbMatrix.length;
        int height = rgbMatrix[0].length;

        int[][][] newRGBMatrix = new int[width][height][4];

        IntStream.range(0, width).forEach(x ->
                IntStream.range(0, height).forEach(y -> {
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
                })
        );

        return newRGBMatrix;
    }

    private int[][][] compositePersonOverLandscape(int[][][] personRGBMatrix, int[][][] landscapeRGBMatrix) {
        int width = landscapeRGBMatrix.length;
        int height = landscapeRGBMatrix[0].length;

        int[][][] composedRGBAMatrix = new int[width][height][4];

        IntStream.range(0, width).forEach(x ->
                IntStream.range(0, height).forEach(y -> {
                    composedRGBAMatrix[x][y][0] = landscapeRGBMatrix[x][y][0];
                    composedRGBAMatrix[x][y][1] = landscapeRGBMatrix[x][y][1];
                    composedRGBAMatrix[x][y][2] = landscapeRGBMatrix[x][y][2];
                    composedRGBAMatrix[x][y][3] = 255;
                })
        );

        int personWidth = personRGBMatrix.length;
        int personHeight = personRGBMatrix[0].length;

        int startX = (width - personWidth) / 2;
        int startY = (height - personHeight) / 2;

        IntStream.range(0, personWidth).forEach(x ->
                IntStream.range(0, personHeight).forEach(y -> {
                    if (personRGBMatrix[x][y][3] > 0) {
                        composedRGBAMatrix[startX + x][startY + y][0] = personRGBMatrix[x][y][0];
                        composedRGBAMatrix[startX + x][startY + y][1] = personRGBMatrix[x][y][1];
                        composedRGBAMatrix[startX + x][startY + y][2] = personRGBMatrix[x][y][2];
                        composedRGBAMatrix[startX + x][startY + y][3] = personRGBMatrix[x][y][3];
                    }
                })
        );

        return composedRGBAMatrix;
    }

    private int[][][] applyEnhancedSmoothFilter(int[][][] rgbMatrix, int KERNEL_SIZE) {
        int width = rgbMatrix.length;
        int height = rgbMatrix[0].length;

        int[][][] smoothedRGBMatrix = new int[width][height][3];

        int[][] KERNEL = new int[KERNEL_SIZE][KERNEL_SIZE];
        IntStream.range(0, KERNEL_SIZE).forEach(i ->
                IntStream.range(0, KERNEL_SIZE).forEach(j -> KERNEL[i][j] = 1)
        );

        int SMOOTH_FACTOR = KERNEL_SIZE * KERNEL_SIZE;
        int offset = KERNEL_SIZE / 2;

        IntStream.range(offset, width - offset).forEach(x ->
                IntStream.range(offset, height - offset).forEach(y -> {
                    int[] sums = IntStream.range(0, KERNEL_SIZE * KERNEL_SIZE)
                            .mapToObj(k -> {
                                int dx = k / KERNEL_SIZE;
                                int dy = k % KERNEL_SIZE;
                                int pixelX = x + dx - offset;
                                int pixelY = y + dy - offset;
                                return new int[]{
                                        rgbMatrix[pixelX][pixelY][0] * KERNEL[dx][dy],
                                        rgbMatrix[pixelX][pixelY][1] * KERNEL[dx][dy],
                                        rgbMatrix[pixelX][pixelY][2] * KERNEL[dx][dy]
                                };
                            })
                            .reduce(new int[]{0, 0, 0}, (acc, val) -> new int[]{
                                    acc[0] + val[0], acc[1] + val[1], acc[2] + val[2]
                            });

                    smoothedRGBMatrix[x][y][0] = sums[0] / SMOOTH_FACTOR;
                    smoothedRGBMatrix[x][y][1] = sums[1] / SMOOTH_FACTOR;
                    smoothedRGBMatrix[x][y][2] = sums[2] / SMOOTH_FACTOR;
                })
        );

        return smoothedRGBMatrix;
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
                displaySelectedImage(selectedPersonImage, "Imagem da pessoa selecionada");
            } else {
                selectedLandscapeImage = selectedImage;
                displaySelectedImage(selectedLandscapeImage, "Imagem da paisagem selecionada");
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
        SwingUtilities.invokeLater(Main::new);
    }
}
