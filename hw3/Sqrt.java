import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sqrt { 
    private static String checkRedundantZero(String num) {
        if(num.endsWith(".00")) {
            return num.substring(0, num.length() - 3);
        }

        Pattern p = Pattern.compile("\\.\\d0$");
        Matcher m = p.matcher(num);
        if(m.find()) {
           return num.substring(0, num.length() - 1); 
        }

        return num;
    }
   public static double sqrt(double number, double epsilon) {
        if (number < 0) {
            throw new IllegalArgumentException("Number must be non-negative.");
        }
        if (number == 0) {
            return 0;
        }

        double guess = number / 2.0;
        while (Math.abs(guess * guess - number) > epsilon) {
            guess = (guess + number / guess) / 2.0;
        }

        return guess;
    }
    
    public static void main(String[] args) {
        String str = checkRedundantZero(args[0]);
        System.out.println(str);
    }
}
