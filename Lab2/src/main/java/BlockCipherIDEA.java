import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

public class BlockCipherIDEA {
    private final int rounds = 8; // количество раундов
    private int[] encSubKey;
    private int[] decSubKey;

    public BlockCipherIDEA(){
        // генерируемр рандомный ключ размером 16 байт
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[16];
        random.nextBytes(key);

        encSubKey = setEncSubKeys(key);
        decSubKey = setDecSubKeys(encSubKey);
    }

    // генерируем 52 субключа - К(1), К(2),., К(52)
    private int[] setEncSubKeys(byte[] key) {
        int[] subKey = new int[rounds * 6 + 4];

        for (int i = 0; i < key.length / 2; i++)
            subKey[i] = ((key[2 * i] & 0xFF) << 8) | (key[2 * i + 1] & 0xFF);

        for (int i = key.length / 2; i < subKey.length; i++)
            subKey[i] = ((subKey[(i + 1) % 8 != 0 ? i - 7 : i - 15] << 9) | (subKey[(i + 2) % 8 < 2 ? i - 14 : i - 6] >> 7)) & 0xFFFF;

        return subKey;
    }

    // генерируем 52 субключа - К(1), К(2),., К(52) для дешифрования
    private int[] setDecSubKeys(int[] key) {
        int[] invKey = new int[key.length];
        int p = 0;
        int i = rounds * 6;
        invKey[i + 0] = mulInv(key[p++]);
        invKey[i + 1] = addInv(key[p++]);
        invKey[i + 2] = addInv(key[p++]);
        invKey[i + 3] = mulInv(key[p++]);
        for (int r = rounds - 1; r >= 0; r--) {
            i = r * 6;
            int m = r > 0 ? 2 : 1;
            int n = r > 0 ? 1 : 2;
            invKey[i + 4] = key[p++];
            invKey[i + 5] = key[p++];
            invKey[i + 0] = mulInv(key[p++]);
            invKey[i + m] = addInv(key[p++]);
            invKey[i + n] = addInv(key[p++]);
            invKey[i + 3] = mulInv(key[p++]);
        }
        return invKey;
    }

    public byte[] encrypt(byte[] data, int length){
        byte[] result = new byte[length];

        // расшифровываем по 8 байт
        for(int i = 0; i < length; i += 8) {
            crypt(data, i, encSubKey);
        }
        System.arraycopy(data,0, result, 0, length);
        return result;
    }

    public byte[] decrypt(byte[] data, int length){
        byte[] result = new byte[length];

        for(int i = 0; i < length; i += 8) {
            crypt(data, i, decSubKey);
        }
        System.arraycopy(data,0, result, 0, length);
        return result;
    }

    private void crypt(byte[] data, int i, int[] subKey){
        // разделяем на четыре части данные входных байтов
        int x0 = ((data[i+0] & 0xFF) << 8) | (data[i+1] & 0xFF);
        int x1 = ((data[i+2] & 0xFF) << 8) | (data[i+3] & 0xFF);
        int x2 = ((data[i+4] & 0xFF) << 8) | (data[i+5] & 0xFF);
        int x3 = ((data[i+6] & 0xFF) << 8) | (data[i+7] & 0xFF);

//        А = А * К(1); B = B + K(2); C = C + K(3); D = D * K(4);
//        Первый цикл вычислений включает в себя:
//
//        E = A XOR C; F = B XOR D
//        E = E * K(5)
//        F = F + E
//        F = F * K(6)
//        E = E + F
//        A = A XOR F
//        C = C XOR F
//        B = B XOR E
//        D = D XOR E

        int p = 0;
        for (int round = 0; round < rounds; round++) {
            int y0 = mul(x0, subKey[p++]);
            int y1 = add(x1, subKey[p++]);
            int y2 = add(x2, subKey[p++]);
            int y3 = mul(x3, subKey[p++]);

            int t0 = mul(y0 ^ y2, subKey[p++]);
            int t1 = add(y1 ^ y3, t0);
            int t2 = mul(t1, subKey[p++]);
            int t3 = add(t0, t2);

            x0 = xor(y0,t2);
            x1 = xor(y2,t2);
            x2 = xor(y1,t3);
            x3 = xor(y3,t3);
        }

//        A = A * K(49)
//        B = B + K(50)
//        C = C + K(51)
//        D = D * K(52)

        int r0 = mul(x0, subKey[p++]);
        int r1 = add(x2, subKey[p++]);
        int r2 = add(x1, subKey[p++]);
        int r3 = mul(x3, subKey[p++]);

        // сохраняем результат
        data[i+0] = (byte)(r0 >> 8);
        data[i+1] = (byte)r0;
        data[i+2] = (byte)(r1 >> 8);
        data[i+3] = (byte)r1;
        data[i+4] = (byte)(r2 >> 8);
        data[i+5] = (byte)r2;
        data[i+6] = (byte)(r3 >> 8);
        data[i+7] = (byte)r3;
    }

    private int add(int a, int b) {
        return (a + b) & 0xFFFF;
    }

    private int mul(int a, int b) {
        long r = (long) a * (long) b;
        if (r != 0)
            return (int)(r % 0x10001) & 0xFFFF;
        else
            return (1 - a - b) & 0xFFFF;
    }

    private static int xor(int a, int b) {
        return a ^ b;
    }

    private int addInv (int x) {
        return (0x10000 - x) & 0xFFFF;
    }

    private int mulInv (int x) {
        if (x <= 1) {
            return x;
        }
        int y = 0x10001;
        int t0 = 1;
        int t1 = 0;
        while (true) {
            t1 += y / x * t0;
            y %= x;
            if (y == 1) {
                return 0x10001 - t1;
            }
            t0 += x / y * t1;
            x %= y;
            if (x == 1) {
                return t0;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        FileInputStream fileInputStream = new FileInputStream("src/main/resources/file.txt");
        FileOutputStream fileOutputStream = new FileOutputStream("src/main/resources/cipheredFile.txt");

        BlockCipherIDEA idea = new BlockCipherIDEA();

        byte buffer[] = new byte[64];

        int bufferLen;
        while ((bufferLen = fileInputStream.read(buffer, 0, 64)) > 0) {
            fileOutputStream.write(idea.encrypt(buffer, bufferLen));
        }

        fileInputStream = new FileInputStream("src/main/resources/cipheredFile.txt");
        fileOutputStream = new FileOutputStream("src/main/resources/decipheredFile.txt");

        while ((bufferLen = fileInputStream.read(buffer, 0, 64)) > 0) {
            fileOutputStream.write(idea.decrypt(buffer, bufferLen));
        }
    }
}
