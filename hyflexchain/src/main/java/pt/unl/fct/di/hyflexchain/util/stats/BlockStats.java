package pt.unl.fct.di.hyflexchain.util.stats;

import java.util.EnumMap;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;

import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.list.primitive.MutableIntList;

import pt.unl.fct.di.hyflexchain.planes.consensus.ConsensusMechanism;

public class BlockStats {

    public static EnumMap<ConsensusMechanism, BlockStats> stats;

    static {
        reset();
    }

    public static void reset() {
        stats = new EnumMap<>(ConsensusMechanism.class);
        for (var c : ConsensusMechanism.values())
            stats.put(c, new BlockStats(c));
    }

    public static void addLatency(ConsensusMechanism c, int latency)
    {
        stats.get(c).addLatency(latency);
    }
    
    private final ConsensusMechanism mechanism;

    private int maxBlockSize;

    private MutableIntList finalizationLatencies;

    private long[] timeElapsed;

    private int nBlocks;

    /**
     * @param mechanism
     */
    public BlockStats(ConsensusMechanism mechanism) {
        this.mechanism = mechanism;
        this.finalizationLatencies = IntLists.mutable.withInitialCapacity(5000);
    }

    public void addLatency(int latency)
    {
        finalizationLatencies.add(latency);
    }

    /**
     * @return the mechanism
     */
    public ConsensusMechanism getMechanism() {
        return mechanism;
    }

    /**
     * @return the maxBlockSize
     */
    /* public int getMaxBlockSize() {
        return maxBlockSize;
    } */

    /**
     * @return the finalizationLatencies
     */
    public int[] getFinalizationLatencies() {
        return finalizationLatencies.toArray();
    }

    public IntSummaryStatistics getFinalizationLatencyStats() {
        return finalizationLatencies.summaryStatistics();
    }

    /* public ThroughputStats getBlockThroughputStats() {
        if (nBlocks < 2)
            return ThroughputStats.EMPTY;

        long timeElapsed = (this.timeElapsed[1] - this.timeElapsed[0]) / 1000;

        if (timeElapsed == 0)
            return ThroughputStats.EMPTY;

        return new ThroughputStats((double) this.nBlocks / (double) timeElapsed, timeElapsed);
    }

    public static record ThroughputStats(double blocksPerSec, long timeElapsed) {
        
        public static ThroughputStats EMPTY = new ThroughputStats(0, 0);
    } */

}
