package decision_generator.parser.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import decision_generator.utility.PatternConstants;

/**
 * Sentence.
 * @author komur
 *
 */
public abstract class Sentence  implements Serializable {
    
    /** innerSentence */
    protected List<Sentence> innerSentences = new ArrayList<>();
    
    protected String blockContent;
    
    protected boolean isReturn = false;
    
    /** conditions */
    protected List<Condition> conditions = new ArrayList<>();

    public void setBlockContent(String blockContent) {
        this.blockContent = blockContent;
        this.setIsReturn();
        this.parseCondition();
    }
    
    protected void setIsReturn() {
        int firstRemovePoint = this.getBeforeContentStart();
        String innerContent = this.blockContent.substring(firstRemovePoint);
        Matcher isReturnMatcher = PatternConstants.IS_RETURN_OR_THROW_PATTERN.matcher(innerContent);
        this.isReturn = isReturnMatcher.find();
    }
    
    protected int getBeforeContentStart() {
        Matcher m = PatternConstants.START_MIDDLE_BRACKET_CONTENT_PATTERN
                .matcher(blockContent);
        return m.find() ? m.end() : 0;
    }
    
    public String getBlockContent() {
        return this.blockContent;
    }
    
    public boolean isReturn() {
        return this.isReturn;
    }
    
    /**
     * conditions getter.
     * @return contains condition
     */
    public List<Condition> getConditions() {
        return this.conditions;
    };

    /**
     * innerSentences getter.
     * @return innerSentence
     */
    public List<Sentence> getInnerSentences() {
        return this.innerSentences;
    }
    
    /**
     * innerSentences setter.
     * @param sentences
     */
    public void setInnerSentences(List<Sentence> sentences) {
        this.innerSentences = sentences;
    }
    
    public SentenceType getSentenceType() {
        java.lang.Class<? extends Sentence> cls = this.getClass();
        for (SentenceType type : SentenceType.class.getEnumConstants()) {
            if (type.getClassType() == cls) {
                return type;
            }
        }
        return SentenceType.UNKNOWN;
    }
    
    /**
     * parse condition.
     * @param conditionContents condition string
     */
    public abstract void parseCondition();
    
    public abstract List<String> getConditionContents();
}
