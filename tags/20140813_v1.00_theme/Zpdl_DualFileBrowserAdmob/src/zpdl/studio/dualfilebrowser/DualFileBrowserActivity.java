package zpdl.studio.dualfilebrowser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import zpdl.studio.api.dialog.ApiDialog;
import zpdl.studio.api.dialog.ApiDialog.ApiDialogListener;
import zpdl.studio.api.dialog.ApiDialogButtonParam;
import zpdl.studio.api.dialog.ApiDialogContextMenu;
import zpdl.studio.api.dialog.ApiDialogFileEditor;
import zpdl.studio.api.dialog.ApiDialogProgress;
import zpdl.studio.api.dialog.ApiDialogSimple;
import zpdl.studio.api.drawable.ApiDrawableConfig;
import zpdl.studio.api.drawable.ApiDrawableFactory;
import zpdl.studio.api.theme.ApiTheme;
import zpdl.studio.api.util.ApiFile;
import zpdl.studio.api.util.ApiLog;
import zpdl.studio.duallist.DualListFragment;
import zpdl.studio.duallist.DualListFragment.onDualListFragmentListener;
import zpdl.studio.duallist.DualListTheme;
import zpdl.studio.duallist.dialog.DualListDialogBrowser;
import zpdl.studio.duallist.dialog.DualListDialogDetail;
import zpdl.studio.duallist.view.DualListItem;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class DualFileBrowserActivity extends Activity implements onDualListFragmentListener, ApiDialogListener {
    private static final String mSharedPreferencesKey   = "Zpdl_DualFileBrowser";
    private static final String mSharedPreferencesPath  = "Zpdl_Path";
    private static final String mSharedPreferencesTheme = "Zpdl_Theme";

    private static final int OPTION_THEME            = 0x0001;
    private static final int OPTION_FILE             = 0x0002;
    private static final int OPTION_ADD_FOLDER       = 0x0003;
    private static final int OPTION_COPY             = 0x0004;
    private static final int OPTION_MOVE             = 0x0005;
    private static final int OPTION_DELETE           = 0x0006;
    private static final int OPTION_SELECT_ALL_TRUE  = 0x0007;
    private static final int OPTION_SELECT_ALL_FALSE = 0x0008;

    private static final int CONTEXT_MENU            = 0x0010;
    private static final int CONTEXT_EXECUTION       = 0x0011;
    private static final int CONTEXT_ADD_FOLDER      = 0x0012;
    private static final int CONTEXT_COPY            = 0x0013;
    private static final int CONTEXT_MOVE            = 0x0014;
    private static final int CONTEXT_DELETE          = 0x0015;
    private static final int CONTEXT_RENAME          = 0x0016;
    private static final int CONTEXT_SHARE           = 0x0017;
    private static final int CONTEXT_DETAIL          = 0x0018;

    private static final int PROGRESS_COPY           = 0x0021;
    private static final int PROGRESS_MOVE           = 0x0022;
    private static final int PROGRESS_DELETE         = 0x0023;


    private static final int BACK_PRESS_CANCEL  = 0x0001;
    private static final int FILE_PROGRESS      = 0x0002;
    private static final int FILE_INIT          = 0x0003;
    private static final int FILE_COMPLETE      = 0x0004;
    private static final int FILE_ERROR         = 0x0005;

    private Context          mContext;
    private boolean          mPressedBack;

    private MenuItem         mOptionMenuTheme;
    private MenuItem         mOptionMenuFile;
    private MenuItem         mOptionMenuAddFolder;
    private MenuItem         mOptionMenuCopy;
    private MenuItem         mOptionMenuMove;
    private MenuItem         mOptionMenuDelete;
    private MenuItem         mOptionMenuSelectAllTrue;
    private MenuItem         mOptionMenuSelectAllFalse;

    private AdView           mAdView;
    private TextView         mPathView;
    private DualListFragment mDualListFragment;
    private ApiDialog         mDialog;

    private String mContextMenuPath;
    private String[] mFileActionList;

    private boolean mActivityState;
    private boolean mDialogDismiss;
    private String  mDialogDismissMessage;

    private ApiDrawableConfig mDrawableConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiLog.i("DualFileBrowserActivity : onCreate");
        DualListTheme.setTheme(getSharedPreferences(mSharedPreferencesKey, MODE_PRIVATE).getInt(mSharedPreferencesTheme, ApiTheme.THEME_WHITEBLUE));
        DualListTheme.initDip(this);
        this.getWindow().setBackgroundDrawableResource(DualListTheme.BackgoundId());
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_dualfilebrowser);

        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if(pi.versionName == null) {
            ApiLog.i("Version name = %s",pi.versionName);
        }

        mContext = this;
        mPressedBack = false;
        mActivityState = false;
        mDialogDismiss = false;

        mFileActionList = null;

        mDrawableConfig = new ApiDrawableConfig(mContext.getResources());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        float dip = DualListTheme.Dip();
        mPathView = (TextView) findViewById(R.id.kh_duallist_info);
        mPathView.setPadding((int)(6 * dip), (int)(4 * dip), (int)(6 * dip), (int)(4 * dip));
        mPathView.setBackgroundResource(DualListTheme.DualListPathBackgroundId());
        mPathView.setTextAppearance(this, DualListTheme.TextApperrancePathId());
        mPathView.setTextSize(DualListTheme.TextSize(16f));

        _initAdView();

        FragmentManager fm = getFragmentManager();
        mDualListFragment = (DualListFragment)fm.findFragmentById(R.id.kh_duallist_fragment);
        mDualListFragment.setPath(getSharedPreferences(mSharedPreferencesKey, MODE_PRIVATE).getString(mSharedPreferencesPath, Environment.getExternalStorageDirectory().getPath()), false);
        mDualListFragment.setRegisterForContextMenu(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }

        mActivityState = true;
        if(mDialogDismiss) {
            mDialogDismiss = false;
            if(mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
            if(mDialogDismissMessage != null) {
                Toast.makeText(this, mDialogDismissMessage, Toast.LENGTH_SHORT).show();
                mDialogDismissMessage = null;
            }
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }

        SharedPreferences.Editor editor = getSharedPreferences(mSharedPreferencesKey, MODE_PRIVATE).edit();
        editor.putString(mSharedPreferencesPath, mDualListFragment.getPath());
        editor.commit();
        mActivityState = false;

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }

        super.onDestroy();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mFileActionList = savedInstanceState.getStringArray("fileactionlist");

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mFileActionList != null)
            outState.putStringArray("fileactionlist", mFileActionList);
    }

    @Override
    public void onBackPressed() {
        if(!mDualListFragment.back()) {
            if(mPressedBack) {
                finish();
            } else {
                mPressedBack = true;
//                Toast.makeText(mContext, "'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext, R.string.Press_the_back_key_again_to_exit, Toast.LENGTH_SHORT).show();
                DialogHandler.sendEmptyMessageDelayed(BACK_PRESS_CANCEL, 1000);
            }
        }
    }

    private void _initAdView() {
      try {
        Class<?> idClass = Class.forName("zpdl.studio.dualfilebrowser.R$id");
        Field idField = idClass.getField("adView");

        mAdView = (AdView) findViewById(idField.getInt(idClass));
      } catch (ClassNotFoundException e) {
          mAdView = null;
      } catch (NoSuchFieldException e) {
          mAdView = null;
      } catch (IllegalAccessException e) {
          mAdView = null;
      } catch (IllegalArgumentException e) {
          mAdView = null;
      }

      if(mAdView == null) {
          ApiLog.i("_initAdView fail");
      } else {
//        AdRequest adRequest = new AdRequest.Builder()
//            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//            .addTestDevice("ca-app-pub-4203963132345012/2205004686")
//            .build();
          mAdView.setBackgroundResource(DualListTheme.BackgoundId());
          mAdView.setAdListener(new AdListener() {
              @Override
              public void onAdLoaded() {
                  mAdView.setVisibility(View.VISIBLE);
              }
          });
          mAdView.loadAd(new AdRequest.Builder().build());
      }
    }

    private static final int DRAWABLE_ACTIONBAR_FILE            = 0x0001;
    private static final int DRAWABLE_ACTIONBAR_ADD_FOLDER      = 0x0002;
    private static final int DRAWABLE_ACTIONBAR_COPY            = 0x0003;
    private static final int DRAWABLE_ACTIONBAR_MOVE            = 0x0004;
    private static final int DRAWABLE_ACTIONBAR_DELETE          = 0x0005;
    private static final int DRAWABLE_ACTIONBAR_SELECT_ALL      = 0x0006;
    private static final int DRAWABLE_ACTIONBAR_UNSELECT_ALL    = 0x0007;

    private Drawable FileBrowserDrawable(int id) {
        Resources res = mContext.getResources();

        switch(id) {
            case DRAWABLE_ACTIONBAR_FILE : {
                Bitmap enable  = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_file_enable);
                Bitmap press   = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_file_press);
                Bitmap disable = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_file_disable);

                return ApiDrawableFactory.getDrawable(res, enable, press, disable);
            }
            case DRAWABLE_ACTIONBAR_ADD_FOLDER : {
                Bitmap enable  = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_add_enable);
                Bitmap press   = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_add_press);
                Bitmap disable = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_add_disable);

                return ApiDrawableFactory.getDrawable(res, enable, press, disable);
            }
            case DRAWABLE_ACTIONBAR_COPY : {
                Bitmap enable  = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_copy_enable);
                Bitmap press   = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_copy_press);
                Bitmap disable = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_copy_disable);

                return ApiDrawableFactory.getDrawable(res, enable, press, disable);
            }
            case DRAWABLE_ACTIONBAR_MOVE : {
                Bitmap enable  = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_move_enable);
                Bitmap press   = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_move_press);
                Bitmap disable = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_move_disable);

                return ApiDrawableFactory.getDrawable(res, enable, press, disable);
            }
            case DRAWABLE_ACTIONBAR_DELETE : {
                Bitmap enable  = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_delete_enable);
                Bitmap press   = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_delete_press);
                Bitmap disable = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_delete_disable);

                return ApiDrawableFactory.getDrawable(res, enable, press, disable);
            }
            case DRAWABLE_ACTIONBAR_SELECT_ALL : {
                Bitmap enable  = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_select_all_true_enable);
                Bitmap press   = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_select_all_true_press);
                Bitmap disable = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_select_all_true_disabled);

                return ApiDrawableFactory.getDrawable(res, enable, press, disable);
            }
            case DRAWABLE_ACTIONBAR_UNSELECT_ALL : {
                Bitmap enable  = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_select_all_false_enable);
                Bitmap press   = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_select_all_false_press);
                Bitmap disable = ApiDrawableFactory.getBitmap(res, mDrawableConfig, R.drawable.kh_duallist_ic_actionbar_select_all_false_disabled);

                return ApiDrawableFactory.getDrawable(res, enable, press, disable);
            }
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionMenuTheme = menu.add(0, OPTION_THEME, OPTION_THEME, R.string.File);
        mOptionMenuTheme.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mOptionMenuTheme.setIcon(FileBrowserDrawable(DRAWABLE_ACTIONBAR_FILE));

        mOptionMenuFile = menu.add(0, OPTION_FILE, OPTION_FILE, R.string.File);
        mOptionMenuFile.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mOptionMenuFile.setIcon(FileBrowserDrawable(DRAWABLE_ACTIONBAR_FILE));

        mOptionMenuAddFolder = menu.add(0, OPTION_ADD_FOLDER, OPTION_ADD_FOLDER, R.string.Add_folder);
        mOptionMenuAddFolder.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mOptionMenuAddFolder.setIcon(FileBrowserDrawable(DRAWABLE_ACTIONBAR_ADD_FOLDER));

        mOptionMenuCopy = menu.add(0, OPTION_COPY, OPTION_COPY, R.string.Copy);
        mOptionMenuCopy.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mOptionMenuCopy.setIcon(FileBrowserDrawable(DRAWABLE_ACTIONBAR_COPY));

        mOptionMenuMove = menu.add(0, OPTION_MOVE, OPTION_MOVE, R.string.Move);
        mOptionMenuMove.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mOptionMenuMove.setIcon(FileBrowserDrawable(DRAWABLE_ACTIONBAR_MOVE));

        mOptionMenuDelete = menu.add(0, OPTION_DELETE, OPTION_DELETE, R.string.Delete);
        mOptionMenuDelete.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mOptionMenuDelete.setIcon(FileBrowserDrawable(DRAWABLE_ACTIONBAR_DELETE));

        mOptionMenuSelectAllTrue = menu.add(0, OPTION_SELECT_ALL_TRUE, OPTION_SELECT_ALL_TRUE, R.string.Select_all);
        mOptionMenuSelectAllTrue.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mOptionMenuSelectAllTrue.setIcon(FileBrowserDrawable(DRAWABLE_ACTIONBAR_SELECT_ALL));

        mOptionMenuSelectAllFalse = menu.add(0, OPTION_SELECT_ALL_FALSE, OPTION_SELECT_ALL_FALSE, R.string.Unselect_all);
        mOptionMenuSelectAllFalse.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mOptionMenuSelectAllFalse.setIcon(FileBrowserDrawable(DRAWABLE_ACTIONBAR_UNSELECT_ALL));

        _invalidateMenu();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                if(!mDualListFragment.back()) {
                    ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                            new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                            new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                        };
                    ApiDialogSimple.show(this,
                                        android.R.id.home,
                                        R.string.Exit,
                                        R.string.Do_you_want_exit,
                                        btnParam);
                }
            return true;

            case OPTION_THEME : {
                ApiLog.i("onOptionsItemSelected DualListTheme.nextTheme() = %d", DualListTheme.nextTheme());
                SharedPreferences.Editor editor = getSharedPreferences(mSharedPreferencesKey, MODE_PRIVATE).edit();
                editor.putInt(mSharedPreferencesTheme, DualListTheme.nextTheme());
                editor.commit();

                recreate();
            } return true;

            case OPTION_FILE : {
                mDualListFragment.modeSelect();
            }
            return true;

            case OPTION_ADD_FOLDER : {
                ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                        new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                        new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                    };
                ApiDialogFileEditor.show(this,
                                        OPTION_ADD_FOLDER,
                                        R.string.Add_folder,
                                        "",
                                        R.string.Enter_new_folder_name,
                                        btnParam);
            }
            return true;

            case OPTION_COPY : {
                mFileActionList = mDualListFragment.getFileSelectArray();

                ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                        new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                        new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                    };
                mDialog = DualListDialogBrowser.show(this, OPTION_COPY, R.string.Copy, mDualListFragment.getPath(), btnParam);
            }
            return true;

            case OPTION_MOVE : {
                mFileActionList = mDualListFragment.getFileSelectArray();

                ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                        new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                        new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                    };
                mDialog = DualListDialogBrowser.show(this, OPTION_MOVE, R.string.Move, mDualListFragment.getPath(), btnParam);
            }
            return true;

            case OPTION_DELETE : {
                mFileActionList = mDualListFragment.getFileSelectArray();
                _deleteDialog(OPTION_DELETE, mFileActionList);
            }
            return true;

            case OPTION_SELECT_ALL_TRUE : {
                mDualListFragment.selectAll(false);
                _invalidateMenu();
            }
            return true;

            case OPTION_SELECT_ALL_FALSE : {
                mDualListFragment.selectAll(true);
                _invalidateMenu();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        ListView lv = (ListView) v;
        AdapterView.AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
        DualListItem item = (DualListItem) lv.getItemAtPosition(acmi.position);

        mContextMenuPath = item.getPath();

        if(item.getType() == DualListItem.SDCARD || item.getType() == DualListItem.EXTSDCARD) {
            return;
        } else if(item.getType() == DualListItem.UP) {
            menu.add(0, CONTEXT_ADD_FOLDER, 0, R.string.Add_folder);
        } else if(item.getType() == DualListItem.FOLDER) {
            menu.add(0, CONTEXT_ADD_FOLDER, 0, R.string.Add_folder);
            menu.add(0, CONTEXT_COPY,       1, R.string.Copy);
            menu.add(0, CONTEXT_MOVE,       2, R.string.Move);
            menu.add(0, CONTEXT_DELETE,     3, R.string.Delete);
            menu.add(0, CONTEXT_RENAME,     4, R.string.Rename);
        } else { // File
            String minetype = ApiFile.getMimeType(mContextMenuPath);
            if(minetype != null) {
                menu.add(0, CONTEXT_EXECUTION,  0, R.string.Execution);
            }
            menu.add(0, CONTEXT_COPY,       1, R.string.Copy);
            menu.add(0, CONTEXT_MOVE,       2, R.string.Move);
            menu.add(0, CONTEXT_DELETE,     3, R.string.Delete);
            menu.add(0, CONTEXT_RENAME,     4, R.string.Rename);
            menu.add(0, CONTEXT_SHARE,      5, R.string.Share);
            menu.add(0, CONTEXT_DETAIL,     6, R.string.Detail);
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case CONTEXT_ADD_FOLDER : {
            ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                    new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                    new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                };
            ApiDialogFileEditor.show(this,
                                    CONTEXT_ADD_FOLDER,
                                    R.string.Add_folder,
                                    "",
                                    R.string.Enter_new_folder_name,
                                    btnParam);
        } break;
        case CONTEXT_EXECUTION : {
            _execution(mContextMenuPath);
        } break;

        case CONTEXT_COPY : {
            mFileActionList = new String[1];
            mFileActionList[0] = mContextMenuPath;

            ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                    new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                    new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                };
            mDialog = DualListDialogBrowser.show(this,
                                                    CONTEXT_COPY,
                                                    R.string.Copy,
                                                    (new File(mContextMenuPath)).getParent(),
                                                    btnParam);
        } break;

        case CONTEXT_MOVE : {
            mFileActionList = new String[1];
            mFileActionList[0] = mContextMenuPath;

            ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                    new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                    new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                };
            mDialog = DualListDialogBrowser.show(this,
                                                    CONTEXT_MOVE,
                                                    R.string.Move,
                                                    (new File(mContextMenuPath)).getParent(),
                                                    btnParam);
        } break;

        case CONTEXT_DELETE : {
            mFileActionList = new String[1];
            mFileActionList[0] = mContextMenuPath;
            _deleteDialog(CONTEXT_DELETE, mFileActionList);
        } break;

        case CONTEXT_RENAME : {
            mFileActionList = new String[1];
            mFileActionList[0] = mContextMenuPath;

            ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                    new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                    new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                };
            ApiDialogFileEditor.show(this,
                                    CONTEXT_RENAME,
                                    R.string.Rename,
                                    (new File(mContextMenuPath)).getName(),
                                    R.string.Enter_the_name,
                                    btnParam);
        } break;

        case CONTEXT_SHARE : {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mContextMenuPath)));
            shareIntent.setType(ApiFile.getMimeType(mContextMenuPath));
            startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.Share)));

