package com.fluiggers.controller;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ws.rs.ForbiddenException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluig.sdk.service.SecurityService;
import com.fluig.sdk.service.UserService;

public abstract class BaseController {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @EJB(lookup = SecurityService.JNDI_REMOTE_NAME)
    protected SecurityService securityService;

    @EJB(lookup = UserService.JNDI_REMOTE_NAME)
    protected UserService userService;

    @PostConstruct
    private void assertUserAccess() {
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
        } catch (Exception ignore) {
            log.error("Erro não capturado ao validar usuário Administrador", ignore);
        }

        throw new ForbiddenException();
    }
}
