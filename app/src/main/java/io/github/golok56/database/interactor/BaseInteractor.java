package io.github.golok56.database.interactor;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import io.github.golok56.callback.IOnReadCompleted;
import io.github.golok56.callback.base.IBaseOnOperationCompleted;
import io.github.golok56.database.DBHelper;


abstract class BaseInteractor<T> {

    // For handling every database works
    SQLiteDatabase mDb;

    BaseInteractor(Context ctx){
        mDb = DBHelper.getDb(ctx);
    }

    BaseInteractor(SQLiteDatabase db){
        mDb = db;
    }

    /**
     * Read all the object of T type from database.
     *
     * @param name The object to get inserted to database.
     * @param callback Callback after the operation complete.
     */
    public abstract void getList(String name, IOnReadCompleted<T> callback);

    // Clearing the database
    public abstract void clear(String name, IBaseOnOperationCompleted callback);

    // Delete a row from database
    public abstract void delete(T obj);

    /**
     * Inserting a new object of T type to database.
     *
     * @param obj The object to get inserted to database.
     */
    public abstract void insert(T obj);

}
