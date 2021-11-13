package desicion_generator.parser.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import desicion_generator.parser.models.Pattern.Bool;

public class Catch extends Sentence  implements Serializable {

    
    private Pattern exceptions = new Pattern();
    
    @Override
    public void parseCondition() {
        String condition = this.blockContent.replaceAll("(^.*?\\(|\\).*$)", "");
        String[] catchExceptions = condition.split("\s*\\|\s*");
        for (int i = 0; i < catchExceptions.length; i++) {
            String exception = catchExceptions[i];
            exception = exception.replaceAll("^\s*", "").replaceFirst("\s.*$", "");
            Condition exceptionCondition = new Condition(exception);
            this.exceptions.addToken(exceptionCondition);
            if (i < catchExceptions.length - 1) {
                this.exceptions.addBool(Bool.OR);
            }
        }
    }
    
    public Pattern getExceptions() {
        return this.exceptions;
    }

    @Override
    public List<String> getConditionContents() {
        List<String> conditionContents = new ArrayList<>();
        for (Token exceptionToken : this.exceptions.getTokens()) {
            conditionContents.add(exceptionToken.toString());
        }
        return conditionContents;
    }

}
