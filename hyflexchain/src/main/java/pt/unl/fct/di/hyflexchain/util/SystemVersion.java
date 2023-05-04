package pt.unl.fct.di.hyflexchain.util;

/**
 * The HyFlexChain Version
 */
public enum SystemVersion
{
	/**
	 * HyFlexChain supports only one consensus mechanism ->
	 * PoW (Blockmess). Mainly for performance tests comparison.
	 */
	V1_0,
	
	/**
	 * HyFlexChain supports 2 consensus mechanisms ->
	 * PoW (Blockmess), PBFT (BFT-SMaRt).
	 */
	V2_0;
}
