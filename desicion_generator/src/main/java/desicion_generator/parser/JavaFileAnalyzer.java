package desicion_generator.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import desicion_generator.exceptions.FailedReadFileException;
import desicion_generator.parser.models.Class;
import desicion_generator.parser.models.Meta;
import desicion_generator.parser.models.Meta.Scope;
import desicion_generator.parser.models.Method;
import desicion_generator.parser.models.Sentence;
import desicion_generator.parser.models.SentenceType;
import desicion_generator.utility.DGLogger;
import desicion_generator.utility.PatternConstants;
import desicion_generator.utility.SyntaxAnalyzeUtility;

/**
 * JavaFile
 * for code analyzing
 * @author komur
 *
 */
public class JavaFileAnalyzer {
    
    private String sourceCode;
    
    public JavaFileAnalyzer(String fileName) {
        this.sourceCode = readFile(fileName);
    }
    
    private String readFile(String fileName) {
        try {
            Path file = Paths.get(fileName);
            return Files.readString(file);
        } catch (IOException e) {
            throw new FailedReadFileException("Failed read file : " + fileName);
        }
    }
    
    public List<Class> analyzeJavaFile() {
        // sourceCode = sourceCode.replaceAll("[(;\s*)([(\r\n)\n\r]\s*)]", "");
        sourceCode = sourceCode.replaceAll("\s*//.*", "");
        sourceCode = sourceCode.replaceAll("(\r\n|\r|\n)\s*", "");
        sourceCode = sourceCode.replaceAll(";\s*", ";");
        sourceCode = sourceCode.replaceAll("\s*/\\*+.+?\\*/", "");
        DGLogger.debug("replaced : \n" + sourceCode);
        List<Class> classes = this.findClasses("", sourceCode);
        outputClasses(classes, "");
        return classes;
    }
    
    public void outputClasses(List<Class> classes, String pad) {
        for (Class c : classes) {
            DGLogger.debug(pad + c.packageName + "." + c.className);
            for (Method m : c.getMethods()) {
                DGLogger.debug(pad + "  " + m.name);
            }
            outputClasses(c.getInnerClasses(), pad + "  ");
        }
    }

    public static String generateRegexClassSignature() {
        // generate metadata regex
        StringBuilder regexBuilder = new StringBuilder();
        regexBuilder.append(".*?(");
        int scopeLength = Meta.Scope.values().length - 1;
        for (int i = 0; i <= scopeLength; i++) {
            Scope scope = Meta.Scope.values()[i];
            regexBuilder.append(scope.getContent());
            if (i < scopeLength) {
                regexBuilder.append("|");
            }
        }
        regexBuilder.append(")?\s*");
        regexBuilder.append("((final\s+)?(static|abstract\s+class|interface|class|enum)\s+)+");
        return regexBuilder.toString();
    }
    
    private List<Class> findClasses(String parentPackage, String code) {
        List<Class> classes = new ArrayList<>();
        String classSignatureRegex = generateRegexClassSignature();
        String className;
        String replacedContent = code;
        
        Matcher packageMatcher = PatternConstants.PACKAGE_SPECIFIED_PATTERN.matcher(replacedContent);
        String packageName = packageMatcher.find() ?
                packageMatcher.group().replaceAll("(^(|\s+?)package)|;\s*$", "") : parentPackage;
        while(true) {
            Matcher classMatcher = PatternConstants.CLASS_IDENTIDY_PATTERN.matcher(replacedContent);
            /*replacedContent = replacedContent.replaceFirst(classSignatureRegex, "");
            className = replacedContent.replaceAll("\s*\\{.*\\}", "");
            if (!className.equals(className.replaceFirst("\\(.*?\\)", ""))
                    || StringUtils.isEmpty(className)) {
                break;
            }*/
            if (!classMatcher.find()) {
                break;
            }
            String matchClass = classMatcher.group();
            className = matchClass.replaceFirst("(class|interface|enum)\s+", "")
                    .replaceFirst("\s*((implements|extends)\s+[^\s]+)?\s*\\{", "");
            Class classInfo = new Class(packageName, className);
            String classContent = replacedContent.replaceFirst("^.*" + SyntaxAnalyzeUtility.transEscaped(matchClass), "{");
            classContent = SyntaxAnalyzeUtility.extractBlock(classContent);
            DGLogger.debug("className : " + className);
            DGLogger.debug("classContent : " + classContent);
            String replaceTarget = SyntaxAnalyzeUtility.transEscaped(classContent);
            DGLogger.debug("replaceTarget : " + replaceTarget);
            DGLogger.debug("replacedContent(B) : " + replacedContent);
            replacedContent = replacedContent.replaceAll("^.*" + className + "\s*((implements|extends)\s+[^\s]+)?\s*" + replaceTarget + "\\}?", "");
            DGLogger.debug("replacedContent(A) : " + replacedContent);
            DGLogger.debug("");
            classInfo.setInnerClasses(findClasses(packageName + "." + className, classContent));
            for (Class c : classInfo.getInnerClasses()) {
                String tmpContent = classContent.replaceFirst(".*\s*" + classSignatureRegex.substring(3) + c.className + "\s*\\{", "{");
                tmpContent = SyntaxAnalyzeUtility.transEscaped(SyntaxAnalyzeUtility.extractBlock(tmpContent));
                classContent = classContent.replaceFirst("\s*" + classSignatureRegex.substring(3) + c.className + "\s*" + tmpContent, "");
            }
            classInfo.setMethods(findMethods(classContent));
            classes.add(classInfo);
            //DGLogger.debug(transEscaped(classContent));
            
        }
        // String className = this.sourceCode.replaceFirst(regexBuilder.toString() + "class\s+", "");
        //findMethod(classContent);
        DGLogger.debug("");
        for(Class c : classes) {
            DGLogger.debug(c.packageName + "." + c.className);
        }
        return classes;
    }
    
