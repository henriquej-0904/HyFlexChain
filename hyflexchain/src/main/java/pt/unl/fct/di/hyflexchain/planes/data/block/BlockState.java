package pt.unl.fct.di.hyflexchain.planes.data.block;

/**
 * The possible states of a block
 */
public enum BlockState {
	/**
	 * The finalized state
	 */
	FINALIZED,

	/**
	 * A pending state that can eventually become finalized
	 */
	PENDING;
}
