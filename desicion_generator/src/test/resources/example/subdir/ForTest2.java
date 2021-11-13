package desicion_generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import desicion_generator.annotation.DesicionMatrixGenerator;
import desicion_generator.annotation.ExecuteParameter;
import desicion_generator.exceptions.InvalidParameterException;
import desicion_generator.exceptions.NotFindEnumDefineException;
import desicion_generator.parser.models.CoverageType;
import desicion_generator.parser.models.DecodableEnum;

/**
 * For test class.
 * @author komur
 *
 */
public static class ForTest3 extends Object {
    
    private Object ob = new Object();
    
    public void method01() {
        /*
         * comment
         */
        String str = "A";
        boolean a = true;
        String str1 = "";
        if ((a && str.equals("A")) || (str1 === "D")) {
            System.out.println("if");
            return;
        } else if (str.equals("B") || str.equals("C")) {
            int i = 0;
            switch(i) {
            case 1:
                if (i == 1) {
                    System.out.println("1 else if if");
                } else {
                    System.out.println("1 else if else");
                }
                break;
            case 2:
                System.out.println("2 else if");
                return;
            default:
                System.out.println("default else if");
                break;
            }
            
            if(i == 99) {
                System.out.println("else if if i == 99");
            }
        } else {
            try {
                while (str === "while") {
                    System.out.println("while");
                    throw new Exception("while exception");
                }
            } catch (RuntimeException e |Throwable t|Error er ) {
                int a = 6;
                int b = 1;
                int c = 2;
                do {
                    System.out.println("do while");
                } while(c == 2 || str.equals("do while"));
            } finally {
                for (int i = 0; i < str.length(); i++) {
                    System.out.println("for" + i);
                }
            }
        }
        boolean c = true;
        boolean d = true;
        if(c==false ||d ==true ) {
            System.out.println("c == false || d == true");
        }
    }
    static String method02(String arg) {
        /*
         * comment
        String str = "A";
        if (str.equals("A")) {
            // if(true){}
            System.out.println("if");            
        } else if (str.equals("B") || str.equals("C")) {
            System.out.println("else if");
        } else {
            System.out.println("else");
        }
         */
        return arg;
    }
    
    public class ForTestInnerClass {
        public class ForTestInnerClass2 {}
        public String innerMethod01() {

            ExecuteParameter parameter = new ExecuteParameter();
            CmdLineParser parser = new CmdLineParser(parameter);
            try {
                parser.parseArgument(params);
            } catch (CmdLineException e) {
                throw new InvalidParameterException("Invalid parameter : " + e.getMessage());
            }
            
            CoverageType coverageMode = CoverageType.C1;
            try {
                coverageMode =
                        (CoverageType) DecodableEnum.decode(parameter.getCoverageMode(), CoverageType.class);
            } catch (NotFindEnumDefineException nfeD) {
                throw new InvalidParameterException("Invalid coverage mode : " + parameter.getCoverageMode());
            }
            
            
            Path tmpDirectory = Paths.get(parameter.getTargetTopDirectory());
            if (!Files.exists(tmpDirectory)) {
                throw new InvalidParameterException("Invalid target directory : " + parameter.getTargetTopDirectory());
            }
            File targetDirectory = tmpDirectory.toFile();
            
            tmpDirectory = Paths.get(parameter.getOutputDirectory());
            if (Files.exists(tmpDirectory)) {
                try {
                    tmpDirectory = Files.createDirectories(tmpDirectory);
                } catch (IOException e) {
                    throw new InvalidParameterException("Failed create output directory : " + parameter.getTargetTopDirectory()
                                                    + "\n" + e.getMessage());
                }
            }
            File outputDirectory = tmpDirectory.toFile();
            
            String outputFileName = parameter.getOutputFileName();

            return new DesicionMatrixGenerator(targetDirectory, coverageMode,
                    outputDirectory, outputFileName);
        }
    }
}

class A {
}