package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.replylistener;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.util.reply.MultiSignedReply;
import pt.unl.fct.di.hyflexchain.util.reply.SignedReply;

public class BftSmartReplyListener implements ReplyListener
{
    protected static final Logger LOG = LoggerFactory.getLogger(BftSmartReplyListener.class);

    protected final AsynchServiceProxy proxy;

    protected final int requestId;

    protected final Set<Address> committeeAddresses;

    protected final int minReplies;

    protected final List<MultiSignedReply> replies;

    protected final Consumer<MultiSignedReply> postAction;

    protected MultiSignedReply topReply;

    /**
     * Create a new Reply Listener for BFT-SMaRt that waits for
     * the specified number of replies and then calls the
     * consumer.
     * @param proxy The bft-smart proxy
     * @param requestId The requestId returned by the proxy
     * @param committeeAddresses The addresses of all the replicas in the committee
     * @param minReplies The min number of replies to conclude the
     * async bft-smart invocation
     * @param postAction The action to execute when this client
     * receives {@link #minReplies} replies
     * @return The created listener.
     */
    public BftSmartReplyListener(AsynchServiceProxy proxy,
        int requestId,
        Set<Address> committeeAddresses, int minReplies, Consumer<MultiSignedReply> postAction)
    {
        this.proxy = proxy;
        this.requestId = requestId;
        this.committeeAddresses = committeeAddresses;
        this.minReplies = minReplies;
        this.replies = new LinkedList<>();
        this.postAction = postAction;
    }

    @Override
    public void replyReceived(RequestContext arg0, TOMMessage arg1)
    {
        if (this.topReply != null)
            return;

        ByteBuffer replyBytes = ByteBuffer.wrap(arg1.getContent());

        if (replyBytes.capacity() == 0)
        {
            LOG.info("Invalid bft-smart replica reply: empty");
            return;
        }

        if (replyBytes.capacity() == 1)
        {
            LOG.info("Invalid bft-smart replica reply: invalid transactions");
            return;
        }

        try {
            SignedReply reply = SignedReply.SERIALIZER.deserialize(replyBytes);
            
            if (!this.committeeAddresses.contains(reply.signature.address()))
            {
                LOG.info("Invalid bft-smart replica reply: replica not part of committee");
                return;
            }

            if (!reply.verifySignature())
            {
                LOG.info("Invalid bft-smart replica reply: invalid signature");
                return;
            }
            
            updateReplies(reply);

            // reply successfully processed!

            if (checkConsensus())
            {
                proxy.cleanAsynchRequest(this.requestId);
                this.postAction.accept(this.topReply);
            }

        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
            return;
        }
    }

    /**
     * Update replies
     * @param reply The reply to process
     */
    protected void updateReplies(SignedReply reply)
    {
        if (this.topReply == null)
        {
            this.topReply = createAndAddReply(reply);
            return;
        }

        if (isThisReply(reply, this.topReply))
        {
            this.topReply.addSignature(reply.signature);
            return;
        }

        MultiSignedReply found = null;
        var it = replies.iterator();
        while (it.hasNext() && found == null) {
            var tmp = it.next();
            if (tmp != this.topReply && isThisReply(reply, tmp))
                found = tmp;
        }

        if (found == null)
            createAndAddReply(reply);
        else
        {
            found.addSignature(reply.signature);
            
            if (found.getSignaturesCount() > this.topReply.getSignaturesCount())
                this.topReply = found;
        }
    }

    /**
     * Create a new MultiSignedReply based on the specified reply
     * and add it to the list of replies.
     * @param reply
     * @return The created MultiSignedReply
     */
    protected MultiSignedReply createAndAddReply(SignedReply reply)
    {
        var res = new MultiSignedReply(reply, this.minReplies);
        this.replies.add(res);
        return res;
    }

    /**
     * Check if this reply is associated with the multi signed reply.
     * @param reply
     * @param toCompare
     * @return true if the reply is associated with the multi signed reply.
     */
    protected boolean isThisReply(SignedReply reply, MultiSignedReply toCompare)
    {
        return Arrays.equals(reply.replyBytes, toCompare.getReplyBytes());
    }

    /**
     * Check if the top reply has the specified number of
     * replies to reach consensus.
     * @return true if consensus
     */
    protected boolean checkConsensus()
    {
        return this.topReply.getSignaturesCount() >= minReplies;
    }

    @Override
    public void reset() {
        this.replies.clear();
        this.topReply = null;
    }

    /**
     * @return the reply
     */
    public MultiSignedReply getTopReply() {
        return topReply;
    }
}
