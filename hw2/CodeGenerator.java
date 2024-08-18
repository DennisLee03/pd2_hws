import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.Map;

class FileReader {
    public String read(String fileName) {
        try {
            return (Files.readString(Paths.get(fileName)));
        } catch (IOException e) {
            System.err.println("Failed to read the file " + fileName);
            e.printStackTrace();
            return "failed to read";
        }
    }
}//done

class ToolKit {
    public static void splitByClass(String text, 
                 Map<String, List<String>> classNameToMembers) {
        //need to match 2 conponent : the signature and its content
        String classSignature = "(class)\\s+([\\w&&\\S]+)";
        Pattern signaturePattern = Pattern.compile(classSignature);
        Matcher signatureMatcher = signaturePattern.matcher(text);

        //Map<ClassName, information> : List contains information and ClassName maps the information
        int sIndex = 0;
        //outter loop for search "class ClassName"
        while (signatureMatcher.find(sIndex)) {
            String sigBuffer = String.format("public %s %s {", signatureMatcher.group(1), signatureMatcher.group(2));
            String className = signatureMatcher.group(2);
            List<String> substringContainer = new ArrayList<>();

            //TODO challenge point
            //transform type_{} into type_:
            substringContainer.add(sigBuffer);
            String statementForm = String.format("%s\\s*:\\s*(.+)(\\[\\])*",className);// name:member
            Pattern contentPattern = Pattern.compile(statementForm);
            Matcher contentMatcher = contentPattern.matcher(text);
            int cIndex = 0;
            
            //inner loop for search the members of the class
            while(contentMatcher.find(cIndex)) {
                String conBuffer = contentMatcher.group(1);
                //String scBuffer = scopeMatcher.group(1);
                if(conBuffer.indexOf("(") != -1) {
                    conBuffer = methodSolve(conBuffer);
                } 
                else {
                    conBuffer = attributeSolve(conBuffer);
                }
                substringContainer.add(conBuffer);
                cIndex = contentMatcher.end();
            }
            substringContainer.add("}");
            //

            sIndex = signatureMatcher.end();
            classNameToMembers.put(className, substringContainer);
        }
    }

