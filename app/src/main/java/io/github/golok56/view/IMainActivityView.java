package io.github.golok56.view;

import io.github.golok56.view.base.IBaseView;

/**
 * Interface that {@link io.github.golok56.absenpelatih.MainActivity} need to implement. So it can
 * interact with the corresponding presenter.
 *
 * @author Satria Adi Putra
 */
public interface IMainActivityView extends IBaseView {

    /**
     * Show form dialog to add a new {@link io.github.golok56.object.School} to database.
     */
    void showAddSchoolDialog();

}
