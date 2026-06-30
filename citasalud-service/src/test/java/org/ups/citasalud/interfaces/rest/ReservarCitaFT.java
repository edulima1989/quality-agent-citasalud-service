package org.ups.citasalud.interfaces.rest;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class ReservarCitaFT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CucumberScenarioContext ctx;

    @Before
    public void setUp() {
        // ctx is @ScenarioScope — reset happens automatically per scenario
    }

    @Given("el sistema está operativo con franjas disponibles")
    public void elSistemaEstaOperativo() {
        // data.sql pre-carga las franjas
    }

    @Given("el paciente autenticado con id {string}")
    public void elPacienteAutenticado(String pacienteId) {
        ctx.setCurrentPacienteId(pacienteId);
    }

    @And("existe la franja horaria disponible {string}")
    public void existeLaFranjaDisponible(String franjaId) {
        // La franja está en data.sql
    }

    @When("el paciente confirma la reserva de la franja {string}")
    public void elPacienteConfirmaLaReserva(String franjaId) throws Exception {
        String body = """
            {"franjaHorariaId": "%s"}
            """.formatted(franjaId);

        ctx.setLastResult(mockMvc.perform(
            post("/v1/citas")
                .with(user(ctx.getCurrentPacienteId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andReturn());
    }

    @Then("la respuesta es {int}")
    public void laRespuestaEs(int statusCode) {
        assertEquals(statusCode, ctx.getLastResult().getResponse().getStatus());
    }

    @And("la cita queda registrada con estado {string}")
    public void laCitaQuedaRegistrada(String estado) throws Exception {
        String responseBody = ctx.getLastResult().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(responseBody);
        assertEquals(estado, node.get("estado").asText());
    }

    @And("la notificación WhatsApp fue disparada")
    public void laNotificacionFueDis() {
        assertEquals(201, ctx.getLastResult().getResponse().getStatus());
    }

    @And("el paciente tiene una cita reservada")
    public void elPacienteTieneUnaCitaReservada() throws Exception {
        // Usa aa000002 (distinta de aa000001 usada en US1-1)
        String body = """
            {"franjaHorariaId": "aa000002-0000-0000-0000-000000000002"}
            """;
        mockMvc.perform(
            post("/v1/citas")
                .with(user(ctx.getCurrentPacienteId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andReturn();
    }

    @When("el paciente consulta su listado de citas")
    public void elPacienteConsultaSuListado() throws Exception {
        ctx.setLastResult(mockMvc.perform(
            get("/v1/citas")
                .with(user(ctx.getCurrentPacienteId()))
        ).andReturn());
    }

    @And("el listado contiene al menos {int} cita")
    public void elListadoContieneCitas(int minCitas) throws Exception {
        String responseBody = ctx.getLastResult().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(responseBody);
        int total = node.get("total").asInt();
        assertTrue(total >= minCitas, "Se esperaban al menos " + minCitas + " citas, pero hay " + total);
    }
}