//            ArrayList<Uri> imageUris = new ArrayList<Uri>();
//            imageUris.add(Uri.fromFile(new File(mContextMenuPath))); // Add your image URIs here
//
//            Intent shareIntent = new Intent();
//            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
//            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
//            shareIntent.setType(ApiFile.getMimeType(mContextMenuPath));
//            startActivity(Intent.createChooser(shareIntent, "Share images to.."));

        } break;

        case CONTEXT_DETAIL : {
            ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                    new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Ok))
                };
            DualListDialogDetail.show(this, CONTEXT_DETAIL, R.string.Move, mContextMenuPath, btnParam);
        } break;
        }
        return super.onContextItemSelected(item);
    }
    @Override
    public void onFolderChange(String path) {
        ApiLog.i("FileBrowserActivity : onFolderChange %s", path);
        if(mPathView != null)
            mPathView.setText(mDualListFragment.getPath());
    }

    @Override
    public void onFileClick(String path) {
        _execution(path);
    }

    @Override
    public boolean onLongClick(DualListItem item) {
        mContextMenuPath = item.getPath();

        int[] itemId = null;
        String[] itemSubject = null;

        if(item.getType() == DualListItem.SDCARD || item.getType() == DualListItem.EXTSDCARD) {
            return false;
        } else if(item.getType() == DualListItem.UP) {
            itemId = new int[] { CONTEXT_ADD_FOLDER };
            itemSubject = new String[] { this.getResources().getString(R.string.Add_folder) };
        } else if(item.getType() == DualListItem.FOLDER) {
            Resources res = this.getResources();
            itemId = new int[] { CONTEXT_ADD_FOLDER,
                                 CONTEXT_COPY,
                                 CONTEXT_MOVE,
                                 CONTEXT_DELETE,
                                 CONTEXT_RENAME,
                                 CONTEXT_DETAIL };
            itemSubject = new String[] { res.getString(R.string.Add_folder),
                                         res.getString(R.string.Copy),
                                         res.getString(R.string.Move),
                                         res.getString(R.string.Delete),
                                         res.getString(R.string.Rename),
                                         res.getString(R.string.Detail)};
        } else if(item.getType() == DualListItem.SUPPORT_NOT) {
            Resources res = this.getResources();
            itemId = new int[] { CONTEXT_COPY,
                                 CONTEXT_MOVE,
                                 CONTEXT_DELETE,
                                 CONTEXT_RENAME,
                                 CONTEXT_SHARE,
                                 CONTEXT_DETAIL};
            itemSubject = new String[] { res.getString(R.string.Copy),
                                         res.getString(R.string.Move),
                                         res.getString(R.string.Delete),
                                         res.getString(R.string.Rename),
                                         res.getString(R.string.Share),
                                         res.getString(R.string.Detail) };
        } else {
            Resources res = this.getResources();
            itemId = new int[] { CONTEXT_EXECUTION,
                                 CONTEXT_COPY,
                                 CONTEXT_MOVE,
                                 CONTEXT_DELETE,
                                 CONTEXT_RENAME,
                                 CONTEXT_SHARE,
                                 CONTEXT_DETAIL};
            itemSubject = new String[] { res.getString(R.string.Execution),
                                         res.getString(R.string.Copy),
                                         res.getString(R.string.Move),
                                         res.getString(R.string.Delete),
                                         res.getString(R.string.Rename),
                                         res.getString(R.string.Share),
                                         res.getString(R.string.Detail) };
        }

        mDialog = ApiDialogContextMenu.show(this.getFragmentManager(),
                                            CONTEXT_MENU,
                                            itemId,
                                            itemSubject);
        return true;
    }

    @Override
    public void onUpdateMenu() {
//        _invalidateMenu();
    }

    private void _execution(String path) {
        String minetype = ApiFile.getMimeType(path);
        ApiLog.i("FileBrowserActivity : _execution = %s mime type = %s", path, minetype);

        if(minetype != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndTypeAndNormalize(Uri.fromFile(new File(path)), minetype);
            if(_isIntentAvailable(mContext, intent)) {
            startActivity(intent);
            } else {
                Toast.makeText(mContext, "Not Execution", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public static boolean _isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static boolean _isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void _invalidateMenu() {
        if(mDualListFragment.isSelectMode()) {
            mOptionMenuTheme.setVisible(false);
            mOptionMenuFile.setVisible(false);
            mOptionMenuAddFolder.setVisible(true);
            mOptionMenuCopy.setVisible(true);
            mOptionMenuMove.setVisible(true);
            mOptionMenuDelete.setVisible(true);
            if(mDualListFragment.getSelectedCount() > 0) {
                mOptionMenuCopy.setEnabled(true);
                mOptionMenuMove.setEnabled(true);
                mOptionMenuDelete.setEnabled(true);
            } else {
                mOptionMenuCopy.setEnabled(false);
                mOptionMenuMove.setEnabled(false);
                mOptionMenuDelete.setEnabled(false);
            }
            if(mDualListFragment.isSelectPossible()) {
                if(mDualListFragment.isSelectAll()) {
                    mOptionMenuSelectAllTrue.setVisible(true);
                    mOptionMenuSelectAllTrue.setEnabled(true);
                    mOptionMenuSelectAllFalse.setVisible(false);
                } else {
                    mOptionMenuSelectAllTrue.setVisible(false);
                    mOptionMenuSelectAllFalse.setVisible(true);
                    mOptionMenuSelectAllFalse.setEnabled(true);
                }
            } else {
                mOptionMenuSelectAllTrue.setVisible(false);
                mOptionMenuSelectAllFalse.setVisible(true);
                mOptionMenuSelectAllFalse.setEnabled(false);
            }
        } else {
            mOptionMenuTheme.setVisible(true);
            mOptionMenuFile.setVisible(true);
            mOptionMenuAddFolder.setVisible(false);
            mOptionMenuCopy.setVisible(false);
            mOptionMenuMove.setVisible(false);
            mOptionMenuDelete.setVisible(false);
            mOptionMenuSelectAllTrue.setVisible(false);
            mOptionMenuSelectAllFalse.setVisible(false);
        }
    }

    private Handler DialogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case BACK_PRESS_CANCEL : {
                    mPressedBack = false;
                } break;
                case FILE_PROGRESS : {
                    if(mDialog != null) {
                        ApiDialogProgress dialog = (ApiDialogProgress) mDialog;
                        dialog.setCount(msg.arg1);
                        dialog.setProgress(msg.arg2);
                        dialog.setMessage((String) msg.obj);
                    }
                } break;
                case FILE_INIT : {
                    if(mDialog != null) {
                        ApiDialogProgress dialog = (ApiDialogProgress) mDialog;
                        dialog.setMaxCount(msg.arg1);
                        dialog.setMessage((String) msg.obj);
                    }
                } break;
                case FILE_COMPLETE : {
                    if(mDialog != null) {
                        int id = ((ApiDialogProgress) mDialog).getDialogId();
                        int MaxCnt = ((ApiDialogProgress) mDialog).getMaxCount();
                        if(id == PROGRESS_COPY) {
                            if(MaxCnt == 1) {
                                mDialogDismissMessage = getResources().getString(R.string.files_copied);
                            } else {
                                mDialogDismissMessage = String.format(getResources().getString(R.string.number_files_copied), MaxCnt);
                            }
                        } else if(id == PROGRESS_MOVE) {
                            if(MaxCnt == 1) {
                                mDialogDismissMessage = getResources().getString(R.string.files_moved);
                            } else {
                                mDialogDismissMessage = String.format(getResources().getString(R.string.number_files_moved), MaxCnt);
                            }
                        } else if(id == PROGRESS_DELETE) {
                            if(MaxCnt == 1) {
                                mDialogDismissMessage = getResources().getString(R.string.files_deleted);
                            } else {
                                mDialogDismissMessage = String.format(getResources().getString(R.string.number_files_deleted), MaxCnt);
                            }
                        } else {
                            mDialogDismissMessage = null;
                        }

                        if(mActivityState) {
                            mDialog.dismiss();
                            mDialog = null;
                            if(mDialogDismissMessage != null) {
                                Toast.makeText(mContext, mDialogDismissMessage, Toast.LENGTH_SHORT).show();
                                mDialogDismissMessage = null;
                            }
                        } else {
                            mDialogDismiss = true;
                        }
                    }

                    if(msg.obj == null) {
                        mDualListFragment.setPath(mDualListFragment.getPath(), true);
                    } else {
                        mDualListFragment.setPath((String) msg.obj, true);
                    }
                    mDualListFragment.modeBase();
                } break;
                case FILE_ERROR : {
                    if(mDialog != null) {
                        mDialogDismissMessage = (String)msg.obj;
                        if(mActivityState) {
                            mDialog.dismiss();
                            mDialog = null;
                            if(mDialogDismissMessage != null) {
                                Toast.makeText(mContext, mDialogDismissMessage, Toast.LENGTH_SHORT).show();
                                mDialogDismissMessage = null;
                            }
                        } else {
                            mDialogDismiss = true;
                        }
                    }
                } break;
            }
        }
    };

    private void _deleteDialog(int id, String[] deleteList) {
        long cnt = ApiFile.getFileCount(deleteList);
        String msg = null;
        if(cnt == 1) {
            msg = getResources().getString(R.string.Do_you_want_to_delete);
        } else {
            msg = String.format(getResources().getString(R.string.Do_you_want_to_delete_number_files), cnt);
        }

        ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
            };
        ApiDialogSimple.show(getFragmentManager(),
                            id,
                            getResources().getString(R.string.Delete),
                            msg,
                            btnParam);
    }

    private ApiFile.onFileListener fileListener = new ApiFile.onFileListener() {
        @Override
        public void onFileProgress(int count, int progress, String name) {
            DialogHandler.removeMessages(FILE_PROGRESS);

            Message msg = DialogHandler.obtainMessage();
            msg.what = FILE_PROGRESS;
            msg.arg1 = count;
            msg.arg2 = progress;
            msg.obj = name;
            DialogHandler.sendMessage(msg);
        }

        @Override
        public void onFileInit(int count, String name) {
            Message msg = DialogHandler.obtainMessage();
            msg.what = FILE_INIT;
            msg.arg1 = count;
            msg.obj = name;
            DialogHandler.sendMessage(msg);
        }

        @Override
        public void onFileComplete(boolean err, String result) {
            if(err) {
                Message msg = DialogHandler.obtainMessage();
                msg.what = FILE_ERROR;
                msg.obj = result;
                DialogHandler.sendMessage(msg);
            } else {
                Message msg = DialogHandler.obtainMessage();
                msg.what = FILE_COMPLETE;
                msg.obj = result;
                DialogHandler.sendMessageDelayed(msg, 100);
            }
        }
    };

    @Override
    public void onDialogLoaded(ApiDialog dialog) {
        if(mDialog == null) {
            if(dialog.getDialogId() == PROGRESS_COPY) {
                if(ApiFile.getMode() == ApiFile.COPY) {
                    mDialog = dialog;
                    ApiFile.setOnFileListener(fileListener);
                } else {
                    dialog.dismiss();
                }
            } else if(dialog.getDialogId() == PROGRESS_MOVE) {
                if(ApiFile.getMode() == ApiFile.MOVE) {
                    mDialog = dialog;
                    ApiFile.setOnFileListener(fileListener);
                } else {
                    dialog.dismiss();
                }
            } else if(dialog.getDialogId() == PROGRESS_DELETE) {
                if(ApiFile.getMode() == ApiFile.DELETE) {
                    mDialog = dialog;
                    ApiFile.setOnFileListener(fileListener);
                } else {
                    dialog.dismiss();
                }
            } else {
                mDialog = dialog;
            }
        }
    }

    @Override
    public void onDialogUnLoaded(ApiDialog dialog) {
        mDialog = null;
        if(dialog.getId() == PROGRESS_COPY || dialog.getId() == PROGRESS_MOVE || dialog.getId() == PROGRESS_DELETE) {
            ApiFile.setOnFileListener(null);
        }
    }

    private static final int DIALOG_BTN_ID_POSITIVE = 0x0001;
    private static final int DIALOG_BTN_ID_CANCEL   = 0x0002;
    @Override
    public void onButtonClick(ApiDialog dialog, int id) {
        switch(id) {
            case DIALOG_BTN_ID_POSITIVE : {
                _onButtonClickPositive(dialog);
            } break;
            case DIALOG_BTN_ID_CANCEL : {
                _onButtonClickCancel(dialog);
            } break;
            case CONTEXT_ADD_FOLDER : {
                ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                        new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                        new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                    };
                ApiDialogFileEditor.show(this,
                                        CONTEXT_ADD_FOLDER,
                                        R.string.Add_folder,
                                        "",
                                        R.string.Enter_new_folder_name,
                                        btnParam);
            } break;
            case CONTEXT_EXECUTION : {
                _execution(mContextMenuPath);
            } break;

            case CONTEXT_COPY : {
                mFileActionList = new String[1];
                mFileActionList[0] = mContextMenuPath;

                ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                        new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                        new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                    };
                mDialog = DualListDialogBrowser.show(this,
                                                        CONTEXT_COPY,
                                                        R.string.Copy,
                                                        (new File(mContextMenuPath)).getParent(),
                                                        btnParam);
            } break;

            case CONTEXT_MOVE : {
                mFileActionList = new String[1];
                mFileActionList[0] = mContextMenuPath;

                ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                        new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                        new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                    };
                mDialog = DualListDialogBrowser.show(this,
                                                        CONTEXT_MOVE,
                                                        R.string.Move,
                                                        (new File(mContextMenuPath)).getParent(),
                                                        btnParam);
            } break;

            case CONTEXT_DELETE : {
                mFileActionList = new String[1];
                mFileActionList[0] = mContextMenuPath;
                _deleteDialog(CONTEXT_DELETE, mFileActionList);
            } break;

            case CONTEXT_RENAME : {
                mFileActionList = new String[1];
                mFileActionList[0] = mContextMenuPath;

                ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                        new ApiDialogButtonParam(DIALOG_BTN_ID_POSITIVE, mContext.getResources().getString(R.string.Yes)),
                        new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                    };
                ApiDialogFileEditor.show(this,
                                        CONTEXT_RENAME,
                                        R.string.Rename,
                                        (new File(mContextMenuPath)).getName(),
                                        R.string.Enter_the_name,
                                        btnParam);
            } break;

            case CONTEXT_SHARE : {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mContextMenuPath)));
                shareIntent.setType(ApiFile.getMimeType(mContextMenuPath));
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.Share)));

