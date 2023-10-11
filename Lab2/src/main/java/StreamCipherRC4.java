import java.security.SecureRandom;
import java.util.Scanner;

public class StreamCipherRC4 {
    private final int KEY_LENGTH = 16;
    private final int S_LENGTH = 256;
    private byte[] key;
    private int[] S;

    public StreamCipherRC4(){
        generateKey();
    }

    public byte[] cipher(byte[] line){
        initS();
        int length = line.length;
        byte[] cipheredBytes = new byte[length];

        int x = 0, y = 0;

        // данные XOR-ятся с потоком битов S, чтобы создать зашифрованный поток данных
        for (int i = 0; i < length; i++) {
            x = (x + 1) % S_LENGTH;
            y = (y + S[x]) % S_LENGTH;

            swap(x, y);

            int keyItem = S[(S[x] + S[y]) % S_LENGTH];
            cipheredBytes[i] = (byte) (keyItem ^ line[i]);
        }

        return cipheredBytes;
    }

    public String decipher(byte[] line){
        // зашифрованный поток данных XOR-ятся с потоком битов, чтобы получить оригинальные данные
        return new String(cipher(line));
    }

    private void generateKey(){
        // генерируемр рандомный ключ размером 16 байт
        SecureRandom random = new SecureRandom();
        key = new byte[KEY_LENGTH];
        random.nextBytes(key);
    }

    private void initS(){
        // инициализируем S - состояние генератора псевдослучайных чисел на основе ключа.
        S = new int[S_LENGTH];
        for (int i = 0; i < S_LENGTH; i++) {
            S[i] = i;
        }

        int j = 0;
        for (int i = 0; i < S_LENGTH; i++) {
            j = (j + S[i] + (key[i % KEY_LENGTH]) & 0xFF) % S_LENGTH;
            swap(i, j);
        }
    }

    private void swap(int i, int j) {
        int t = S[i];
        S[i] = S[j];
        S[j] = t;
    }

    public static void main(String[] args) {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();

            StreamCipherRC4 streamCipher = new StreamCipherRC4();

            byte[] cipheredLine = streamCipher.cipher(line.getBytes());

            System.out.println("Ciphered string: " + new String(cipheredLine));
            System.out.println("Deciphered line: " + streamCipher.decipher(cipheredLine));
            System.out.println('\n');
        }
    }
}
