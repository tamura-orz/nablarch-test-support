package nablarch.test.support.web.servlet;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class MockServletFilterConfig implements FilterConfig {

    private ServletContext context;

    @Override
    public String getFilterName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getInitParameter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration getInitParameterNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

    public MockServletFilterConfig setServletContext(ServletContext context) {
        this.context = context;
        return this;
    }

}
