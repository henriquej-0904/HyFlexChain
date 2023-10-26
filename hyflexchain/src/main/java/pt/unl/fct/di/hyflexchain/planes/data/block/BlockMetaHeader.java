package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.io.IOException;
import java.util.Arrays;

import org.apache.tuweni.bytes.Bytes;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;
import pt.unl.fct.di.hyflexchain.planes.consensus.committees.CommitteeId;
import pt.unl.fct.di.hyflexchain.util.BytesOps;
import pt.unl.fct.di.hyflexchain.util.Utils;
import pt.unl.fct.di.hyflexchain.util.crypto.HyFlexChainSignature;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

/**
 * The MetaHeader of a block.
 */
public class BlockMetaHeader implements BytesOps
{
	public static final Serializer SERIALIZER = new Serializer();

	/**
	 * The version of this meta header
	 */
	protected String version;

	/**
	 * The consensus mechanism of this block
	 */
	protected ConsensusMechanism consensus;

	/**
	 * The proof-of-work algorithm difficulty target for this block
	 */
	protected int difficultyTarget;

	/**
	 * The list of validators that participated in the consensus of this block
	 */
	protected HyFlexChainSignature[] validators;

	/**
	 * The id of the committee assigned to the consensus of this block
	 */
	protected CommitteeId committeeId;

	/**
	 * Create a new Meta Header for a block.
	 * @param hash The hash of this block
	 * @param version The version of this meta header
	 * @param consensus The consensus mechanism of this block
	 * @param difficultyTarget The proof-of-work algorithm difficulty target for this block
	 * @param validators The list of validators that participated in the consensus of this block
	 * @param committeeId The id of the committee assigned to the consensus of this block
	 */
	public BlockMetaHeader(String version, ConsensusMechanism consensus,
			int difficultyTarget, HyFlexChainSignature[] validators, CommitteeId committeeId) {
		this.version = version;
		this.consensus = consensus;
		this.difficultyTarget = difficultyTarget;
		this.validators = validators;
		this.committeeId = committeeId;
	}

	/**
	 * Create a new Meta Header for a block with the current version.
	 * @param consensus The consensus mechanism of this block
	 * @param difficultyTarget The proof-of-work algorithm difficulty target for this block
	 * @param validators The list of validators that participated in the consensus of this block
	 * @param committeeId The id of the committee assigned to the consensus of this block
	 */
	public BlockMetaHeader(ConsensusMechanism consensus,
			int difficultyTarget, HyFlexChainSignature[] validators, CommitteeId committeeId) {
		this.version = Version.V1_0.getVersion();
		this.consensus = consensus;
		this.difficultyTarget = difficultyTarget;
		this.validators = validators;
		this.committeeId = committeeId;
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
	public ConsensusMechanism getConsensus() {
		return consensus;
	}

	/**
	 * The consensus mechanism of this block
	 * @param consensus the consensus to set
	 */
	public void setConsensus(ConsensusMechanism consensus) {
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
	public HyFlexChainSignature[] getValidators() {
		return validators;
	}

	/**
	 * The list of validators that participated in the consensus of this block
	 * @param validators the validators to set
	 */
	public void setValidators(HyFlexChainSignature[] validators) {
		this.validators = validators;
	}

	/**
	 * The id of the committee assigned to the consensus of this block
	 * @return the committeeId
	 */
	public CommitteeId getCommitteeId() {
		return committeeId;
	}

	/**
	 * The id of the committee assigned to the consensus of this block
	 * @param committeeId the committeeId to set
	 */
	public void setCommitteeId(CommitteeId committeeId) {
		this.committeeId = committeeId;
	}

	@Override
	public int serializedSize() {
		return BytesOps.serializedSize(version)
			+ BytesOps.serializedSize(consensus.getConsensus())
			+ Integer.BYTES
			+ BytesOps.serializedSize(validators)
			+ committeeId.serializedSize();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockMetaHeader other = (BlockMetaHeader) obj;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		if (consensus != other.consensus)
			return false;
		if (difficultyTarget != other.difficultyTarget)
			return false;
		if (!Arrays.equals(validators, other.validators))
			return false;
		if (!committeeId.equals(other.committeeId))
			return false;
		return true;
	}

	public static class Serializer implements ISerializer<BlockMetaHeader>
	{
		protected static final ISerializer<String> stringSerializer =
			Utils.serializer.getSerializer(String.class);

		protected static final ISerializer<HyFlexChainSignature[]> signatureArraySerializer =
			Utils.serializer.getArraySerializer(HyFlexChainSignature.class, HyFlexChainSignature.SERIALIZER);


		@Override
		public void serialize(BlockMetaHeader t, ByteBuf out) throws IOException {
			stringSerializer.serialize(t.version, out);
			stringSerializer.serialize(t.consensus.getConsensus(), out);
			out.writeInt(t.difficultyTarget);
			signatureArraySerializer.serialize(t.validators, out);
			CommitteeId.SERIALIZER.serialize(t.committeeId, out);
		}

		@Override
		public BlockMetaHeader deserialize(ByteBuf in) throws IOException {
			var res = new BlockMetaHeader();

			res.version = stringSerializer.deserialize(in);

			try {
				res.consensus = ConsensusMechanism.parse(stringSerializer.deserialize(in));
			} catch (Exception e) {
				throw new IOException(e.getMessage(), e);
			}

			res.difficultyTarget = in.readInt();
			res.validators = signatureArraySerializer.deserialize(in);
			res.committeeId = CommitteeId.SERIALIZER.deserialize(in);
			
			return res;
		}
		
	}
	
}
