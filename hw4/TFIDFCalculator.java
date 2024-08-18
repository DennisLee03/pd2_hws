import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TFIDFCalculator {
    public static final String DESTINATION = "output.txt";

    public static double[] idfs(String[] terms, List<Trie> docs, int size) {
        double[] idfs = new double[terms.length];
        List<Double> valBuffer = new ArrayList<>();
        List<String> existedTerms = new ArrayList<>();

        for (int i = 0; i < terms.length; i++) {
            int num = 0;

            if (!existedTerms.contains(terms[i])) {
                for (Trie each : docs) {
                    if (each.search(terms[i]) != 0) {
                        num++;
                    }
                }
                existedTerms.add(terms[i]);
                valBuffer.add(Math.log((double) size / num));
            }

            idfs[i] = valBuffer.get(existedTerms.indexOf(terms[i])).doubleValue();
        }

        return idfs;
    }

    public static double tfidf(String t, Trie doc, double idfValues, int size) {
        int frequency = doc.search(t);
        int total = doc.totalTerm;

        // System.out.println(frequency + "\n" + total + "\n" + idfValues +
        // "\n---------------------");

        double result = ((double) frequency / total) * idfValues;

        return result;
    }

    public static List<Trie> read_and_form_docs(String path) {
        List<Trie> docs = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;

            int rowCount = 0;
            String[] termsBuffer = null;
            Trie trieBuffer = new Trie();
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("[^a-zA-z]", " ").toLowerCase();
                line = line.replaceAll("`", " ");
                line = line.replaceAll("\\s+", " ");
                line = line.substring(1);

                termsBuffer = line.split("\\s");

                for (String t : termsBuffer) {
                    trieBuffer.insert(t);
                }

                if ((rowCount + 1) % 5 == 0) {
                    docs.add(trieBuffer);
                    trieBuffer = new Trie();
                }

                rowCount++;
            }

            br.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return docs;
    }

    public static String[][] readTestCase(String tc) {
        String[][] input = new String[2][];
        try {
            BufferedReader br = new BufferedReader(new FileReader(tc));
            String line;
            int rowCount = 0;
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("[^a-zA-z0-9]", " ").toLowerCase();
                line = line.replaceAll("`", " ");
                line = line.replaceAll("\\s+", " ");
                input[rowCount] = line.split("\\s"); // input[0] is terms, input[1] is selected docs
                rowCount++;
            }

            br.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return input;
    }

    public static void writeOutput(String fileName, String line) {
        try {

            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(line);
                bw.close();
            } catch (IOException e) {
                System.out.println("failed to write.");
            }

        } catch (IOException e) {
            System.out.println("failed to create a file.");
        }
    }

    public static void main(String[] args) {
        // setup Trie
        String corpusPath = args[0];
        List<Trie> docs = read_and_form_docs(corpusPath);
        int size = docs.size();

        // read test case
        String test_case = args[1];
        String[][] input = readTestCase(test_case);
        String[] terms = input[0];
        int[] n = new int[input[1].length];
        for (int i = 0; i < input[1].length; i++) {
            n[i] = Integer.parseInt(input[1][i]);
        }

        // pre-compute idf
        double[] idfValues = idfs(terms, docs, size);

        double start = System.currentTimeMillis();
        // calculate TF-IDF values
        StringBuilder result = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.00000");
        for (int i = 0; i < n.length; i++) {

            double value = tfidf(terms[i], docs.get(n[i]), idfValues[i], size);

            result.append(df.format(value));
            if (i < (n.length - 1)) {
                result.append(" ");
            }
        }

        double end = System.currentTimeMillis();

        double calculateTime = (end - start) / 1000.0d;

        // write the result into output.txt
        writeOutput(DESTINATION, result.toString());

        System.out.println("calculate time : " + calculateTime + " s");
    }
}

class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
    int wordCount = 0;
}

class Trie {
    TrieNode root = new TrieNode();
    int totalTerm = 0;

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null) {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }
        node.isEndOfWord = true;
        node.wordCount += 1;
        totalTerm += 1;
    }

    public int search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return 0;
            }
        }
        return node.wordCount;
    }
}