//                ArrayList<Uri> imageUris = new ArrayList<Uri>();
//                imageUris.add(Uri.fromFile(new File(mContextMenuPath))); // Add your image URIs here
    //
//                Intent shareIntent = new Intent();
//                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
//                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
//                shareIntent.setType(ApiFile.getMimeType(mContextMenuPath));
//                startActivity(Intent.createChooser(shareIntent, "Share images to.."));

            } break;

            case CONTEXT_DETAIL : {
                ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                        new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Ok))
                    };
                DualListDialogDetail.show(this, CONTEXT_DETAIL, R.string.Move, mContextMenuPath, btnParam);
            } break;
        }
    }

    private void _onButtonClickPositive(ApiDialog dialog) {
        switch(dialog.getDialogId()) {
            case android.R.id.home : {
                finish();
            } break;

            case OPTION_ADD_FOLDER :
            case CONTEXT_ADD_FOLDER : {
                try {
                    String name = ((ApiDialogFileEditor)mDialog).getEditText().equals("") ? "new" : ((ApiDialogFileEditor)mDialog).getEditText();
                    String parent = mDualListFragment.getPath();
                    mDualListFragment.addFolder(ApiFile.createfolder(mContext, parent + File.separator + name));
                } catch (IOException e) {
                    Toast.makeText(this, R.string.Failed_add_folder, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } finally {
                    mDialog.dismiss();
                }
            } break;

            case CONTEXT_RENAME : {
                try {
                    String name = ((ApiDialogFileEditor)mDialog).getEditText().equals("") ? "rename" : ((ApiDialogFileEditor)mDialog).getEditText();
                    String parent = (new File(mFileActionList[0])).getParent();
                    mDualListFragment.rename(mFileActionList[0], ApiFile.rename(mContext, mFileActionList[0], parent + File.separator + name));
                } catch (IOException e) {
                    Toast.makeText(this, R.string.Failed_rename, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } finally {
                    mDialog.dismiss();
                }
            }break;

            case OPTION_COPY :
            case CONTEXT_COPY : {
                if(mDialog instanceof DualListDialogBrowser) {
                    String out = ((DualListDialogBrowser)mDialog).getPath();
                    mDialog.dismiss();
                    ApiFile.copy(mContext, mFileActionList, out);

                    ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                            new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                        };
                    mDialog = ApiDialogProgress.show(this, PROGRESS_COPY, R.string.Copy, btnParam);
                    ApiFile.setOnFileListener(fileListener);
                }
            } break;

            case OPTION_MOVE :
            case CONTEXT_MOVE : {
                if(mDialog instanceof DualListDialogBrowser) {
                    String out = ((DualListDialogBrowser)mDialog).getPath();
                    mDialog.dismiss();
                    ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                            new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                        };
                    mDialog = ApiDialogProgress.show(this, PROGRESS_MOVE, R.string.Move, btnParam);
                    ApiFile.move(mContext, mFileActionList, out);
                    ApiFile.setOnFileListener(fileListener);
                }
            } break;

            case OPTION_DELETE :
            case CONTEXT_DELETE : {
                if(mDialog != null) {
                    mDialog.dismiss();
                }
                ApiDialogButtonParam[] btnParam = new ApiDialogButtonParam[] {
                        new ApiDialogButtonParam(DIALOG_BTN_ID_CANCEL, mContext.getResources().getString(R.string.Cancel))
                    };
                mDialog = ApiDialogProgress.show(this, PROGRESS_DELETE, R.string.Delete, btnParam);
                ApiFile.delete(mContext, mFileActionList);
                ApiFile.setOnFileListener(fileListener);
            } break;
        }
    }

    private void _onButtonClickCancel(ApiDialog dialog) {
        dialog.dismiss();
    }
}
