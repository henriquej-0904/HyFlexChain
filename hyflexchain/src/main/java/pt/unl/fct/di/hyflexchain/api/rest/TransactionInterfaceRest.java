package pt.unl.fct.di.hyflexchain.api.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pt.unl.fct.di.hyflexchain.planes.application.ti.InvalidTransactionException;
import pt.unl.fct.di.hyflexchain.planes.application.ti.TransactionInterface;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * A REST interface for the Transaction Interface in the
 * Application Service Plane.
 */
@Path(TransactionInterfaceRest.PATH)
public interface TransactionInterfaceRest {

	static final String PATH="/tx";

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * Send transaction primitive:
	 * submits a transaction for verification and,
	 * if successfull, dispatch it to the system for ordering. <p>
	 * This method waits for the transaction to be
	 * finalized.
	 * @param tx The transaction to send.
	 * 
	 * @return The generated transaction id.
	 */
	String sendTransactionAndWait(HyFlexChainTransaction tx);
	
}
