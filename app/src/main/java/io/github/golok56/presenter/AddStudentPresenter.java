package io.github.golok56.presenter;

import java.util.ArrayList;

import io.github.golok56.callback.base.IBaseOnOperationCompleted;
import io.github.golok56.database.interactor.StudentInteractor;
import io.github.golok56.object.School;
import io.github.golok56.object.Student;
import io.github.golok56.view.IAddStudentView;

/**
 * The presenter for {@link io.github.golok56.view.activity.AddStudentActivity}.
 *
 * @author Satria Adi Putra
 */
public class AddStudentPresenter {

    private IAddStudentView mView;

    private StudentInteractor mStudentInteractor;

    public AddStudentPresenter(IAddStudentView view, StudentInteractor studentInteractor) {
        mView = view;
        mStudentInteractor = studentInteractor;
    }

    public void onAddStudentClicked(School school) {
        if (mView.hasEmptyEditText()) {
            mView.showToast("Tidak boleh ada field yang kosong!");
        } else {
            final ArrayList<Student> students = mView.getStudents();
            mStudentInteractor.insert(students, school, new IBaseOnOperationCompleted() {
                @Override
                public void onFinished() {
                    mView.showToast("Berhasil menambahkan sejumlah " + students.size() + " murid!");
                    mView.showSchoolMenu();
                }
            });
        }
    }

    public void setupForm() {
        mView.setupForm();
    }

}
