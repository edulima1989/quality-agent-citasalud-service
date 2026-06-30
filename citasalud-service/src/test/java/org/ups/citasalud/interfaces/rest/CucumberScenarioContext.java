package org.ups.citasalud.interfaces.rest;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

@Component
@ScenarioScope
public class CucumberScenarioContext {

    private String currentPacienteId;
    private MvcResult lastResult;

    public String getCurrentPacienteId() {
        return currentPacienteId;
    }

    public void setCurrentPacienteId(String id) {
        this.currentPacienteId = id;
    }

    public MvcResult getLastResult() {
        return lastResult;
    }

    public void setLastResult(MvcResult result) {
        this.lastResult = result;
    }
}
