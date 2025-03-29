package com.fluiggers.services;

import com.fluiggers.FluiggersWidgetBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.json.JSONArray;

@Path("/widget")
public class Widget extends FluiggersWidgetBase {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {

        log.info("Listando as Widgets");

        if (!isUserLoggedAdmin()) {
            return notAuthorizedResponse();
        }

        JSONArray widgetsNames = new JSONArray();

        File[] widgetsFiles = (new File(getAppWidgetsPath())).listFiles();

        for (File widgetFile : widgetsFiles) {
            widgetsNames.put(widgetFile.getName());
        }

        return Response.ok(widgetsNames.toString()).build();
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
