package pt.unl.fct.di.hyflexchain.api.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;

@Path(SmartContractManagementInterfaceRest.PATH)
public interface SmartContractManagementInterfaceRest {
    
    static final String PATH="/hyflexchain/scmi";

    /**
	 * Create a new smart contract with the provided code.
	 * The owner of the newly created smart contract is
	 * the node that process this request.
	 * @param contractCode The code of the smart contract.
	 * @return The address of the newly created smart contract.
	 */
    @Path("/install")
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	String installSmartContract(byte[] contractCode);

    /**
	 * Revoke the specified smart contract.
	 * This operation only permits to revoke smart contracts
	 * owned by the node executing this operation.
	 * @param contractAddress The address of the smart contract to revoke
	 * @return The address of the revoked smart contract.
	 */
    @Path("/revoke")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	String revokeSmartContract(Address contractAddress);

}
