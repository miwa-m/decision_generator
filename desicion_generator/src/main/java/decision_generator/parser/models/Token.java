package decision_generator.parser.models;

import java.io.Serializable;

public abstract class Token implements Serializable {
    protected boolean notFlag = false;
    
    public boolean isNot() {
        return this.notFlag;
    }
    
    public void setNot() {
        this.notFlag = true;
    }
}
