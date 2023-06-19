package pt.unl.fct.di.hyflexchain.planes.application.ti;

import pt.unl.fct.di.hyflexchain.util.config.MultiLedgerConfig;

public class TransactionInterfaceInstance {

	private static TransactionInterface instance;

	public static TransactionInterface getInstance() {
		if (instance != null)
			return instance;

		synchronized (TransactionInterfaceInstance.class) {
			if (instance != null)
				return instance;

			var sysVersion = MultiLedgerConfig.getInstance()
					.getSystemVersion();

			switch (sysVersion) {
				case V1_0:
					instance = new TransactionInterfaceV1_0();
					break;
				case V2_0:
					instance = new TransactionInterfaceV2_0();
					break;
			}

			instance = new TransactionInterfaceV1_0();
			return instance;
		}
	}

}
