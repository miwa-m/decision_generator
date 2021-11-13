package desicion_generator.parser.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import desicion_generator.parser.models.Pattern.Bool;
import desicion_generator.utility.DGLogger;
import desicion_generator.utility.PatternConstants;

public class If extends Sentence implements Serializable {
    
    protected Pattern rootPattern;
    
    public Pattern getPattern() {
        return this.rootPattern;
    }
    
    protected String getConditionPart(String conditionContent) {
        conditionContent = conditionContent
                .replaceFirst("[^(]*?\\(", "(")
                .replaceFirst("\\)\s*\\{.*", ")");
        conditionContent = conditionContent
                .replaceAll("(^\\({1}|\\){1}$)", "");
        return conditionContent;
    }
    
    private List<String> splitCondition(String conditionContent) {
        // split "&&" or "||"
        List<String> conditionList = new ArrayList<>();
        StringBuffer elementSb = new StringBuffer();
        
        for (char c : conditionContent.toCharArray()) {
            if (c == '&' || c == '|') {
                //DGLogger.debug(c);
                //DGLogger.debug("now sb : " + elementSb);
                if (elementSb.substring(elementSb.length()-1)
                        .equals(String.valueOf(c))) {
                    String elementCondition = elementSb
                            .substring(0, elementSb.length() - 1).toString();
                    conditionList.add(removeTerminalBracket(elementCondition));
                    conditionList.add(String.valueOf(new char[] {c, c}));
                    elementSb = new StringBuffer();
                    continue;
                }
            }
            elementSb.append(c);
        }
        conditionList.add(removeTerminalBracket(elementSb.toString()));
        return conditionList;
    }
    
    private Pattern analyzeCondition(List<String> conditionList) {
        // analyze condition block
        Pattern rootPattern = new Pattern();
        Pattern pattern = rootPattern;
        Condition condition = null;
        for (int idx = 0; idx < conditionList.size(); idx++) {
            String s = conditionList.get(idx);
            if (isConditionUnit(s)) {
                // is condition unit
                condition = new Condition(s);
                pattern.addToken(condition);
                if (isCompletablePattern(idx, conditionList)) {
                    Pattern tmpPattern = pattern;
                    pattern = new Pattern();
                    pattern.setParentPattern(tmpPattern);
                }
            } else if (s.startsWith("(") || s.startsWith("!{")) { // && !s.endsWith(")")) {
                // generate pattern (means start pattern
                // delete (
                List<Boolean> nestList = countTopBracket(s);
                int notCount = nestList.size();
                for (Boolean b : nestList) {
                    if (b == true) {
                        notCount++;
                    }
                }
                Condition patternPart = new Condition(s.substring(notCount));
                for(int i = 0; i < nestList.size(); i++) {
                    Pattern tmpPattern = new Pattern();
                    if (nestList.get(i) == true) {
                        tmpPattern.setNot();
                    }
                    if (i == nestList.size() - 1) {
                        tmpPattern.addToken(patternPart);
                    }
                    tmpPattern.setParentPattern(pattern);
                    pattern = tmpPattern;
                }
            } else if (s.endsWith(")")) { // && !s.startsWith("(")) {
                // end pattern
                // delete )
                int roopCnt = countBottomBracket(s);
                Condition patternPart = new Condition(s.substring(0, s.length() - roopCnt));
                for (int i = 0; i < roopCnt; i++) {
                    if (i == 0) {
                        pattern.addToken(patternPart);
                    }
                    pattern.getParentPattern().addToken(pattern);
                    pattern = pattern.getParentPattern();
                }
            } else if(isBool(s)) {
                Bool bool = (Bool) DecodableEnum.decode(s, Bool.class);
                pattern.addBool(bool);
            }
        }
        this.rootPattern = rootPattern;
        return rootPattern;
    }
    
    @Override
    public void parseCondition() {
        String conditionContent = this.blockContent;
        conditionContent = this.getConditionPart(conditionContent);
        DGLogger.debug(conditionContent);
        DGLogger.debug("");
        List<String> conditionList = this.splitCondition(conditionContent);
        for (String s : conditionList) {
            DGLogger.debug(s);
        }
        Pattern rootPattern = this.analyzeCondition(conditionList);
        DGLogger.debug(rootPattern.toString());
    }
    
    private int countBottomBracket(String content) {
        int cnt = 0;
        while(StringUtils.countMatches(content, "(")
                < StringUtils.countMatches(content, ")")) {
            cnt++;
            content = content.replaceFirst("\\)$", "");
        }
        return cnt;
    }
    
    private List<Boolean> countTopBracket(String content) {
        List<Boolean> nestList = new ArrayList<>();
        char[] cArr = content.toCharArray();
        boolean isNot = false;
        for (int i = 0; i < cArr.length; i++) {
            char c = cArr[i];
            if (c == '(') {
                nestList.add(isNot);
                isNot = false;
            } else if (i < cArr.length - 1 && c == '!'){
                isNot = !isNot;
            } else {
                break;
            }
        }
        /*while(content.startsWith("(")) {
            cnt++;
            content = content.replaceFirst("^\\(", "");
        }*/
        return nestList;
    }
    
