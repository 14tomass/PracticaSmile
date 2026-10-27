package es.usal.pa;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import smile.classification.DecisionTree;
import smile.classification.DecisionTree.Options;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.io.Read;
import smile.model.cart.DecisionTreeNavegar2;
import smile.model.cart.SplitRule;

public class CyclicBehaviourClasificador extends CyclicBehaviour
{
	protected DecisionTree decisionTree;
	protected Map<String, DecisionTreeNavegar2> conversaciones;

	public CyclicBehaviourClasificador()
	{
		super();
		conversaciones = new HashMap<>();
		try {
			// Cargar el dataset y construir el árbol de decisión una sola vez
			DataFrame dataFrame = Read.arff("mushrooms.arff");
			dataFrame = dataFrame.dropna();
			Options options = new Options(SplitRule.ENTROPY, Integer.MAX_VALUE, Integer.MAX_VALUE, 1);
			decisionTree = DecisionTree.fit(Formula.of("class~."), dataFrame, options);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void action()
	{
		// Esperar petición o respuesta de cualquier cliente
		ACLMessage msg = this.myAgent.blockingReceive(
				MessageTemplate.and(
						MessageTemplate.MatchOntology("ontologia"),
						MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
				)
		);

		if (msg == null) {
			return;
		}

		try {
			String conversationId = msg.getConversationId();
			if (conversationId == null) {
				conversationId = msg.getSender().getName();
			}

			ACLMessage respuestaMsg = msg.createReply();
			respuestaMsg.setPerformative(ACLMessage.INFORM);

			// Si no existe la conversación en el mapa, es una nueva petición de clasificación
			if (!conversaciones.containsKey(conversationId)) {
				DecisionTreeNavegar2 navegador = new DecisionTreeNavegar2(decisionTree);
				conversaciones.put(conversationId, navegador);

				// Enviar la pregunta del nodo raíz (ej. odor=f)
				String primeraPregunta = navegador.condicionActual();
				respuestaMsg.setContentObject(primeraPregunta);
				this.myAgent.send(respuestaMsg);
			} else {
				// Conversación en curso: recibir respuesta sí/no del cliente y avanzar por el árbol
				DecisionTreeNavegar2 navegador = conversaciones.get(conversationId);
				String respuestaCliente = (String) msg.getContentObject();

				boolean irRamaIzquierda = respuestaCliente != null &&
						(respuestaCliente.trim().equalsIgnoreCase("s") ||
						 respuestaCliente.trim().equalsIgnoreCase("si") ||
						 respuestaCliente.trim().equalsIgnoreCase("sí"));

				navegador.navegarNodo(irRamaIzquierda);

				// Comprobar si hemos llegado a un nodo hoja (nodo de decisión final)
				if (navegador.esNodoDecision()) {
					String clasificacionFinal = navegador.getClasificacion("class");
					// Eliminar la conversación del mapa porque ya ha terminado esta seta
					conversaciones.remove(conversationId);

					respuestaMsg.setContentObject(clasificacionFinal);
					this.myAgent.send(respuestaMsg);
				} else {
					// Todavía es un nodo interno: enviar la siguiente pregunta
					String siguientePregunta = navegador.condicionActual();
					respuestaMsg.setContentObject(siguientePregunta);
					this.myAgent.send(respuestaMsg);
				}
			}
		} catch (UnreadableException | IOException e) {
			e.printStackTrace();
		}
	}
}
