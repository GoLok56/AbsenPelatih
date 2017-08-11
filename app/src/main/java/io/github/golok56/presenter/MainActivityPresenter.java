package io.github.golok56.presenter;

import io.github.golok56.view.IMainActivityView;

/**
 * The presenter for {@link io.github.golok56.absenpelatih.MainActivity}.
 *
 * @author Satria Adi Putra
 */
public class MainActivityPresenter {

    /** The view to be presented. */
    private IMainActivityView mView;

    public MainActivityPresenter(IMainActivityView view){
        mView = view;
    }

}