    private String removeTerminalBracket(String content) {
        int replaceSize = 0;
        content = content.replaceAll("(^\s+|\s+$)", "");
        String tmpElement = content;
        Matcher mS = PatternConstants.TOP_BRACKET_PATTERN.matcher(content);
        Matcher mE = PatternConstants.BOTTOM_BRACKET_PATTERN.matcher(content);
        int sBLen = StringUtils.countMatches(content, "(");
        int eBLen = StringUtils.countMatches(content, ")");
        int sCBLen = mS.find() ? mS.group().length() : 0;
        int eCBLen = mE.find() ? mE.group().length() : 0;
        
        if(sBLen == sCBLen) {
            if (sBLen == eBLen) {
                // e.g ((a == b)) → a == b only
                // return content.replaceAll("(^\\({" + sCBLen + "}|\\){" + eCBLen + "}$)", "");
                replaceSize = sCBLen;
            } else if (sBLen > eBLen) {
                // e.g ((a == b) → (a == b
                return optimizeTokenFromRight(content, sBLen - sCBLen);
                // return content.replaceAll("(^\\({" + eCBLen + "}|\\){" + eCBLen + "}$)", "");
            } else {
                // sBLen < eBLen
                // e.g (a == b)) → a == b)
                return optimizeTokenFromLeft(content, eBLen - eCBLen);
                // return content.replaceAll("(^\\({" + sCBLen + "}|\\){" + sCBLen + "}$)", "");
            }
        } else {
            // (sBLen > sCBLen)
            if (eBLen == eCBLen) {
                if (sBLen == eBLen) {
                    // e.g (a == b()) → a == b()
                    replaceSize = sCBLen;
                    // return content.replaceAll("(^\\({" + sCBLen + "}|\\){" + sCBLen + "}$)", "");
                } else if (sBLen > eBLen) {
                    // e.g (((a == b()) → ((a == b()
                    return optimizeTokenFromRight(content, sBLen - sCBLen);
                    // return content.replaceAll("(^\\({" + (sCBLen - eCBLen) + "}|\\){" + (sCBLen - eCBLen) + "}$)", "");
                } else {
                    // sBLen < eBLen
                    // e.g (a == b())) → a == b())
                    return optimizeTokenFromLeft(content, eBLen - eCBLen);
                    // return content.replaceAll("(^\\({" + sCBLen + "}|\\){" + sCBLen + "}$)", "");
                }
            } else {
                // (eBLen > eCBLen)
                if (sBLen == eBLen) {
                    // e.g (a() == b()) → a() == b()
                    replaceSize = sCBLen;
                    // return content.replaceAll("(^\\({" + sCBLen + "}|\\){" + sCBLen + "}$)", "");
                } else if (sBLen > eBLen) {
                    // e.g (((a() == b())) → (a() == b()
                    return optimizeTokenFromRight(content, sBLen - sCBLen);
                    // return content.replaceAll("(^\\({" + (sBLen - eCBLen) + "}|\\){" + (sBLen - eCBLen) + "}$)", "");
                } else {
                    // sBLen < eBLen
                    // e.g ((a() == b))) → a() == b)
                    return optimizeTokenFromLeft(content, eBLen - eCBLen);
                    // return content.replaceAll("(^\\({" + (eBLen - sCBLen) + "}|\\){" + (eBLen - sCBLen) + "}$)", "");
                }
            }
        }
        return content.replaceAll("(^\\({" + replaceSize + "}|\\){" + replaceSize + "}$)", "");
    }
    
    private String optimizeTokenFromLeft(String content, int rightSize) {
        while(StringUtils.countMatches(content, "(") > rightSize + 1) {
            content = content.replaceFirst("^\\(", "");
            content = content.replaceFirst("\\)$", "");
        }
        return content;
    }
    
    private String optimizeTokenFromRight(String content, int leftSize) {
        while(StringUtils.countMatches(content, ")") > leftSize) {
            content = content.replaceFirst("^\\(", "");
            content = content.replaceFirst("\\)$", "");
        }
        return content;
    }
    
    private boolean isCompletablePattern(int idx, List<String> conditionList) {
        // finded unit and next element is not bool
        return idx < conditionList.size() - 1 && !isBool(conditionList.get(idx + 1));
    }
    
    private boolean isBool(String target) {
        return Bool.AND.getContent().equals(target)
                || Bool.OR.getContent().equals(target);
        
    }
    
    private boolean isConditionUnit(String condition) {
        return (StringUtils.countMatches(condition, '(')
                == StringUtils.countMatches(condition, ')'))
                && !isBool(condition);
    }

    @Override
    public List<String> getConditionContents() {
        List<String> conditionContents = new ArrayList<>();
        conditionContents = this.rootPattern.getPatternContent();
        return conditionContents;
    }

}
