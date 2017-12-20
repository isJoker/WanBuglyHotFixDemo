# WanBuglyHotFixDemo
Bugly热更新 demo




----------


对于广大移动开发者而言，App的版本更新升级是再寻常不过的事。但是当你发现你刚发出去的包有紧急Bug需要修复时，你就不淡定了，又要经过繁琐的传统的App版本更新流程，重新发布一个修复Bug的版本，再将Apk上传到各大应用商店，用户需要花费时间去应用商店重新下载安装。如果Bug比较严重，有些用户可能会失去耐心，直接卸载掉App，于是乎，你们的用户就这样流失了。


----------


传统的更新流程有几个弊端，一是重新发布版本费时费力代价高，二是用户安装需要去应用商店下载成本高，三是不能及时的修复bug，用户体验差。但是H5的出现使使这种情况有了小小的转机，把需要经常变更的业务逻辑以H5的方式独立出来，再嵌入App中。为什么说是小小的转机，因为App中仍有原生的代码，你不能保证原生的代码不会出Bug。于是乎，热更新就应运而生。


----------


	
目前国内的热更新技术有很多，比如阿里的AndFix、Sophix；微信的Tinker；QQ空间的超级补丁；饿了吗的Amigo；美团的Robust等，各有优缺点。实现代码修复主要有两大方案：阿里系的底层替换和腾讯系的类加载。底层替换限制比较多，但能立即加载补丁包实现热修复。类加载需要冷启动才能使热修复生效，但限制少，修复的范围广。有兴趣的同学都可以去了解下。推荐一本阿里的工程师出的热修复书籍《深入探索Android热修复技术原理》，内容详细的讲解了热修复原理。今天我主要教大家如何将腾讯的基于Tinker的热修复框架Bugly集成到项目中，接下来开始讲集成的步骤。


----------


第一步：添加插件依赖
工程根目录下“build.gradle”文件中添加：

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        // tinkersupport插件, 其中latest.release指拉取最新版本，也可以指定明确版本号，例如1.0.4
        classpath "com.tencent.bugly:tinker-support: latest.release"
    }
}
```


----------


第二步：集成SDK
gradle配置
在app module的“build.gradle”文件中添加（示例配置）：

```
android {
        defaultConfig {
        }
      }
      dependencies {
          compile "com.android.support:multidex:1.0.1" // 多dex配置
          //注释掉原有bugly的仓库
          //compile 'com.tencent.bugly:crashreport:latest.release'
          //其中latest.release指代最新版本号，也可以指定明确的版本号，例如2.3.2
          compile 'com.tencent.bugly:crashreport_upgrade:1.3.4'
      }
```

 
在app module的“build.gradle”文件中添加：

```
// 依赖插件脚本
apply from: 'tinker-support.gradle'
```


tinker-support.gradle内容如下所示（示例配置）：
注：您需要在同级目录下创建tinker-support.gradle这个文件哦。

```
apply plugin: 'com.tencent.bugly.tinker-support'

def bakPath = file("${buildDir}/bakApk/")

/**
 * 此处填写每次构建生成的基准包目录
 */
def baseApkDir = "app-0208-15-10-00"

/**
 * 对于插件各参数的详细解析请参考
 */
tinkerSupport {

    // 开启tinker-support插件，默认值true
    enable = true

    // 指定归档目录，默认值当前module的子目录tinker
    autoBackupApkDir = "${bakPath}"

    // 是否启用覆盖tinkerPatch配置功能，默认值false
    // 开启后tinkerPatch配置不生效，即无需添加tinkerPatch
    overrideTinkerPatchConfiguration = true

    // 编译补丁包时，必需指定基线版本的apk，默认值为空
    // 如果为空，则表示不是进行补丁包的编译
    // @{link tinkerPatch.oldApk }
    baseApk = "${bakPath}/${baseApkDir}/app-release.apk"

    // 对应tinker插件applyMapping
    baseApkProguardMapping = "${bakPath}/${baseApkDir}/app-release-mapping.txt"

    // 对应tinker插件applyResourceMapping
    baseApkResourceMapping = "${bakPath}/${baseApkDir}/app-release-R.txt"

    // 构建基准包和补丁包都要指定不同的tinkerId，并且必须保证唯一性
    tinkerId = "1.0.0-base"

    // 构建多渠道补丁时使用
    // buildAllFlavorsDir = "${bakPath}/${baseApkDir}"
    // 是否启用加固模式，默认为false.(tinker-spport 1.0.7起支持）
    // isProtectedApp = true

    // 是否开启反射Application模式
enableProxyApplication = false
supportHotpugComponent = true
}

/**
 * 一般来说,我们无需对下面的参数做任何的修改
 * 对于各参数的详细介绍请参考:
 * https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97
 */
