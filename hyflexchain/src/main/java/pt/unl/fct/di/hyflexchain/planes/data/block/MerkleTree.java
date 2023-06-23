package pt.unl.fct.di.hyflexchain.planes.data.block;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

import static pt.unl.fct.di.hyflexchain.util.Utils.fromHex;
import static pt.unl.fct.di.hyflexchain.util.Utils.toHex;
import static pt.unl.fct.di.hyflexchain.util.crypto.Crypto.getSha256Digest;

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
		String hash, Node left, Node right
	) implements Cloneable {

		/**
		 * Create a new Leaf node based on the hash of a transaction.
		 * @param txHash The hash (string hex) of the transaction
		 * @return A new Leaf node.
		 */
		public static Node createLeafNode(String txHash)
		{
			var fstHash = fromHex(txHash);
			var sndHash = getSha256Digest().digest(fstHash);
			return new Node(toHex(sndHash), null, null);
		}

		/**
		 * Create a new Node based on the left and right children nodes.
		 * @param left The left (child) node
		 * @param right The right (child) node
		 * @return A new Node.
		 */
		public static Node createNode(Node left, Node right)
		{
			var msgDigest = getSha256Digest();
			msgDigest.update(fromHex(left.hash()));
			msgDigest.update(fromHex(right.hash()));
			var digest = msgDigest.digest();

			// double hash
			digest = msgDigest.digest(digest);

			return new Node(toHex(digest), left, right);
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			return new Node(hash, left, right);
		}

		@Override
		public int hashCode() {
			return hash.hashCode();
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

	public MerkleTree() {}

	/**
	 * Create a new Merkle Tree based on a list of transactions.
	 * @param txHashes The list of transaction hashes
	 */
	public MerkleTree(Collection<String> txHashes)
	{
		this.root = createMerkleTree(txHashes);
	}

	protected Node createMerkleTree(Collection<String> txHashes)
	{
		Queue<Node> nodes = createLeafNodes(txHashes);

		while (nodes.size() >= 2) {
			nodes = createNodes(nodes);
		}

		assert nodes.size() == 1;
		return nodes.poll();
	}

	/**
	 * Create the base level of leaf nodes based on the tx hashes.
	 * @param txHashes A list of tx hashes
	 * @return A list of computed nodes.
	 */
	protected Queue<Node> createLeafNodes(Collection<String> txHashes)
	{
		if (txHashes.isEmpty())
			throw new IllegalArgumentException("txHashes is empty");

		boolean isEven = txHashes.size() % 2 == 0;
		Queue<Node> nodes = new ArrayDeque<>(isEven ? txHashes.size() : txHashes.size() + 1);

		if (isEven)
		{
			for (String txHash : txHashes) {
				nodes.add(Node.createLeafNode(txHash));
			}
		} else
		{
			Node n = null;
			var it = txHashes.iterator();

			while (it.hasNext()) {
				n = Node.createLeafNode(it.next());
				nodes.add(n);
			}

			// add a copy of the last node
			nodes.add(n);
		}

		return nodes;
	}

	/**
	 * Create a new Queue<Node> level of nodes on the tree.
	 * @param nodes The list of nodes to compute the upper level
	 * @return The computed list of nodes.
	 * @precondition {@code nodes.size() >= 2 && nodes.size() % 2 == 0}
	 */
	protected Queue<Node> createNodes(Queue<Node> nodes)
	{
		assert nodes.size() >= 2 && nodes.size() % 2 == 0;

		int size = nodes.size();

		for (int i = 0; i < size / 2; i++)
		{
			nodes.add(Node.createNode(nodes.poll(), nodes.poll()));
		}

		return nodes;
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

	/**
	 * Verify if this merkle tree is valid for
	 * the specified list of transactions.
	 * @param txHashes The list of transaction hashes.
	 * @return true if this merkle tree is valid.
	 */
	public boolean verifyTree(Collection<String> txHashes)
	{
		var calculated = new MerkleTree(txHashes);
		return verifyTree(this.getRoot(), calculated.getRoot());
	}
	

	protected boolean verifyTree(Node n1, Node n2)
	{
		if (n1 == null && n2 == null)
			return true;

		if (n1 == null)
			return false;

		if (!n1.equals(n2))
			return false;

		return verifyTree(n1.left, n2.left)
			&& verifyTree(n1.right, n2.right);		
	}

	
}
