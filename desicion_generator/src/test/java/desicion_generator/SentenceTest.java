package desicion_generator;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import decision_generator.parser.models.Case;
import decision_generator.parser.models.Catch;
import decision_generator.parser.models.Sentence;
import decision_generator.parser.models.Switch;

public class SentenceTest {

    @Test
    public void switchTest() {
        /*int i = 0;
        switch (i) {
        default:
            break;
        case 1:
            break;
        }*/
        String bC = ";switch(i) {case 1:System.out.println(\"1 else if\");break;case 2:System.out.println(\"2 else if\");break;default:System.out.println(\"default else if\");break;}";
        Case cs1 = new Case();
        cs1.setBlockContent("case 1:System.out.println(\"1 else if\");break;");
        Case cs2 = new Case();
        cs2.setBlockContent("case 2:System.out.println(\"2 else if\");break;");
        Case def = new Case();
        def.setBlockContent("default:System.out.println(\"default else if\");break;");
        List<Sentence> cases = new ArrayList<>();
        cases.add(cs1);
        cases.add(cs2);
        cases.add(def);
        Switch swt = new Switch();
        swt.setInnerSentences(cases);
        swt.setBlockContent(bC);
        System.out.println(swt.getCases().toString());
    }
    
    @Test
    public void catchTest() {
        String bC = "catch (RuntimeException e |Throwable t|Error er ) {if(true) {System.out.println(\"catch true\");} else {System.out.println(\"catch false\");}}";
        Catch cth = new Catch();
        cth.setBlockContent(bC);
        System.out.println(cth.getExceptions().toString());
    }
}
