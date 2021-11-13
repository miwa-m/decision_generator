package decision_generator.utility;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DGLogger {

    private static Logger logger = LogManager.getLogger();
    
    public static void info(String msg) {
        logger.info(msg);
    }

    public static void debug(String msg) {
        logger.debug(msg);
    }
    public static void debug(Exception exception) {
        logger.debug("Occured Error : \n"
                + exception.getMessage()
                + getStackTrace(exception));
    }
    
    public static void warn(String msg) {
        logger.warn(msg);
    }

    public static void trace(String msg) {
        logger.trace(msg);
    }

    public static void error(String msg) {
        logger.error(msg);
    }

    public static void error(Exception exception) {
        logger.error("Occured Error : \n"
                    + exception.getMessage()
                    + getStackTrace(exception));
    }
    public static void fatal(String msg) {
        logger.fatal(msg);
    }
    
    private static String getStackTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        pw.flush();
        return sw.toString();
        
    }
}
