package froggy.winterframework.web.method.annotation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import froggy.winterframework.web.ModelAndView;
import froggy.winterframework.web.method.HandlerMethod;
import org.junit.Test;

public class ModelAndViewMethodReturnValueHandlerTest {

    private final ModelAndViewMethodReturnValueHandler handler =
        new ModelAndViewMethodReturnValueHandler();

    @Test
    public void supportsModelAndViewReturnType() throws Exception {
        // Given
        HandlerMethod method = handlerMethod("modelAndView");

        // When
        boolean supported = handler.supportsReturnType(method);

        // Then
        assertTrue(supported);
    }

    @Test
    public void supportsModelAndViewSubclassReturnType() throws Exception {
        // Given
        HandlerMethod method = handlerMethod("subclass");

        // When
        boolean supported = handler.supportsReturnType(method);

        // Then
        assertTrue(supported);
    }

    @Test
    public void rejectsObjectReturnType() throws Exception {
        // Given
        HandlerMethod method = handlerMethod("object");

        // When
        boolean supported = handler.supportsReturnType(method);

        // Then
        assertFalse(supported);
    }

    @Test
    public void rejectsUnrelatedReturnType() throws Exception {
        // Given
        HandlerMethod method = handlerMethod("string");

        // When
        boolean supported = handler.supportsReturnType(method);

        // Then
        assertFalse(supported);
    }

    private HandlerMethod handlerMethod(String name) throws Exception {
        return new HandlerMethod(new TestController(), TestController.class,
            TestController.class.getMethod(name));
    }

    private static class TestController {
        public ModelAndView modelAndView() { return null; }

        public DerivedModelAndView subclass() { return null; }

        public Object object() { return null; }

        public String string() { return null; }
    }

    private static class DerivedModelAndView extends ModelAndView {
        private DerivedModelAndView() { super(null); }
    }
}
