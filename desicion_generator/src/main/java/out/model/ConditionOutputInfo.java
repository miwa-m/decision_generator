package out.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import decision_generator.parser.models.ConditionCombination;
import decision_generator.parser.models.Sentence;

public class ConditionOutputInfo {
    
    private Map<Sentence, ConditionOutputInfo> conditionInfoMap =
            new LinkedHashMap<>();
    
    private Map<Sentence, List<ConditionCombination>> conditionPattern = new LinkedHashMap<>();
    
    private ConditionOutputInfo parentOutputInfo;
    
    private Sentence parentSentence;
    
    public List<ConditionCombination> getParentCombinationList() {
        return this.parentOutputInfo.getCombinationList(this.parentSentence);
    }
    
    public ConditionOutputInfo getParentOutputInfo() {
        return parentOutputInfo;
    }

    public void setParentOutputInfo(ConditionOutputInfo parentOutputInfo) {
        this.parentOutputInfo = parentOutputInfo;
    }

    public Sentence getParentSentence() {
        return parentSentence;
    }

    public void setParentSentence(Sentence parentSentence) {
        this.parentSentence = parentSentence;
    }

    private int usingColumnsCount = 0;
    
    public ConditionOutputInfo() { }
    
    public void setCombinationList(Sentence sentence, List<ConditionCombination> conditionList) {
        this.conditionPattern.put(sentence, conditionList);
    }

    public List<ConditionCombination> getCombinationList(Sentence sentence) {
        return this.conditionPattern.get(sentence);
    }
    
    public Map<Sentence, List<ConditionCombination>> getConditionPattern() {
        return this.conditionPattern;
    }
    
    public Set<Sentence> getSentences() {
        return this.conditionInfoMap.keySet();
    }
    
    public Collection<ConditionOutputInfo> getSubConditionInfos() {
        return this.conditionInfoMap.values();
    }
    
    public Map<Sentence, ConditionOutputInfo> getConditionInfoMap() {
        return this.conditionInfoMap;
    }
    
    private void addUsingColumnsCount(int count) {
        this.usingColumnsCount += count;
    }
    
    private void setUsingColumnsCount(int usingColumnsCount) {
        this.usingColumnsCount = usingColumnsCount;
    }
    
    public void addSubConditionInfo(Sentence sentence, ConditionOutputInfo subConditionInfo) {
        this.conditionInfoMap.put(sentence, subConditionInfo);
    }
    
    public int getUsingRowCountSelf() {
        int rowCount = 0;
        for (Sentence sentence : this.conditionInfoMap.keySet()) {
            rowCount += sentence.getConditionContents().size();
        }
        return rowCount;
    }
    
    public int getUsingRowCountSubCondition() {
        int rowCount = 0;
        for (ConditionOutputInfo conditionInfo : this.conditionInfoMap.values()) {
            rowCount += conditionInfo.getUsingRowCountSelf();
            rowCount += conditionInfo.getUsingRowCountSubCondition();
        }
        return rowCount;
    }
    
    public boolean isEmpty() {
        return conditionInfoMap.size() == 0;
    }
}
