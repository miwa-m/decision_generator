package out;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import desicion_generator.exceptions.FailedGenerateBookException;
import desicion_generator.parser.models.Case;
import desicion_generator.parser.models.Catch;
import desicion_generator.parser.models.Class;
import desicion_generator.parser.models.Condition;
import desicion_generator.parser.models.ConditionCombination;
import desicion_generator.parser.models.If;
import desicion_generator.parser.models.Method;
import desicion_generator.parser.models.Pattern;
import desicion_generator.parser.models.Pattern.Bool;
import desicion_generator.parser.models.Sentence;
import desicion_generator.parser.models.SentenceType;
import desicion_generator.parser.models.Switch;
import desicion_generator.parser.models.Token;
import desicion_generator.utility.ColumnIdentify;
import desicion_generator.utility.DGLogger;
import out.model.ConditionOutputInfo;

public class BookGenerator {

    private static final int CHARACTER_WIDTH = 256;
    private String fileName;
    private String destinationDirectory;
    private Workbook testCaseBook;
    private List<List<Class>> classList;
    private int maxMethodLength = 0;
    private int maxClassLength = 0;
    private int maxConditionLength = 0;
    
    
    public BookGenerator(String destinationDirectory, String fileName) {
        this.destinationDirectory =
                StringUtils.isEmpty(destinationDirectory) ?
                        Paths.get("").toAbsolutePath().toString()
                        : destinationDirectory;
        this.fileName = fileName;
        this.testCaseBook = new XSSFWorkbook();
    }
    
    public void setClassList(List<List<Class>> classList) {
        this.classList = classList;
    }
    
    public void makeBookFile() {
        // generate sheet per classes 
        generateClassSheet();
        writeFile();
    }
    
    private void settingColumnWidth(Sheet sheet) {
        sheet.setColumnWidth(ColumnIdentify.A.getNum(), 2 * CHARACTER_WIDTH);
        
        if (this.maxConditionLength > 0) {
            if (this.maxConditionLength > 255) {
                this.maxConditionLength = 255;
            }
            sheet.setColumnWidth(ColumnIdentify.D.getNum(), maxConditionLength * CHARACTER_WIDTH);
        }
        if (this.maxMethodLength > 0) {
            if (this.maxMethodLength > 255) {
                this.maxMethodLength = 255;
            }
            sheet.setColumnWidth(ColumnIdentify.C.getNum(), maxMethodLength * CHARACTER_WIDTH);
        }
        if (this.maxClassLength > 0) {
            if (this.maxClassLength > 255) {
                this.maxClassLength = 255;
            }
            sheet.setColumnWidth(ColumnIdentify.B.getNum(), maxClassLength * CHARACTER_WIDTH);
        }
    }
    
    private void generateClassSheet() {
        for (List<Class> classes : this.classList) {
            if (classes == null || classes.size() == 0) {
                continue;
            }
            String sheetName = generateSheetName(classes.get(0));
            Sheet sheet = testCaseBook.createSheet(sheetName);

            writeHeaderContent(sheet);
            outputCaseContent(sheet, classes, 1, 1);
            settingColumnWidth(sheet);
        }
    }
    
    private int outputCaseContent(Sheet sheet, List<Class> classes, int rowPosition, int classNum) {
        int rowCount = 0;
        for (Class classInfo : classes) {
            rowCount += writeClassInfo(sheet, classNum, classInfo, rowPosition + rowCount);
            rowCount += writeAboutMethod(sheet, classInfo.getMethods(), rowPosition + rowCount);
            classNum++;
            rowCount += outputCaseContent(sheet, classInfo.getInnerClasses(), rowPosition + rowCount, classNum);
            classNum += classInfo.getInnerClasses().size() + getChildClassCount(classInfo.getInnerClasses());
        }
        return rowCount;
    }
    
    private int getChildClassCount(List<Class> classes) {
        int classCount = 0;
        for (Class cls : classes) {
            classCount += cls.getInnerClasses().size();
            classCount += getChildClassCount(cls.getInnerClasses());
        }
        return classCount;
    }
    
    private int writeAboutMethod(Sheet sheet, List<Method> methods, int rowPosition) {
        int rowCount = 0;
        int methodNum = 1;
        for (Method method : methods) {
            rowCount += writeMethodInfo(sheet, methodNum, method, rowPosition + rowCount);
            rowCount += writeAboutBlock(sheet, method.getSentences(), rowPosition + rowCount - 1);
            methodNum++;
            if (this.maxMethodLength < method.name.length()) {
                this.maxMethodLength = method.name.length();
            }
        }
        return rowCount;
    }
    
