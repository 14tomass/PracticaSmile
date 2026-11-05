package es.usal.pa;

import java.io.IOException;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CyclicBehaviourCliente extends CyclicBehaviour
{
    protected String[] respuestasInstancia;
    protected int pasoActual = 0;

    public CyclicBehaviourCliente(String respuestasStr)
    {
        super();
        this.respuestasInstancia = respuestasStr.split(",");
    }

    @Override
    public void action()
    {
        // 1. Iniciar la clasificación de una nueva seta
        String conversationId = Utils.enviarMensaje(this.myAgent, "clasificar", "iniciar");

        if (conversationId == null) {
            block(1000);
            return;
        }

        boolean clasificacionTerminada = false;
        pasoActual = 0; // Reiniciamos los pasos para la nueva seta

        while (!clasificacionTerminada) {
            // Filtrar mensajes de esta conversación concreta
            MessageTemplate filtro = MessageTemplate.and(
                    MessageTemplate.and(
                            MessageTemplate.MatchOntology("ontologia"),
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
                    ),
                    MessageTemplate.MatchConversationId(conversationId)
            );

            ACLMessage msg = this.myAgent.blockingReceive(filtro);

            if (msg != null) {
                String contenido = msg.getContent();

                // Si es la clasificación final ("e" o "p")
                if (contenido.equals("e") || contenido.equals("p")) {
                    System.out.println(">>> [" + this.myAgent.getLocalName() + "] Clasificación obtenida: " + contenido);
                    clasificacionTerminada = true;
                } else {
                    // Es una pregunta del árbol (ej: "odor=f")
                    System.out.println("[" + this.myAgent.getLocalName() + "] Pregunta recibida: " + contenido);

                    // Pequeña pausa de 500 ms para simular que el cliente procesa la pregunta
                    // y para que en la consola se aprecie cómo los dos agentes se intercalan
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {}

                    // Obtener la respuesta que corresponde a la instancia de este cliente
                    String miRespuesta = "n";
                    if (pasoActual < respuestasInstancia.length) {
                        miRespuesta = respuestasInstancia[pasoActual].trim();
                        pasoActual++;
                    }

                    System.out.println("[" + this.myAgent.getLocalName() + "] Respondo: " + miRespuesta);

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REQUEST);
                    reply.setContent(miRespuesta);
                    this.myAgent.send(reply);
                }
            }
        }
    }
}