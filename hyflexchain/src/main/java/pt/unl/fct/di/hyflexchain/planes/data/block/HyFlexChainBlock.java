package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.util.Set;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusType;

/**
 * Represents a block of transactions with all necessary metadata.
 */
public abstract class HyFlexChainBlock {

	/**
	 * Create a block
	 */
	public HyFlexChainBlock() {
	}

	/**
	 * meta header
	 * version
	 * consenso
	 * difficulty target
	 * validators (PBFT - nos envolvidos no consenso comite)
	 * id comite
	 * block id do metabloco para comite
	 */

	 /**
	  * Header

	  	version
	  	prev hash
		merkle root
		timestamp
	  * nonce
	  */

	  /**
	   * Body
	   * 
	   * version
	   * List Transactions
	   */

	/**
	 * The header of the block.
	 */
	public static record Header(
		long slot, String parentRoot, String stateRoot,
		ConsensusType consensus
	) {}

	/**
	 * The body of the block.
	 */
	public static record Body(
		long nonce,
		byte[] data
	)
	{
		public static record ExecutionPayload(
			
		)
		{
			
			public static record ExecutionPayloadHeader(
				String parentHash, Set<String> feeRecipients,
				String stateRoot, String receiptsRoot,
				long previousNonce, long blockNumber
			) {}
		}
	}


	
}
