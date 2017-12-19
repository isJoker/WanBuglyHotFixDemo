package com.jokerwan.wanhotfixdemo;

/**
 * Created by JokerWan on 2017/12/18.
 * WeChat: wjc398556712
 * Function:
 */

public class LoadBugClass {
    /**
     * 获取bug字符串.
     *
     * @return 返回bug字符串
     */
    public static String getBugString() {
        BugClass bugClass = new BugClass();
        return bugClass.bug();
    }
}
