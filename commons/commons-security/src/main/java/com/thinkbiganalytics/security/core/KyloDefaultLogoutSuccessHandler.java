package com.thinkbiganalytics.security.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * Default logout handler for Kylo UI & services.  Additional handlers can be added to
 * the UI or services app by creating a bean listener of type KyloLogoutSuccessListener
 */
@Component
public class KyloDefaultLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements KyloLogoutSuccessHandler {
    protected static final Logger logger = LoggerFactory.getLogger(KyloDefaultLogoutSuccessHandler.class);

    private Collection<KyloLogoutSuccessListener> listeners = new LinkedList<>();

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String name = (authentication!=null)?authentication.getName():"UNKNOWN_USER";

        logger.debug("Logout occurred for '{}'", name);

        for( KyloLogoutSuccessListener listener : listeners ) {
            listener.onLogoutSuccess(request, response, authentication);
        }

        // Now allow SimpleUrlLogoutSuccessHandler parent to redirect
        super.onLogoutSuccess(request,response,authentication);
    }

    @Override
    public void addListener( @NotNull KyloLogoutSuccessListener kyloLogoutSuccessListener) {
        listeners.add(kyloLogoutSuccessListener);
    }
}
