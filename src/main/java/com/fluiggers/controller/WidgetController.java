package com.fluiggers.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
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
        try {
            return new WidgetService().findAll();
        } catch (Exception ignore) {
            log.error("Erro não capturado ao listar widgets", ignore);
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
        try {
            var service = new WidgetService();
            var inputStream = service.getWidgetFileInputStream(context, filename);

            log.info(
                "Usuário \"{}\" efetuou download da Widget \"{}\"",
                userService.getCurrent().getLogin(),
                filename
            );

            return inputStream;
        } catch (FileNotFoundException e) {
            throw new NotFoundException();
        } catch (Exception e) {
            log.error("Erro efetuar download da widget \"" + filename + "\"", e);
            throw new InternalServerErrorException("Consulte o Log do Fluig para mais informações.");
        }
    }
}
