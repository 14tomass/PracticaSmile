package es.usal.pa;

import java.util.Scanner;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CyclicBehaviourCliente extends CyclicBehaviour
{
	protected final Scanner scanner = new Scanner(System.in);

	@Override
	public void action()
	{
		// Iniciar el proceso de clasificación de una nueva seta
		String conversationId = Utils.enviarMensaje(this.myAgent, "clasificar", "iniciar");

		if (conversationId == null) {
			// Si el AgenteClasificador aún no está registrado en el DF, esperar un poco y reintentar
			block(1000);
			return;
		}

		boolean clasificacionTerminada = false;
		while (!clasificacionTerminada) {
			// Esperar la pregunta o clasificación final correspondiente a esta conversación
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

				// Si el mensaje es la clasificación final ("e" o "p"), la imprimimos y terminamos esta seta
				if (contenido.equals("e") || contenido.equals("p")) {
					System.out.println("[" + this.myAgent.getLocalName() + "] Clasificación " + contenido);
					clasificacionTerminada = true;
				} else {
					// Es una pregunta del árbol: mostrarla por pantalla y leer respuesta por teclado
					System.out.println("[" + this.myAgent.getLocalName() + "] Responde (s/n): " + contenido);
					
					if (!scanner.hasNextLine()) {
						this.myAgent.doDelete(); // Cierra el agente limpiamente si se cierra la consola
						return;
					}
					String respuesta = scanner.nextLine();

					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REQUEST);
					reply.setContent(respuesta);
					this.myAgent.send(reply);
				}
			}
		}
	}
}