tinkerPatch {
    //oldApk ="${bakPath}/${appName}/app-release.apk"
    ignoreWarning = false
    useSign = true
    dex {
        dexMode = "jar"
        pattern = ["classes*.dex"]
        loader = []
    }
    lib {
        pattern = ["lib/*/*.so"]
    }

    res {
        pattern = ["res/*", "r/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]
        ignoreChange = []
        largeModSize = 100
    }

    packageConfig {
    }
    sevenZip {
        zipArtifact = "com.tencent.mm:SevenZip:1.1.10"
//        path = "/usr/local/bin/7za"
    }
    buildConfig {
        keepDexApply = false
        //tinkerId = "1.0.1-base"
        //applyMapping = "${bakPath}/${appName}/app-release-mapping.txt" //  可选，设置mapping文件，建议保持旧apk的proguard混淆方式
        //applyResourceMapping = "${bakPath}/${appName}/app-release-R.txt" // 可选，设置R.txt文件，通过旧apk文件保持ResId的分配
    }
}
```


----------


第三步：初始化SDK
enableProxyApplication = false 的情况
这是Tinker推荐的接入方式，一定程度上会增加接入成本，但具有更好的兼容性。
集成Bugly升级SDK之后，我们需要按照以下方式自定义ApplicationLike来实现Application的代码（以下是示例）：
自定义Application

```
public class MyApplication extends TinkerApplication {
    public MyApplication() {
        super(ShareConstants.TINKER_ENABLE_ALL, "xxx.xxx.MyApplicationLike",
                "com.tencent.tinker.loader.TinkerLoader", false);
    }
}
```

别忘了将MyApplication 加入到AndroidMenifest中的application标签下的name属性中。

自定义ApplicationLike

```
public class MyApplicationLike extends DefaultApplicationLike {
    public MyApplicationLike(Application application, int tinkerFlags,
            boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime,
            long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        // 调试时，将第三个参数改为true
        Bugly.init(getApplication(), "fb2bada5b4", true);
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        // 安装tinker
        // TinkerManager.installTinker(this); 替换成下面Bugly提供的方法
        Beta.installTinker(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallback(Application.ActivityLifecycleCallbacks callbacks) {
        getApplication().registerActivityLifecycleCallbacks(callbacks);
    }

}
```


enableProxyApplication = true 时直接在你的Application的onCreate()方法中调用
Bugly.init(getApplication(), "fb2bada5b4", true);


----------


第四步：AndroidManifest.xml配置
在AndroidMainfest.xml中进行以下配置：
权限配置

```
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_LOGS" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```


----------


第五步：混淆配置
为了避免混淆SDK，在Proguard混淆文件中增加以下配置：

```
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
```

如果你使用了support-v4包，你还需要配置以下混淆规则：

```
 -keep class android.support.**{*;}
```

到此为止，已经成功的集成了Bugly框架


----------


接下来测试热修复


----------


1、编译基准包
配置基准包的tinkerId

![这里写图片描述](http://img.blog.csdn.net/20171220102925302?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

执行assembleRelease编译生成基准包：

![这里写图片描述](http://img.blog.csdn.net/20171220103011396?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


这个会在build/bakApk路径下生成每次编译的基准包、混淆配置文件、资源Id文件，如下图所示：

![这里写图片描述](http://img.blog.csdn.net/20171220103134493?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


启动上一步生成的app-release.apk，启动后，SDK会自动上报联网数据
我们每次冷启动都会请求补丁策略，会上报当前版本号和tinkerId，这样我们后台就能将这个唯一的tinkerId对应到一个版本。


----------


2、对基线版本的bug修复
未修复前

![这里写图片描述](http://img.blog.csdn.net/20171220103222625?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

修复后

![这里写图片描述](http://img.blog.csdn.net/20171220103311406?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

----------


3、根据基线版本生成补丁包
修改待修复apk路径、mapping文件路径、resId文件路径和tinkerId

![这里写图片描述](http://img.blog.csdn.net/20171220103432927?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

执行构建补丁包的task

![这里写图片描述](http://img.blog.csdn.net/20171220103840758?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

生成的补丁包在build/outputs/patch目录下：

![这里写图片描述](http://img.blog.csdn.net/20171220103901422?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

----------


4、上传补丁包到平台
上传补丁包到平台并下发编辑规则

![这里写图片描述](http://img.blog.csdn.net/20171220103919737?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

![这里写图片描述](http://img.blog.csdn.net/20171220104021882?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

选择文件后会自动识别目标版本，若出现“未匹配到可用补丁的App版本”，如果你的基线版本没有上报过联网，基于这个版本生成的补丁包就无法匹配到，启动之前生成的app-release.apk，启动后，SDK会自动上报联网数据


----------


5、测试补丁应用效果
启动基线版本App,点击显示文本时崩溃，只是我们前面造的一个空指针异常，重新启动基线版本App，等待一两分钟后，会开始自动下载补丁包，下载成功之后会立即合成补丁，我配置了Beta.canNotifyUserRestart = true ,所以会弹出对话框，提醒用户更新应用。由于Tinker需要再次冷启动才能使补丁生效，点击重启应用后会退出，再重新启动apk,点击显示文本，文本内容显示为修复后的内容，之前的的空指针异常就被成功修复了。

![这里写图片描述](http://img.blog.csdn.net/20171220104040143?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

![这里写图片描述](http://img.blog.csdn.net/20171220104111947?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

![这里写图片描述](http://img.blog.csdn.net/20171220104127714?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaXNKb2tlcg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

----------


Bugly还支持全量升级、异常上报、运行统计，详情可以看Bugly官方文档：https://bugly.qq.com/docs/

本文demo: https://github.com/isJoker/WanBuglyHotFixDemo


       

