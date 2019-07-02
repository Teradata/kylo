package com.thinkbiganalytics.ui.config;

/*-
 * #%L
 * kylo-ui-app
 * %%
 * Copyright (C) 2017 - 2019 ThinkBig Analytics, a Teradata Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

public class CustomRememberMeAuthenticationFilter extends RememberMeAuthenticationFilter{

	private ApplicationEventPublisher eventPublisher;
	
	public CustomRememberMeAuthenticationFilter(AuthenticationManager authenticationManager,
			RememberMeServices rememberMeServices) {
		super(authenticationManager, rememberMeServices);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			Authentication rememberMeAuth = getRememberMeServices().autoLogin(request,
					response);

			if (rememberMeAuth != null) {
				try {
					// Store to SecurityContextHolder
					SecurityContextHolder.getContext().setAuthentication(rememberMeAuth);

					onSuccessfulAuthentication(request, response, rememberMeAuth);

					if (logger.isDebugEnabled()) {
						logger.debug("SecurityContextHolder populated with remember-me token: '"
								+ SecurityContextHolder.getContext().getAuthentication()
								+ "'");
					}

					// Fire event
					if (this.eventPublisher != null) {
						eventPublisher
								.publishEvent(new InteractiveAuthenticationSuccessEvent(
										SecurityContextHolder.getContext()
												.getAuthentication(), this.getClass()));
					}
				}
				catch (AuthenticationException authenticationException) {
					if (logger.isDebugEnabled()) {
						logger.debug(
								"SecurityContextHolder not populated with remember-me token, as "
										+ "AuthenticationManager rejected Authentication returned by RememberMeServices: '"
										+ rememberMeAuth
										+ "'; invalidating remember-me token",
								authenticationException);
					}

					getRememberMeServices().loginFail(request, response);

					onUnsuccessfulAuthentication(request, response,
							authenticationException);
				}
			}

			chain.doFilter(request, response);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("SecurityContextHolder not populated with remember-me token, as it already contained: '"
						+ SecurityContextHolder.getContext().getAuthentication() + "'");
			}

			chain.doFilter(request, response);
		}
	}
	public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

}
