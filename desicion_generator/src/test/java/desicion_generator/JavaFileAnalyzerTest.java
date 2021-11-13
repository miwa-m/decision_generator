package desicion_generator;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import desicion_generator.parser.JavaFileAnalyzer;
import desicion_generator.parser.models.Block;
import desicion_generator.parser.models.Case;
import desicion_generator.parser.models.Class;
import desicion_generator.parser.models.Condition;
import desicion_generator.parser.models.If;
import desicion_generator.parser.models.Sentence;
import desicion_generator.parser.models.SentenceType;
import desicion_generator.utility.PatternConstants;
import desicion_generator.utility.SyntaxAnalyzeUtility;

public class JavaFileAnalyzerTest {

    @Test
    public void analyzeJavaFile() throws Exception {
        String fileName = "ForTestParsing.java";
        Path file = Paths.get(Thread.currentThread()
        .getContextClassLoader().getResource(fileName).toURI());
        JavaFileAnalyzer analyzer = new JavaFileAnalyzer(file.toAbsolutePath().toString());
        List<Class> classes = analyzer.analyzeJavaFile();
        
        ObjectOutputStream objOutStream =
                new ObjectOutputStream(
                    new FileOutputStream("src/test/resources/test_classes.obj"));

            objOutStream.writeObject(classes);

            objOutStream.close();
        //XtSystem.out.println(sourceCode);
    }
    
    // @Test
    public void transEscapedTest() {
        String str = "{}[]().+*?^$-\"";
        System.out.println(SyntaxAnalyzeUtility.transEscaped(str));
    }
    
    // @Test
    public void generateRegexClassSignatureTest() {
        String str = "public String innerMethod01(){}";
        System.out.println(str.replaceFirst(
                JavaFileAnalyzer.generateRegexClassSignature(), ""));
    }

    @Test
    public void generateRegexMethodSignatureTest() {
        String str = "public static void innerMethod01(String args){}";
        Pattern p = Pattern.compile("[^\s]+\s*\\(.*?\\)");
        Matcher m = p.matcher(str);
        m.find();
        System.out.println(m.group());
        //System.out.println(str.replaceFirst(
         //       JavaFileAnalyzer.generateRegexMethodSignature(), ""));
    }
    @Test
    public void test() {
        String str = "public static class innerMethod01 implements Inf {}";
        Pattern p = Pattern.compile("(class|interface)\s+[^\s]+\s*((implements|extends)\s+[^\s]+)?\s*\\{");
        Matcher m = p.matcher(str);
        m.find();
        System.out.println(m.group().replaceFirst("(class|interface)\\s+", "")
                .replaceFirst("\s*((implements|extends)\s+[^\s]+)?\s*\\{", ""));
    }
    static public class AAA{}
    
    // @Test
    public void verify() {
        String mC = "{String str = \"A\";if (str.equals(\"A\")) {System.out.println(\"if\");} else if (str.equals(\"B\") || str.equals(\"C\")) {if (true) {System.out.println(\"else if\"):}} else {System.out.println(\"else\");}{}}";
        mC = "{String str = \"A\";if (str.equals(\"A\")) {System.out.println(\"if\");} else if (str.equals(\"B\") || str.equals(\"C\")) {int i = 0;switch(i) {case 1:System.out.println(\"1 else if\");break;case 2:System.out.println(\"2 else if\");break;default:System.out.println(\"default else if\");break;}} else {try {System.out.println(\"else\");} catch (RuntimeException e) {if(true) {System.out.println(\"RuntimeException true\");} else {System.out.println(\"RuntimeException false\");}}}}";
        //mC = mC.replaceAll("(^\\{|\\}$)", "");
        //System.out.println(mC);
        List<Sentence> sentences = collectSentence(mC);
        outputSentenceInfo(sentences, "");
        // 同階層に登場する分岐文を検知
            // 条件を抽出
        // そのブロック内の分岐文を検知(繰り返し)
    }
    
    private void outputSentenceInfo(List<Sentence> sentences, String pad) {
        
        for (Sentence s : sentences) {
            System.out.println(pad + s.getBlockContent());
            outputSentenceInfo(s.getInnerSentences(), pad + "  ");
        }
    }
    
