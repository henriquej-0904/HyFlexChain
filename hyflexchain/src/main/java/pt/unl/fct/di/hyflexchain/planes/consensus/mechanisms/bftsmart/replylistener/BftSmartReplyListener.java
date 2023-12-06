package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.replylistener;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.core.messages.TOMMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.util.reply.MultiSignedReply;
import pt.unl.fct.di.hyflexchain.util.reply.SignedReply;

/**
 * An implementation of the ReplyListener
 */
public class BftSmartReplyListener implements ReplyListener
{
    protected static final Logger LOG = LoggerFactory.getLogger(BftSmartReplyListener.class);

    protected final AsynchServiceProxy proxy;

    protected final Set<Address> committeeAddresses;

    protected final int minReplies;

    protected final List<MultiSignedReply> replies;

    protected int requestId;

    protected MultiSignedReply topReply;

    protected Consumer<MultiSignedReply> postAction;

    protected boolean complete;

    /**
     * Create a new Reply Listener for BFT-SMaRt that waits for
     * the specified number of replies.
     * @param proxy The bft-smart proxy
     * @param committeeAddresses The addresses of all the replicas in the committee
     * @param minReplies The min number of replies to conclude the
     * async bft-smart invocation
     * @return The created listener.
     */
    public BftSmartReplyListener(AsynchServiceProxy proxy,
        Set<Address> committeeAddresses, int minReplies)
    {
        this.proxy = Objects.requireNonNull(proxy);
        this.committeeAddresses = Objects.requireNonNull(committeeAddresses);

        if (minReplies <= 0)
            throw new IllegalArgumentException("minReplies must be greater than 0");

        this.minReplies = minReplies;
        this.replies = new LinkedList<>();
        this.requestId = -1;

        this.complete = false;
    }

    /**
     * Submit an asynchronous ordered request,
     * set the request id and execute the
     * specified callback when reached consensus.
     * @param request The serialized request
     * @param postAction The action to execute when this client
     * reaches consensus (receives {@link #minReplies} equal replies)
     * @return The request Id
     */
    public int submitAsyncOrderedRequest(byte[] request, Consumer<MultiSignedReply> postAction)
    {
        this.postAction = Objects.requireNonNull(postAction);
        this.requestId =
            this.proxy.invokeAsynchRequest(request, this, TOMMessageType.ORDERED_REQUEST);

        return this.requestId;
    }

    /**
     * Submit an asynchronous unordered request,
     * set the request id and execute the
     * specified callback when reached consensus.
     * @param request The serialized request
     * @param postAction The action to execute when this client
     * reaches consensus (receives {@link #minReplies} equal replies)
     * @return The request Id
     */
    public int submitAsyncUnorderedRequest(byte[] request, Consumer<MultiSignedReply> postAction)
    {
        this.postAction = Objects.requireNonNull(postAction);
        this.requestId =
            this.proxy.invokeAsynchRequest(request, this, TOMMessageType.UNORDERED_REQUEST);

        return this.requestId;
    }

    @Override
    public void replyReceived(RequestContext arg0, TOMMessage arg1)
    {
        if (this.complete)
            return;

        // LOG.info("Received BFT-SMART reply!");

        if (this.requestId == -1)
            this.requestId = arg0.getOperationId();

        if (arg1.getContent().length == 0)
        {
            LOG.info("Invalid bft-smart replica reply: empty");
            return;
        }

        if (arg1.getContent().length == 1)
        {
            LOG.info("Invalid bft-smart replica reply: invalid transactions");
            return;
        }

        ByteBuf replyBytes = Unpooled.wrappedBuffer(arg1.getContent());

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
                this.complete = true;
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

    /**
     * The requestId
     * @return The requestId
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * The min number of replies to conclude the
     * async bft-smart invocation
     * @return The number of min replies
     */
    public int getMinReplies() {
        return minReplies;
    }

    
}
