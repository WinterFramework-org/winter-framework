package froggy.winterframework.web;

import java.io.IOException;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DispatcherServlet {

    public void doDispatch(HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView)
        throws ServletException, IOException {

        for(Map.Entry<String, Object> entry : modelAndView.getModel().entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(modelAndView.getView());
        dispatcher.forward(request, response);
    }
}