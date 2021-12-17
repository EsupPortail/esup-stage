package org.esup_portail.esup_stage.security.filter;

import org.esup_portail.esup_stage.security.TokenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CookieFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CookieFilter.class);

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {

    }

    /**
     * @see Filter#destroy()
     */
    public void destroy() {

    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.info("CookieFilter.doFilter");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        Cookie tokenCookie = new Cookie("idsToken", "");
        tokenCookie.setValue(TokenFactory.create(req));
        tokenCookie.setSecure(req.getRequestURL().toString().startsWith("https"));
        resp.addCookie(tokenCookie);
        chain.doFilter(request, response);
    }
}
