package org.smartregister.reveal.activity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.smartregister.reveal.event.ViewConfigurationSyncCompleteEvent;
import org.smartregister.reveal.presenter.LoginPresenter;
import org.smartregister.task.SaveTeamLocationsTask;
import org.smartregister.ug.reveal.R;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

import static org.smartregister.util.Log.logInfo;

/**
 * Created by ndegwamartin on 21/06/2018.
 */
public class LoginActivity extends BaseLoginActivity implements BaseLoginContract.View {
    public static final String TAG = BaseLoginActivity.class.getCanonicalName();

    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initializePresenter() {
        mLoginPresenter = new LoginPresenter(this);
    }

    @Override
    public void goToHome(boolean remote) {
        if (remote) {
            org.smartregister.util.Utils.startAsyncTask(new SaveTeamLocationsTask(), null);
        }
        gotToHomeRegister(remote);
//        if (mLoginPresenter.isSiteCharacteristicsSet()) {
//
//            gotToHomeRegister(remote);
//
//        } else {
//
//            goToSiteCharacteristics(remote);
//        }

        finish();
    }

    private void gotToHomeRegister(boolean remote) {
////        Intent intent = new Intent(this, HomeRegisterActivity.class);
//        intent.putExtra(AllConstants.INTENT_KEY.IS_REMOTE_LOGIN, remote);
//        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mLoginPresenter.processViewCustomizations();
        if (!mLoginPresenter.isUserLoggedOut()) {
            goToHome(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void refreshViews(ViewConfigurationSyncCompleteEvent syncCompleteEvent) {
        if (syncCompleteEvent != null) {
            logInfo("Refreshing Login View...");
            mLoginPresenter.processViewCustomizations();

        }
    }
}
