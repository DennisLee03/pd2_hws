import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RegExp {

    public static void isPalindrome(String targetStr) {
        //先StringBuilder.reverse().toString()再透過String.equals()去看新字串與舊字串是否相同
        String reversedStr = new StringBuilder(targetStr).reverse().toString();
        if(targetStr.equals(reversedStr)) {
            System.out.print("Y,");
        } else {
            System.out.print("N,");
        }
    }

    public static void containSubstringOrNot(String targetStr, String substring) {
        //String.indexOf()
        if(targetStr.indexOf(substring) != -1) {
            System.out.print("Y,");
        } else {
            System.out.print("N,");
        }
    }

    public static void countSubstring(String targetStr, String substring, int targetCount) {
        //手刻計算substring出現次數，配合indexOf()使用
        //可能還需要寫另一個算次數的method
        int count = 0;
        int strIndex = 0;

        while((strIndex = targetStr.indexOf(substring, strIndex)) != -1) {
            count += 1;
            strIndex += 1;//substring.length();
        }

        if(count >= targetCount) {
            System.out.print("Y,");
        } else {
            System.out.print("N,");
        }
    }

    public static void findAXB2(String targetStr) {
        //TO DO
        //似乎只要a先至找出現一次，然後bb出現，就能夠實作了
        int indexOfA = targetStr.indexOf('a');

        if (indexOfA != -1) {
            int indexOfBB = targetStr.indexOf("bb", indexOfA + 1); // 從上一個'a'的後面開始查找
            if (indexOfBB != -1) {
                System.out.print("Y");
            } else {
                System.out.print("N");
            }
        } else {
            System.out.print("N");
        }

    }

    public static void main(String[] args) {
        String str1 = args[1].toLowerCase();
        String str2 = args[2].toLowerCase();
        int s2Count = Integer.parseInt(args[3]);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            String line;
            while ((line = reader.readLine()) != null) {//每行每行幫你讀檔

                line = line.toLowerCase();
                
                isPalindrome(line);
                containSubstringOrNot(line, str1);
                countSubstring(line, str2, s2Count);
                findAXB2(line);

                System.out.println();
                //System.out.println(line);
            }
            reader.close();

        } catch (IOException e) {
            System.out.println("---------");
            e.printStackTrace();
        }
    }
}

