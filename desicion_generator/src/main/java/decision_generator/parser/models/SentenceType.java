package decision_generator.parser.models;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import decision_generator.exceptions.FailedGenerateObjectException;

public enum SentenceType {
    /*IF("(^|[;\\{\\}])\s*if\s*\\(.+?\\)\s*", If.class, "{{"),
    ELSE_IF("(^|\\})\s*else\s+if\s*\\(.+?\\)\s*", ElseIf.class, "{{"),
    ELSE("(^|\\})\s*else\s*", Else.class, "{"),
    FOR("(^|[;\\{\\}])\s*for\s*\\(.+?\\)\s*", For.class, "{{"),
    WHILE("(^|[;\\{\\}])\s*while\s*\\(.+?\\)\s*", While.class, "{{"),
    DO_WHILE("(^|[;\\{\\}])\s*do\s*", DoWhile.class, "{{"),
    TRY("(^|[;\\{\\}])\s*try\s*(\\(.+?\\))?\s*", Try.class, "{{"),
    CATCH("(^|\\})\s*catch\s*\\(.+?\\)\s*", Catch.class, "{{"),
    FINALLY("(^|\\})\s*finally\s*", Finally.class, "{{"),
    SWITCH("(^|[;\\{\\}])\s*switch\s*\\(.+?\\)\s*", Switch.class, "{{"),
    CASE("(^|[;\\{\\}])\s*(case\s+.+?|default)\s*:", Case.class, ": "),
    UNKNOWN("$^", null, "");*/
    

    IF("\s*if\s*\\(.+?\\)\s*", If.class, "{{"),
    ELSE_IF("^\s*else\s+if\s*\\(.+?\\)\s*", ElseIf.class, "{{"),
    ELSE("^\s*else\s*", Else.class, "{"),
    FOR("\s*for\s*\\(.+?\\)\s*", For.class, "{{"),
    WHILE("\s*while\s*\\(.+?\\)\s*", While.class, "{{"),
    DO_WHILE("\s*do\s*", DoWhile.class, "{{"),
    TRY("\s*try\s*(\\(.+?\\))?\s*", Try.class, "{{"),
    CATCH("^\s*catch\s*\\(.+?\\)\s*", Catch.class, "{{"),
    FINALLY("^\s*finally\s*", Finally.class, "{{"),
    SWITCH("\s*switch\s*\\(.+?\\)\s*", Switch.class, "{{"),
    CASE("\s*(case\s+.+?|default)\s*:", Case.class, ": "),
    UNKNOWN("$^", null, "");
    
    private Pattern sentencePattern;
    private String sentenceRegex;
    private String horizonToken;
    private java.lang.Class<? extends Sentence> sentenceClass;
    
    private SentenceType(String sentenceRegex, java.lang.Class<? extends Sentence> sentenceClass, String horizonToken) {
        this.sentencePattern = Pattern.compile(sentenceRegex);
        this.sentenceClass = sentenceClass;
        this.sentenceRegex = sentenceRegex;
        this.horizonToken = horizonToken;
    }
    
    public String getHorizonToken() {
        return horizonToken;
    }
    
    public java.lang.Class getClassType() {
        return this.sentenceClass;
    }
    
    public String getSentenceRegex() {
        return this.sentenceRegex;
    }
    
    public Matcher getMatcher(String target) {
        return this.sentencePattern.matcher(target);
    }
    
    public Matcher getMatcher(String block, String target) {
        return Pattern.compile(this.sentenceRegex + "\s*" + block).matcher(target);
    }
    
    public Sentence generateSentenceInstance() {
        try {
            return this.sentenceClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new FailedGenerateObjectException("Failed generate object of" + this.sentenceClass.getName());
        }
    }
}
