package cz.cvut.kbss.jopa.query.soql;

public class SoqlAttribute {

    private SoqlNode firstNode;

    private String value;

    private boolean isNot = false;

    private String operator;

    private String prefix;

    private String rdfType;

    public SoqlAttribute() {
    }

    public SoqlNode getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(SoqlNode firstNode) {
        this.firstNode = firstNode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isNot() {
        return isNot;
    }

    public void setNot(boolean not) {
        isNot = not;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    public String getPrefix(){
        return this.prefix;
    }

    public void setRdfType(String rdfType){
        this.rdfType = rdfType;
    }

    public String getRdfType(){
        return this.rdfType;
    }

    public boolean isFilter() {
        return !operator.isEmpty() && !operator.equals("=");
    }

    private boolean isTable(){
        return !firstNode.hasNextChild();
    }

    public String getFilter(){
        StringBuilder buildFilter = new StringBuilder();
        if(operator.equals("LIKE")){
            buildFilter.append("regex(").append(getAsParam()).append(",").append(this.value).append(")");
        } else {
            buildFilter.append(getAsParam()).append(" ").append(this.operator).append(" ").append(this.value);
        }
        return buildFilter.toString();
    }

    public String getTripplePattern(){
        StringBuilder buildTP = new StringBuilder("?x ");
        if(isTable()){
            buildTP.append(getRdfType()).append(" ")
                    .append(toIri(firstNode.getValue())).append(" . ");
        }else{
            SoqlNode pointer = firstNode.getChild();
            StringBuilder buildParam = new StringBuilder("?");
            buildParam.append(firstNode.getValue());
            buildParam.append(pointer.getCapitalizedvalue());
            buildTP.append(toIri(pointer.getValue())).append(" ?").append(pointer.getValue()).append(" . ");
            while(pointer.hasNextChild()){
                SoqlNode newPointer = pointer.getChild();
                buildTP.append("?").append(pointer.getValue())
                        .append(" ").append(toIri(newPointer.getValue())).append(" ");
                if(newPointer.hasNextChild()){
                    buildTP.append(newPointer.getChild().getValue());
                }else{
                    if (isFilter()){
                        buildTP.append(buildParam);
                    }else{
                        buildTP.append("?").append(this.value.substring(1));
                    }
                }
                buildTP.append(" . ");
                pointer = newPointer;
                buildParam.append(pointer.getCapitalizedvalue());
            }
        }
        return buildTP.toString();
    }

    private StringBuilder toIri(String param){
        StringBuilder sb = new StringBuilder("<");
        sb.append(getPrefix()).append(param).append(">");
        return sb;
    }

    public String getAsParam(){
        StringBuilder buildParam = new StringBuilder("?");
        buildParam.append(firstNode.getValue());
        SoqlNode pointer = firstNode;
        while (pointer.hasNextChild()) {
            pointer = pointer.getChild();
            buildParam.append(pointer.getCapitalizedvalue());
        }
        return buildParam.toString();
    }
}
