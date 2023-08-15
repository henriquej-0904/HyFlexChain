package pt.unl.fct.di.hyflexchain.api.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * A REST interface for the Transaction Interface in the
 * Application Service Plane.
 */
@Path(TransactionInterfaceRest.PATH)
public interface TransactionInterfaceRest {

	static final String PATH="/hyflexchain/ti";

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
	@Path("/transaction-json")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	String sendTransactionAndWait(HyFlexChainTransaction tx);

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
	@Path("/transaction")
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	String sendTransactionAndWait(byte[] tx);
	
}
