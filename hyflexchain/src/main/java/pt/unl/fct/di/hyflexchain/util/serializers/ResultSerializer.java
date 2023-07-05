package pt.unl.fct.di.hyflexchain.util.serializers;

import java.nio.ByteBuffer;

import pt.unl.fct.di.hyflexchain.util.result.Result;

public class ResultSerializer<Ok, Failed> implements BytesSerializer<Result>
{
    private final BytesSerializer<Ok> okSerializer;

    private final BytesSerializer<Failed> failedSerializer;

    private final byte TRUE = 1;

    private final byte FALSE = 0;

    /**
     * @param okSerializer
     * @param failedSerializer
     */
    public ResultSerializer(BytesSerializer<Ok> okSerializer, BytesSerializer<Failed> failedSerializer) {
        this.okSerializer = okSerializer;
        this.failedSerializer = failedSerializer;
    }

    @Override
    public Class<Result> getType() {
        return Result.class;
    }

    @Override
    public int serializedSize(Result obj) {
        return 1 + (obj.isOk() ?
            okSerializer.serializedSize((Ok) obj.getOkResult()) :
            failedSerializer.serializedSize((Failed) obj.getFailedResult())
        );
    }

    @Override
    public ByteBuffer serialize(Result obj, ByteBuffer buff) {
        if (obj.isOk())
        {
            Ok ok = (Ok) obj.getOkResult();
            buff.put(TRUE);
            return okSerializer.serialize(ok, buff);
        }
        else
        {
            Failed failed = (Failed) obj.getFailedResult();
            buff.put(FALSE);
            return failedSerializer.serialize(failed, buff);
        }
    }

    @Override
    public Result deserialize(ByteBuffer buff) {
        return buff.get() == TRUE ?
            Result.ok(okSerializer.deserialize(buff)) :
            Result.failed(failedSerializer.deserialize(buff));
    }

    
    
}