    public List<Sentence> collectSentence(String code) {
        code = code.replaceAll("(^\\{|\\}$)", "");
        List<Sentence> sentenceList = new ArrayList<>();
        List<Block> blockList = new ArrayList<>();
        while (true) {
            // 最初に出現するブロックを取り出し
            String block = SyntaxAnalyzeUtility.extractBlock(code);
            if (StringUtils.isEmpty(block)
                    || block.equals("{}")) {
                break;
            }
            String escapedBlock = SyntaxAnalyzeUtility.transEscaped(block);
            for (SentenceType type : SentenceType.class.getEnumConstants()) {
                Matcher m = type.getMatcher(escapedBlock, code);
                if (m.find()) {
                    System.out.println("matched sentence : " + type.name());
                    block = m.group();
                    Sentence sentence = type.generateSentenceInstance();
                    sentence.setBlockContent(block);
                    sentenceList.add(sentence);
                    sentence.setInnerSentences(collectSentence(block.replaceFirst(".+?\\{", "{")));
                    break;
                }
            }
            System.out.println("Find block : " + block);
            System.out.println("code : " + code);
            code = code.replaceFirst(".*?" + escapedBlock, "");
            System.out.println("replaced Code : " + code);
            System.out.println();
            
            /*if (StringUtils.isEmpty(code)
                    || code.equals("{}")) {
                break;
            }*/
        }
        return sentenceList;
    }
    
    @Test
    public void iftest() {
        Condition c1 = new Condition("aaa");
        List<Condition> cL = new ArrayList<>();
        cL.add(c1);
        c1 = null;
        if (cL.get(0) == null) {
            System.out.println("null");
        }else {
            System.out.println(cL.get(0).conditionContents);
            System.out.println(String.valueOf(c1));
        }
    }
    
    @Test
    public void iftest2() {
        String cc = "if (!(\"b\".equals(\"b\") == g != j) || ((k() == c || g()) && (j(a(b)) === 1) || (k() != g(v))) &&((a == d(b.is())||e != f.is())&&((c == a())||e() == g))) {int i = 0;switch(i) {case 1:System.out.println(\"1 else if\");break;case 2:System.out.println(\"2 else if\");break;default:System.out.println(\"default else if\");break;}}";
        //cc = "if ((\"b\".equals(\"b\")) && (a == (d(b.is()) == c || d() == g))) {int i = 0;switch(i) {case 1:System.out.println(\"1 else if\");break;case 2:System.out.println(\"2 else if\");break;default:System.out.println(\"default else if\");break;}}";
        
        If s = new If();
        s.setBlockContent(cc);
        for(String string : s.getConditionContents()) {
            System.out.println(string);
        }
        // s.parseCondition();
    }

    
    @Test
    public void iftest3() {
        String cc = "if (!(\"b\".equals(\"b\") == g != j) || (!(k() == c || g()) && !(j(a(b)) === 1) || (k() != g(v))) &&((a == d(b.is())||e != f.is())&&((c == a())||e() == g))) {int i = 0;switch(i) {case 1:System.out.println(\"1 else if\");break;case 2:System.out.println(\"2 else if\");break;default:System.out.println(\"default else if\");break;}}";
        //cc = "if ((\"b\".equals(\"b\")) && (a == (d(b.is()) == c || d() == g))) {int i = 0;switch(i) {case 1:System.out.println(\"1 else if\");break;case 2:System.out.println(\"2 else if\");break;default:System.out.println(\"default else if\");break;}}";
        
        If s = new If();
        s.setBlockContent(cc);
        for(String string : s.getConditionContents()) {
            System.out.println(string);
        }
        // s.parseCondition();
    }
    
    @Test
    public void booltest() {
        /*
        int i = 0;
        switch (i) {
        case 1 :{}
        }
        try {
            System.out.println("try");
        } finally {
            System.out.println("finally");
        }*/
        
        String code = "{  case 1 :  if (true) {  System.out.println(\"1 true\");  } else {  System.out.println(\"1 false\");  }  break;  case 2:  break;  default:  System.out.println(\"default\");  }  try {  System.out.println(\"try\");  } finally {  System.out.println(\"finally\");  }";
        System.out.println(findCaseBlock(code));
        System.out.println("---");
        code = "{  case 1 :case 2:  break;  default:  System.out.println(\"default\");  }  try {  System.out.println(\"try\");  } finally {  System.out.println(\"finally\");  }";
        System.out.println(findCaseBlock(code));
        System.out.println("---");
        code = "{  case 1 :{}}  try {  System.out.println(\"try\");  } finally {  System.out.println(\"finally\");  }";
        System.out.println(findCaseBlock(code));
        System.out.println("---");
        code = "{  case 1 :break;}  try {  System.out.println(\"try\");  } finally {  System.out.println(\"finally\");  }";
        System.out.println(findCaseBlock(code));
        System.out.println("---");
        
    }
    
