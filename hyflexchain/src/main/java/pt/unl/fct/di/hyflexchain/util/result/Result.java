package pt.unl.fct.di.hyflexchain.util.result;

/**
 * Represents a successfull or failed result
 */
public interface Result<Ok, Failed>  {
    
    boolean isOk();

    Ok getOkResult();

    Failed getFailedResult();

    static <Ok, Failed> Result<Ok, Failed> ok(Ok result)
    {
        return new OkResult<Ok,Failed>(result);
    }

    static <Ok, Failed> Result<Ok, Failed> failed(Failed result)
    {
        return new FailedResult<Ok,Failed>(result);
    }

    class OkResult<Ok, Failed> implements Result<Ok, Failed>
    {
        private final Ok result;

        /**
         * @param result
         */
        public OkResult(Ok result) {
            this.result = result;
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public Ok getOkResult() {
            return this.result;
        }

        @Override
        public Failed getFailedResult() {
            throw new RuntimeException("There is no failed result.");
        }
        
    }

    class FailedResult<Ok, Failed> implements Result<Ok, Failed>
    {
        private final Failed result;

        /**
         * @param result
         */
        public FailedResult(Failed result) {
            this.result = result;
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public Ok getOkResult() {
            throw new RuntimeException("There is no successfull result.");
        }

        @Override
        public Failed getFailedResult() {
            return this.result;
        }
        
    }
}
