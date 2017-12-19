package com.jokerwan.wanhotfixdemo;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * Created by JokerWan on 2017/12/18.
 * WeChat: wjc398556712
 * Function:
 */

public class MyApplication extends TinkerApplication {

    public MyApplication() {
        super(ShareConstants.TINKER_ENABLE_ALL, "com.jokerwan.wanhotfixdemo.MyApplicationLike",
                "com.tencent.tinker.loader.TinkerLoader", false);
    }
}
