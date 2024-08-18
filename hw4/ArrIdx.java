import java.text.DecimalFormat;

public class ArrIdx {
    public static void main(String[] args) {
        double x = 111113.1415926d;
        DecimalFormat df = new DecimalFormat("#.######");
        x = Double.parseDouble(df.format(x));
        System.out.println(x);
    }
}
