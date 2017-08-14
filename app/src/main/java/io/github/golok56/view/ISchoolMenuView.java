package io.github.golok56.view;

import io.github.golok56.object.Student;
import io.github.golok56.view.activity.SchoolMenuActivity;
import io.github.golok56.view.base.IBaseView;

/**
 * Interface that {@link SchoolMenuActivity} need to implement. So it can
 * interact with the corresponding presenter.
 *
 * @author Satria Adi Putra
 */
public interface ISchoolMenuView extends IBaseView {
    /**
     *
     *
     * @param layoutActive
     */
    void changeAdapter(SchoolMenuActivity.LayoutActive layoutActive);

    /**
     *
     * @param student
     */
    void toggleStudentChecked(Student student);

    /**
     *
     */
    void showTotalStudentPickerDialog();

    /**
     *
     */
    void showClearConfirmationDialog();

    /**
     *
     * @param studentTotal
     */
    void showAddStudentActivity(int studentTotal);

    /**
     *
     */
    void showDeleteSchoolConfirmationDialog();

    /**
     *
     */
    void restartView();

    /**
     *
     */
    void showAttendanceCheckActivity();

    /**
     * Go back to the Main Activity
     */
    void showMainActivity();
}
