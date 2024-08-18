import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.io.IOException;
import java.nio.file.*;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParser {
    public static final String DESTINATION = "output.csv";
    public static final String SOURCE = "data.csv";

    private static String setupNameStr(Elements elements) {
        StringBuilder sb = new StringBuilder();
        for (Element element : elements) {
            sb.append(element.text().toString() + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static String setupDataStr(Elements elements) {
        StringBuilder sb = new StringBuilder();
        for (Element element : elements) {
            double data = Double.parseDouble(element.text());
            String dataStr = String.format("%.2f", data);
            dataStr = checkRedundantZero(dataStr);
            sb.append(dataStr + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static void wrtie(String fileName, boolean writeIntoFile, StringBuilder line) {
        File file = new File(fileName);
        if (!file.exists() && line.charAt(0) == '\n') {
            line.deleteCharAt(0);
        }
        if (writeIntoFile) {
            try (FileWriter writer = new FileWriter(fileName, true)) {
                line.deleteCharAt(line.length() - 1);
                writer.write(line.toString());
                writer.flush();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void crawler(String url) {
        try {
            // connect to the website and get the whole html text
            Document doc = Jsoup.connect(url).get();

            // read the datas
            Elements datas = doc.select("td");

            String fileName = SOURCE;
            File outputFile = new File(fileName);

            if (outputFile.exists() && outputFile.length() > 0) {
                String datasString = setupDataStr(datas);
                StringBuilder sb = new StringBuilder();
                sb.append("\n" + datasString);
                wrtie(fileName, true, sb);
            } else {// not exist or length = 0
                Elements names = doc.select("th");
                String namesStr = setupNameStr(names);
                String datasString = setupDataStr(datas);
                StringBuilder sb = new StringBuilder();
                sb.append(namesStr + "\n" + datasString);
                wrtie(fileName, true, sb);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void toOutput(StringBuilder sb) {
        if (sb == null) {
            Path filePath = Paths.get(SOURCE);
            try {
                String content = Files.readString(filePath);
                StringBuilder lines = new StringBuilder();
                lines.append(content);
                lines.append(",");
                wrtie(DESTINATION, true, lines);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {

        }
    }

    private static void selectData(String stockName, int start, int end, List<Double> selectedDatas) {
        try (BufferedReader reader = new BufferedReader(new FileReader(SOURCE))) {
            String nameLine = reader.readLine();
            if (nameLine != null) {
                String[] headers = nameLine.split(",");
                int index = -1;
                // get where the stock is.
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].equals(stockName)) {
                        index = i;
                        break;
                    }
                }

                if (index != -1) {
                    // according to index, start, end to select the datas
                    int day = 1;
                    String dataLine;
                    while ((dataLine = reader.readLine()) != null) {
                        if (start <= day && end >= day) {
                            String[] datas = dataLine.split(",");
                            selectedDatas.add(Double.parseDouble(datas[index]));
                        }
                        if (day > end) {
                            break;
                        }
                        day++;
                    }
                } else {
                    System.out.println("stock not found");
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    // mathematic functions
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

    private static String checkRedundantZero(String num) {
        if (num.endsWith(".00")) {
            return num.substring(0, num.length() - 3);
        }

        if (num.endsWith(".0")) {
            return num.substring(0, num.length() - 2);
        }

        Pattern p = Pattern.compile("\\.\\d0$");
        Matcher m = p.matcher(num);
        if (m.find()) {
            return num.substring(0, num.length() - 1);
        }

        return num;
    }

    public static void simpleMovAvg(String stockName, int start, int end) {
        List<Double> selectedDatas = new ArrayList<>();
        selectData(stockName, start, end, selectedDatas);
        int count = (end - start + 1) - 5 + 1;// window = 5
        StringBuilder lines = new StringBuilder();
        lines.append("\n" + stockName + "," + start + "," + end + "\n");
        for (int i = 0; i < count; i++) {
            double sma = 0;
            for (int j = 0; j < 5; j++) {
                sma += selectedDatas.get(i + j);
            }
            sma /= 5.0;
            String temp = String.format("%.2f", sma);
            temp = checkRedundantZero(temp);
            temp += ",";
            lines.append(temp);
        }
        wrtie(DESTINATION, true, lines);
    }

    private static double averge(List<Double> datas, int window) {
        double result = 0;
        for (double data : datas) {
            result += data;
        }
        result /= window;
        return result;
    }

    public static double stdev(String stockName, int start, int end, boolean write) {
        List<Double> selectedDatas = new ArrayList<>();
        selectData(stockName, start, end, selectedDatas);
        double avg = averge(selectedDatas, selectedDatas.size());
        double sumOfSqOfDiffs = 0.0;
        for (double data : selectedDatas) {
            sumOfSqOfDiffs += (data - avg) * (data - avg);
        }
        double var = sumOfSqOfDiffs / (selectedDatas.size() - 1);
        double stdev = sqrt(var, 0.0000000001);
        StringBuilder sb = new StringBuilder();
        if (write) {
            String dataStr = checkRedundantZero(String.format("%.2f", stdev));
            // System.out.println(dataStr);
            sb.append("\n" + stockName + "," + start + "," + end + "\n" + dataStr + " ");
            wrtie(DESTINATION, write, sb);
        }
        return stdev;
    }

    public static StringBuilder findTop3(Map<String, Double> nameToStdev, int start, int end) {
        PriorityQueue<Double> heap = new PriorityQueue<>(3);

        Map<Double, String> reveseMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : nameToStdev.entrySet()) {
            double value = entry.getValue();
            String key = entry.getKey();
            if (heap.size() < 3 || value > heap.peek()) {
                if (heap.size() == 3) {
                    heap.poll();
                }
                reveseMap.put(value, key);
                heap.offer(value);
            }
        }

        StringBuilder sb = new StringBuilder();
        String[] top3Header = new String[3];
        double[] top3Stdev = new double[3];
        int index = 0;
        while (!heap.isEmpty()) {
            top3Stdev[3 - index - 1] = heap.poll();
            index++;
        }

        sb.append("\n");
        for (int i = 0; i < 3; i++) {
            top3Header[i] = reveseMap.get(top3Stdev[i]);
            sb.append(top3Header[i] + ",");
        }
        sb.append(start + "," + end + "\n");

        for (int i = 0; i < 3; i++) {
            String temp = String.format("%.2f", top3Stdev[i]);
            temp = checkRedundantZero(temp);
            sb.append(temp + ",");
        }

        return sb;
    }

    public static void top3Stdev(String stockName, int start, int end) {
        try (BufferedReader reader = new BufferedReader(new FileReader(SOURCE))) {
            String nameLine = reader.readLine();
            String[] headers = nameLine.split(",");
            int size = headers.length;

            Map<String, Double> headerToStdev = new HashMap<String, Double>();
            for (int i = 0; i < size; i++) {
                double stdev = stdev(headers[i], start, end, false);
                headerToStdev.put(headers[i], stdev);
            }
            StringBuilder result = findTop3(headerToStdev, start, end);
            wrtie(DESTINATION, true, result);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static double intercept(double[] factors) {
        double result = 0.0;

        result = factors[1] - factors[2] * factors[0];

        return result;
    }

    private static double[] slope(List<Double> datas, int start, int end) {
        double t_bar = 0.5 * ((start + end));
        double y_bar = 0.0;
        double dataSum = 0.0;

        double numerator = 0.0;
        double denominator = 0.0;

        double[] result = { 0.0, 0.0, 0.0 };

        result[0] = t_bar;

        // StringBuilder sb = new StringBuilder();

        for (int t = start; t <= end; t++) {
            // sb.append(t + ",");
            dataSum += datas.get(t - 1);
        } // sb.append("\n");
        y_bar = dataSum / (end - start + 1);
        result[1] = y_bar;
        // sb.append("t_bar," + t_bar + "\n");
        // sb.append("y_bar," + y_bar + "\n");

        for (int t = start; t <= end; t++) {
            double y = datas.get(t - 1);
            // sb.append(y + ",");
            numerator += (t - t_bar) * (y - y_bar);
            denominator += (t - t_bar) * (t - t_bar);
        }
        result[2] = numerator / denominator;

        // wrtie(DESTINATION, true, sb);

        return result;
    }

    public static void regressionLine(String stockName, int start, int end) {
        List<Double> datas = new ArrayList<>();
        selectData(stockName, 1, 30, datas);

        double factors[] = slope(datas, start, end);
        double b1 = factors[2];
        double b0 = intercept(factors);

        // System.out.println(b0 + " " + b1);

        StringBuilder sb = new StringBuilder();
        String b1Str = checkRedundantZero(String.format("%.2f", b1));
        String b0Str = checkRedundantZero(String.format("%.2f", b0));
        sb.append("\n" + stockName + "," + start + "," + end + "\n" + b1Str + "," + b0Str + " ");

        wrtie(DESTINATION, true, sb);
    }

    public static void main(String[] args) {
        int mode = Integer.parseInt(args[0]);

        // $ java HtmlParser {mode} {task} {stock} {start} {end}

        // crawler mode
        if (mode == 0) {
            HtmlParser.crawler("https://pd2-hw3.netdb.csie.ncku.edu.tw/");
        }

        File file = new File(DESTINATION);
        // parser mode
        if (mode == 1) {
            int task = Integer.parseInt(args[1]);

            if (task == 0) { // append the content of data.csv to output.csv
                toOutput(null);
            }

            if (task == 1) {// append the SMA of the stock from startDay to endDay to output.csv
                String stockName = args[2];
                int start = Integer.parseInt(args[3]);
                int end = Integer.parseInt(args[4]);
                simpleMovAvg(stockName, start, end);
            }

            if (task == 2) {// append the stdev of the stock from startDay to endDay to output.csv
                String stockName = args[2];
                int start = Integer.parseInt(args[3]);
                int end = Integer.parseInt(args[4]);
                stdev(stockName, start, end, true);
            }

            if (task == 3) {// append the top-3 stdev of stocks from startDay to endDay to output.csv
                String stockName = args[2];
                int start = Integer.parseInt(args[3]);
                int end = Integer.parseInt(args[4]);
                top3Stdev(stockName, start, end);
            }

            if (task == 4) {// append the regression line of the stock from startDay to endDay to output.csv
                String stockName = args[2];
                int start = Integer.parseInt(args[3]);
                int end = Integer.parseInt(args[4]);
                regressionLine(stockName, start, end);
            }
        }
    }
}