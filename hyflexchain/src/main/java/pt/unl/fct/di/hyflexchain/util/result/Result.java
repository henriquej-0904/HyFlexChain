package pt.unl.fct.di.hyflexchain.util.result;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.ws.rs.WebApplicationException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OkResult.class, name = "OkResult"),

    @JsonSubTypes.Type(value = ErrorResult.class, name = "ErrorResult") }
)
public interface Result<T>
{
    boolean isOK();

    T resultOrThrow() throws WebApplicationException;

    T value();

    int error();

    WebApplicationException errorException();

    static <T> Result<T> ok(T result) {
        return new OkResult<T>(result);
    }

    static <T> OkResult<T> ok() {
        return new OkResult<T>(null);
    }

    static <T> ErrorResult<T> error(int error) {
        return new ErrorResult<T>(error);
    }

    static <T> ErrorResult<T> error(WebApplicationException e) {
        return new ErrorResult<T>(e);
    }
}