    private String findCaseBlock(String code) {
        // [^((\"|\').*?\\{\\}.*?(\"|\'))]
        String caseBlock = "";
        // search case
        Matcher caseStartMatcher = Pattern.compile("^.*?case\s+.+?\s*:").matcher(code);
        Pattern caseEndPattern = Pattern.compile("(case\s+.+?:|default\s?:)");
        // find case xxx: start
        if(caseStartMatcher.find()) {
            // find case end
            // (.*?(case\s+.+?|default)\s*?:/})
            int caseStartPoint = caseStartMatcher.end();
            String endSearchCode = code.substring(caseStartPoint);
            Matcher caseEndMatcher = caseEndPattern.matcher(endSearchCode);
            if (caseEndMatcher.find()) {
                caseBlock = endSearchCode.substring(0, caseEndMatcher.start());
            } else {
                // search switch block end  "}"
                int sBCnt = 0;
                int eBCnt = 0;
                for (int i = 0; i < endSearchCode.length(); i++) {
                    char c = endSearchCode.charAt(i);
                    if (c == '{') {
                        sBCnt++;
                    } else if (c == '}') {
                        eBCnt++;
                        if (sBCnt < eBCnt) {
                            caseBlock =  endSearchCode.substring(0, i);
                            break;
                        }
                    }
                }
            }
        }
        return caseBlock;
    }
    
    @Test
    public void regexTest01() {
        String cts = "case 1: int i = 0;break;";
        Pattern p = Pattern.compile("break;$");
        Matcher m = p.matcher(cts);
        System.out.println(m.find() ? m.group() : "not b");
    }
    
    @Test
    public void regexTest02() {
        String s = "case 1:int i = 0;System.out.println(\"\");";
        Case i = new Case();
        i.setBlockContent(s);
        System.out.println(i.isReturn());
        /*
        Matcher isReturnMatcher = PatternConstants.IS_RETURN_PATTERN
                .matcher("int i = 0;System.out.println(\"\");return;}");
        boolean isReturn = isReturnMatcher.find();
        System.out.println(isReturn);*/
        
    }
    
    @Test
    public void regexTest03() {
        String a = "a\\ncb";
        System.out.println(a);
        String esBlock = SyntaxAnalyzeUtility.transEscaped("a\\nc");
        Matcher m = Pattern.compile(esBlock).matcher(a);
        System.out.println(m.find() ? m.group() : "not");
    }

    @Test
    public void regexTest04() {
        String code = "File targetDirectory = tmpDirectory.toFile();tmpDirectory = Paths.get(parameter.getOutputDirectory());if (Files.exists(tmpDirectory)) {try {tmpDirectory = Files.createDirectories(tmpDirectory);} catch (IOException e) {throw new InvalidParameterException(\"Failed create output directory : \" + parameter.getTargetTopDirectory()+ \"\\n\" + e.getMessage());}}File outputDirectory = tmpDirectory.toFile();String outputFileName = parameter.getOutputFileName();return new DesicionMatrixGenerator(targetDirectory, coverageMode,outputDirectory, outputFileName); ";
        String block = "{try {tmpDirectory = Files.createDirectories(tmpDirectory);} catch (IOException e) {throw new InvalidParameterException(\"Failed create output directory : \" + parameter.getTargetTopDirectory()+ \"\\n\" + e.getMessage());}}";
        //String block = "{try {tmpDirectory = Files.createDirectories(tmpDirectory);} catch (IOException e) {throw new InvalidParameterException(\"Failed create output directory : \" + parameter.getTargetTopDirectory()+ \"\n\"";// + e.getMessage());}}";
        String escapedBlock = SyntaxAnalyzeUtility.transEscaped(block);
        //escapedBlock = "\\{try \\{tmpDirectory = Files\\.createDirectories\\(tmpDirectory\\);\\} catch \\(IOException e\\) \\{throw new InvalidParameterException\\(\"";
        Matcher m = SentenceType.IF.getMatcher(escapedBlock, code);
        System.out.println(m.find() ? m.group() : "not");
    }

