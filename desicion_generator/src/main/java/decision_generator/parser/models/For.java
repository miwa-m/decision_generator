package decision_generator.parser.models;

import java.io.Serializable;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import decision_generator.utility.PatternConstants;

public class For extends If implements Serializable {

    @Override
    protected String getConditionPart(String conditionContent) {
        conditionContent = conditionContent
                .replaceFirst("^.*?;\s*?", "(")
                .replaceFirst("\s*;.*?\\).*", ")")
                .replaceAll("(^\\({1}|\\){1}$)", "");
        return conditionContent;
    }
    
    @Override
    public void parseCondition() {
        String isExpansion = isExpansionFor(this.blockContent);
        if (!StringUtils.isEmpty(isExpansion)) {
            Condition condition = new Condition(isExpansion);
            this.rootPattern = new Pattern();
            this.rootPattern.addToken(condition);
        } else {
            super.parseCondition();
        }
    }
    
    private String isExpansionFor(String content) {
        Matcher forMatcher = java.util.regex.Pattern.compile("for\s*\\(.+?\\)\s*").matcher(content);
        content = forMatcher.find() ? forMatcher.group() : content;
        Matcher isExpansionForMatcher =
                PatternConstants.EXPANSION_FOR_CONTENT_PATTERN.matcher(content);
        return isExpansionForMatcher.find() ?
                isExpansionForMatcher.group().replaceFirst("for\s*", "").replaceAll("(^\\(|\\)$)", "") : "";
    }
}
