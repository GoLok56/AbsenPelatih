package io.github.golok56.presenter;

import io.github.golok56.callback.IOnBasicOperationCompleted;
import io.github.golok56.callback.base.IBaseOnOperationCompleted;
import io.github.golok56.database.interactor.AttendanceInteractor;
import io.github.golok56.database.interactor.SchoolInteractor;
import io.github.golok56.database.interactor.StudentInteractor;
import io.github.golok56.object.School;
import io.github.golok56.object.Student;
import io.github.golok56.utility.PreferenceManager;
import io.github.golok56.view.activity.SchoolMenuActivity;
import io.github.golok56.view.ISchoolMenuView;

/**
 * The presenter for {@link SchoolMenuActivity}.
 *
 * @author Satria Adi Putra
 */
public class SchoolMenuPresenter {

    private ISchoolMenuView mView;

    private StudentInteractor mStudentInteractor;

    private SchoolInteractor mSchoolInteractor;

    private AttendanceInteractor mAttendanceInteractor;

    private PreferenceManager mPref;

    public SchoolMenuPresenter(ISchoolMenuView view, StudentInteractor studentInteractor,
                               SchoolInteractor schoolInteractor,
                               AttendanceInteractor attendanceInteractor, PreferenceManager pref) {
        mView = view;
        mPref = pref;
        mStudentInteractor = studentInteractor;
        mSchoolInteractor = schoolInteractor;
        mAttendanceInteractor = attendanceInteractor;
    }

    public void refresh(SchoolMenuActivity.LayoutActive layoutActive) {
        mView.changeAdapter(layoutActive);
    }

    public void onItemDeleteStudentClicked(Student student) {
        mView.toggleStudentChecked(student);
    }

    public void onItemListLongClicked(SchoolMenuActivity.LayoutActive layoutActive) {
        mView.changeAdapter(layoutActive);
    }

    public void onAddStudentClicked() {
        mView.showTotalStudentPickerDialog();
    }

    public void onTotalConfirmClicked(int studentTotal) {
        mView.showAddStudentActivity(studentTotal);
    }

    public void onTotalCancelClicked() {
        mView.showToast("Penambahan murid dibatalkan!");
    }

    public void onDeleteSchoolClicked() {
        mView.showDeleteSchoolConfirmationDialog();
    }

    public void onSchoolDeleteConfirmClicked(final School school) {
        mSchoolInteractor.delete(school, new IOnBasicOperationCompleted() {
            @Override
            public void onSuccess() {
                mView.showToast("Berhasil menghapus " + school.getSchoolName() + "!");
            }

            @Override
            public void onFinished() {
                mView.showToast("Ada kesalahan saat menghapus " + school.getSchoolName() + "!");
            }
        });
    }

    public void onSchoolDeleteCancelClicked(String schoolName) {
        mView.showToast("Batal menghapus " + schoolName + "!");
    }

    public void onDeleteStudentClicked(School school) {
        mStudentInteractor.delete(school, new IBaseOnOperationCompleted() {
            @Override
            public void onFinished() {
                mView.showToast("Berhasil menghapus semua murid!");
                mView.restartView();
            }
        });
    }

    public void onDoCheckClicked() {
        mView.showAttendanceCheckActivity();
    }

    public void onClearClicked() {
        mView.showClearConfirmationDialog();
    }

    public void onClearConfirmClicked(String pass) {
        if (mPref.checkPassword(pass)) {
            mAttendanceInteractor.clear("", new IBaseOnOperationCompleted(){
                @Override
                public void onFinished() {
                    mView.showToast("Berhasil menghapus absen!");
                    mView.restartView();
                }
            });
        } else {
            mView.showToast("Password yang dimasukkan salah!");
        }
    }

    public void onClearCancelClicked(){
        mView.showToast("Batal menghapus!");
    }

}
