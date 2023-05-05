package pt.unl.fct.di.blockmess.cryptonode.util.result;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

public class ErrorResult<T> implements Result<T>
{
    private int error;
    private WebApplicationException exception;

    

    /**
     * 
     */
    public ErrorResult() {
    }

    public ErrorResult(int error) {
        this.error = error;
    }

    public ErrorResult(WebApplicationException ex) {
        this.error = ex.getResponse().getStatus();
        this.exception = ex;
    }

    @JsonIgnore
    public boolean isOK() {
        return false;
    }

    public T value() {
        throw new RuntimeException("Attempting to extract the value of an Error: " + error());
    }

    public int error() {
        return this.error;
    }

    public String toString() {
        return "(" + error() + ")";
    }

    public T resultOrThrow() {
        throw errorException();
    }

    @Override
    public WebApplicationException errorException() {
        return this.exception == null ?
            this.exception = new WebApplicationException(Status.fromStatusCode(this.error))
            : this.exception;
    }

    /**
     * @return the error
     */
    public int getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(int error) {
        this.error = error;
    }
}