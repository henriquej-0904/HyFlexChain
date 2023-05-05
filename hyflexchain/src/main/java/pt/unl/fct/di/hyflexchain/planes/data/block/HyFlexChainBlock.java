package pt.unl.fct.di.hyflexchain.planes.data.block;

/**
 * Represents a block of transactions with all necessary metadata.
 */
public record HyFlexChainBlock(
	BlockHeader header, BlockBody body
) {}
