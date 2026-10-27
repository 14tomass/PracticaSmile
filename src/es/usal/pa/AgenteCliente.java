package es.usal.pa;

import jade.core.Agent;

public class AgenteCliente extends Agent
{
	@Override
	protected void setup()
	{
		addBehaviour(new CyclicBehaviourCliente());
	}
}
