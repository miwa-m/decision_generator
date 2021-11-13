package desicion_generator;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import desicion_generator.utility.SyntaxAnalyzeUtility;

public class verify {
    public static void main(String[] args) {
       System.out.println(
               optimizeTokenFromLeft("((a == b)"));
       System.out.println(
               optimizeTokenFromLeft("(((a() == b()"));
       System.out.println(
               optimizeTokenFromRight("(a() == b())))))"));
       System.out.println(
               optimizeTokenFromRight("a() == b()))"));
    }
    
    public static String optimizeTokenFromLeft(String content) {
        Matcher mS = java.util.regex.Pattern.compile("^\\(+").matcher(content);
        Matcher mE = java.util.regex.Pattern.compile("\\)+$").matcher(content);
        int sBLen = StringUtils.countMatches(content, "(");
        int eBLen = StringUtils.countMatches(content, ")");
        int sCBLen = mS.find() ? mS.group().length() : 0;
        int eCBLen = mE.find() ? mE.group().length() : 0;
        
        int rightSize = sBLen - sCBLen;
        while(StringUtils.countMatches(content, ")") > rightSize) {
            content = content.replaceFirst("^\\(", "");
            content = content.replaceFirst("\\)$", "");
        }
        return content;
    }

    public static String optimizeTokenFromRight(String content) {
        Matcher mS = java.util.regex.Pattern.compile("^\\(+").matcher(content);
        Matcher mE = java.util.regex.Pattern.compile("\\)+$").matcher(content);
        int sBLen = StringUtils.countMatches(content, "(");
        int eBLen = StringUtils.countMatches(content, ")");
        int sCBLen = mS.find() ? mS.group().length() : 0;
        int eCBLen = mE.find() ? mE.group().length() : 0;
        
        int leftSize = eBLen - eCBLen + 1;
        while(StringUtils.countMatches(content, "(") > leftSize) {
            content = content.replaceFirst("^\\(", "");
            content = content.replaceFirst("\\)$", "");
        }
        return content;
    }
    
    public static void analyzeRoop(String content, int l) {
        while (true) {
            String block = analyzeLevel(content);
            if (StringUtils.isEmpty(block)) {
                System.out.println(l + " : " + content + "");
                System.out.println();
                break;
            }
            analyzeRoop(block.replaceAll("(^\\(|\\)$)", ""), l + 1);
            content = content.replaceFirst(
                    SyntaxAnalyzeUtility.transEscaped(block),
                    "");
        }    
    }
    
    
    public static String analyzeLevel(String code) {
        boolean findBlacketStart = false;
        int startIdx = -1;
        int blockStartCount = 0;
        int blockEndCount = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '(') {
                blockStartCount++;
                if (startIdx < 0) {
                    startIdx = i;
                }
            } else if (c == ')') {
                blockEndCount++;
            }
            if (startIdx >= 0
                    && blockStartCount == blockEndCount) {
                return code.substring(startIdx, i + 1);
            }
        }
        return "";
    }
}
