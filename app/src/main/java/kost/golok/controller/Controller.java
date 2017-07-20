package kost.golok.controller;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import kost.golok.database.DBHelper;

abstract class Controller<T> {

    // For handling every database works
    SQLiteDatabase mDb;

    Controller(Context ctx){
        mDb = DBHelper.getDb(ctx);
    }

    Controller(SQLiteDatabase db){
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
