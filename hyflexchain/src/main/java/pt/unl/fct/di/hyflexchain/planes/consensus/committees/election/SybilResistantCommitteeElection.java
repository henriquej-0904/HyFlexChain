package pt.unl.fct.di.hyflexchain.planes.consensus.committees.election;

import pt.unl.fct.di.hyflexchain.planes.application.lvi.LedgerViewInterface;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.Committee;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeElectionCriteria;

/**
 * An Interface for electing a committee of nodes with sybil resistance.
 */
public interface SybilResistantCommitteeElection
	<T extends Committee, Criteria extends CommitteeElectionCriteria>
{
	
	/**
	 * The consensus mechanism of the elected committees.
	 * @return The consensus mechanism of the elected committees.
	 */
	ConsensusMechanism consensus();


	/**
	 * Perform a commitee election procedure based on the current
	 * Ledger view, consensus mechanism and election criteria.
	 * @param lvi The current Ledger view
	 * @param criteria Other election criteria
	 * @return The elected committee.
	 */
	T performCommitteeElection(LedgerViewInterface lvi, Criteria criteria);


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
