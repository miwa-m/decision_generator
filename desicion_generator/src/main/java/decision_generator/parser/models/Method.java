package decision_generator.parser.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Method.
 * @author komur
 *
 */
public class Method implements Meta, Serializable {
    
    /** contains sentences */
    private List<Sentence> sentences = new ArrayList<>();
    
    public String name;
    
    public Method(String name) {
        this.name = name;
    }
    
    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }
    
    public List<Sentence> getSentences() {
        return this.sentences;
    }
    
    /**
     * generate combination patterns
     * @return
     */
    public Pattern generatePatterns() {
        return null;
    }
    
    /**
     * sentence setter.
     * @param sentence sentence
     */
    public void addSentence(Sentence sentence) {
        this.sentences.add(sentence);
    }
}
