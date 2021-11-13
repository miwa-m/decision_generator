package desicion_generator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import decision_generator.parser.models.Class;
import decision_generator.parser.models.Condition;
import decision_generator.parser.models.ConditionCombination;
import decision_generator.parser.models.Pattern;
import decision_generator.parser.models.Token;
import decision_generator.parser.models.Pattern.Bool;
import out.BookGenerator;

public class BookGeneratorTest {

    // @Test
    public void b() {
        System.out.println(";switch(a){a;}".replaceAll("(^.*switch\s*\\(|\\)??\s*?\\{.*)", ""));
    }
    private String outputDst = "C:\\Users\\komur\\Documents\\develop_test\\dg";
    
    // @Test
    public void generateSheetTest1() throws IOException, InterruptedException, Exception {
        List<List<Class>> classList = new ArrayList<>();
        try (ObjectInputStream objInStream
            = new ObjectInputStream(
            new FileInputStream("src/test/resources/test_classes.obj"))) {
            List<Class> classes = (ArrayList<Class>) objInStream.readObject();

            classList.add(classes);
            //classList.add(classes);
        }

        String bookName = "test";
        Files.deleteIfExists(
                Paths.get(outputDst + "\\" + bookName + ".xlsx"));
        Thread.sleep(1000);
        BookGenerator sheetGenerator =
                new BookGenerator(outputDst, bookName);
        sheetGenerator.setClassList(classList);
        sheetGenerator.makeBookFile();
    }
    
    @Test
    public void pattenConbinationTest01() {
        Pattern pattern = new Pattern();
        Condition c1 = new Condition("a == 1");
        // c1.setNot();
        Condition c2 = new Condition("b == 2");
        Condition c3 = new Condition("c == 3");

        Pattern p2 = new Pattern();
        p2.addToken(c2);
        p2.addBool(Bool.AND);
        p2.addToken(c3);
        // p2.setNot();
        pattern.addToken(c1);
        pattern.addBool(Bool.OR);
        pattern.addToken(p2);
        //pattern.addToken(c3);
        //pattern.addBool(Bool.AND);
        List<ConditionCombination> conbinationList =
                generateConbinationList(pattern);
        
        
        for(ConditionCombination cc : conbinationList) {
            for (Boolean b : cc.getConditionBoolList()) {
                System.out.print(b.toString() + " ");
            }
            boolean result = emuratePattern(pattern, cc.getConditionBoolList());
            // System.out.println("→  "+ result);
            System.out.println("→ " + cc.isTrue());
        }
    }

    @Test
    public void pattenConbinationTest02() {
        Condition c1 = new Condition("a");
        Condition c2 = new Condition("b");
        Condition c3 = new Condition("d");

        Pattern p1 = new Pattern();
        Pattern p2 = new Pattern();
        Pattern p3 = new Pattern();
        Pattern p4 = new Pattern();
        Pattern p5 = new Pattern();
        Pattern p6 = new Pattern();
        
        p5.addToken(c1);
        p5.addBool(Bool.AND);
        p5.addToken(c2);
        
        p3.addToken(c3);
        p3.addBool(Bool.AND);
        p3.addToken(p5);
        

        p4.addToken(p3);
        p4.addBool(Bool.AND);

        p2.addToken(c1);
        p2.addBool(Bool.OR);
        p2.addToken(c2);
        
        p4.addToken(p2);
        
        p1.addToken(c1);
        p1.addBool(Bool.AND);
        p1.addToken(p4);
        
        System.out.println(p1.toString());
        List<ConditionCombination> conbinationList =
                generateConbinationList(p1);
        
        for(ConditionCombination cc : conbinationList) {
            for (Boolean b : cc.getConditionBoolList()) {
                System.out.print(b.toString() + " ");
            }
            boolean result = emuratePattern(p1, cc.getConditionBoolList());
            // System.out.println("→  "+ result);
            System.out.println("→ " + cc.isTrue());
        }
    }
    
    private List<ConditionCombination> generateConbinationList(Pattern pattern) {

        System.out.println();
        List<ConditionCombination> conbinationList =
                new ArrayList<ConditionCombination>();
        int tokenSize = getTokenSize(pattern.getTokens());
        for (int i = 0; i < Math.pow(2, tokenSize); i++) {
            String binaryString = String.format("%" +tokenSize + "s",
                Integer.toBinaryString(i)).replaceAll(" ", "0");
            ConditionCombination conbination = new ConditionCombination();
            
            List<Boolean> boolList = new ArrayList<>();
            char[] binArray = binaryString.toCharArray();
            boolean finallyResult = true;
            
            for (int j = 0; j < tokenSize; j++) {
                char b = binArray[j];
                boolean result = b == '1';
                
                boolList.add(result);
                /*if (j > 0) {
                    finallyResult = pattern.getBools().get(j - 1) == Bool.AND
                            ? finallyResult && result : finallyResult || result;
                } else {
                    finallyResult = result;
                }*/
            }
            conbination.setConditionBoolList(boolList);
            conbination.setResult(emuratePattern(pattern, boolList));
            conbinationList.add(conbination);
        }
       return conbinationList;
    }
    
    private boolean emuratePattern(Pattern pattern, List<Boolean> conbination) {
        Boolean result = null;
        List<Bool> bools = pattern.getBools();
        List<Token> tokens = pattern.getTokens();
        int addIdx = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Boolean tmpResult = null;
            Token token = tokens.get(i);
            if (token instanceof Pattern) {
                Pattern tokenPtrn = (Pattern) token;
                int subTokenSize = getTokenSize(tokenPtrn.getTokens());

                tmpResult = emuratePattern(tokenPtrn,
                        conbination.subList(addIdx, addIdx + subTokenSize));
                addIdx += subTokenSize;
            } else if (token instanceof Condition) {
                tmpResult = conbination.get(addIdx);
                addIdx++;
                // System.out.print(tmpResult + " ");
            }

            tmpResult = token.isNot() ? !tmpResult : tmpResult;
            
            if (i == 0) {
                result = tmpResult;
            } else if (i > 0) {
                result = bools.get(i - 1) == Bool.AND ?
                        result && tmpResult : result || tmpResult;
            }
        }
        return result;
    }
    
    private int getTokenSize(List<Token> tokens) {
        int tokenSize = 0;
        for (Token token : tokens) {
            if (token instanceof Pattern) {
                tokenSize += getTokenSize(((Pattern) token).getTokens());
            } else if (token instanceof Condition) {
                tokenSize++;
            }
        }
        return tokenSize;
    }
}
