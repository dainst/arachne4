package de.uni_koeln.arachne.response;

import java.util.HashMap;

public class FieldListMap extends HashMap<String, FieldList> {

    private static final long serialVersionUID = 1L;
	private FieldList mainList;
    private HashMap<String, String> labels = new HashMap<String, String>();

    public FieldListMap(FieldList mainList) {
        super();
        this.mainList = mainList;
    }

    public FieldList getMainList() {
        return mainList;
    }

    @Override
    public FieldList put(String filterCriterion, FieldList targetFieldList) {
        return put(filterCriterion, filterCriterion, targetFieldList);
    }

    public FieldList put(String label, String filterCriterion, FieldList targetFieldList) {
        labels.put(filterCriterion, label);
        return super.put(fullFilterCriterion(filterCriterion), targetFieldList);
    }

    private String fullFilterCriterion(String filterCriterion) {
        return  "[^\\.]*\\." + filterCriterion + "(\\.[^\\.]*)?";
    }

    public FieldList get(String field) {
        for (String key : keySet()) {
            if ((key != null) && field.matches(key)) {
                return super.get(key);
            }
        }
        return mainList;
    }

    public String getLabel(String field) {
        return labels.get(fullFilterCriterion(field));
    }

    /*
    public void compile() {
        for (String key : keySet()) {
            mainList.add(super.get(key));
        }
    }*/

}