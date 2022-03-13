package org.testshift.testcube.branches;

import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeToUml {



     public static Map<String, Integer> codeToUml(String code, int startLineNumber){
        int branchNum = 0;
        String[] lines = code.split("\n");
//        int[] instrumented = new int[lines.length];
        branchNum += instrumentIF(lines, startLineNumber);
        branchNum += instrumentDoWhile(lines, startLineNumber);
        branchNum += instrumentWhile(lines, startLineNumber);
        branchNum += instrumentSwitch(lines, startLineNumber);
        branchNum += instrumentFor(lines, startLineNumber);
        branchNum += instrumentTriple(lines, startLineNumber);
        instrumentNormal(lines, startLineNumber);


        String output = "@startuml\n" +
                        "\n" +
                        "start\n";

        for(String line: lines){
            output = output+line+"\n";
        }

        output = output + "stop\n" +
                 "\n" +
                 "@enduml";

        String[] instrumentedLines = output.split("\n");
        String result = "";
        for(String line : instrumentedLines){
            if(!nonline(line)){
//                line = line.replaceAll("\\}", "");
//                line = line.replaceAll("\\{", "");
                result = result + line +"\n";
            }
        }
        return Map.of(result, branchNum);
    }

    public static class Block{
        private String identifier;
        private int branchLine;
        private int endLine;
        private char endMark;

        Block(String identifier, int branchLine, int endLine, char endMark){
            this.identifier = identifier;
            this.branchLine = branchLine;
            this.endLine = endLine;
            this.endMark = endMark;
        }

        public String getIdentifier() {
            return identifier;
        }

        public int getBranchLine() {
            return branchLine;
        }

        public int getEndLine() {
            return endLine;
        }

        public char getEndMark() {
            return endMark;
        }
    }

    public static int instrumentWhile(String[] lines, int startLineNumber){
        int branchNum = 0;
        for(int i=0; i<lines.length; i++){
            if(lines[i].contains("while") && !lines[i].contains("endwhile") && !lines[i].contains("repeat while")){
                branchNum += 1;
//                instrumented[i] = 1;
                lines[i] = lines[i].replace("while", "\nwhile");
                int endBracket = findEndBracket(lines, i, lines[i].indexOf("while")+5);
                char endMark = ' ';
                if(endBracket!=-1){
                    endMark = findEndMark(lines,endBracket+1, i);
                    StringBuilder stringBuilder = new StringBuilder(lines[i]);
                    stringBuilder.insert(endBracket+1, "is ("+(i+startLineNumber)+": True and enter loop)\n");
                    lines[i] = stringBuilder.toString();
                }
                if(endMark!=' '){
                    int endLine = findEndline(endMark, lines, i);
                    StringBuilder stringBuilder = new StringBuilder(lines[endLine]);
                    stringBuilder.insert(lines[endLine].indexOf(endMark)+1, "\nendwhile ("+(i+startLineNumber) +": " +
                                                                            "False and end loop)\n");
                    lines[endLine] = stringBuilder.toString();
                }
            }
        }
        return branchNum;
    }

    public static int instrumentDoWhile(String[] lines, int startLineNumber){
        int branchNum = 0;
        for(int i=0; i<lines.length; i++){
            if(lines[i].contains("do") && (lines[i].indexOf("do")+2<lines[i].length() && lines[i].charAt(lines[i].indexOf("do")+2)!='u')){
                branchNum += 1;
                lines[i] = lines[i].replace("do", "\nrepeat\n:"+(startLineNumber+i)+": ");
                for(int row = i; row< lines.length; row++){
                    if(lines[row].contains("while")){
                        lines[row] = lines[row].replace("while", "\nrepeat while");
                        int endBracket = findEndBracket(lines,row, lines[row].indexOf("while")+5);
                        StringBuilder stringBuilder = new StringBuilder(lines[row]);
                        stringBuilder.insert(endBracket+1,
                                             "is(" + (startLineNumber+row) + ": True and continue do) not(" + (startLineNumber+row) + ": False and end loop)\n");
                        lines[row] = stringBuilder.toString();
                        break;
                    }
                }
            }
        }
        return branchNum;
    }

    public static int instrumentFor(String[] lines, int starLineNumber){
        int branchNum = 0;
        for(int i=0; i<lines.length; i++){
            if(lines[i].contains("for") && Text(lines[i]).stream().filter(str -> str.contains("for")).findAny().isEmpty()){
                branchNum += 1;
                int endBracket = findEndBracket(lines, i, lines[i].indexOf("for")+3);
                char endMark = findEndMark(lines, endBracket+1, i);
                int endLine = findEndline(endMark, lines, i);
                StringBuilder stringBuilder = new StringBuilder(lines[i]);
                stringBuilder.insert(endBracket+1, "is ("+(starLineNumber+i)+": True / enter loop)\n");
                lines[i] = stringBuilder.toString();
                lines[i] = lines[i].replace("for", "\nwhile");
                stringBuilder = new StringBuilder(lines[endLine]);
                stringBuilder.insert(lines[endLine].indexOf(endMark)+1, "\nendwhile ("+(i+starLineNumber)+": False " +
                                                                        "/ end loop)\n");
                lines[endLine] = stringBuilder.toString();
            }
        }
        return branchNum;
    }

    public static int instrumentTriple(String[] lines, int startLineNumber){
        int branchNum = 0;
        for(int i=0; i<lines.length; i++){
            if(lines[i].contains("=") && lines[i].contains("?") && lines[i].contains(":")){
                branchNum += 1;
                int endIndex = lines[i].substring(lines[i].indexOf(":")).indexOf(";") + lines[i].indexOf(":");
                lines[i] = new StringBuilder(lines[i]).insert(endIndex, ";\nendif\n").toString();
                lines[i] = lines[i].replace(":",
                                            ";\nelse (" + (startLineNumber+i) + ": False)\n:" + (startLineNumber+i) + ": ");
                lines[i] = lines[i].replaceFirst("=", "=; \nif(").replace("?", ") then (" + (startLineNumber+i) +": " +
                                                                               "True)\n:" + (startLineNumber+i) + ": ");
            }
        }
        return branchNum;
    }

    public static int instrumentSwitch(String[] lines, int startLineNumber){
        int branchNum = 0;
        for(int i = 0; i<lines.length; i++){
            if(lines[i].contains("switch") && !lines[i].contains("endswitch")){
//                instrumented[i] = 1;
                int endLine;
                lines[i] = lines[i].replace("switch", "\nswitch");
                if(lines[i].substring(lines[i].indexOf("switch")).contains("{")) {
                    endLine = findEndline('}', lines, i);
                }
                else{
                    endLine = findEndline('}', lines, i+1);
                }
                StringBuilder stringBuilder = new StringBuilder(lines[endLine]);
                stringBuilder.insert(lines[endLine].indexOf("}")+1, "\nendswitch\n");
                lines[endLine] = stringBuilder.toString();
            }
            if(lines[i].contains(("case"))){
                branchNum += 1;
//                instrumented[i]=1;
                lines[i] = lines[i].replace("case", "\ncase").replace(":", " is True)");
                StringBuilder stringBuilder = new StringBuilder(lines[i]);
                stringBuilder.insert(stringBuilder.indexOf("case")+4, "("+(startLineNumber+i)+": case ");
                lines[i] = stringBuilder.toString();
            }
            if(lines[i].contains("default")){
                branchNum += 1;
//                instrumented[i] = 1;
                lines[i] = lines[i].replace(":", "").replace("default", "\ncase(" +(startLineNumber+i)+": case default is True)\n");
            }
        }
        return branchNum;
    }

    public static void instrumentNormal(String[] lines, int startLineNumber){
        for(int i = 0 ;i <lines.length; i++){
//            if(instrumented[i]==0){
            lines[i] = ":" + (i+startLineNumber) + ": " + lines[i];
//            }
        }
    }



    public static int instrumentIF(String[] lines, int startLineNumber){
        Stack<Block> blocks = new Stack<>();
        int branchNum = 0;
        //process lines
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].strip().replaceAll("\\/\\/[^\\n]*", "\n");
            if (lines[i].contains("if")) {
//                instrumented[i] = 1;
                branchNum+=1;
                char endMark = ' ';
                int endLine=-1;
                // add then(true) to the bracket
                int index = lines[i].indexOf("if");
                int endBracket = findEndBracket(lines, i, index+2);
                if(endBracket!=-1){
                    endMark = findEndMark(lines, endBracket+1, i);
                    // and then (true) and start a new line for following content
                    StringBuilder stringBuilder = new StringBuilder(lines[i]);
                    stringBuilder.insert(endBracket+1,
                                         "then ("+ (startLineNumber+i)+": True)\n:"+(startLineNumber+i)+": ");
                    lines[i] = stringBuilder.toString();
                }
                endLine = findEndline(endMark, lines, i);

                // record if block
                if(lines[i].contains("else if")){
                    // we need every else to identify branch
                    int branchline = blocks.peek().getBranchLine();
                    lines[i] = lines[i].replaceFirst("else", "\nelse ("+ (branchline+startLineNumber) +": False)\n");
                    blocks.push(new Block("elseif", i, endLine, endMark));
                }
                else {
                    //encounter new if, add end if to previous if and delete related blobks in stack
                    if(!blocks.isEmpty() && blocks.peek().getEndLine()<=i) {
                        Block lastEndBlock = blocks.pop();
                        int lastEndLine = lastEndBlock.getEndLine();
                        StringBuilder stringBuilder = new StringBuilder(lines[lastEndLine]);
                        int endColumn = lines[lastEndLine].substring(lines[lastEndLine].indexOf('{')+1).indexOf(lastEndBlock.getEndMark()) + lines[lastEndLine].indexOf('{')+1;
                        if(lastEndBlock.getIdentifier().equals("else")) {
                            stringBuilder.insert(endColumn + 1, "\nendif\n");
                        }
                        else{
                            int branchline = lastEndBlock.getBranchLine();
                            stringBuilder.insert(endColumn + 1, "\nelse ("+ (branchline+startLineNumber) +": False)" +
                                                                "\nendif\n");
                            blocks.push(lastEndBlock);
                        }
//                        lines[lastEndBlock.getEndLine()] = stringBuilder.toString();
                        while (!blocks.isEmpty() && !(blocks.peek().getIdentifier().equals("if"))) {
                            blocks.pop();
                            stringBuilder.insert(stringBuilder.lastIndexOf("endif\n")+6, "endif\n");
                        }
                        blocks.pop();
                        lines[lastEndBlock.getEndLine()] = stringBuilder.toString();
                    }
                    lines[i] = lines[i].replace("if", "\nif");
                    blocks.push(new Block("if", i, endLine, endMark));
                }
            } else if (lines[i].contains("else")) {
//                instrumented[i] =1;
                char endMark = findEndMark(lines, lines[i].indexOf("else")+4, i);
                int endLine = findEndline('}', lines, i);
                int branchline = blocks.peek().getBranchLine();
                lines[i] = lines[i].replaceFirst("else",
                                                 "\nelse ("+ (branchline+startLineNumber) +": False)\n:"+(startLineNumber+branchline)+": ");
                blocks.push(new Block("else", i, endLine, endMark));
            }
        }

        while(!blocks.isEmpty()){
            Block lastEndBlock = blocks.pop();
            int lastEndLine = lastEndBlock.getEndLine();
            StringBuilder stringBuilder = new StringBuilder(lines[lastEndLine]);
            int endColumn = lines[lastEndLine].substring(lines[lastEndLine].indexOf('{')+1).indexOf(lastEndBlock.getEndMark()) + lines[lastEndLine].indexOf('{')+1;
            if(lastEndBlock.getIdentifier().equals("else")) {
                stringBuilder.insert(endColumn + 1, "\nendif\n");
            }
            else{
                int branchline = lastEndBlock.getBranchLine();
                stringBuilder.insert(endColumn + 1, "\nelse ("+ (branchline+startLineNumber) +": False)\nendif\n");
                blocks.push(lastEndBlock);
            }
//                        lines[lastEndBlock.getEndLine()] = stringBuilder.toString();
            while (!blocks.isEmpty() && !(blocks.peek().getIdentifier().equals("if"))) {
                blocks.pop();
                stringBuilder.insert(stringBuilder.lastIndexOf("endif\n")+6, "\nendif\n");
            }
            blocks.pop();
            lines[lastEndBlock.getEndLine()] = stringBuilder.toString();
        }
        return branchNum;
    }

    private static char findEndMark(String[] lines, int startIndex, int startLine){
        for(int i = startIndex; i<lines[startLine].length(); i++){
            if(lines[startLine].charAt(i)!=' ' && lines[startLine].charAt(i)!='\n'){
                if(lines[startLine].charAt(i)=='{'){
                    return '{';
                }
            }
        }
        if(startLine+1 < lines.length) {
            for (int i = 0; i < lines[startLine + 1].length(); i++) {
                if (lines[startLine + 1].charAt(i) != ' ' && lines[startLine + 1].charAt(i) != '\n') {
                    if (lines[startLine + 1].charAt(i) == '{') {
                        return '{';
                    }
                }
            }
        }
        return ';';
    }

    private static int findEndBracket(String[] lines, int lineNumber, int startIndex){
        Stack<Character> brackets = new Stack<>();
        for(int start = startIndex; start<lines[lineNumber].length(); start++){
            if(lines[lineNumber].charAt(start)=='('){
                brackets.push(')');
            }
            else if(lines[lineNumber].charAt(start)==')'){
                //the location of the back bracket
                if(brackets.size()==1){
                    return start;
                }
                brackets.pop();
            }
        }
        return -1;
    }


    // find a block's endline according to } or ;
    private static int findEndline(char endMark, String[] lines, int lineNumber){
        int endLine = -1;
        if(endMark==';'){
            for(int row = lineNumber; row<lines.length; row++){
                if(lines[row].contains(";")){
                    endLine = row;
                    return endLine;
                }
            }
        }
        else{
            Stack<Character> braces = new Stack<>();
            int index = lines[lineNumber].indexOf("{");
            braces.push('}');
            for(int start = index+1; start<lines[lineNumber].length(); start++){
                if(lines[lineNumber].charAt(start)=='{'){
                    braces.push('}');
                }
                else if(lines[lineNumber].charAt(start)=='}'){
                    if(braces.size()==1){
                        // and then (true) and start a new line for following content
                        return lineNumber;
                    }
                    braces.pop();
                }
            }
            for(int row = lineNumber+1; row<lines.length; row++){
                if(lines[row].contains("{") || lines[row].contains("}")) {
                    for (index = 0; index < lines[row].length(); index++) {
                        if (lines[row].charAt(index) == '{') {
                            braces.push('}');
                        } else if (lines[row].charAt(index) == '}') {
                            if (braces.size() == 1) {
                                // and then (true) and start a new line for following content
                                return row;
                            }
                            braces.pop();
                        }
                    }
                }
            }
        }

        return lineNumber;
    }

    private static List<String> Text(String line){
        Pattern p1=Pattern.compile("\"(.*?)\"");

        Matcher m = p1.matcher(line);

        ArrayList<String> list = new ArrayList<String>();
        while (m.find()) {
            list.add(m.group().trim().replace("\"","")+" ");
        }
        return  list;
    }

    private static boolean nonline(String line){
        if(line.equals("")){
            return true;
        }
        if(line.charAt(0) == ':'){
            String[]  tmps = line.split(":");
            if(tmps[tmps.length-1].replaceAll("\\{", "").replaceAll("\\}", "").replaceAll(":", "").replaceAll(";","").replaceAll("\n", "").strip().equals("")){
                return true;
            }
        }
        else{
            if(line.replaceAll("\\{", "").replaceAll("\\}", "").replaceAll(":", "").replaceAll(";","").replaceAll("\n", "").strip().equals("")){
                return true;
            }
        }
        return false;
    }
}
