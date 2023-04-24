package pt.unl.fct.di.hyflexchain.util.result;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.ws.rs.WebApplicationException;

public class OkResult<T> implements Result<T> {

    private T result;

    /**
     * 
     */
    public OkResult() {}

    public OkResult(T result) {
        this.result = result;
    }

    @JsonIgnore
    public boolean isOK() {
        return true;
    }

    public T value() {
        return this.result;
    }

    public T getResult() {
        return this.result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(T result) {
        this.result = result;
    }

    public int error() {
        if (this.result == null) {
            return 204;
        }
        
        return 200;
    }

    public String toString() {
        return "(OK, " + value() + ")";
    }

    public T resultOrThrow() {
        return this.result;
    }

    @Override
    public WebApplicationException errorException() {
        return null;
    }

    
}