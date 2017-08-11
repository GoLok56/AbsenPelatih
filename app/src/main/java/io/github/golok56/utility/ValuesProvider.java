package io.github.golok56.utility;

import android.content.ContentValues;

import io.github.golok56.database.DBSchema;
import io.github.golok56.object.School;
import io.github.golok56.object.Student;

/**
 * A utility class to provide a {@link android.content.ContentValues} for each corresponding table
 * in database.
 *
 * @author Satria Adi putra
 */
public class ValuesProvider {

    /**
     * This method will provide a {@link ContentValues} for {@link Student} table in database.
     *
     * @param student The data of student to get inserted.
     * @return Values for {@link Student} table.
     */
    public static ContentValues get(Student student){
        ContentValues values = new ContentValues();
        values.put(DBSchema.Student.CLASS_COLUMN, student.getStudentClass());
        values.put(DBSchema.Student.NAME_COLUMN, student.getName());
        return values;
    }

    /**
     * This method will provide a {@link ContentValues} for {@link School} table in database.
     *
     * @param school The data of school to get inserted.
     * @return Values for {@link Student} table.
     */
    public static ContentValues get(School school){
        ContentValues values = new ContentValues();
        values.put(DBSchema.School.NAME_COLUMN, school.getSchoolName());
        return values;
    }

}
