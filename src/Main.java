import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
/**
 *
 * @author Rümeysa ÖZ
 */
public class Main {
    static int M;
    static int BIT_LENGTH = 1024;
    private final static Random rand = new Random();
    static List<BigInteger> nList = new ArrayList<BigInteger>();
    static List<BigInteger> dList = new ArrayList<BigInteger>();
    static List<BigInteger> eList = new ArrayList<BigInteger>();

    static int keyNum = 0;
    static BigInteger d, n, e;

    public static void main(String[] args) {


        JFrame f= new JFrame("RSA");

        JComboBox<String> jComboBox = new JComboBox<>();
        jComboBox.setBounds(50, 400, 140, 20);

        JButton jButton = new JButton("Select");
        jButton.setBounds(50, 450, 90, 20);

        JButton generateButton = new JButton("Generate Key");
        generateButton.setBounds(250, 400, 150, 20);

        JLabel jLabel = new JLabel();
        jLabel.setBounds(50, 460, 400, 100);

        f.add(jButton);
        f.add(jComboBox);
        f.add(jLabel);
        f.add(generateButton);

        // Generate key button
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                jComboBox.addItem("Key Pair(" + keyNum + ")");
                keyNum += 1;
                generateKeys();
            }
        });


        /**
         * Select key
          */
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String selectedKey = jComboBox.getItemAt(jComboBox.getSelectedIndex());

                int key = Integer.parseInt(Character.toString(selectedKey.charAt(selectedKey.indexOf('(')+1)));
                jLabel.setText("You selected " + key);
                d = dList.get(key);
                n = nList.get(key);
                e = eList.get(key);


            }
        });

        /**
         *    text areas
          */
        JTextArea t1,t2,t3;
        t1=new JTextArea();
        t1.setBounds(50,100, 200,100);
        t3=new JTextArea();
        t3.setBounds(50,250, 400,100);
        t2=new JTextArea();
        t2.setBounds(300,100, 200,100);
        f.add(t1); f.add(t2); f.add(t3);
        f.setSize(800,800);
        f.setLayout(null);
        f.setVisible(true);
        JButton b=new JButton("Encrypt");
        b.setBounds(50,20,95,30);
        f.add(b);
        t1.setLineWrap(true);
        t1.setWrapStyleWord(true);
        t2.setLineWrap(true);
        t2.setWrapStyleWord(true);
        t3.setLineWrap(true);
        t3.setWrapStyleWord(true);

        /**
         *  if encrypt button is pressed
         */
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){

                String message = t1.getText();
                String encrypted = encrypt(message,e,n);

                t3.setText(encrypted);

            }
        });

        JButton b2=new JButton("Decrypt");
        b2.setBounds(300,20,95,30);
        f.add(b2);

        /**
         * if decrypt button is pressed
         */
        b2.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){

                String message = t2.getText();

                String decrypted = decrypt(message,d,n);
                t3.setText(decrypted);

            }
        });



    }

    private static BigInteger encryptBlock(int block, BigInteger e, BigInteger n) {
        BigInteger M = BigInteger.valueOf(block);


        return M.modPow(e,n);
    }

    private static BigInteger decryptBlock(BigInteger C, BigInteger d, BigInteger n) {


        return C.modPow(d, n);
    }

    private static String encrypt(String message, BigInteger e, BigInteger n) {
        String encrypted = "";
        String binaries = "";

        /**
         * convert every number to ascii and then binary representation
         */
        for (int i = 0; i < message.length(); i ++) {
            int ascii = (int) message.charAt(i);

            String binaryString = Integer.toBinaryString(ascii);

            binaryString = zeroPad(binaryString, 8);
            binaries += binaryString;

        }
        List<String> plains = new ArrayList<>();
        int start = 0;
        int end = 16;

        /**
         * split binaries to 16bit blocks
         */
        while (end <= binaries.length()) {
            plains.add(binaries.substring(start, end));
            start += 16;
            end += 16;
        }

        /**
         * if there is a block to be pad
         */
        if (binaries.substring(start).length() >0) {

            // encrypt all except the last two as usual
            for(int i=0; i<plains.size()-1; i++) {
                int block = Integer.parseInt(plains.get(i), 2);
                BigInteger encryption = encryptBlock(block, e, n);

                encrypted += convertToString(zeroPad(encryption.toString(2), 2048));
            }

            /**
             *  encrypt second to last
             */
            String secondToLast = plains.get(plains.size()-1);

            secondToLast = encryptBlock(Integer.parseInt(secondToLast, 2), e, n).toString(2);


            String last = binaries.substring(start);

            /**
             * calculate padding amount
             */
            M = 16 - last.length();

            /**
             * create head and tail parts
             */
            String tail = secondToLast.substring(secondToLast.length()-M);
            String head = secondToLast.substring(0, secondToLast.length()-M);

            tail = zeroPad(tail, 8);

            /**
             * add tail
             */
            last = last + tail;

            /**
             * add both two to encryped string
             */
            encrypted += convertToString(zeroPad(encryptBlock(Integer.parseInt(last, 2), e, n).toString(2), 2048));

            encrypted += convertToString(zeroPad(head, 2048));



        }else {

            /**
             * if there is no need for padding encrypt them block by block
             */
            for(int i=0; i<plains.size(); i++) {
                int block = Integer.parseInt(plains.get(i), 2);
                BigInteger encryption = encryptBlock(block, e, n);

                encrypted +=convertToString( zeroPad(encryption.toString(2), 2048));
            }
        }
        return encrypted;


    }
    private static String decrypt(String encrypted, BigInteger d, BigInteger n)  {
        String output = "";

        /**
         * convert read text to binary string
         */
        encrypted = convertStringToBinary(encrypted);
        List<String> blockList = new ArrayList<>();

        /**
         * split binary text to bigInteger blocks
         */
        for (int i=0; i < encrypted.length()-2047; i+= 2048) {
            blockList.add(encrypted.substring(i, i+2048));
        }
        if (M == 0) {

            /**
             * if there is no padding decrypt every block and convert block to two chars
             */
            for(String block:blockList) {

                BigInteger blockInt = decryptBlock(new BigInteger(block,2), d, n);
                String binary = blockInt.toString(2);
                String binary1 = binary.substring(0,binary.length()/2);
                String binary2 = binary.substring(binary.length()/2,binary.length());
                int block1 = Integer.parseInt(binary1, 2);
                int block2 = Integer.parseInt(binary2, 2);
                output += (char)block1;
                output += (char)block2;
            }
        }else{

            /**
             * if there is padding convert every block except last two
             */
            for (int i = 0; i<blockList.size()-2; i++) {
                BigInteger blockInt = decryptBlock(new BigInteger(blockList.get(i), 2), d, n);
                String binary = blockInt.toString(2);
                String binary1 = binary.substring(0,binary.length()/2);
                String binary2 = binary.substring(binary.length()/2,binary.length());
                int block1 = Integer.parseInt(binary1, 2);
                int block2 = Integer.parseInt(binary2, 2);
                output += (char)block1;
                output += (char)block2;
            }

            /**
             * decrypt padded block
             */
            String Dn = decryptBlock(new BigInteger(blockList.get(blockList.size()-2), 2), d, n).toString(2);

            String Cn = blockList.get(blockList.size()-1);

            /**
             * find head & tail
             */
            String en1 = Cn + Dn.substring(Dn.length()-M);

            String Pn = Dn.substring(0,Dn.length()-M);
            String Pn1 = decryptBlock(new BigInteger(en1, 2),d,n).toString(2);

            /**
             * head & tail is 16bit, there are two chars
             */
            String binary1 = Pn1.substring(0,Pn1.length()/2);
            String binary2 = Pn1.substring(Pn1.length()/2,Pn1.length());
            int block1 = Integer.parseInt(binary1, 2);
            int block2 = Integer.parseInt(binary2, 2);
            output += (char)block1;
            output += (char)block2;

            /**
             * the last one that was padded
             */
            output += (char) Integer.parseInt(Pn, 2);
            M = 0;

        }



        return output;

    }


    private static BigInteger getRandomFermatBase(BigInteger n)
    {
        /**
         * generate random int until its between 1 and n
         */
        while (true)
        {
            final BigInteger a = new BigInteger (n.bitLength(), rand);
            /**
             * 1 <= a < n
             */
            if (BigInteger.ONE.compareTo(a) <= 0 && a.compareTo(n) < 0)
            {
                return a;
            }
        }
    }

    /**
     * Fermat's prime
     * @param n
     * @param maxIterations
     * @return
     */
    public static boolean checkPrime(BigInteger n, int maxIterations)
    {
        if (n.equals(BigInteger.ONE))
            return false;

        for (int i = 0; i < maxIterations; i++)
        {
            /**
             *  a ^ n-1 mod n == 1 ise prime degil
             */
            BigInteger a = getRandomFermatBase(n);
            a = a.modPow(n.subtract(BigInteger.ONE), n);

            if (!a.equals(BigInteger.ONE))
                return false;
        }

        return true;
    }


    static void generateKeys() {

        /**
         * generate p and q, and check if they are prime
         */
        BigInteger p = BigInteger.probablePrime(BIT_LENGTH, rand);

        while (checkPrime(p, 20) == false) {

            p = BigInteger.probablePrime(BIT_LENGTH, rand);

        }
        BigInteger q = BigInteger.probablePrime(BIT_LENGTH, rand);
        while (checkPrime(q, 20) == false) {

            q = BigInteger.probablePrime(BIT_LENGTH, rand);

        }

        /**
         * generate n, d, e
         */
        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE)
                .multiply(q.subtract(BigInteger.ONE));


        BigInteger e;
        do e = new BigInteger(phi.bitLength(), rand);
        while (e.compareTo(BigInteger.ONE) <= 0
                || e.compareTo(phi) >= 0
                || !e.gcd(phi).equals(BigInteger.ONE));

        BigInteger d = e.modInverse(phi);


        dList.add(d);
        eList.add(e);
        nList.add(n);
    }

    /**
     * zero pad to achieve given length
     * @param binaryString
     * @param K
     * @return
     */
    static String zeroPad(String binaryString, int K) {
        int padNum = K - binaryString.length();

        for (int j = 0; j < padNum; j++) {
            binaryString = "0" + binaryString;
        }

        return binaryString;

    }


    static String convertToString(String binaryStr) {
        String output = "";
        for(int i=0; i<binaryStr.length()-15; i+=16) {
            output += (char) Integer.parseInt(binaryStr.substring(i,i+16),2);
        }
        return output;
    }

    static String convertStringToBinary(String chars) {
        String output = "";

        for (int i = 0; i < chars.length(); i++) {
            output += zeroPad(Integer.toBinaryString((int)chars.charAt(i)),16);

        }
        return output;
    }
}