    private int writeAboutBlock(Sheet sheet, List<Sentence> sentences, int rowPosition) {
        int rowCount = 0;
        rowCount += writeAboutCondition(sheet, sentences, rowPosition);
        /**
        for (int idx = sentences.size(); idx >= 0;i--) {
            // take from the back
            Sentence sentence = sentences.get(i);
            rowCount += writeCase(sheet, )
        }*/
        return rowCount;
    }
    
    private int writeAboutCondition(Sheet sheet, List<Sentence> sentences, int rowPosition) {
        int rowCount = 0;
        Sentence beforeSentence;
        // write condition
        ConditionOutputInfo conditionHierarchy =
                writeConditionContent(sheet, sentences, rowPosition);
        
        // write pattern
        // default coverage 100%
        // (T or F about per conditions as bool)
        // genarateCombinationPattern(conditionHierarchy, rowPosition);
        List<Entry<Integer, List<String>>> writePositionAndCombinationList =
                new ArrayList<Entry<Integer, List<String>>>();
        generateCombinationPattern(conditionHierarchy);
        writeCombinationPattern(sheet, conditionHierarchy, writePositionAndCombinationList,
                rowPosition, ColumnIdentify.E);
        if (conditionHierarchy.isEmpty()){
            return 0;
        }
        return conditionHierarchy.getUsingRowCountSelf() + conditionHierarchy.getUsingRowCountSubCondition() - 1;
    }
    

    private ColumnIdentify writeCombinationPattern(Sheet sheet,
                                        ConditionOutputInfo conditionHierarchy,
                                        List<Entry<Integer, List<String>>> writePositionAndCombinationList,
                                        int rowPosition, ColumnIdentify columnPosition) {
        int rowCount = 0;
        Map<Sentence, ConditionOutputInfo> sentencePerConditionInfo =
                conditionHierarchy.getConditionInfoMap();
        Map<Sentence, List<ConditionCombination>> sentencePerConditions =
                conditionHierarchy.getConditionPattern();
        
        Set<Entry<Sentence, ConditionOutputInfo>> conditionInfoSet = sentencePerConditionInfo.entrySet();
        List<Entry<Sentence, ConditionOutputInfo>> conditionInfoList = new ArrayList<Entry<Sentence, ConditionOutputInfo>>();
        // convert set to list
        for(Entry<Sentence, ConditionOutputInfo> conditionInfoEntry : conditionInfoSet) {
            conditionInfoList.add(conditionInfoEntry);
        }
        int conditionSize = conditionInfoList.size();
        List<Entry<Integer, List<String>>> beforePositionAndCombinationList = null;
        for (int idx = 0; idx < conditionSize; idx++) {
            writePositionAndCombinationList =
                    removeUnnecessayPositionAndCombination(writePositionAndCombinationList, beforePositionAndCombinationList);
            Entry<Sentence, ConditionOutputInfo> conditionInfoEntry = conditionInfoList.get(idx);
            Sentence sentence = conditionInfoEntry.getKey();
            SentenceType type = sentence.getSentenceType();
            ConditionOutputInfo childConditionInfo = conditionInfoEntry.getValue();
                List<ConditionCombination> combinationList =
                        conditionHierarchy.getCombinationList(sentence);
                ConditionCombination useCombination = null;
                switch (type) {
                case SWITCH:
                    Switch switchSentence = (Switch) sentence;
                    List<Sentence> cases = switchSentence.getInnerSentences();
                    int insertPoint = 0;
                    for (Sentence caseSentence : cases) {
                        ConditionOutputInfo caseCondition =
                                childConditionInfo.getConditionInfoMap().get(caseSentence);
                        Entry<Sentence, ConditionOutputInfo> caseEntry = new SimpleEntry<>(caseSentence, caseCondition);
                        conditionHierarchy.setCombinationList(caseSentence,
                                childConditionInfo.getCombinationList(caseSentence));
                        conditionInfoList.add(idx + insertPoint + 1, caseEntry);
                        
                        insertPoint++;
                    }
                    conditionSize += insertPoint;
                    break;
                case IF:
                case ELSE_IF:
                case WHILE:
                case DO_WHILE:
                case FOR:
                case CASE:
                case CATCH:
                    if (!(sentence instanceof Case)
                            || !(((Case) sentence).isDefault())) {
                        List<String> authenticityList = new ArrayList<String>();
                        for (boolean throughResult : new boolean[]{true, false}) {
                            useCombination = getEnableCombination(combinationList, throughResult);
                            authenticityList = useCombination.transAuthenticityStringList();
                            Entry<Integer, List<String>> writePositionEntry =
                                    new SimpleEntry<Integer, List<String>>(rowPosition + rowCount,
                                            authenticityList);
                            writePositionAndCombinationList.add(writePositionEntry);
                            beforePositionAndCombinationList =
                                    copyPositionAndCombinationList(writePositionAndCombinationList);
                            if (throughResult) {
                                if (childConditionInfo.isEmpty()) {
                                    writeCombination(sheet, writePositionAndCombinationList, columnPosition);
                                    columnPosition = columnPosition.next();
                                } else {
                                    columnPosition = writeCombinationPattern(sheet,
                                             childConditionInfo, writePositionAndCombinationList,
                                             rowPosition + rowCount + authenticityList.size(), columnPosition);
                                }
                                writePositionAndCombinationList.remove(writePositionEntry);
                                beforePositionAndCombinationList.remove(writePositionEntry);
                                writePositionAndCombinationList =
                                        removeUnnecessayPositionAndCombination(writePositionAndCombinationList, beforePositionAndCombinationList);
                            } else {
                                if (idx == conditionSize - 1) {
                                    writeCombination(sheet, writePositionAndCombinationList, columnPosition);
                                    columnPosition = columnPosition.next();
                                }
                            }
                        }
                        rowCount += authenticityList.size();
                        rowCount += calcurateRowCountContainsChild(childConditionInfo);
                        break;
                    }
                case ELSE:
                case TRY:
                case FINALLY:
                    if (!childConditionInfo.isEmpty()) {
                        beforePositionAndCombinationList =
                                copyPositionAndCombinationList(writePositionAndCombinationList);
                        columnPosition = writeCombinationPattern(sheet,
                                childConditionInfo, writePositionAndCombinationList,
                                rowPosition + rowCount, columnPosition);
                        rowCount += calcurateRowCountContainsChild(childConditionInfo);
                    }
                    break;
                default:
                    break;
                }
        }
        return columnPosition;
    }
    
