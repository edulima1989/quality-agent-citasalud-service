# Constitución del proyecto · citasalud-agenda

Principios no negociables que el coding agent respeta en specify, plan e implement,
y que el **Quality & Governance Agent** hace cumplir en la fase de verificación.

## Principios

1. **Pruebas primero (NON-NEGOTIABLE).** Toda funcionalidad nace con su prueba.
   Cobertura mínima del módulo: **80%**.
2. **Cada requisito, una prueba.** Cada Functional Requirement del `spec.md` —incluidos
   los casos de error y de **concurrencia**— debe tener al menos una prueba que lo verifique.
3. **Seguridad de base.** Cero vulnerabilidades críticas y cero secretos en el código.
   Endpoints sensibles autenticados.
4. **El spec manda.** El código sigue al `spec.md`; cuando difieren, o se corrige el
   código o se revisa el spec — nunca se ignora la diferencia.

> El umbral de cobertura (80%) y la regla "cada FR una prueba" los comprueba de forma
> determinista el gate del Quality Agent (`.claude/hooks/quality-gate.py`).
