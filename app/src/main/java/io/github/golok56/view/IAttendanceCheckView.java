package io.github.golok56.view;

import java.util.ArrayList;

import io.github.golok56.object.Student;
import io.github.golok56.view.base.IBaseView;

/**
 * Interface that {@link io.github.golok56.view.activity.AttendanceCheckActivity} need to implement.
 * So it can interact with the corresponding presenter.
 *
 * @author Satria Adi Putra
 */
public interface IAttendanceCheckView extends IBaseView {

    /**
     *
     */
    void showSchoolMenu();

    /**
     *
     * @param list
     */
    void setAdapter(ArrayList<Student> list);
}
