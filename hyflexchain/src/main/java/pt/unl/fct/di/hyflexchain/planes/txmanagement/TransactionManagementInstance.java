package pt.unl.fct.di.hyflexchain.planes.txmanagement;

import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

public class TransactionManagementInstance {
	private static TransactionManagement instance;

	public static TransactionManagement getInstance()
	{
		if (instance != null)
			return instance;

		synchronized(TransactionManagementInstance.class)
		{
			if (instance != null)
				return instance;

			var sysVersion = MultiLedgerConfig.getInstance()
				.getSystemVersion();

			switch (sysVersion) {
				case V1_0:
					instance = new TransactionManagementV1_0();
					break;
				case V2_0:
					instance = new TransactionManagementV2_0();
					break;
			}

			return instance;
		}
	}
}