    public static String generateRegexMethodSignature() {
        // generate metadata regex
        StringBuilder regexBuilder = new StringBuilder();
        regexBuilder.append("^\\{?(");
        int scopeLength = Meta.Scope.values().length - 1;
        for (int i = 0; i <= scopeLength; i++) {
            Scope scope = Meta.Scope.values()[i];
            regexBuilder.append(scope.getContent());
            if (i < scopeLength) {
                regexBuilder.append("|");
            }
        }
        regexBuilder.append(")?\s*");
        regexBuilder.append("(static\s+final\s+|final\s+static\s+|static\s+|final\s+)?");
        regexBuilder.append("\s*?.+?\s*\\(.*?\\)\s*\\{");
        //regexBuilder = new StringBuilder("([^\s]+\s+){1,4}");
        return regexBuilder.toString();
    }
    
    private String findMethodDefine(String code) {
        String methodSignature = JavaFileAnalyzer.generateRegexMethodSignature();
        Pattern signaturePattern = Pattern.compile(methodSignature);
        Pattern varPattern = Pattern.compile("\s+=\s");
        while (true) {
            Matcher signatureMatcher = signaturePattern.matcher(code);
            if(!signatureMatcher.find()) {
                return "";
            }
            code = signatureMatcher.group();
            Matcher varMatcher = varPattern.matcher(code);
            if (!varMatcher.find()) {
                return code;
            }
            code = code.replaceFirst("^.+?;", "");
        }
    }
    
    private List<Method> findMethods(String code) {
        DGLogger.debug("code : " + code);
        List<Method> methods = new ArrayList<>();
        String classSigunatureRegex = generateRegexClassSignature().substring(3) + "\s*.+?\s+";
        DGLogger.debug("find method target code : " + code);
        // String replacedMethod = code.replaceAll(classSigunatureRegex, "");
        String replacedMethod = code;
        //Pattern p = Pattern.compile("[^\s]+\s*\\(.*?\\)\s*\\{");
        DGLogger.debug("replacedMethod(B1) : " + replacedMethod);
        while (true) {
            /*Matcher m = p.matcher(replacedMethod);
            if(!m.find()) {
                break;
            }*/
            String methodDefine = findMethodDefine(replacedMethod);
            DGLogger.debug("method define : " + methodDefine);
            if (StringUtils.isEmpty(methodDefine)) {
                break;
            }
            //
            String methodName  = methodDefine.replaceFirst("\s*\\((\s*[^\\(\\)]+?\s+.[^\\(\\)]?\s*,\s*)*(\s*[^\\(\\)]+?\s+[^\\(\\)]+?\s*)?\\)\s*\\{$", "").replaceFirst("^.*\s", "");
            // String methodContent = SyntaxAnalyzeUtility.extractBlock(replacedMethod.replaceFirst("^.+?\\)", ""));
            String methodContent = SyntaxAnalyzeUtility.extractBlock(
                    replacedMethod.replaceFirst("^.*?" + SyntaxAnalyzeUtility.transEscaped(methodDefine), "{"));
            Method method = new Method(methodName);
            DGLogger.debug("methodName : " + methodName);
            DGLogger.debug("methodContent : " + methodContent);
            replacedMethod = replacedMethod.replaceAll("^.*" + methodName + "\s*\\(.*?\\)\s*" + SyntaxAnalyzeUtility.transEscaped(methodContent) + "\\}?", "");
            DGLogger.debug("replacedMethod(A) : " + replacedMethod);
            method.setSentences(collectSentences(methodContent));

            SyntaxAnalyzeUtility.outputSentenceInfo(method.getSentences(), "");
            methods.add(method);
            DGLogger.debug("");
        }
        return methods;
    }
    
    private List<Sentence> collectSentences(String code) {
        // code = code.replaceAll("(^\\{|\\}$)", "");
        code = code.replaceAll("(^[\\{\\:]|\\}$)", "");
        List<Sentence> sentenceList = new ArrayList<>();
        while (true) {
            // 最初に出現するブロックを取り出し
            String block = SyntaxAnalyzeUtility.extractBlock(code);
            if (StringUtils.isEmpty(block)) {
                   // || block.equals("{}")) {
                break;
            }
            DGLogger.debug("Extracted block : " + block);
            String escapedBlock = SyntaxAnalyzeUtility.transEscaped(block);
            for (SentenceType type : SentenceType.class.getEnumConstants()) {
                Matcher m = type.getMatcher(escapedBlock, code);
                if (m.find()) {
                    DGLogger.debug("matched sentence : " + type.name());
                    block = m.group();
                    DGLogger.debug("Matched block : " + block);
                    Sentence sentence = type.generateSentenceInstance();
                    String horizonToken = type.getHorizonToken();
                    /* sentence.setInnerSentences(
                            collectSentences(
                                    block.replaceFirst(".+?\\" + horizonToken.substring(0, 1), ""))); */
                    sentence.setInnerSentences(
                            collectSentences(
                                    block.replaceFirst(".+?\\" + horizonToken.substring(0, 1),
                                            horizonToken.substring(1))));
                    sentence.setBlockContent(block);
                    DGLogger.debug("Is return : " + sentence.isReturn());
                    sentenceList.add(sentence);
                    break;
                }
            }
            DGLogger.debug("Find block : " + block);
            DGLogger.debug("code : " + code);
            code = code.replaceFirst(".*?" + escapedBlock, "");
            DGLogger.debug("replaced Code : " + code);
            DGLogger.debug("");
            
            /*if (StringUtils.isEmpty(code)
                    || code.equals("{}")) {
                break;
            }*/
        }
        return sentenceList;
    }
    
   
    
}
    