    private List<Entry<Integer, List<String>>> removeUnnecessayPositionAndCombination(
            List<Entry<Integer, List<String>>> writePositionAndCombinationList,
            List<Entry<Integer, List<String>>> beforePositionAndCombinationList) {
        if (beforePositionAndCombinationList != null) {
            // List<Entry<Integer, List<String>>> removeList = new ArrayList<Entry<Integer, List<String>>>();
            for (Entry<Integer, List<String>> entry : writePositionAndCombinationList) {
                if (!beforePositionAndCombinationList.contains(entry)) {
                    List<String> boolList = entry.getValue();
                    for (int i = 0; i < boolList.size(); i++) {
                        boolList.set(i, "*");
                    }
                }
            }
            // writePositionAndCombinationList.removeAll(removeList);
        }
        return writePositionAndCombinationList;
    }

    private int calcurateRowCountContainsChild(ConditionOutputInfo conditionInfo) {
        return conditionInfo.getUsingRowCountSelf() + conditionInfo.getUsingRowCountSubCondition();
    }
    
    private ConditionCombination getEnableCombination(List<ConditionCombination> conditionCombinations, Boolean result) {
        ConditionCombination matchedCombination = null;
        for (ConditionCombination cc : conditionCombinations) {
            if (cc.isTrue() == result) {
                matchedCombination = cc;
                break;
            }
        }
        return matchedCombination;
    }
    
