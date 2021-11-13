package desicion_generator.utility;

import java.util.List;
import java.util.regex.Matcher;

import desicion_generator.parser.models.Sentence;

/**
 * SyntaxAnalyzeUtility.
 * @author komur
 *
 */
public final class SyntaxAnalyzeUtility {
    public static String transEscaped(String content) {
        return content.replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("\\+", "\\\\+")
                .replaceAll("\\*", "\\\\\\*")
                .replaceAll("\\[", "\\\\\\[")
                .replaceAll("\\]", "\\\\\\]")
                .replaceAll("\\(", "\\\\\\(")
                .replaceAll("\\)", "\\\\\\)")
                .replaceAll("\\{", "\\\\\\{")
                .replaceAll("\\}", "\\\\\\}")
                .replaceAll("\\.", "\\\\\\.")
                .replaceAll("\\?", "\\\\\\?")
                .replaceAll("\\^", "\\\\\\^")
                .replaceAll("\\|", "\\\\\\|")
                .replaceAll("\\$", "\\\\\\$")
                .replaceAll("\\:", "\\\\\\:")
                .replaceAll("\"", "\\\"")
                .replaceAll("\\-", "\\\\\\-");
    }
    
    public static String extractBlock_(String code) {
        boolean findBlacketStart = false;
        int startIdx = -1;
        int blockStartCount = 0;
        int blockEndCount = 0;
        
        // search {}
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '{') {
                blockStartCount++;
                if (startIdx < 0) {
                    startIdx = i;
                }
            } else if (c == '}') {
                blockEndCount++;
            }
            if (startIdx >= 0
                    && blockStartCount == blockEndCount) {
                return code.substring(startIdx, i + 1);
            }
        }
        return "";
    }
    
    public static String extractBlock(String code) {
        String blockContent = "";
        int startIdx = -1;
        int blockStartCount = 0;
        int blockEndCount = 0;
        
        // search {}
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '{') {
                blockStartCount++;
                if (startIdx < 0) {
                    startIdx = i;
                }
            } else if (c == '}') {
                blockEndCount++;
            }
            if (startIdx >= 0
                    && blockStartCount == blockEndCount) {
                String afterBottomBracket = code.substring(i);
                blockContent = code.substring(startIdx, i + 1);
                Matcher doWhileMatcher = PatternConstants.DOWHILE_END_PATTERN.matcher(afterBottomBracket);
                if (doWhileMatcher.find()) {
                    // find do while end
                    blockContent = blockContent + doWhileMatcher.group().substring(1);
                }
                break;
            }
        }
        // search case
        Matcher caseStartMatcher = PatternConstants.CASEBLOCK_START_PATTERN.matcher(code);
        // find case xxx: start
        if(caseStartMatcher.find()) {
            int caseStartPoint = caseStartMatcher.end();
            if (startIdx == -1 || caseStartPoint < startIdx) {
                // find case end
                // (.*?(case\s+.+?|default)\s*?:/})
                String endSearchCode = code.substring(caseStartPoint);
                Matcher caseEndMatcher = PatternConstants.CASEBLOCK_END_PATTERN.matcher(endSearchCode);
                if (caseEndMatcher.find()) {
                    blockContent = endSearchCode.substring(0, caseEndMatcher.start());
                } else {
                    // search switch block end  "}"
                    int sBCnt = 0;
                    int eBCnt = 0;
                    for (int i = 0; i < endSearchCode.length(); i++) {
                        char c = endSearchCode.charAt(i);
                        if (c == '{') {
                            sBCnt++;
                        } else if (c == '}') {
                            eBCnt++;
                            if (sBCnt < eBCnt) {
                                blockContent = endSearchCode.substring(0, i);
                                break;
                            }
                        }
                    }
                    if (sBCnt == 0 && eBCnt == 0) {
                        blockContent = endSearchCode;
                    }
                }
            }
        }
        
        return blockContent;
    }
    
    public static void outputSentenceInfo(List<Sentence> sentences, String pad) {
        
        for (Sentence s : sentences) {
            DGLogger.debug(pad + s.getBlockContent());
            outputSentenceInfo(s.getInnerSentences(), pad + "  ");
        }
    }
}
