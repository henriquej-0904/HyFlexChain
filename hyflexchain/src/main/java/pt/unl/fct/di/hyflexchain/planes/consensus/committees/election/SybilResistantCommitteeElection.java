package pt.unl.fct.di.hyflexchain.planes.consensus.committees.election;

import java.util.Optional;

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
	 * Perform a committee election procedure based on the election criteria.
	 * @param criteria The election criteria
	 * @return The elected committee.
	 */
	Optional<T> performCommitteeElection(Criteria criteria);

	/**
	 * Perform a specified number of committee elections based on the election criteria.
	 * @param criteria The election criteria
	 * @param n The number of committees to elect
	 * @return The elected committees.
	 */
	Optional<T[]> performCommitteeElections(Criteria criteria, int n);


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
