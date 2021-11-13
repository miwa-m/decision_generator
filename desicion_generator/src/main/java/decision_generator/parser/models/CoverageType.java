package decision_generator.parser.models;

public enum CoverageType implements DecodableEnum {
    
    C0("C0"),
    C1("C1"),
    C2("C2"),
    MCC("MC");

    private String code;
    
    private CoverageType(String code) {
        this.code = code;
    }
    
    @Override
    public String getContent() {
        return this.code;
    }

}
