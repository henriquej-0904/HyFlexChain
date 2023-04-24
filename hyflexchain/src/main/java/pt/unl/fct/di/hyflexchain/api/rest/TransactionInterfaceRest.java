package pt.unl.fct.di.hyflexchain.api.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pt.unl.fct.di.hyflexchain.planes.application.ti.TransactionInterface;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * A REST interface for the Transaction Interface in the
 * Application Service Plane.
 */
@Path(TransactionInterfaceRest.PATH)
public interface TransactionInterfaceRest extends TransactionInterface {

	static final String PATH="/tx";

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	String sendTransaction(HyFlexChainTransaction tx);
	
}
