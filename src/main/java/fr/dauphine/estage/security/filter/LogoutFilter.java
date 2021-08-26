package fr.dauphine.estage.security.filter;

import fr.dauphine.estage.bootstrap.ApplicationBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LogoutFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LogoutFilter.class);

    @Autowired
    ApplicationBootstrap applicationBootstrap;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        req.getSession().invalidate();
        httpServletResponse.sendRedirect(applicationBootstrap.getAppConfig().getCasUrlLogout());
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
