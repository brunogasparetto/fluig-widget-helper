package com.fluiggers.services;

import com.fluiggers.FluiggersWidgetBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/widgets")
public class Widget extends FluiggersWidgetBase {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {

        log.info("Listando as Widgets");

        if (!isUserLoggedAdmin()) {
            return notAuthorizedResponse();
        }

        JSONArray widgets = new JSONArray();

        InitialContext ic = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(DB_DATASOURCE_NAME);
            conn = ds.getConnection();
            stmt = conn.createStatement();

            rs = stmt.executeQuery(
                "SELECT APPLICATION_CODE, APPLICATION_TITLE, DESCRIPTION, FILE_NAME "
                + "FROM wcm_application "
                + "WHERE INTERNAL = 0 AND APPLICATION_TYPE = 'widget' "
                + "ORDER BY APPLICATION_TITLE"
            );

            while (rs.next()) {
                JSONObject widget = new JSONObject();
                widget.put("code", rs.getString("APPLICATION_CODE"));
                widget.put("title", rs.getString("APPLICATION_TITLE"));
                widget.put("description", rs.getString("DESCRIPTION"));
                widget.put("filename", rs.getString("FILE_NAME"));

                widgets.put(widget);
            }

            return Response.ok(widgets.toString()).build();
        } catch (Exception e) {
            log.error(e);

            return Response
                .status(Status.INTERNAL_SERVER_ERROR)
                .entity("Verifique o Log do Fluig para mais informações.")
                .build()
            ;
        } finally {
            closeDatabaseResources(ic, conn, stmt, rs);
        }
    }

    @GET
    @Path("/{widgetFilename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getWidgetFile(@PathParam("widgetFilename") String widgetFilename) throws FileNotFoundException {

        log.info("Efetuando o download da widget " + widgetFilename);

        if (!isUserLoggedAdmin()) {
            return notAuthorizedResponse();
        }

        if (!widgetFilename.endsWith(".war") || widgetFilename.matches("^[./\\\\]")) {
            return Response.status(Status.BAD_REQUEST).entity("Operação Inválida.").build();
        }

        File widgetFile = new File(getAppWidgetsPath() + File.separator + widgetFilename);

        if (!widgetFile.exists()) {
            return Response.status(Status.NOT_FOUND).entity("Widget não encontrada.").build();
        }

        return Response.ok(new FileInputStream(widgetFile)).build();
    }

    protected String getAppWidgetsPath() {
        return context
                .getRealPath("/")
                .replaceAll("^(.+appserver).*", "$1")
                + File.separator
                + "apps";
    }
}
