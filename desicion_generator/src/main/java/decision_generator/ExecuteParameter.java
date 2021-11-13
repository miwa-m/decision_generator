package decision_generator;

import org.kohsuke.args4j.Option;

public class ExecuteParameter {
    @Option(name = "-T", aliases = {"--target" }, required = true, usage = "Target top directory")
    private String targetTopDirectory;

    @Option(name = "-M", aliases = {"--mode" }, usage = "Coverage Mode")
    private String coverageMode = "C1";

    @Option(name = "-D", aliases = {"--directory" }, usage = "Output path for destination directory")
    private String outputDirectory = ".";

    @Option(name = "-F", aliases = {"--file" }, usage = "Output file name")
    private String outputFileName = "decision_matrix";

    public String getTargetTopDirectory() {
        return targetTopDirectory;
    }

    public String getCoverageMode() {
        return coverageMode;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public String getOutputFileName() {
        return outputFileName;
    }
    
    
}
