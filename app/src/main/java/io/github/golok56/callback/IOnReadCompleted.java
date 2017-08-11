package io.github.golok56.callback;

import java.util.ArrayList;

import io.github.golok56.callback.base.IBaseOnOperationCompleted;

/**
 * Interface to do a callback when insert operation from database is success.
 *
 * @author Satria Adi Putra
 */
public interface IOnReadCompleted<T> extends IBaseOnOperationCompleted {

    /**
     * A callback when the operation is a success.
     */
    void onSuccess(ArrayList<T> list);

}
