package com.fluiggers.controller;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.fluiggers.dto.WidgetDto;
import com.fluiggers.service.WidgetService;

@Path("/widgets")
public class WidgetController extends BaseController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<WidgetDto> list() {
        assertUserAccess();

        var service = new WidgetService();

        try {
            return service.findAll();
        } catch (Exception e) {
            log.error(e);
        }

        return new ArrayList<WidgetDto>();
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{filename:[a-zA-Z0-9_.-]+\\.war}")
    public FileInputStream download(
        @Context ServletContext context,
        @PathParam("filename") String filename
    ) {
        assertUserAccess();

        try {
            var service = new WidgetService();
            var inputStream = service.getWidgetFileInputStream(context, filename);

            try {
                log.infof(
                    "Usuário \"%s\" efetuou download da Widget \"%s\"",
                    userService.getCurrent().getLogin(),
                    filename
                );
            } catch (Exception ignore) { }

            return inputStream;
        } catch (Exception e) {
            log.error(e);
            throw new NotFoundException();
        }
    }
}
