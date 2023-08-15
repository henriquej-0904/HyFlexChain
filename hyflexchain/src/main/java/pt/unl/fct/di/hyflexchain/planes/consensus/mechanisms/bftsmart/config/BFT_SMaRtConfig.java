package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.config;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeElectionCriteria.CommitteeValidity;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeElectionCriteria.RandSource;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommitteeElectionCriteria;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
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

    public LedgerConfig getLedgerConfig()
    {
        return this.config;
    }

    public File getBftSmartConfigFolder()
    {
        var res = this.config.getConfigValue(Configs.BFT_SMaRt_Config_Folder.name);
        if (res == null)
            return new File(this.config.getConfigFolder(), "config");

        return new File(this.config.getConfigFolder(), res);
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

    public BftCommitteeElectionCriteria getStaticElectionCriteria()
    {
        var n = this.config.getConfigIntValueOrThrowError(Configs.COMMITTEE_N.name);
        var f = this.config.getConfigIntValueOrThrowError(Configs.COMMITTEE_F.name);
        var threshold = this.config.getConfigIntValueOrThrowError(Configs.COMMITTEE_THRESHOLD.name);
        var validityBlocks = this.config.getConfigIntValueOrThrowError(Configs.COMMITTEE_VALIDITY_N_BLOCKS.name);

        return new BftCommitteeElectionCriteria(n, threshold, RandSource.LOCAL_RAND,
            new CommitteeValidity(validityBlocks), f);
    }
    
    public List<Address> getStaticCommitteeAddresses()
    {
        var res = this.config.getConfigValue(Configs.STATIC_COMMITTEE.name);

        if (res == null)
            throw configError(Configs.STATIC_COMMITTEE.name);

        return Stream.of(res.split(";"))
            .map(Address::fromHexString)
            .toList();
    }

    public boolean dynamicCommittees()
    {
        return this.config.getConfigBooleanValueOrThrowError(Configs.COMMITTEE_DYNAMIC.name);
    }

    public int getCommitteeElectionParamRecipientNodesOfLastFinalizedBlocks()
    {
        return this.config.getConfigIntValueOrThrowError(Configs.COMMITTEE_ELECTION_RECIPIENT_NODES_LAST_N_FINALIZED_BLOCKS.name);
    }

    public int getCommitteeElectionParamPreviousCommittees()
    {
        return this.config.getConfigIntValueOrThrowError(Configs.COMMITTEE_ELECTION_PREVIOUS_N_COMMITTEES.name);
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

        Blockmess_Connector_Port ("Blockmess_Connector_Port"),

        /**
         * A file that specifies for each HyFlexChain address,
         * the corresponding IP address
         */
        ADDRESSES_CONFIG_FILE ("ADDRESSES_CONFIG_FILE"),

        COMMITTEE_DYNAMIC("COMMITTEE_DYNAMIC"),

        COMMITTEE_N ("COMMITTEE_N"),

        COMMITTEE_F ("COMMITTEE_F"),

        COMMITTEE_THRESHOLD("COMMITTEE_THRESHOLD"),

        COMMITTEE_VALIDITY_N_BLOCKS("COMMITTEE_VALIDITY_N_BLOCKS"),

        COMMITTEE_ELECTION_RECIPIENT_NODES_LAST_N_FINALIZED_BLOCKS("COMMITTEE_ELECTION_RECIPIENT_NODES_LAST_N_FINALIZED_BLOCKS"),

        COMMITTEE_ELECTION_PREVIOUS_N_COMMITTEES ("COMMITTEE_ELECTION_PREVIOUS_N_COMMITTEES"),

        /**
         * A list of HyFlexChain addresses (';' separated) that belong to the static
         * committee that will be executing the BFT-SMaRt protocol.
         */
        STATIC_COMMITTEE ("STATIC_COMMITTEE");

        public final String name;

        /**
         * @param name
         */
        private Configs(String name) {
            this.name = name;
        }
    }
}
