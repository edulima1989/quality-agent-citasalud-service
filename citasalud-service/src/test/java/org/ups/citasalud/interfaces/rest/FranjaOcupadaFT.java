package org.ups.citasalud.interfaces.rest;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class FranjaOcupadaFT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CucumberScenarioContext ctx;

    @Given("la franja {string} ya fue reservada por otro paciente")
    public void laFranjaYaFueReservada(String franjaId) throws Exception {
        String body = """
            {"franjaHorariaId": "%s"}
            """.formatted(franjaId);
        mockMvc.perform(
            post("/v1/citas")
                .with(user("ffffffff-ffff-ffff-ffff-ffffffffffff"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andReturn();
    }

    @When("el paciente intenta reservar la franja {string}")
    public void elPacienteIntentaReservar(String franjaId) throws Exception {
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

    @And("el cuerpo contiene el código {string}")
    public void elCuerpoContieneElCodigo(String codigo) throws Exception {
        String responseBody = ctx.getLastResult().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(responseBody);
        assertEquals(codigo, node.get("codigo").asText());
    }

    @And("el cuerpo incluye franjas alternativas")
    public void elCuerpoIncluyeAlternativas() throws Exception {
        String responseBody = ctx.getLastResult().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(responseBody);
        assertNotNull(node.get("alternativas"));
    }
}
