package pt.unl.fct.di.hyflexchain.planes.data.block;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusType;

/**
 * The MetaHeader of a block.
 */
public class BlockMetaHeader
{
	/**
	 * The hash of this block
	 */
	protected String hash;

	/**
	 * The version of this meta header
	 */
	protected String version;

	/**
	 * The consensus mechanism of this block
	 */
	protected ConsensusType consensus;

	/**
	 * The proof-of-work algorithm difficulty target for this block
	 */
	protected int difficultyTarget;

	/**
	 * The list of validators that participated in the consensus of this block
	 */
	protected String[] validators;

	/**
	 * The id of the committee assigned to the consensus of this block
	 */
	protected String committeeId;

	/**
	 * The hash of the block where the committee was created
	 */
	protected String committeeBlockHash;

	/**
	 * Create a new Meta Header for a block.
	 * @param hash The hash of this block
	 * @param version The version of this meta header
	 * @param consensus The consensus mechanism of this block
	 * @param difficultyTarget The proof-of-work algorithm difficulty target for this block
	 * @param validators The list of validators that participated in the consensus of this block
	 * @param committeeId The id of the committee assigned to the consensus of this block
	 * @param committeeBlockHash The hash of the block where the committee was created
	 */
	public BlockMetaHeader(String hash, String version, ConsensusType consensus,
			int difficultyTarget, String[] validators, String committeeId,
			String committeeBlockHash) {
		this.hash = hash;
		this.version = version;
		this.consensus = consensus;
		this.difficultyTarget = difficultyTarget;
		this.validators = validators;
		this.committeeId = committeeId;
		this.committeeBlockHash = committeeBlockHash;
	}

	/**
	 * Create a new Meta Header for a block with the current version.
	 * @param consensus The consensus mechanism of this block
	 * @param difficultyTarget The proof-of-work algorithm difficulty target for this block
	 * @param validators The list of validators that participated in the consensus of this block
	 * @param committeeId The id of the committee assigned to the consensus of this block
	 * @param committeeBlockHash The hash of the block where the committee was created
	 */
	public BlockMetaHeader(ConsensusType consensus,
			int difficultyTarget, String[] validators, String committeeId,
			String committeeBlockHash) {
		this.version = Version.V1_0.getVersion();
		this.consensus = consensus;
		this.difficultyTarget = difficultyTarget;
		this.validators = validators;
		this.committeeId = committeeId;
		this.committeeBlockHash = committeeBlockHash;
	}

	/**
	 * Create a new Meta Header for a block.
	 */
	public BlockMetaHeader() {}

	/**
	 * The version of this Block Meta Header
	 */
	public static enum Version {
		V1_0("v1.0");

		private String version;

		/**
		 * @param version
		 */
		private Version(String version) {
			this.version = version;
		}

		/**
		 * @return the version
		 */
		public String getVersion() {
			return version;
		}
	}

	/**
	 * The hash of this block
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * The hash of this block
	 * @param hash the hash to set
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * The version of this meta header
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * The version of this meta header
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * The consensus mechanism of this block
	 * @return the consensus
	 */
	public ConsensusType getConsensus() {
		return consensus;
	}

	/**
	 * The consensus mechanism of this block
	 * @param consensus the consensus to set
	 */
	public void setConsensus(ConsensusType consensus) {
		this.consensus = consensus;
	}

	/**
	 * The proof-of-work algorithm difficulty target for this block
	 * @return the difficultyTarget
	 */
	public int getDifficultyTarget() {
		return difficultyTarget;
	}

	/**
	 * The proof-of-work algorithm difficulty target for this block
	 * @param difficultyTarget the difficultyTarget to set
	 */
	public void setDifficultyTarget(int difficultyTarget) {
		this.difficultyTarget = difficultyTarget;
	}

	/**
	 * The list of validators that participated in the consensus of this block
	 * @return the validators
	 */
	public String[] getValidators() {
		return validators;
	}

	/**
	 * The list of validators that participated in the consensus of this block
	 * @param validators the validators to set
	 */
	public void setValidators(String[] validators) {
		this.validators = validators;
	}

	/**
	 * The id of the committee assigned to the consensus of this block
	 * @return the committeeId
	 */
	public String getCommitteeId() {
		return committeeId;
	}

	/**
	 * The id of the committee assigned to the consensus of this block
	 * @param committeeId the committeeId to set
	 */
	public void setCommitteeId(String committeeId) {
		this.committeeId = committeeId;
	}

	/**
	 * The hash of the block where the committee was created
	 * @return the committeeBlockHash
	 */
	public String getCommitteeBlockHash() {
		return committeeBlockHash;
	}

	/**
	 * The hash of the block where the committee was created
	 * @param committeeBlockHash the committeeBlockHash to set
	 */
	public void setCommitteeBlockHash(String committeeBlockHash) {
		this.committeeBlockHash = committeeBlockHash;
	}

	
}