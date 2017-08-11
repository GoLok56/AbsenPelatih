package io.github.golok56.callback.base;

/**
 * Base interface to do a callback when operation from database is success.
 *
 * @author Satria Adi Putra
 */
public interface IBaseOnOperationCompleted {

    /**
     * A callback if the operation is failed.
     */
    void onFailed();

}
