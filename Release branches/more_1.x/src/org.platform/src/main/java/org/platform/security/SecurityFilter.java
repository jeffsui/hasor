/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.platform.security;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.platform.Assert;
import org.platform.Platform;
import org.platform.context.AppContext;
import org.platform.context.ViewContext;
import org.platform.startup.RuntimeListener;
/**
 * Ȩ��ϵͳURL������֧�֡�
 * @version : 2013-4-9
 * @author ������ (zyc@byshell.org)
 */
class SecurityFilter implements Filter {
    private AppContext       appContext      = null;
    private SecuritySettings settings        = null;
    private SecurityContext  secService      = null;
    private SecurityProcess  securityProcess = null;
    //
    /**��ʼ��*/
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Platform.info("init SecurityFilter...");
        ServletContext servletContext = filterConfig.getServletContext();
        this.appContext = (AppContext) servletContext.getAttribute(RuntimeListener.AppContextName);
        Assert.isNotNull(this.appContext, "AppContext is null.");
        //
        this.settings = this.appContext.getBean(SecuritySettings.class);
        this.secService = this.appContext.getBean(SecurityContext.class);
        this.securityProcess = this.appContext.getBean(SecurityProcess.class);
    }
    //
    /**����*/
    @Override
    public void destroy() {}
    //
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this._doFilter(request, response, chain);
        //�ۻ�
        AuthSession[] authSessions = secService.getCurrentAuthSession();
        for (AuthSession authSession : authSessions)
            secService.inactivationAuthSession(authSession.getSessionID()); /*�ۻ�AuthSession*/
    }
    /***/
    public void _doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpRequest.getSession(true);
        //1.��������״̬
        if (this.settings.isEnableURL() == false) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }
        //2.
        if (this.settings.isGuestEnable() == true) {
            try {
                AuthSession targetAuthSession = this.secService.getCurrentBlankAuthSession();
                if (targetAuthSession == null)
                    targetAuthSession = this.secService.createAuthSession();
                String guestAccount = this.settings.getGuestAccount();
                String guestPassword = this.settings.getGuestPassword();
                String guestAuthSystem = this.settings.getGuestAuthSystem();
                targetAuthSession.doLogin(guestAuthSystem, guestAccount, guestPassword);/*��½�����ʺ�*/
            } catch (Exception e) {
                Platform.warning(Platform.logString(e));
            }
        }
        //3.�ָ��Ự
        try {
            this.securityProcess.recoverAuthSession(httpRequest, httpResponse);
        } catch (SecurityException e) {
            Platform.error("recover AuthSession failure!\n" + Platform.logString(e));
        }
        //3.������
        String reqPath = httpRequest.getRequestURI();
        reqPath = reqPath.substring(httpRequest.getContextPath().length());
        if (reqPath.endsWith(this.settings.getLoginURL()) == true) {
            /*A.����*/
            SecurityDispatcher dispatcher = this.secService.getDispatcher(reqPath);
            try {
                this.securityProcess.processLogin(httpRequest, httpResponse);
                dispatcher.forwardIndex(ViewContext.currentViewContext());//��ת�����ַ
            } catch (SecurityException e) {
                dispatcher.forwardFailure(ViewContext.currentViewContext(), e);//��ת����ǳ�ʧ�ܵ�ַ
            }
            return;
        }
        if (reqPath.endsWith(this.settings.getLogoutURL()) == true) {
            /*B.�ǳ�*/
            SecurityDispatcher dispatcher = this.secService.getDispatcher(reqPath);
            try {
                this.securityProcess.processLogout(httpRequest, httpResponse);
                dispatcher.forwardLogout(ViewContext.currentViewContext());//��ת�ǳ���ַ
            } catch (SecurityException e) {
                dispatcher.forwardFailure(ViewContext.currentViewContext(), e);//��ת����ǳ�ʧ�ܵ�ַ
            }
            return;
        }
        {
            /*C.��������*/
            try {
                this.securityProcess.processTestFilter(reqPath);
                chain.doFilter(httpRequest, httpResponse);
            } catch (PermissionException e) {
                Platform.debug("testPermission failure! uri= " + reqPath + "\n" + Platform.logString(e));/*û��Ȩ��*/
                SecurityDispatcher dispatcher = this.secService.getDispatcher(reqPath);
                if (dispatcher != null) {
                    dispatcher.forwardFailure(ViewContext.currentViewContext(), e);
                } else {
                    e.printStackTrace(httpResponse.getWriter());
                }
            }
            /*D.���authSession�е�Ȩ�����ݷ����ı䱣�浽�����У�ͬʱˢ��AuthSession����*/
            AuthSession[] authSessions = this.secService.getCurrentAuthSession();
            for (AuthSession authSession : authSessions)
                authSession.refreshCacheTime();/*ˢ�»����е�����*/
        }
    }
}