    @Test
    public void regexTest05() {
        new Object() {};
        String rm1 = "{private Object ob = new Object(){};public void method01() {String str = \"A\";boolean a = true;String str1 = \"\";if ((a && str.equals(\"A\")) || (str1 === \"D\")) {System.out.println(\"if\");return;} else if (str.equals(\"B\") || str.equals(\"C\")) {int i = 0;switch(i) {case 1:if (i == 1) {System.out.println(\"1 else if if\");} else {System.out.println(\"1 else if else\");}break;case 2:System.out.println(\"2 else if\");return;default:System.out.println(\"default else if\");break;}if(i == 99) {System.out.println(\"else if if i == 99\");}} else {try {while (str === \"while\") {System.out.println(\"while\");throw new Exception(\"while exception\");}} catch (RuntimeException e |Throwable t|Error er ) {int a = 6;int b = 1;int c = 2;do {System.out.println(\"do while\");} while(c == 2 || str.equals(\"do while\"));} finally {for (int i = 0;i < str.length();i++) {System.out.println(\"for\" + i);}}}boolean c = true;boolean d = true;if(c==false ||d ==true ) {System.out.println(\"c == false || d == true\");}}method02(String arg) {return arg;}}";
        String methodSignature = JavaFileAnalyzer.generateRegexMethodSignature();
        // Pattern p = Pattern.compile("[^\s]+\s*\\(.*?\\)");
        Pattern p = Pattern.compile(methodSignature);
        Matcher m = p.matcher(rm1);
        if(!m.find()) {
            System.out.println(m.find() ? m.group() : "not");
        }
        String code = m.group();

        m = Pattern.compile("\s+=\s").matcher(code);
        if (!m.find()) {
            System.out.println(code + " is method");
            return;
        }
        String rm2 = rm1.replaceFirst("^.+?;", "");//rm1.substring(m.end() + 1);
        System.out.println(rm2);
        m = p.matcher(rm2);
        System.out.println(m.find() ? m.group() : "not");
    }
    
    @Test
    public void regexTest06() {
        String forStr = "for (int idx = 0;idx < conditionSize;idx++) {writePositionAndCombinationList =removeUnnecessayPositionAndCombination(writePositionAndCombinationList, beforePositionAndCombinationList);Entry<Sentence, ConditionOutputInfo> conditionInfoEntry = conditionInfoList.get(idx);Sentence sentence = conditionInfoEntry.getKey();SentenceType type = sentence.getSentenceType();ConditionOutputInfo childConditionInfo = conditionInfoEntry.getValue();List<ConditionCombination> combinationList =conditionHierarchy.getCombinationList(sentence);ConditionCombination useCombination = null;switch (type) {case SWITCH:Switch switchSentence = (Switch) sentence;List<Sentence> cases = switchSentence.getInnerSentences();int insertPoint = 0;for (Sentence caseSentence : cases) {ConditionOutputInfo caseCondition =childConditionInfo.getConditionInfoMap().get(caseSentence);Entry<Sentence, ConditionOutputInfo> caseEntry = new SimpleEntry<>(caseSentence, caseCondition);conditionHierarchy.setCombinationList(caseSentence,childConditionInfo.getCombinationList(caseSentence));conditionInfoList.add(idx + insertPoint + 1, caseEntry);insertPoint++;}conditionSize += insertPoint;break;case IF:case ELSE_IF:case WHILE:case DO_WHILE:case FOR:case CASE:case CATCH:if (!(sentence instanceof Case)|| !(((Case) sentence).isDefault())) {List<String> authenticityList = new ArrayList<String>();for (boolean throughResult : new boolean[]{true, false}) {useCombination = getEnableCombination(combinationList, throughResult);authenticityList = useCombination.transAuthenticityStringList();Entry<Integer, List<String>> writePositionEntry =new SimpleEntry<Integer, List<String>>(rowPosition + rowCount,authenticityList);writePositionAndCombinationList.add(writePositionEntry);beforePositionAndCombinationList =copyPositionAndCombinationList(writePositionAndCombinationList);if (throughResult) {if (childConditionInfo.isEmpty()) {writeCombination(sheet, writePositionAndCombinationList, columnPosition);columnPosition = columnPosition.next();} else {columnPosition = writeCombinationPattern(sheet,childConditionInfo, writePositionAndCombinationList,rowPosition + rowCount + authenticityList.size(), columnPosition);}writePositionAndCombinationList.remove(writePositionEntry);beforePositionAndCombinationList.remove(writePositionEntry);writePositionAndCombinationList =removeUnnecessayPositionAndCombination(writePositionAndCombinationList, beforePositionAndCombinationList);} else {if (idx == conditionSize - 1) {writeCombination(sheet, writePositionAndCombinationList, columnPosition);columnPosition = columnPosition.next();}}}rowCount += authenticityList.size();rowCount += calcurateRowCountContainsChild(childConditionInfo);break;}case ELSE:case TRY:case FINALLY:if (!childConditionInfo.isEmpty()) {beforePositionAndCombinationList =copyPositionAndCombinationList(writePositionAndCombinationList);columnPosition = writeCombinationPattern(sheet,childConditionInfo, writePositionAndCombinationList,rowPosition + rowCount, columnPosition);rowCount += calcurateRowCountContainsChild(childConditionInfo);}break;default:break;}}";
        forStr = "for(String s : slist) {for(Integer i : iList){}}";
        System.out.print(isExpansionFor(forStr));
    }

