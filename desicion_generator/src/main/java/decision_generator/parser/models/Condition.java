package decision_generator.parser.models;

/**
 * Condition.
 * @author komur
 *
 */
public class Condition extends Token {
    /** condition String */
    public String conditionContents;
    
    public Condition(String conditionContents) {
        int eCount = 0;
        for (char c : conditionContents.toCharArray()) {
            if (c != '!') {
                break;
            }
            eCount++;
        }
        
        if (eCount % 2 != 0) {
            notFlag = true;
        }
        this.conditionContents = conditionContents;
    }
    
    @Override
    public String toString() {
        return this.conditionContents;
    }
}
