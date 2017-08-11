package io.github.golok56.presenter;

import android.widget.Toast;

import io.github.golok56.absenpelatih.MainActivity;
import io.github.golok56.database.interactor.SchoolInteractor;
import io.github.golok56.object.School;
import io.github.golok56.view.IMainActivityView;

/**
 * The presenter for {@link io.github.golok56.absenpelatih.MainActivity}.
 *
 * @author Satria Adi Putra
 */
public class MainActivityPresenter {

    /**
     * The view to be presented.
     */
    private IMainActivityView mView;

    private SchoolInteractor mSchoolInteractor;

    public MainActivityPresenter(IMainActivityView view, SchoolInteractor schoolInteractor) {
        mView = view;
        mSchoolInteractor = schoolInteractor;
    }

    public void onAddSchoolClicked() {
        mView.showAddSchoolDialog();
    }

    public void onSaveSchoolClicked(String schoolName) {
        if (schoolName.isEmpty()) {
            mView.showToast("Nama sekolah tidak boleh kosong!");
        } else {
            if (mSchoolInteractor.insert(new School(schoolName))) {
                MainActivity.this.refreshView();
                Toast.makeText(MainActivity.this, "Berhasil menambahkan " + schoolName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Terjadi Kesalahan!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