    @Test
    public void regexTest07() {
        String forStr = "for (int idx = 0;idx < conditionSize;idx++) {writePositionAndCombinationList =removeUnnecessayPositionAndCombination(writePositionAndCombinationList, beforePositionAndCombinationList);Entry<Sentence, ConditionOutputInfo> conditionInfoEntry = conditionInfoList.get(idx);Sentence sentence = conditionInfoEntry.getKey();SentenceType type = sentence.getSentenceType();ConditionOutputInfo childConditionInfo = conditionInfoEntry.getValue();List<ConditionCombination> combinationList =conditionHierarchy.getCombinationList(sentence);ConditionCombination useCombination = null;switch (type) {case SWITCH:Switch switchSentence = (Switch) sentence;List<Sentence> cases = switchSentence.getInnerSentences();int insertPoint = 0;for (Sentence caseSentence : cases) {ConditionOutputInfo caseCondition =childConditionInfo.getConditionInfoMap().get(caseSentence);Entry<Sentence, ConditionOutputInfo> caseEntry = new SimpleEntry<>(caseSentence, caseCondition);conditionHierarchy.setCombinationList(caseSentence,childConditionInfo.getCombinationList(caseSentence));conditionInfoList.add(idx + insertPoint + 1, caseEntry);insertPoint++;}conditionSize += insertPoint;break;case IF:case ELSE_IF:case WHILE:case DO_WHILE:case FOR:case CASE:case CATCH:if (!(sentence instanceof Case)|| !(((Case) sentence).isDefault())) {List<String> authenticityList = new ArrayList<String>();for (boolean throughResult : new boolean[]{true, false}) {useCombination = getEnableCombination(combinationList, throughResult);authenticityList = useCombination.transAuthenticityStringList();Entry<Integer, List<String>> writePositionEntry =new SimpleEntry<Integer, List<String>>(rowPosition + rowCount,authenticityList);writePositionAndCombinationList.add(writePositionEntry);beforePositionAndCombinationList =copyPositionAndCombinationList(writePositionAndCombinationList);if (throughResult) {if (childConditionInfo.isEmpty()) {writeCombination(sheet, writePositionAndCombinationList, columnPosition);columnPosition = columnPosition.next();} else {columnPosition = writeCombinationPattern(sheet,childConditionInfo, writePositionAndCombinationList,rowPosition + rowCount + authenticityList.size(), columnPosition);}writePositionAndCombinationList.remove(writePositionEntry);beforePositionAndCombinationList.remove(writePositionEntry);writePositionAndCombinationList =removeUnnecessayPositionAndCombination(writePositionAndCombinationList, beforePositionAndCombinationList);} else {if (idx == conditionSize - 1) {writeCombination(sheet, writePositionAndCombinationList, columnPosition);columnPosition = columnPosition.next();}}}rowCount += authenticityList.size();rowCount += calcurateRowCountContainsChild(childConditionInfo);break;}case ELSE:case TRY:case FINALLY:if (!childConditionInfo.isEmpty()) {beforePositionAndCombinationList =copyPositionAndCombinationList(writePositionAndCombinationList);columnPosition = writeCombinationPattern(sheet,childConditionInfo, writePositionAndCombinationList,rowPosition + rowCount, columnPosition);rowCount += calcurateRowCountContainsChild(childConditionInfo);}break;default:break;}}";
        System.out.print(getConditionPart(forStr));
    }

    String getConditionPart(String conditionContent) {
        conditionContent = conditionContent
                .replaceFirst("^.*?;\s*?", "(")
                .replaceFirst("\s*;.*?\\).*", ")")
                .replaceAll("(^\\({1}|\\){1}$)", "");
        return conditionContent;
    }
    private String isExpansionFor(String content) {
        Matcher forMatcher = Pattern.compile("for\s*\\(.+?\\)\s*").matcher(content);
        content = forMatcher.find() ? forMatcher.group() : content;
        Matcher isExpansionForMatcher =
                PatternConstants.EXPANSION_FOR_CONTENT_PATTERN.matcher(content);
        return isExpansionForMatcher.find() ?
                isExpansionForMatcher.group().replaceFirst("for\s*", "").replaceAll("(^\\(|\\)$)", "") : "";
    }
}
