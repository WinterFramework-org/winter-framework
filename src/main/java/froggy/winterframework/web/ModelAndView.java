package froggy.winterframework.web;

import java.util.HashMap;
import java.util.Map;

/**
 * MVC 패턴에서 Model(데이터)과 View(화면 정보)를 함께 관리하는 객체.
 *
 * <p>Handler(Controller)의 실행 결과를 저장하고
 * DispatcherServlet이 해당 정보를 View로 전달할 수 있도록 지원.
 */
public class ModelAndView {
    private String view;
    private Map<String, Object> model;

    public ModelAndView(String view) {
        this(view, new HashMap<String, Object>());
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
