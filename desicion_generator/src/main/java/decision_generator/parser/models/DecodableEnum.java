package decision_generator.parser.models;

import java.lang.Class;

import decision_generator.exceptions.NotFindEnumDefineException;

/**
 * DecodableEnum.
 * @author komur
 */
public interface DecodableEnum {
    /**
     * enum decode;
     * @param dEnum
     */
    public static DecodableEnum decode(String targetValue, Class<? extends DecodableEnum> dEnumClass) {
        DecodableEnum[] enums = dEnumClass.getEnumConstants();
        for (DecodableEnum dEnum : enums) {
            if (isMatchDefine(targetValue, dEnum)) {
                return dEnum;
            }
        }
        throw new NotFindEnumDefineException("Not find "
            +  "define in "
            + dEnumClass.getName()
            + " : "
            + targetValue);
    }
    
    /**
     * check matching define and target.
     * @param targetValue
     * @param dEnum
     * @return isMatched
     */
    private static boolean isMatchDefine(String targetValue, DecodableEnum dEnum) {
        String content = dEnum.getContent();
        if(content.equals(targetValue)) {
            return true;
        }
        return false;
    }
    
    String getContent();
}
