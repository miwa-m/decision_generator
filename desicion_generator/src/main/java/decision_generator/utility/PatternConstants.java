package decision_generator.utility;

import java.util.regex.Pattern;

public final class PatternConstants {
    public static final Pattern TOP_BRACKET_PATTERN =
                Pattern.compile("^\\(+");
    public static final Pattern BOTTOM_BRACKET_PATTERN =
                Pattern.compile("\\)+$");
    public static final Pattern CLASS_IDENTIDY_PATTERN =
                Pattern.compile("(class|interface|enum)\s+[^\s]+\s*((implements|extends)\s+[^\s]+)?\s*\\{");
    public static final Pattern PACKAGE_SPECIFIED_PATTERN =
                Pattern.compile("^(|\s+?)package\s*.+?;");
    public static final Pattern CASEBLOCK_START_PATTERN =
                Pattern.compile("^.*?(case\s+.+?|default)\s*:");
    public static final Pattern CASEBLOCK_END_PATTERN =
                Pattern.compile("(case\s+.+?|default)\s*:");
    public static final Pattern DEFAULTBLOCK_START_PATTERN =
            Pattern.compile("^\s*?default\s*?:");
    public static final Pattern CASE_ISBREAK_PATTERN =
            Pattern.compile("^break;$");
    public static final Pattern DOWHILE_END_PATTERN =
            Pattern.compile("^\\}\s*while\s*\\(.*\\)\s*;");
    public static final Pattern EXPANSION_FOR_CONTENT_PATTERN =
            Pattern.compile("for\s*\\(\s*[^\\)]+?\s*:\s*.+?\s*\\)+");
    public static final Pattern IS_RETURN_OR_THROW_PATTERN =
            Pattern.compile("[;\\{\\}\\:]\s*(return(\s+.*?|\s*)|throw\s*.+?);");
    public static final Pattern START_MIDDLE_BRACKET_CONTENT_PATTERN =
            Pattern.compile("^.+?\\)\s*\\{");
    public static final Pattern START_CASE_CONTENT_PATTERN =
            Pattern.compile("^\s+?(case\s+|default)\s*:");
            
}
