package es.usal.pa;

import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class AgenteClasificador extends Agent
{
	@Override
	protected void setup()
	{
		// Registrar servicio en las páginas amarillas (DFService)
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		ServiceDescription sd = new ServiceDescription();
		sd.setName("Clasificador");
		sd.setType("clasificar");
		sd.addOntologies("ontologia");
		sd.addLanguages(new SLCodec().getName());
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		// Añadir el comportamiento cíclico que gestiona todas las conversaciones simultáneas
		addBehaviour(new CyclicBehaviourClasificador());
	}
}
