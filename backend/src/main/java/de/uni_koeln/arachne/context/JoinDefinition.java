package de.uni_koeln.arachne.context;

public class JoinDefinition {

    private String type = "";
    private String connectFieldParent = "";
    private String connectFieldChild = "";

    public JoinDefinition(String type, String connectFieldParent, String connectFieldChild) {
        this.setType(type);
        this.setConnectFieldParent(connectFieldParent);
        this.setConnectFieldChild(connectFieldChild);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConnectFieldParent() {
        return connectFieldParent;
    }

    public void setConnectFieldParent(String connectFieldParent) {
        if (connectFieldParent != null) this.connectFieldParent = connectFieldParent;
    }

    public String getConnectFieldChild() {
        return connectFieldChild;
    }

    public void setConnectFieldChild(String connectFieldChild) {
        if (connectFieldChild != null) this.connectFieldChild = connectFieldChild;
    }
}

