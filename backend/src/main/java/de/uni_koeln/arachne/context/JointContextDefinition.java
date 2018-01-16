package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;

public class JointContextDefinition {

    private String id = "";
    private String description = "";
    private String type = "";
    private String groupBy = "";
    private String groupName = "";
    private String StandardCIDOCConnectionType = "";
    private String fields = "*";
    private String connectFieldParent = "";
    private ArrayList<String> wheres = new ArrayList<String>();
    private ArrayList<JoinDefinition> joins = new ArrayList<JoinDefinition>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null) this.description = description;
    }

    public String getStandardCIDOCConnectionType() {
        return StandardCIDOCConnectionType;
    }

    public void setStandardCIDOCConnectionType(String standardCIDOCConnectionType) {
        if (standardCIDOCConnectionType != null) StandardCIDOCConnectionType = standardCIDOCConnectionType;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        if (fields != null) this.fields = fields;
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type != null) this.type = type;
    }

    public String getConnectFieldParent() {
        return connectFieldParent;
    }

    public void setConnectFieldParent(String connectFieldParent) {
        if (connectFieldParent != null) this.connectFieldParent = connectFieldParent;
    }

    public Boolean check() {
        return (!(connectFieldParent.equals("") || type.equals("")));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getWheres() {
        return (ArrayList<String>) wheres.clone();
    }

    public void addWhere(String where) {
        wheres.add(where);
    }

    public ArrayList<JoinDefinition> getJoins() {
        return (ArrayList<JoinDefinition>) joins.clone();
    }

    public void addJoin(String type, String connectFieldParent, String connectFieldChild) {
        joins.add(new JoinDefinition(type, connectFieldParent, connectFieldChild));
    }


    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isGrouped() {
        return (!((groupName.equals("") || (groupName == null)) || ((groupBy.equals("") || (groupBy == null)))));
    }

}


