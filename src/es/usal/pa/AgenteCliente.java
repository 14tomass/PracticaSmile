package es.usal.pa;

import jade.core.Agent;

public class AgenteCliente extends Agent
{
    @Override
    protected void setup()
    {
        // 1. Miramos si en Eclipse le hemos pasado argumentos al cliente
        Object[] args = getArguments();
        String respuestas = "s"; // Por defecto, responde "s" (instancia venenosa rápida)

        if (args != null && args.length > 0) {
            respuestas = (String) args[0];
        }

        // 2. Le pasamos las respuestas al comportamiento
        addBehaviour(new CyclicBehaviourCliente(respuestas));
    }
}