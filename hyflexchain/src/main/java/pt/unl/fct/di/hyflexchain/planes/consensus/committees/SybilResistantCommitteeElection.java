package pt.unl.fct.di.hyflexchain.planes.consensus.committees;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.views.LedgerView;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;

/**
 * An Interface for electing a committee of nodes with sysbil resistance.
 */
public interface SybilResistantCommitteeElection {
	
	/**
	 * Perform a commitee election procedure based on the current
	 * Ledger view, consensus mechanism and election criteria.
	 * @param view The current Ledger view
	 * @param consensusType The consensus mechanism for which the committee
	 * is being elected
	 * @param criteria Other election criteria
	 * @return The elected committee.
	 */
	Committee performCommitteeElection(LedgerView view,
		ConsensusMechanism consensusType, CommitteeElectionCriteria criteria);


	/**
	 * TODO:
	 * com base na base elietoral (dest das transações) e randoms de blocos já finalizdados
	 * 
	 * threshold número elementos comité
	 * 
	 * random: harmony
	 * blockchain: solana
	 * alg BLS
	 * 
	 * no fim de funções do comité, finalizar bloco com o proximo comité
	 * 
	 * criar base eleitoral
	 * computação do comite com prova de verificabilidade
	 * prova de verificação
	 */

}
