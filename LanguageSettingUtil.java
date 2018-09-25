package com.jimu.jm300.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageSettingUtil {
    //单例模式-
    private static LanguageSettingUtil instance;
    private Context context;
    //存储当前系统的language设置-
    private String defaultLanguage;
    //存储当前系统Locale-
    private Locale defaultLocale;

    private LanguageSettingUtil(Context paramContext) {
        //得到系统语言-
        Locale localLocale = Locale.getDefault();
        this.defaultLocale = localLocale;

        //保存系统语言到defaultLanguage
        String str = this.defaultLocale.getLanguage();
        this.defaultLanguage = str;

        this.context = paramContext;
    }

    //检验自身是否被创建-
    public static LanguageSettingUtil get() {
        if (instance == null)
            throw new IllegalStateException(
                    "language setting not initialized yet");
        return instance;
    }

    //初始化-
    public static void init(Context paramContext) {
        if (instance == null) {
            instance = new LanguageSettingUtil(paramContext);
        }
    }

    // 创建Configuration-
    private Configuration getUpdatedLocaleConfig(String paramString) {

        Configuration localConfiguration = context.getResources()
                .getConfiguration();
        Locale localLocale = getLocale(paramString);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            localConfiguration.setLocale(localLocale);
        }else {
            localConfiguration.locale = localLocale;
        }
        return localConfiguration;
    }

    //得到APP配置文件目前的语言设置-
    public String getLanguage() {
        SharedPreferences localSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this.context);
        //如果当前程序没有设置language属性就返回系统语言，如果有，就返回以前的-
        return localSharedPreferences.getString("language",
                this.defaultLanguage);
    }

    //如果配置文件中没有语言设置-
    public Locale getLocale() {
        String str = getLanguage();
        return getLocale(str);
    }

    //创建新Locale并覆盖原Locale-
    public Locale getLocale(String paramString) {
        Locale localLocale = new Locale(paramString);
        Locale.setDefault(localLocale);
        return localLocale;
    }

    //刷新显示配置-
    public void refreshLanguage() {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getSupportLanguage(getLanguage());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // apply locale
            configuration.setLocale(locale);
        } else {
            // updateConfiguration
            configuration.locale = locale;
            DisplayMetrics dm = resources.getDisplayMetrics();
            resources.updateConfiguration(configuration, dm);
        }
    }

    //设置系统语言-
    public void saveLanguage(String paramString) {
        PreferenceManager.getDefaultSharedPreferences(this.context).edit()
                .putString("language", paramString).commit();
    }

    //保存系统的语言设置到SharedPreferences-
    public void saveSystemLanguage() {
        PreferenceManager.getDefaultSharedPreferences(this.context).edit()
                .putString("PreSysLanguage", this.defaultLanguage).commit();
    }

    public void checkSysChanged(String cuerSysLanguage) {
        //如果系统语言设置发生变化-
        if (!cuerSysLanguage.equals(PreferenceManager
                .getDefaultSharedPreferences(this.context).getString(
                        "PreSysLanguage", "zh"))) {
            //如果系统保存了this对象，就在这里修改defaultLanguage的值为当前系统语言cuerSysLanguage
            this.defaultLanguage = cuerSysLanguage;
            saveLanguage(cuerSysLanguage);
            saveSystemLanguage();
        }
    }


    private Context updateResources(Context context) {
        Resources resources = context.getResources();
        Locale locale = getLocale();// getSetLocale方法是获取新设置的语言

        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }


    public static void applyLanguage(Context context, String newLanguage) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getSupportLanguage(newLanguage);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // apply locale
            configuration.setLocale(locale);

        } else {
            // updateConfiguration
            configuration.locale = locale;
            DisplayMetrics dm = resources.getDisplayMetrics();
            resources.updateConfiguration(configuration, dm);
        }
    }

    public static Context attachBaseContext(Context context, String language) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return createConfigurationResources(context, language);
        } else {
            applyLanguage(context, language);
            return context;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context createConfigurationResources(Context context, String language) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale;
        if (TextUtils.isEmpty(language)) {//如果没有指定语言使用系统首选语言
            locale = getSystemPreferredLanguage();
        } else {//指定了语言使用指定语言，没有则使用首选语言
            locale = getSupportLanguage(language);
        }
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    // 简体中文
    public static final String SIMPLIFIED_CHINESE = "zh";
    // 英文
    public static final String ENGLISH = "en";
    //
    public static final String KOREA = "ko";


    private static Map<String, Locale> mSupportLanguages = new HashMap<String, Locale>(7) {{
        put(ENGLISH, Locale.ENGLISH);
        put(SIMPLIFIED_CHINESE, Locale.SIMPLIFIED_CHINESE);
        put(KOREA, Locale.KOREA);
    }};

    /**
     * 是否支持此语言
     *
     * @param language language
     * @return true:支持 false:不支持
     */
    public static boolean isSupportLanguage(String language) {
        return mSupportLanguages.containsKey(language);
    }

    /**
     * 获取支持语言
     *
     * @param language language
     * @return 支持返回支持语言，不支持返回系统首选语言
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getSupportLanguage(String language) {
        if (isSupportLanguage(language)) {
            return mSupportLanguages.get(language);
        }
        return getSystemPreferredLanguage();
    }

    /**
     * 获取系统首选语言
     *
     * @return Locale
     */
    public static Locale getSystemPreferredLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        return locale;
    }


}
