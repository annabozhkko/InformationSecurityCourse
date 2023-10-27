import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LSBExtraction {

    public String extract(String filePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(filePath)); // изображение для извлечения

        int widthImage = image.getWidth();

        int x = 0, y = 0;

        // получаем размер текста
        int length = 0;
        for(int i = 0; i < 32; ++i){
            int rgb = image.getRGB(x, y);
            int bit = rgb & 1;
            length = length | (bit << i);
            x++;
        }

        byte[] result = new byte[length];

        for(int i = 0; i < length; ++i){
            byte b = 0;
            for(int j = 0; j < 8; ++j){
                int rgb = image.getRGB(x, y);
                int bit = rgb & 1;
                b = (byte) (b | (bit << j));

                x++;
                if(x == widthImage){
                    x = 0;
                    y++;
                }
            }
            result[i] = b;
        }

        return new String(result);
    }

    public static void main(String[] args) throws IOException {
        LSBExtraction lsbExtraction = new LSBExtraction();
        String result = lsbExtraction.extract("src/main/resources/imageLSD.png");
        System.out.println("Extraction result: \n" + result);
    }
}

