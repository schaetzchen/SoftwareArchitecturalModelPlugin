package plugindata.entities.architectural_model;

//public class ModelArc <S extends IModelNode, T extends IModelNode> {
//
//    private S source;
//    private T target;
//    private String attributes;
//
//    public ModelArc(S source, T target) {
//        this.source = source;
//        this. target = target;
//        attributes = "";
//    }
//
//    public ModelArc(S source, T target, String attributes) {
//        this.source = source;
//        this.target = target;
//        this.attributes = attributes;
//    }
//
//    public String getDotCode() {
//        return source.getName() + " -> " + target.getName() + "[" + attributes + "]";
//    }
//}

public class ModelArc {

    private String source;
    private String target;
    private String attributes;

    public ModelArc(String source, String target) {
        this.source = source;
        this. target = target;
        attributes = "";
    }

    public ModelArc(String source, String target, String attributes) {
        this.source = source;
        this.target = target;
        this.attributes = attributes;
    }

        public String getDotCode() {
        return source + " -> " + target + "\"[" + attributes + "]\n";
    }
}