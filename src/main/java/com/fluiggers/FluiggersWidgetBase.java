package com.fluiggers;

import com.fluig.sdk.api.common.SDKException;
import com.fluig.sdk.service.SecurityService;
import com.fluig.sdk.service.UserService;
import com.fluig.sdk.tenant.AdminUserVO;
import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jboss.logging.Logger;

public abstract class FluiggersWidgetBase {
    protected final Logger log = Logger.getLogger(getClass());

    @Context
    protected ServletContext context;

    @EJB(lookup = SecurityService.JNDI_REMOTE_NAME)
    protected SecurityService securityService;

    @EJB(lookup = UserService.JNDI_REMOTE_NAME)
    protected UserService userService;

    protected boolean isUserLoggedAdmin() {
        try {
            String login = userService.getCurrent().getLogin();

            AdminUserVO loggedAdmin = securityService
                .listTenantAdmins(securityService.getCurrentTenantId())
                .stream()
                .filter(user -> user.getLogin().equals(login))
                .findAny()
                .orElse(null)
            ;

            return loggedAdmin != null;
        } catch (SDKException e) {
            return false;
        }
    }

    protected Response notAuthorizedResponse() {
        return Response
            .status(Status.UNAUTHORIZED)
            .entity("Você não tem permissão para executar essa ação.")
            .build()
        ;
    }
}
