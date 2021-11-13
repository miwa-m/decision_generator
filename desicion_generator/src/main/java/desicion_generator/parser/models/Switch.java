package desicion_generator.parser.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import desicion_generator.parser.models.Pattern.Bool;

public class Switch extends Sentence implements Serializable {
    /**
     * switch's inner sentences is only case.
     * all 'OR'
     */
    
    private String variableName = "";
    private Pattern cases = new Pattern();
    
    @Override
    public void parseCondition() {
        String content = this.blockContent;
        this.variableName = content.replaceAll("(^.*switch\s*\\(|\\)??\s*?\\{.*)", "");
        for (int i = 0; i < this.innerSentences.size(); i++) {
            Sentence sentence = this.innerSentences.get(i);
            String conditionContent = sentence.getBlockContent();
            Condition condition = new Condition(
                    conditionContent.replaceAll("(^(|.+?)case\s*|\s*?:.*)", ""));
            cases.addToken(condition);
            if (i < this.innerSentences.size() - 1) {
                cases.addBool(Bool.OR);
            }
        }
    }
    
    public String getVariableName() {
        return this.variableName;
    }
    
    public Pattern getCases() {
        return cases;
    }

    @Override
    public List<String> getConditionContents() {
        return new ArrayList<String>();
        
    }

}
