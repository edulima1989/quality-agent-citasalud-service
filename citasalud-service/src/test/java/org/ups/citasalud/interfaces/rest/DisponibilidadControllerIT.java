package org.ups.citasalud.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class DisponibilidadControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void consultarDisponibilidad_conMedicoId_retornaFranjasDisponibles() throws Exception {
        mockMvc.perform(
            get("/v1/disponibilidad")
                .param("medicoId", "11111111-1111-1111-1111-111111111111")
                .param("fechaDesde", LocalDate.now().plusDays(1).toString())
                .param("fechaHasta", LocalDate.now().plusDays(7).toString())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
    }

    @Test
    void consultarDisponibilidad_sinFechas_usaDefaultsYRetorna200() throws Exception {
        mockMvc.perform(
            get("/v1/disponibilidad")
                .param("medicoId", "11111111-1111-1111-1111-111111111111")
        )
        .andExpect(status().isOk());
    }

    @Test
    void consultarDisponibilidad_sinMedicoNiEspecialidad_retorna400() throws Exception {
        mockMvc.perform(
            get("/v1/disponibilidad")
                .param("fechaDesde", LocalDate.now().toString())
                .param("fechaHasta", LocalDate.now().plusDays(5).toString())
        )
        .andExpect(status().isBadRequest());
    }
}
