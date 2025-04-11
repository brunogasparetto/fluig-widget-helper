package com.fluiggers;

import com.fluig.sdk.api.common.SDKException;
import com.fluig.sdk.service.SecurityService;
import com.fluig.sdk.service.UserService;
import com.fluig.sdk.tenant.AdminUserVO;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jboss.logging.Logger;

public abstract class FluiggersWidgetBase {
    protected final String DB_DATASOURCE_NAME = "/jdbc/AppDS";

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

    /**
     * Auxilia a liberar os recursos de uma consulta SQL
     *
     * Tenta fechar cada recurso individualmente, exibindo no log os erros que ocorrem.
     */
    protected void closeDatabaseResources(InitialContext ic, Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                log.error(e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                log.error(e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                log.error(e);
            }
        }
        if (ic != null) {
            try {
                ic.close();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }
}
