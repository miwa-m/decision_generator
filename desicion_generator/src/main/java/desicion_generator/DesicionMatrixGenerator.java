package desicion_generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import desicion_generator.exceptions.InvalidParameterException;
import desicion_generator.exceptions.NotFindEnumDefineException;
import desicion_generator.parser.JavaFileAnalyzer;
import desicion_generator.parser.models.Class;
import desicion_generator.parser.models.CoverageType;
import desicion_generator.parser.models.DecodableEnum;
import desicion_generator.utility.DGLogger;
import out.BookGenerator;

public class DesicionMatrixGenerator {
    
    private CoverageType coverageMode;
    private File targetDirectory;
    private File outputDirectory;
    private String outputFileName;
    
    private ExecuteParameter parameter;
    
    public static DesicionMatrixGenerator buildGenerator(String params[]) {

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
    
    public void executeGenerateDecisionMatrix() {
        printParameter();
        List<File> files = collectJavaFiles(this.targetDirectory);
        printTargetFiles(files);
        List<List<Class>> classes = analyzeClasses(files);
        generateDecitionMatrixFile(classes);
        
        
    }
    
    private void generateDecitionMatrixFile(List<List<Class>> classes) {
        BookGenerator bookGenerator =
                new BookGenerator(this.outputDirectory.getAbsolutePath(), this.outputFileName);
        bookGenerator.setClassList(classes);
        bookGenerator.makeBookFile();
        
    }

    private List<List<Class>> analyzeClasses(List<File> files) {
        List<List<Class>> classes = new ArrayList<List<Class>>();
        for(File file : files) {
            String filePath = file.getAbsolutePath();
            DGLogger.info("Processing " + filePath + "...");
            try {
                JavaFileAnalyzer analyzer = new JavaFileAnalyzer(filePath);
                classes.add(analyzer.analyzeJavaFile());
                DGLogger.info(" ⇒ Completed.");
            } catch(Exception e) {
                DGLogger.info(" ⇒ Failed. (confirm detail.log)");
                DGLogger.error("Failed analyze Java file : " + file);
                DGLogger.debug(e);
            }
        }
        return classes;
    }

    private void printTargetFiles(List<File> files) {
        files.forEach(f -> DGLogger.debug(f.getAbsolutePath()));
    }
    
    private void printParameter() {
        DGLogger.info("Target directory : " + this.targetDirectory.getAbsolutePath());
        DGLogger.info("Coverage mode : " + this.coverageMode.name());   
        DGLogger.info("Output destination directory : " + this.outputDirectory.getAbsolutePath());   
        DGLogger.info("Output file name : " + this.outputFileName);
        
    }

    private List<File> collectJavaFiles(File directory) {
        List<File> files = new ArrayList<>();
        for(File file : directory.listFiles()) {
            if(file.isDirectory()) {
                files.addAll(collectJavaFiles(file));
            } else if (file.isFile() && file.getName().endsWith(".java")) {
                files.add(file);
            }
        }
        return files;
    }
    
    
    private DesicionMatrixGenerator(File targetDirectory,
                                    CoverageType coverageMode,
                                    File outputDirectory,
                                    String outputFileName) {
        this.targetDirectory = targetDirectory;
        this.coverageMode = coverageMode;
        this.outputDirectory = outputDirectory;
        this.outputFileName = outputFileName;
    }
    

    public ExecuteParameter getExecuteParameter() {
        return this.parameter;
    }

    /**
     * main method.
     * args idx
     *  0 : target top directory
     *  1 : mode
     *  2 : output directory
     *  3 : output filename
     *  
     * @param args
     */
    public static void main(String[] args) {
        DesicionMatrixGenerator generator = null;
        try {
            generator = DesicionMatrixGenerator.buildGenerator(args);
        } catch (InvalidParameterException ipE) {
            DGLogger.error(ipE);
            System.exit(1);
        }
        generator.executeGenerateDecisionMatrix();
    }
    
    

}