    public static void fileWrite(Map<String, List<String>> classNameToMember) {
        try {
            for (String className : classNameToMember.keySet()) {
                String output = className + ".java";
                List<String> content = classNameToMember.get(className);
                
                File file = new File(output);
                if (!file.exists()) {
                    file.createNewFile();
                }
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    for (String line : content) {
                        bw.write(line);
                        bw.newLine();
                    }
                }
                //System.out.println("Java class has been generated: " + output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String wholeTextTransform(String text) {
        String result = text;
        String scopeForm = "class\\s+(\\w+)\\s*\\{([^\\}]*)\\}";//somthing wrong here
        Pattern scPattern = Pattern.compile(scopeForm);
        Matcher scMatcher = scPattern.matcher(text);
        int index = 0;
        while (scMatcher.find(index)) {
            String className = scMatcher.group(1);
            String classMembers = "class " + className;
            classMembers += scMatcher.group(2);           
            classMembers = classMembers.replaceAll("\\+(\\w+)", className + " : +$1");
            classMembers = classMembers.replaceAll("\\-(\\w+)", className + " : -$1");
            result = result.replace(scMatcher.group(), classMembers);
            index = scMatcher.end();
        }
        return result;
    }

    private static String methodSolve(String mMethod) {
        //method = +setSize(int size) void
        //(+ or -)\\s*(method name)\\s*(\\()\\s*[(type)\\s*(parameterName)]+
        String methodSignature = "(\\+|\\-)\\s*(\\w+)\\s*(\\(.*\\))\\s*(void|\\w*)";
        Pattern mPattern = Pattern.compile(methodSignature);
        Matcher mMatcher = mPattern.matcher(mMethod);
        String temp = "    ";
        if(mMatcher.find()) {
            temp += (mMatcher.group(1)).equals("+") ? "public" : "private";
            temp += " ";
            String returnType = "";
            if(mMatcher.group(4).equals("")) {
                temp += "void";
                //returnType = "void";
            }
            else {
                returnType = mMatcher.group(4);
                temp += mMatcher.group(4);
            }
            temp += " ";

            //
            if((mMatcher.group(2)).indexOf("get") != -1 || (mMatcher.group(2)).indexOf("set") != -1) {
                temp += solveSetOrGet(mMatcher.group(2), tidyUpPara(mMatcher.group(3)));
                //parameters
                //return statement
            }
            else {
                temp += mMatcher.group(2);//method name
                temp += tidyUpPara(mMatcher.group(3));//parameters
                temp += otherMethod(returnType);//return statement
            }
        }
        return temp;
    }

    private static String attributeSolve(String mAttribute) {
        //attribute = -String name
        String attributeSignature = "(\\-|\\+)\\s*(\\w+(?:\\[\\])*)\\s*(\\w+)";
        Pattern aPattern = Pattern.compile(attributeSignature);
        Matcher aMatcher = aPattern.matcher(mAttribute);
        String temp = "    ";
        if(aMatcher.find()) {
            temp += (aMatcher.group(1)).equals("+") ? "public" : "private";
            temp += " ";  
            temp += aMatcher.group(2);
            temp += " ";
            temp += aMatcher.group(3);
            temp += ";";                                                                                                                                                                                                                                                                                                                                                                                                                                              
        }
        return temp;
    }

    private static String tidyUpPara(String x) {
        String parameterSignature = "\\s*(\\w+)\\s+(\\w+)\\s*";

        Pattern pattern = Pattern.compile(parameterSignature);
        Matcher matcher = pattern.matcher(x);

        StringBuilder result = new StringBuilder("(");
        int index = 0;
        while (matcher.find(index)) {
            String type = matcher.group(1);
            String name = matcher.group(2);
            result.append(type).append(" ").append(name).append(", ");
            index = matcher.end();
        }
        if (result.length() > 1) {
            // Remove the last comma and space
            result.setLength(result.length() - 2);
        }
        result.append(")");
        return result.toString();
    }

    private static String solveSetOrGet(String methodName, String para) {
        String result = "";
        int setOrGet = methodName.indexOf("get");
        String temp = methodName.substring(3);//temp = NameOfSome
        String attribute = Character.toLowerCase(temp.charAt(0)) + temp.substring(1);

        //(methodName.substring(3)).toLowerCase();
        if(setOrGet != -1) { //getter
            result += methodName + para + " {\n";//"getAttr(parameters) {"
            result += "        return ";
            result += attribute;
        } 
        else { //setter
            result += methodName + para + " {\n";//"setAttr(parameters) {"
            result += "        this." + attribute;
            result += " = " + attribute;
        }
        result += ";\n    }";
        return result;
    }

    private static String otherMethod(String returnType) {
        String result = "";
        /*
         * String -> ""
         * int -> 0
         * boolean -> false
        */
        if(returnType.equals("String")) {
            result = " {return \"\";}";
        }
        else if(returnType.equals("int")) {
            result = " {return 0;}";
        }
        else if(returnType.equals("boolean")) {
            result = " {return false;}";
        }
        else {
            result = " {;}";
        }
        return result;
    }
}

public class CodeGenerator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("請輸入檔案名稱");
            return;
        }
        String fileName = args[0];
        //System.out.println("File name: " + fileName);
        String mermaidText = "";

        //read input
        FileReader mermaidCodeReader = new FileReader();
        mermaidText = mermaidCodeReader.read(fileName);
        Map<String, List<String>> classNameToMembers = new HashMap<>();

        //something wrong here
        mermaidText = ToolKit.wholeTextTransform(mermaidText);
        //something wrong here

        System.out.println(mermaidText);
        ToolKit.splitByClass(mermaidText, classNameToMembers);
        
        //write
        ToolKit.fileWrite(classNameToMembers);
    }
}