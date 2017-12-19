package com.jokerwan.wanhotfixdemo;

/**
 * Created by JokerWan on 2017/12/18.
 * WeChat: wjc398556712
 * Function:
 */

public class BugClass {

    public String bug() {
        // 这段代码会报空指针异常
//        String str = null;
//        Log.e("BugClass", "get string length:" + str.length());
//        return "This is a bug class";

        return "This is a fixed bug class";
    }
}
