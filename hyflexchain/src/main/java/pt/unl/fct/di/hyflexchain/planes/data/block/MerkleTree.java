package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Queue;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.util.crypto.Crypto;

/**
 * A Merkle Tree for transactions in a block.
 */
public class MerkleTree
{
	/**
	 * Create a new Node.
	 * 
	 * @param hash Double SHA256 hash
	 * @param left Left node (or null if this node is a leaf)
	 * @param right Right node (or null if this node is a leaf)
	 */
	public static record Node(
		Bytes hash, Node left, Node right
	) implements Cloneable {

		/**
		 * Create a new Leaf node based on the hash of a transaction.
		 * @param txHash The hash of the transaction
		 * @return A new Leaf node.
		 */
		public static Node createLeafNode(Bytes txHash, MessageDigest md)
		{
			txHash.update(md);
			return new Node(Bytes.wrap(md.digest()), null, null);
		}

		/**
		 * Create a new Node based on the left and right children nodes.
		 * @param left The left (child) node
		 * @param right The right (child) node
		 * @return A new Node.
		 */
		public static Node createNode(Node left, Node right, MessageDigest md)
		{
			left.hash.update(md);
			right.hash.update(md);
			var digest = md.digest();

			// double hash
			digest = md.digest(digest);

			return new Node(Bytes.wrap(digest), left, right);
		}

		@Override
		public Object clone() {
			return new Node(hash, left, right);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;

			if (obj == null)
				return false;

			if ( !(obj instanceof Node) )
				return false;

			Node other = (Node) obj;
			return hash.equals(other.hash);
		}
		
	}

	protected Node root;

	protected MerkleTree() {}

	protected MerkleTree(Node root)
	{
		this.root = root;
	}

	/**
	 * Create a new Merkle Tree based on a list of transaction hashes.
	 * @param txHashes The list of transaction hashes
	 */
	public static MerkleTree createMerkleTree(Collection<Bytes> txHashes)
	{
		return new MerkleTree(new MerkleTreeBuilder(txHashes).createMerkleTree());
	}

	public Bytes getMerkleRootHash()
	{
		return root.hash;
	}

	/**
	 * @return the root
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(Node root) {
		this.root = root;
	}

	protected static class MerkleTreeBuilder {

		protected final Collection<Bytes> txHashes;

		protected final MessageDigest md;
		

		/**
		 * @param txHashes
		 */
		public MerkleTreeBuilder(Collection<Bytes> txHashes) {
			this.txHashes = txHashes;
			this.md = Crypto.getSha256Digest();
		}

		public Node createMerkleTree() {
			Queue<Node> nodes = createLeafNodes(txHashes);

			while (nodes.size() >= 2) {
				nodes = createNodes(nodes);
			}

			assert nodes.size() == 1;
			return nodes.poll();
		}

		/**
		 * Create the base level of leaf nodes based on the tx hashes.
		 * 
		 * @param txHashes A stream of tx hashes
		 * @return A queue of computed nodes.
		 */
		protected Queue<Node> createLeafNodes(Collection<Bytes> txHashes) {
			if (txHashes.isEmpty())
				throw new IllegalArgumentException("txHashes is empty");

			boolean isEven = txHashes.size() % 2 == 0;

			Deque<Node> nodes = new ArrayDeque<>(isEven ? txHashes.size() : txHashes.size() + 1);

			for (var txHash : txHashes) {
				nodes.add(Node.createLeafNode(txHash, md));
			}

			if (!isEven) {
				// add a copy of the last node
				nodes.add(nodes.peekLast());
			}

			return nodes;
		}

		/**
		 * Create a new Queue<Node> level of nodes on the tree.
		 * 
		 * @param nodes The list of nodes to compute the upper level
		 * @return The computed list of nodes.
		 * @precondition {@code nodes.size() >= 2 && nodes.size() % 2 == 0}
		 */
		protected Queue<Node> createNodes(Queue<Node> nodes) {
			assert nodes.size() >= 2 && nodes.size() % 2 == 0;

			int size = nodes.size();

			for (int i = 0; i < size / 2; i++) {
				nodes.add(Node.createNode(nodes.poll(), nodes.poll(), md));
			}

			return nodes;
		}
	}
}
