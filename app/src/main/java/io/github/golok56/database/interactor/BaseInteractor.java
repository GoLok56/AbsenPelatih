package io.github.golok56.database.interactor;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

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

    // Get all the object from the database in form of ArrayList
    public abstract ArrayList<T> getList(String name);

    // Clearing the database
    public abstract void clear(String name);

    // Delete a row from database
    public abstract boolean delete(T obj);

    // Insert the given data to database
    public abstract boolean insert(T obj);

}
