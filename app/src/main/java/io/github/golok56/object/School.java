package io.github.golok56.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class School implements Parcelable {

    public static final Creator<School> CREATOR = new Creator<School>() {
        @Override
        public School createFromParcel(Parcel in) {
            return new School(in);
        }

        @Override
        public School[] newArray(int size) {
            return new School[size];
        }
    };
    private int mId;
    private String mNamaSekolah;
    private ArrayList<Student> mStudent = new ArrayList<>();

    public School(String name){
        this.mNamaSekolah = name;
    }

    public School(int id, String name, ArrayList<Student> student){
        mId = id;
        mNamaSekolah = name;
        mStudent = student;
    }

    private School(Parcel in) {
        mId = in.readInt();
        mNamaSekolah = in.readString();
        in.readTypedList(mStudent, Student.CREATOR);
    }

    public ArrayList<Student> getStudents(){
        return mStudent;
    }

    public void add(Student studentBaru){
        mStudent.add(studentBaru);
    }

    public void remove(Student studentApus){
        mStudent.remove(studentApus);
    }

    public int totalStudents(){
        return mStudent.size();
    }

    public String getSchoolName() {
        return mNamaSekolah;
    }

    public int getId() {
        return mId;
    }

    public int getTotalKehadiran(){
        int total = 0;
        for (Student student : mStudent) {
            total += student.getTotalAttendance();
        }
        return total;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mNamaSekolah);
        dest.writeTypedList(mStudent);
    }
}
