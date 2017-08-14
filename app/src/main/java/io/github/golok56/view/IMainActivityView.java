package io.github.golok56.view;

import java.util.ArrayList;

import io.github.golok56.object.School;
import io.github.golok56.view.activity.MainActivity;
import io.github.golok56.view.activity.SchoolMenuActivity;
import io.github.golok56.view.base.IBaseView;

/**
 * Interface that {@link MainActivity} need to implement. So it can
 * interact with the corresponding presenter.
 *
 * @author Satria Adi Putra
 */
public interface IMainActivityView extends IBaseView {

    /**
     * Show form dialog to add a new {@link io.github.golok56.object.School} to database.
     */
    void showAddSchoolDialog();

    /**
     * Show confirmation dialog form to clear the database history.
     */
    void showClearDialog();

    /**
     * Show change password form for confirmation when clearing the database history.
     */
    void showChangePasswordDialog();

    /**
     * Showing error if school name form when adding new school is empty.
     */
    void showSchoolNameError(String msg);

    /**
     * Set the list of schools of the view and update the view.
     */
    void setSchoolList(ArrayList<School> schoolList);

    /**
     * Start an intent to {@link SchoolMenuActivity}.
     *
     * @param school The school data to be passed.
     */
    void showSchoolMenu(School school);

}
