package io.github.golok56.object;

import android.os.Parcel;
import android.os.Parcelable;

public class Student implements Parcelable {

    private int mId;
    private int mJumlahKehadiran;
    private String mNamaMurid;
    private String mKelas;

    public static final Creator<Student> CREATOR = new Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel in) {
            return new Student(in);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };

    public Student(int id, String nama, String kelas, int jmlKehadiran) {
        mId = id;
        mNamaMurid = nama;
        mKelas = kelas;
        mJumlahKehadiran = jmlKehadiran;
    }

    public Student(String nama, String kelas) {
        mNamaMurid = nama;
        mKelas = kelas;
    }

    private Student(Parcel in) {
        mId = (int) in.readLong();
        mJumlahKehadiran = in.readInt();
        mNamaMurid = in.readString();
        mKelas = in.readString();
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getTotalAttendance() {
        return mJumlahKehadiran;
    }

    public String getName() {
        return mNamaMurid;
    }

    public String getStudentClass() {
        return mKelas;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeInt(mJumlahKehadiran);
        dest.writeString(mNamaMurid);
        dest.writeString(mKelas);
    }

    public void addJumlahKehadiran() {
        mJumlahKehadiran++;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Student)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Student other = (Student) obj;
        return this.getId() == other.getId();
    }

}
