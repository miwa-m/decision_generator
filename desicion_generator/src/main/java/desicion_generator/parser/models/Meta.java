package desicion_generator.parser.models;

public interface Meta {
    
    public static enum Scope implements DecodableEnum {
        PUBLIC("public"),
        PROTECTED("protected"),
        PRIVATE("private");
        
        private String syntax;
        
        private Scope(String syntax) {
            this.syntax = syntax;
        }

        @Override
        public String getContent() {
            return this.syntax;
        }
    }
    
    

}
