package com.fluiggers.services;

import com.fluiggers.FluiggersWidgetBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/workflows")
public class Workflow extends FluiggersWidgetBase {

    @GET
    @Path("/{processId}/version")
    @Produces(MediaType.APPLICATION_JSON)
    public Response version(@PathParam("processId") String processId) {

        if (!isUserLoggedAdmin()) {
            return notAuthorizedResponse();
        }

        if (processId.isBlank()) {
            return Response
                .status(Status.BAD_REQUEST)
                .entity("Necessário informar o código do processo.")
                .build()
            ;
        }

        InitialContext ic = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(DB_DATASOURCE_NAME);
            conn = ds.getConnection();
            stmt = conn.prepareStatement(
                "SELECT MAX(NUM_VERS) AS VERSION "
                + "FROM VERS_DEF_PROCES "
                + "WHERE COD_EMPRESA = ? AND COD_DEF_PROCES = ?"
            );

            stmt.setLong(1, securityService.getCurrentTenantId());
            stmt.setString(2, processId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int version = rs.getInt("VERSION");

                if (version == 0) {
                    return Response.status(Status.NOT_FOUND).build();
                }

                return Response.ok(version).build();
            }

            return Response.status(Status.NOT_FOUND).build();

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

    @PUT
    @Path("/{processId}/{version}/events")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @PathParam("processId") String processId,
        @PathParam("version") int version,
        String requestBody
    ) throws Exception {

        if (!isUserLoggedAdmin()) {
            return notAuthorizedResponse();
        }

        JSONObject responseBody = new JSONObject();

        if (processId.isBlank() || version <= 0) {
            responseBody.put("hasErrors", true);
            responseBody.put("message", "Necessário indicar o processo e versão");

            return Response
                .status(Status.BAD_REQUEST)
                .entity(responseBody.toString())
                .build()
            ;
        }

        JSONArray eventsErrors = new JSONArray();
        JSONArray eventsSuccesses = new JSONArray();
        Boolean hasError = false;
        int totalProcessed = 0;

        responseBody.put("processId", processId);
        responseBody.put("version", version);

        try {
            JSONArray events = new JSONArray(requestBody);
            long tenantId = securityService.getCurrentTenantId();

            if (events.isEmpty()) {
                throw new JSONException("Obrigatório informar eventos para atualizar.");
            }

            log.info(
                "Atualizando eventos do processo " + processId
                + "\nVersão " + version
                + "\nEmpresa " + tenantId
                + "\nTotal de eventos enviados: " + events.length()
            );

            InitialContext ic = null;
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                ic = new InitialContext();
                DataSource ds = (DataSource) ic.lookup(DB_DATASOURCE_NAME);
                conn = ds.getConnection();
                stmt = conn.prepareStatement(
                    "UPDATE event_proces "
                    + "SET DSL_EVENT = ? "
                    + "WHERE "
                        + "COD_EMPRESA = ? "
                        + "AND COD_DEF_PROCES = ? "
                        + "AND COD_EVENT = ? "
                        + "AND NUM_VERS = ? "
                );

                for (int scriptIndex = 0; scriptIndex < events.length(); ++scriptIndex) {
                    JSONObject event = events.getJSONObject(scriptIndex);

                    ++totalProcessed;

                    if (!event.has("name") || event.getString("name").isBlank()) {
                        hasError = true;
                        eventsErrors.put("Evento informado no índice " + scriptIndex + " não possuí nome.");
                        continue;
                    }

                    if (!event.has("contents") || event.getString("contents").isBlank()) {
                        hasError = true;
                        eventsErrors.put(
                            event.getString("name") + " não possuí conteúdo. "
                            + "Para remover um evento/script exporte o processo."
                        );

                        continue;
                    }

                    stmt.setString(1, event.getString("contents"));
                    stmt.setLong(2, tenantId);
                    stmt.setString(3, processId);
                    stmt.setString(4, event.getString("name"));
                    stmt.setInt(5, version);

                    int rowsUpdated = stmt.executeUpdate();

                    if (rowsUpdated == 0) {
                        hasError = true;
                        eventsErrors.put(event.getString("name") + " não encontrado.");
                    } else {
                        eventsSuccesses.put(event.getString("name"));
                    }
                }

                responseBody.put("hasError", hasError);
                responseBody.put("totalProcessed", totalProcessed);
                responseBody.put("errors", eventsErrors);
                responseBody.put("successes", eventsSuccesses);

                responseBody.put(
                    "message",
                    hasError
                    ? "Houveram erros ao atualizar os eventos."
                    : "Todos os eventos foram atualizados."
                );
            } catch (Exception e) {
                log.error(e);
            } finally {
                closeDatabaseResources(ic, conn, stmt, null);
            }

        } catch (JSONException e) {
            log.error(e);
            responseBody.put("hasError", true);
            responseBody.put("message", "JSON Inválido. Nenhum evento foi processado.");
            return Response.status(Status.BAD_REQUEST).entity(responseBody.toString()).build();
        }

        return Response.ok(responseBody.toString()).build();
    }
}
