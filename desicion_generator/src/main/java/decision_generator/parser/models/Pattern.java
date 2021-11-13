package decision_generator.parser.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pattern extends Token {
    
    public static enum Bool implements DecodableEnum {
        AND("&&"),
        OR("||");
        
        private String syntax;
        
        private Bool(String syntax) {
            this.syntax = syntax;
        }
        
        @Override
        public String getContent() {
            return this.syntax;
        }
    }
    private Pattern parentPattern = null;
    
    /** patterns */
    private List<Token> tokens = new ArrayList<>();
    
    /** bool */
    private List<Bool> bools = new ArrayList<>();
    
    /*
    private String pattern;
    
    public Pattern(String patten) {
        this.pattern = pattern;
    }*/

    public Pattern getParentPattern() {
        return this.parentPattern;
    }
    
    public void setParentPattern(Pattern parentPattern) {
        this.parentPattern = parentPattern;
    }
    
    public int getTokenSize() {
        return this.tokens.size();
    }
    
    public void addToken(Token token) {
        this.tokens.add(token);
    }

    public void addBool(Bool bool) {
        this.bools.add(bool);
    }
    
    public boolean isCompletedGenerate() {
        return !(this.tokens.size() == this.bools.size());
    }
    
    public List<Token> getTokens() {
        return this.tokens;
    }
    
    public List<Bool> getBools() {
        return this.bools;
    }
    
    
    @Override
    public String toString() {
        StringBuffer conditionSb = new StringBuffer();
        conditionSb.append("(");
        for (int i = 0; i < this.tokens.size(); i++) {
            Token t = this.tokens.get(i);
            conditionSb.append(t.toString());
            if (i <= this.bools.size() - 1) {
                conditionSb.append(" ")
                .append(bools.get(i).getContent())
                .append(" ");
            }
        }
        conditionSb.append(")");
        return conditionSb.toString();
    }
    
    protected List<String> getPatternContent() {
        List<String> conditionContents = new ArrayList<>();
        for (Token token : getTokens()) {
            if (token instanceof Pattern) {
                conditionContents.addAll(
                        (Collection<? extends String>) ((Pattern) token).getPatternContent());
            } else if (token instanceof Condition){
                Condition condition = (Condition) token;
                conditionContents.add(condition.conditionContents);
            }
        }
        return conditionContents;
    }
}
