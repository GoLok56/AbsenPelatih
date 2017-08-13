package io.github.golok56.presenter;

import java.util.ArrayList;

import io.github.golok56.callback.IOnBasicOperationCompleted;
import io.github.golok56.callback.IOnReadCompleted;
import io.github.golok56.database.interactor.SchoolInteractor;
import io.github.golok56.object.School;
import io.github.golok56.utility.PreferenceManager;
import io.github.golok56.view.IMainActivityView;
import io.github.golok56.view.activity.MainActivity;

/**
 * The presenter for {@link MainActivity}.
 *
 * @author Satria Adi Putra
 */
public class MainActivityPresenter {

    /**
     * The view to be presented.
     */
    private IMainActivityView mView;

    private SchoolInteractor mSchoolInteractor;

    private PreferenceManager mPref;

    public MainActivityPresenter(IMainActivityView view, SchoolInteractor schoolInteractor,
                                 PreferenceManager pref) {
        mView = view;
        mSchoolInteractor = schoolInteractor;
        mPref = pref;
    }

    public void getItems(){
        mSchoolInteractor.getList("", new IOnReadCompleted<School>() {
            @Override
            public void onSuccess(ArrayList<School> list) {
                mView.setSchoolList(list);
            }

            @Override
            public void onFailed() {
                mView.setSchoolList(null);
            }
        });
    }

    public void onAddSchoolClicked() {
        mView.showAddSchoolDialog();
    }

    public void onItemClicked(School school){
        mView.showSchoolMenu(school);
    }

    public void onSaveSchoolClicked(final String schoolName) {
        if (schoolName.isEmpty()) {
            mView.showSchoolNameError("Nama sekolah tidak boleh kosong!");
        } else {
            mSchoolInteractor.insert(new School(schoolName), new IOnBasicOperationCompleted() {
                @Override
                public void onSuccess() {
                    getItems();
                    mView.showToast("Berhasil menambahkan " + schoolName);
                }

                @Override
                public void onFailed() {
                    mView.showToast("Terjadi Kesalahan!");
                }
            });
        }
    }

    public void onCancelSaveClicked(){
        mView.showToast("Penambahan sekolah dibatalkan!");
    }

    public void onItemClearClicked(){
        mView.showClearDialog();
    }

    public void onClearConfirmClicked(ArrayList<School> schools, String pass){
        if(mPref.checkPassword(pass)){
            mSchoolInteractor.clear(schools, new IOnBasicOperationCompleted() {
                @Override
                public void onSuccess() {
                    mView.showToast("Berhasil menghapus semua absen!");
                }

                @Override
                public void onFailed() {}
            });
        }
    }

    public void onClearCancelClicked(){
        mView.showToast("Batal menghapus!");
    }

    public void onChangePassClicked(){
        mView.showChangePasswordDialog();
    }

    public void onChangePassComfirmed(String oldPassword, String newPassword){
        if (!oldPassword.isEmpty() || !newPassword.isEmpty()){
            if(mPref.checkPassword(oldPassword)) {
                mPref.changePassword(newPassword);
                mView.showToast("Password berhasil diubah!");
            } else {
                mView.showToast("Password lama anda salah!");
            }
        }
        else {
            if(oldPassword.isEmpty()){
                mView.showOldPasswordError("Isi password lama anda!");
            }
            if(newPassword.isEmpty()){
                mView.showNewPasswordError("Isi password baru anda!");
            }
        }
    }

    public void onChangePassCanceled(){
        mView.showToast("Batal mengganti password!");
    }

}
