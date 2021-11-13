package desicion_generator.parser.models;

import java.util.ArrayList;
import java.util.List;

public class Block {
    public List<Block> innerBlock = new ArrayList<>();
    public String blockContent;
}
