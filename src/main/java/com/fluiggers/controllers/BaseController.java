package com.fluiggers.controllers;

import com.fluig.sdk.service.SecurityService;
import com.fluig.sdk.service.UserService;
import javax.ejb.EJB;
import javax.ws.rs.ForbiddenException;

import org.jboss.logging.Logger;

public abstract class BaseController {
    protected final Logger log = Logger.getLogger(getClass());

    @EJB(lookup = SecurityService.JNDI_REMOTE_NAME)
    protected SecurityService securityService;

    @EJB(lookup = UserService.JNDI_REMOTE_NAME)
    protected UserService userService;

    protected void assertUserAccess() {
        try {
            String login = userService.getCurrent().getLogin();

            Boolean isAdmin = securityService
                .listTenantAdmins(securityService.getCurrentTenantId())
                .stream()
                .anyMatch(user -> user.getLogin().equals(login))
            ;

            if (isAdmin) {
                return;
            }
        } catch (Exception ignore) {}

        throw new ForbiddenException();
    }
}
