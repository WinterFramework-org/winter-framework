package froggy.winterframework.web;

import java.util.Map;

public class ModelAndView {
    private String view;
    private Map<String, Object> model;

    public ModelAndView(String view) {
        this.view = view;
    }

    public ModelAndView(String view, Map<String, Object> model) {
        this.view = view;
        this.model = model;
    }

    public String getView() {
        return this.view;
    }

    public Map<String, Object> getModel() {
        return this.model;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }
}
