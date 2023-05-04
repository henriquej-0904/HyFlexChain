package pt.unl.fct.di.hyflexchain.planes.application.ti;

public class TransactionInterfaceInstance {
	
	private static TransactionInterface instance;

	public static TransactionInterface getInstance()
	{
		if (instance != null)
			return instance;

		synchronized(instance)
		{
			if (instance != null)
				return instance;

			instance = new TransactionInterfaceV1_0();
			return instance;
		}
	}

}
