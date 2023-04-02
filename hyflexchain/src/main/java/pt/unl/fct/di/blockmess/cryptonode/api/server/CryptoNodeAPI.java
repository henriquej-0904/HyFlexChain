package pt.unl.fct.di.blockmess.cryptonode.api.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import pt.unl.fct.di.blockmess.cryptonode.api.Transaction;

/**
 * An interface for interacting with the Cryptocurrency Node.
 */
@Path(CryptoNodeAPI.PATH)
public interface CryptoNodeAPI
{
	static final String PATH="/crypto-node";

	/**
	 * Transfers money from an origin to a destination.
	 * 
	 * @param transaction The transaction to send
	 * @return The Transaction Id (hash).
	 */
    @Path("/transaction")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    String sendTransaction(Transaction transaction);
}