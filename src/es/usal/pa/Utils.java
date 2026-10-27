package es.usal.pa;

import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

public class Utils 
{
	/**
	 * Permite buscar a todos los agentes que implementan un servicio de un tipo dado
	 * @param agent Agente con el que se realiza la búsqueda
	 * @param tipo  Tipo de servicio buscado
	 * @return Listado de agentes que proporcionan el servicio
	 */
    protected static DFAgentDescription [] buscarAgentes(Agent agent, String tipo)
    {
        DFAgentDescription template=new DFAgentDescription();
        ServiceDescription templateSd=new ServiceDescription();
        templateSd.setType(tipo);
        template.addServices(templateSd);
        
        SearchConstraints sc = new SearchConstraints();
        sc.setMaxResults(Long.MAX_VALUE);
        try
        {
            DFAgentDescription [] results = DFService.search(agent, template, sc);
            return results;
        }
        catch(FIPAException e)
        {
        	e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Envía un objeto desde el agente indicado a un agente que proporciona un servicio del tipo dado
     * @param agent Agente desde el que se va a enviar el servicio
     * @param tipo Tipo del servicio buscado
     * @param objeto Mensaje a Enviar
     * @return El conversationId generado para la conversación, o null si no se pudo enviar
     */
    public static String enviarMensaje(Agent agent, String tipo, Object objeto)
    {
        DFAgentDescription[] dfd;
        dfd=buscarAgentes(agent, tipo);
        
        try
        {
            if(dfd!=null && dfd.length > 0)
            {
            	ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            	
            	for(int i=0;i<dfd.length;i++)
	        		aclMessage.addReceiver(dfd[i].getName());
            	
                aclMessage.setOntology("ontologia");
                aclMessage.setLanguage(new SLCodec().getName());
                aclMessage.setEnvelope(new Envelope());
                String conversationId = agent.getName() + "_" + System.currentTimeMillis();
                aclMessage.setConversationId(conversationId);
				aclMessage.getEnvelope().setPayloadEncoding("ISO8859_1");
        		aclMessage.setContentObject((Serializable)objeto);
        		agent.send(aclMessage);
        		return conversationId;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Permite buscar los agentes que dan un servicio de un determinado tipo. Devuelve el primero de ellos.
     * @param agent Agente desde el que se realiza la búsqueda
     * @param tipo Tipo de servicio buscado
     * @return Primer agente que proporciona el servicio
     */
    protected static DFAgentDescription buscarAgente(Agent agent, String tipo)
    {
        DFAgentDescription template=new DFAgentDescription();
        ServiceDescription templateSd=new ServiceDescription();
        templateSd.setType(tipo);
        template.addServices(templateSd);
        
        SearchConstraints sc = new SearchConstraints();
        sc.setMaxResults(Long.valueOf(1));
        
        try
        {
            DFAgentDescription [] results = DFService.search(agent, template, sc);
            if (results.length > 0) 
            {
                for (int i = 0; i < results.length; ++i) 
                {
                    DFAgentDescription dfd = results[i];
                    AID provider = dfd.getName();
                    
                    Iterator it = dfd.getAllServices();
                    while (it.hasNext())
                    {
                        ServiceDescription sd = (ServiceDescription) it.next();
                        if (sd.getType().equals(tipo))
                        {
                            System.out.println("- Servicio \""+sd.getName()+"\" proporcionado por el agente "+provider.getName());
                            return dfd;
                        }
                    }
                }
            }	
        }
        catch(FIPAException e)
        {
        	e.printStackTrace();
        }
        
        return null;
    }
}
