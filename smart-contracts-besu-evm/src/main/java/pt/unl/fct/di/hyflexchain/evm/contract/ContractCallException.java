package pt.unl.fct.di.hyflexchain.evm.contract;

public class ContractCallException extends RuntimeException {
    public ContractCallException(String msg)
    {
        super(msg);
    }
}
