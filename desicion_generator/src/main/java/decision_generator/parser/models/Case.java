package decision_generator.parser.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import decision_generator.utility.PatternConstants;

public class Case extends Sentence implements Serializable {

    private boolean isBreak = false;
    private boolean isDefault = false;
    
    private Pattern pattern = new Pattern();
    
    private List<String> conditionContents = new ArrayList<String>();
    
    @Override
    public void parseCondition() {
        String content = this.blockContent;
        this.isBreak = PatternConstants.CASE_ISBREAK_PATTERN
                .matcher(content).find();
        this.isDefault = PatternConstants.DEFAULTBLOCK_START_PATTERN
                .matcher(content).find();
        Pattern pattern = new Pattern();
        
        String conditionContent =
                this.blockContent.replaceAll("(^\s+?|\\:.*)", "");
        Condition caseCondition = new Condition(conditionContent);
        pattern.addToken(caseCondition);
        this.pattern = pattern;
        if (!isDefault) {
            this.conditionContents.add(conditionContent);
        }
    }
    
    @Override
    protected int getBeforeContentStart() {
        Matcher m = PatternConstants.START_CASE_CONTENT_PATTERN
                .matcher(blockContent);
        return m.find() ? m.end() : 0;
    }
    
    public boolean isBreak() {
        return this.isBreak;
    }
    
    public boolean isDefault() {
        return this.isDefault;
    }
    
    public Pattern getCase() {
        return this.pattern;
    }
    
    @Override
    public List<String> getConditionContents() {
        return this.conditionContents;
        // String conditionContent = this.blockContent.replaceAll("(^(|.+?)(case\s*.+?|default)\s*?:.*)", "");
        // return new ArrayList<String>();
    }

}
