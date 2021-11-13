package desicion_generator.parser.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Try extends Sentence implements Serializable {

    @Override
    public void parseCondition() {
    }

    @Override
    public List<String> getConditionContents() {
        return new ArrayList<String>();
    }

}
