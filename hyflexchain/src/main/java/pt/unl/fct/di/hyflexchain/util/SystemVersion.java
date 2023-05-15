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
	V1_0("1.0"),
	
	/**
	 * HyFlexChain supports 2 consensus mechanisms ->
	 * PoW (Blockmess), PBFT (BFT-SMaRt).
	 */
	V2_0("2.0");

	public final String version;

	/**
	 * @param version
	 */
	private SystemVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return version;
	}

	public static SystemVersion parseSystemVersion(String version)
	{
		switch (version) {
			case "1.0":
				return SystemVersion.V1_0;
			case "2.0":
				return SystemVersion.V2_0;
		}

		throw new IllegalArgumentException("Invalid system version.");
	}

	
}
