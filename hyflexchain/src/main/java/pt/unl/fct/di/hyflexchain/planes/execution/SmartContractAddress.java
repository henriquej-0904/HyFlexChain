package pt.unl.fct.di.hyflexchain.planes.execution;

import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;

public class SmartContractAddress {
    
    public static final int ADDRESS_SIZE = 256/8;


    public static Address random()
    {
        return Address.random(ADDRESS_SIZE);
    }
}
