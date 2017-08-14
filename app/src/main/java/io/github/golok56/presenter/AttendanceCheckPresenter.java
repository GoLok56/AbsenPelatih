package io.github.golok56.presenter;

import java.util.ArrayList;

import io.github.golok56.callback.IOnReadCompleted;
import io.github.golok56.callback.base.IBaseOnOperationCompleted;
import io.github.golok56.database.interactor.AttendanceInteractor;
import io.github.golok56.database.interactor.StudentInteractor;
import io.github.golok56.object.Student;
import io.github.golok56.view.IAttendanceCheckView;
import io.github.golok56.view.activity.AttendanceCheckActivity;

/**
 * The presenter for {@link AttendanceCheckActivity}.
 *
 * @author Satria Adi Putra
 */
public class AttendanceCheckPresenter {

    private IAttendanceCheckView mView;

    private AttendanceInteractor mAttendanceInteractor;

    private StudentInteractor mStudentInteractor;

    public AttendanceCheckPresenter(IAttendanceCheckView view,
                                    AttendanceInteractor attendanceInteractor,
                                    StudentInteractor studentInteractor){
        mView = view;
        mAttendanceInteractor = attendanceInteractor;
        mStudentInteractor = studentInteractor;
    }

    public void onDoCheckClicked(){
        mAttendanceInteractor.insert(new IBaseOnOperationCompleted(){
            @Override
            public void onFinished() {
                mView.showToast("Behasil melakukan absen!");
                mView.showSchoolMenu();
            }
        });
    }

    public void onSearchTextChanged(String subName){
        mStudentInteractor.getList(subName, new IOnReadCompleted<Student>() {
            @Override
            public void onSuccess(ArrayList<Student> list) {
                mView.setAdapter(list);
            }

            @Override
            public void onFinished() {
                mView.setAdapter(null);
            }
        });
    }

}
