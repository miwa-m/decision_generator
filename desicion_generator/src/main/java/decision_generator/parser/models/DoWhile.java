package decision_generator.parser.models;

import java.io.Serializable;

public class DoWhile extends While implements Serializable {

    @Override
    protected String getConditionPart(String conditionContent) {
        conditionContent = conditionContent
                        .replaceFirst("^.*\\}\s*while\s*\\(?", "(")
                        .replaceFirst("\\)\s*;$", ")")
                        .replaceAll("(^\\({1}|\\){1}$)", "");
        return conditionContent;
    }
    
    @Override
    public void parseCondition() {
        super.parseCondition();
    }
}
