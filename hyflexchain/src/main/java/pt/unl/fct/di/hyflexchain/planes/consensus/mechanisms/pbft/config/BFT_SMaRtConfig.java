package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.pbft.config;

import pt.unl.fct.di.hyflexchain.util.config.LedgerConfig;

/**
 * A configuration class for the BFT-SMaRt consensus mechanism.
 */
public class BFT_SMaRtConfig
{
    protected LedgerConfig config;

    /**
     * @param config
     */
    public BFT_SMaRtConfig(LedgerConfig config) {
        this.config = config;
    }

    public String getBftSmartConfigFolder()
    {
        var res = this.config.getConfigValue(Configs.BFT_SMaRt_Config_Folder.name);
        if (res == null)
            res = "";

        return res;
    }

    public int getBftSmartReplicaId()
    {
        var res = this.config.getConfigValue(Configs.BFT_SMaRt_Replica_Id.name);
        if (res == null)
            throw configError(Configs.BFT_SMaRt_Replica_Id.name);

        try {
            return Integer.parseInt(res);
        } catch (Exception e) {
            throw new Error(e.getMessage(), e);
        }
    }

    public String getBlockmessConnectorHost()
    {
        var res = this.config.getConfigValue(Configs.Blockmess_Connector_Host.name);
        if (res == null)
            throw configError(Configs.Blockmess_Connector_Host.name);
        
        return res;
    }

    public int getBlockmessConnectorPort()
    {
        var res = this.config.getConfigValue(Configs.Blockmess_Connector_Port.name);
        if (res == null)
            throw configError(Configs.Blockmess_Connector_Port.name);

        try {
            return Integer.parseInt(res);
        } catch (Exception e) {
            throw new Error(e.getMessage(), e);
        }
    }

    protected Error configError(String property)
    {
        return new Error(String.format("BFT_SMaRtConfig: %s is not defined", property));
    }

    public static enum Configs
    {
        BFT_SMaRt_Config_Folder ("BFT_SMaRt_Config_Folder"),

        BFT_SMaRt_Replica_Id ("BFT_SMaRt_Replica_Id"),

        Blockmess_Connector_Host ("Blockmess_Connector_Host"),

        Blockmess_Connector_Port ("Blockmess_Connector_Port");

        public final String name;

        /**
         * @param name
         */
        private Configs(String name) {
            this.name = name;
        }
    }
}