    private void generateCombinationPattern(ConditionOutputInfo conditionInfo) {
        // 組み合わせを算出
        int rowCount = 0;
        ConditionOutputInfo tmpConditionInfo = conditionInfo;
        for (Entry<Sentence, ConditionOutputInfo> entry : tmpConditionInfo.getConditionInfoMap().entrySet()) {
            Sentence sentence = entry.getKey();
            ConditionOutputInfo innerConditionOutputInfo = entry.getValue();
            SentenceType type = sentence.getSentenceType();
            List<ConditionCombination> combinationList = null;
            Sentence tmpSentence = null;
            switch (type) {
            case IF:
            case ELSE_IF:
            case WHILE:
            case DO_WHILE:
            case FOR:
                If stcIf = (If) sentence;
                Pattern conditionPattern = stcIf.getPattern();
                combinationList =
                        generateCombinationList(conditionPattern);
                tmpConditionInfo.setCombinationList(sentence, combinationList);
                break;
            case CASE:
                Case stcCase = (Case) sentence;
                if (!stcCase.isDefault()) {
                    // ignore default case
                    Pattern casePattern = stcCase.getCase();
                    combinationList =
                            generateCombinationList(casePattern);
                    tmpConditionInfo.setCombinationList(sentence, combinationList);
                }
                break;
            case CATCH:
                Catch stcCth = (Catch) sentence;
                Pattern exceptionPattern = stcCth.getExceptions();
                combinationList =
                        generateCombinationList(exceptionPattern);
                tmpConditionInfo.setCombinationList(sentence, combinationList);
                // set related try too
                tmpConditionInfo.setCombinationList(tmpSentence, combinationList);
                break;
            case SWITCH:
                // analyze child case
                /*Switch stcSwt = (Switch) sentence;
                Pattern casesPattern = stcSwt.getCases();
                combinationList =
                        generateCombinationList(casesPattern);
                tmpConditionInfo.setCombinationList(sentence, combinationList);
                */
            case TRY:
                // tmpSentence = sentence;
                // break;
            case FINALLY:
                break;
            default:
                break;
            }
            DGLogger.debug("sentence type :" + sentence.getClass().getSimpleName() );
            if(combinationList != null) {
                for (ConditionCombination cc : combinationList) {
                    for (Boolean b : cc.getConditionBoolList()) {
                        DGLogger.debug(b + " ");
                    }
                    DGLogger.debug("→ " + cc.isTrue());
                }
            }
            if (innerConditionOutputInfo != null
                    || !innerConditionOutputInfo.isEmpty()) {
                generateCombinationPattern(innerConditionOutputInfo);
            }
        }
    }

    private void writeCombination(Sheet sheet,
            List<Entry<Integer, List<String>>> writePositionAndCombinationList,
            ColumnIdentify columnPosition) {
        Row caseNoRow = getRow(sheet, 1);
        Cell caseNoCell = getCell(caseNoRow, columnPosition.getNum());
        if (columnPosition.getNum() > ColumnIdentify.D.getNum()) {
            Cell prepCaseNoCell = getCell(caseNoRow, columnPosition.prev().getNum());
            caseNoCell.setCellValue(prepCaseNoCell.getNumericCellValue() + 1);
        } else {
            caseNoCell.setCellValue(1);
        }
        Font font = this.testCaseBook.createFont();
        font.setBold(true);
        CellUtil.setFont(caseNoCell, font);
        CellUtil.setAlignment(caseNoCell, HorizontalAlignment.CENTER);
        sheet.setColumnWidth(columnPosition.getNum(), 768);
        // DGLogger.debug(columnPosition.name());
        for (Entry<Integer, List<String>> writeCombinationInfo : writePositionAndCombinationList) {
            int rowCount = 0;
            int rowPosition = writeCombinationInfo.getKey();
            for (String bool : writeCombinationInfo.getValue()) {
                Row row = getRow(sheet, rowPosition + rowCount);
                Cell cell = getCell(row, columnPosition.getNum());
                CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
                cell.setCellValue(bool);
                // DGLogger.debug(bool + " ");
                rowCount++;
            }
        }
        DGLogger.debug("\n");
        
        return;
    }
    
    private ConditionOutputInfo writeConditionContent(Sheet sheet, List<Sentence> sentences, int rowPosition) {
        int rowCount = 0;
        ConditionOutputInfo conditionHierarchy = new ConditionOutputInfo();
        for (Sentence sentence : sentences) {
            List<String> conditionContents = sentence.getConditionContents();
            for (String condition : conditionContents) {
                Row row = getRow(sheet, rowPosition + rowCount);
                Cell conditionCell = row.createCell(ColumnIdentify.D.getNum());
                DGLogger.debug((rowPosition + rowCount) + " : " + condition);
                conditionCell.setCellValue(condition);
                CellUtil.setAlignment(conditionCell, HorizontalAlignment.CENTER);
                // decide self using row count
                rowCount++;
                if (this.maxConditionLength < condition.length()) {
                    this.maxConditionLength = condition.length();
                }
            }
            ConditionOutputInfo subConditionInfo =
                    writeConditionContent(sheet, sentence.getInnerSentences(), rowPosition + rowCount);
            
            subConditionInfo.setParentOutputInfo(conditionHierarchy);
            subConditionInfo.setParentSentence(sentence);
            
            conditionHierarchy.addSubConditionInfo(sentence, subConditionInfo);
            rowCount += subConditionInfo.getUsingRowCountSelf()
                    + subConditionInfo.getUsingRowCountSubCondition();
        }
        return conditionHierarchy;
    }
    
