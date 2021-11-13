package desicion_generator.parser.models;

import java.util.ArrayList;
import java.util.List;

public class ConditionCombination {

    private Boolean result;
    
    private List<Boolean> conditionBoolList =
            new ArrayList<>();
    
    public List<Boolean> getConditionBoolList() {
        return this.conditionBoolList;
    }

    public void setConditionBoolList(List<Boolean> conditionBoolList) {
        this.conditionBoolList = conditionBoolList;
    }
    
    public void setResult(boolean result) {
        this.result = result;
    }
    
    public boolean isTrue() {
        return this.result;
    }
    
    public List<String> transAuthenticityStringList() {
        List<String> authenticityStringList =
                new ArrayList<>();
        for (Boolean b : this.conditionBoolList) {
            authenticityStringList.add(b ? "T" : "F");
        }
        return authenticityStringList;
    }
}
