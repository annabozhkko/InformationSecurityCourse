import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class LSBImplementation {

    public void implement(String text, String filePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(filePath)); // изображение для внедрения текста

        int widthImage = image.getWidth();

        byte[] textBytes = text.getBytes();
        int length = textBytes.length;

        int x = 0, y = 0;

        // внедряем размер текста
        for(int i = 0; i < 32; ++i){
            int rgb = image.getRGB(x, y);
            int bit = (length >> i) & 1; // бит, который нужно установить

            rgb = (rgb % 2 == 0) ? rgb : rgb - 1;
            image.setRGB(x, y, rgb | bit);

            x++;
        }

        for (byte b : textBytes) {
            for (int j = 0; j < 8; ++j) {
                int rgb = image.getRGB(x, y);
                int bit = (b >> j) & 1;

                rgb = (rgb % 2 == 0) ? rgb : rgb - 1;
                image.setRGB(x, y, rgb | bit);

                x++;
                if (x == widthImage) {
                    x = 0;
                    y++;
                }
            }
        }

        File file = new File("src/main/resources/imageLSD.png");
        file.createNewFile();
        ImageIO.write(image, "png", file);
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Line to implementation: ");
        String line = scanner.nextLine();  // считываем строку для внедрения

        LSBImplementation lsbImplementation = new LSBImplementation();
        lsbImplementation.implement(line, "src/main/resources/image.png");
    }
}