    private Row getRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        return row; 
    }
    
    private Cell getCell(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            cell = row.createCell(cellIndex);
        }
        return cell; 
    }

    private int writeMethodInfo(Sheet sheet, int methodNum, Method method, int rowPosition) {
        Row row = sheet.createRow(rowPosition);
        // write method number
        Cell methodNumberCell = row.createCell(ColumnIdentify.B.getNum());
        methodNumberCell.setCellValue(methodNum);
        
        // write method name
        Cell methodNameCell = row.createCell(ColumnIdentify.C.getNum());
        methodNameCell.setCellValue(method.name);
        CellUtil.setAlignment(methodNameCell, HorizontalAlignment.CENTER);
        return 1;
    }
    
    private int writeClassInfo(Sheet sheet, int classNum, Class classInfo, int rowPosition) {
        // write class number
        Row row = sheet.createRow(rowPosition);
        Cell classNumberCell = row.createCell(ColumnIdentify.A.getNum());
        classNumberCell.setCellValue(classNum);
        
        // write class name
        Cell classNameCell = row.createCell(ColumnIdentify.B.getNum());
        CellUtil.setAlignment(classNameCell, HorizontalAlignment.CENTER);
        
        // classNameCell.setCellValue(classInfo.packageName + "." + classInfo.className);
        classNameCell.setCellValue(classInfo.className);
        if (this.maxClassLength < classInfo.className.length()) {
            this.maxClassLength = classInfo.className.length();
        }
        return 1;
    }
    
    private String generateSheetName(Class cls) {
        String sheetName = cls.className;
        int prefixCnt = 1;
        while (testCaseBook.getSheet(sheetName) != null) {
            sheetName = cls.className + "_" + String.valueOf(prefixCnt);
        }
        return sheetName;
    }
    
    private void writeFile() {
        FileOutputStream fileOS;
        try {
            fileOS = new FileOutputStream(this.destinationDirectory + "\\" + this.fileName + ".xlsx");
            testCaseBook.write(fileOS);
        } catch (IOException e) {
            throw new FailedGenerateBookException("Failed generate file : " + this.fileName, e);
        }
    }

    private int writeHeaderContent(Sheet sheet) {
        Font font = this.testCaseBook.createFont();
        font.setBold(true);
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(ColumnIdentify.A.getNum());
        cell.setCellValue("");
        CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
        CellUtil.setFont(cell, font);
        cell = row.createCell(ColumnIdentify.B.getNum());
        cell.setCellValue("Class Name");
        CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
        CellUtil.setFont(cell, font);
        cell = row.createCell(ColumnIdentify.C.getNum());
        cell.setCellValue("Method Name");
        CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
        CellUtil.setFont(cell, font);
        cell = row.createCell(ColumnIdentify.D.getNum());
        cell.setCellValue("Codition");
        CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
        CellUtil.setFont(cell, font);
        cell = row.createCell(ColumnIdentify.E.getNum());
        cell.setCellValue("Cases");
        CellUtil.setAlignment(cell, HorizontalAlignment.LEFT);
        CellUtil.setFont(cell, font);
        return 1;
    }

    private List<ConditionCombination> generateCombinationList(Pattern pattern) {

        DGLogger.debug("");
        List<ConditionCombination> combinationList =
                new ArrayList<ConditionCombination>();
        int tokenSize = getTokenSize(pattern.getTokens());
        for (int i = 0; i < Math.pow(2, tokenSize); i++) {
            String binaryString = String.format("%" +tokenSize + "s",
                Integer.toBinaryString(i)).replaceAll(" ", "0");
            ConditionCombination combination = new ConditionCombination();
            
            List<Boolean> boolList = new ArrayList<>();
            char[] binArray = binaryString.toCharArray();
            boolean finallyResult = true;
            
            for (int j = 0; j < tokenSize; j++) {
                char b = binArray[j];
                boolean result = b == '1';
                
                boolList.add(result);
            }
            combination.setConditionBoolList(boolList);
            combination.setResult(emuratePattern(pattern, boolList));
            combinationList.add(combination);
        }
       return combinationList;
    }
    
    private boolean emuratePattern(Pattern pattern, List<Boolean> combination) {
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
                        combination.subList(addIdx, addIdx + subTokenSize));
                addIdx += subTokenSize;
            } else if (token instanceof Condition) {
                tmpResult = combination.get(addIdx);
                addIdx++;
                // DGLogger.debug(tmpResult + " ");
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

    private List<Entry<Integer, List<String>>> copyPositionAndCombinationList(List<Entry<Integer, List<String>>> writePositionAndCombinationList) {
        List<Entry<Integer, List<String>>> dstList =
                new ArrayList<Entry<Integer, List<String>>>();
        dstList.addAll(writePositionAndCombinationList);
        return dstList;
    }
}
