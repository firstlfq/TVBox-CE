package com.github.tvbox.osc.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.text.TextUtils

import com.github.tvbox.osc.R
import com.github.tvbox.osc.api.ApiConfig
import com.github.tvbox.osc.base.App
import com.github.tvbox.osc.bean.MovieSort
import com.github.tvbox.osc.bean.SourceBean
import com.github.tvbox.osc.server.ControlManager
import com.github.tvbox.osc.ui.activity.HomeActivity
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.hjq.permissions.Permission

import java.io.File
import java.util.regex.Pattern

object DefaultConfig {

    @JvmStatic
    fun adjustSort(sourceKey: String?, list: List<MovieSort.SortData>, withMy: Boolean): List<MovieSort.SortData> {
        val data = mutableListOf<MovieSort.SortData>()
        if (sourceKey != null) {
            val sb: SourceBean = ApiConfig.get().getSource(sourceKey)
            val categories = sb.categories
            if (categories.isNotEmpty()) {
                for (cate in categories) {
                    for (sortData in list) {
                        if (sortData.name == cate) {
                            if (sortData.filters == null)
                                sortData.filters = ArrayList()
                            data.add(sortData)
                        }
                    }
                }
            } else {
                for (sortData in list) {
                    if (sortData.filters == null)
                        sortData.filters = ArrayList()
                    data.add(sortData)
                }
            }
        }
        if (withMy)
            data.add(0, MovieSort.SortData("my0", HomeActivity.getRes().getString(R.string.app_home)))
        java.util.Collections.sort(data)
        return data
    }

    @JvmStatic
    fun getAppVersionCode(mContext: Context): Int {
        val pm = mContext.packageManager
        return try {
            val packageInfo = pm.getPackageInfo(mContext.packageName, 0)
            packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            -1
        }
    }

    @JvmStatic
    fun resetApp(mContext: Context) {
        clearPublic(mContext)
        clearPrivate(mContext)
        restartApp()
    }

    @JvmStatic
    fun restartApp() {
        val activity = AppManager.getInstance().getActivity(HomeActivity::class.java)
        val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)!!
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activity.startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    @JvmStatic
    fun clearPublic(mContext: Context) {
        var dir = File(App.getInstance().getExternalFilesDir("")!!.parentFile!!.absolutePath)
        var files = dir.listFiles()
        if (files != null) {
            for (file in files) {
                FileUtils.recursiveDelete(file)
            }
        }
        val publicFilePath = Environment.getExternalStorageDirectory().path + "/" + packageName(mContext)
        dir = File(publicFilePath)
        files = dir.listFiles()
        if (files != null) {
            for (file in files) {
                FileUtils.recursiveDelete(file)
            }
        }
    }

    @JvmStatic
    fun clearPrivate(mContext: Context) {
        val dir = File(mContext.filesDir.parent!!)
        val files = dir.listFiles()
        if (files != null) {
            for (file in files) {
                if (!file.name.contains("lib")) {
                    FileUtils.recursiveDelete(file)
                }
            }
        }
    }

    @JvmStatic
    fun packageName(mContext: Context): String {
        val pm = mContext.packageManager
        return try {
            val packageInfo = pm.getPackageInfo(mContext.packageName, 0)
            packageInfo.packageName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    @JvmStatic
    fun getAppVersionName(mContext: Context): String {
        val pm = mContext.packageManager
        return try {
            val packageInfo = pm.getPackageInfo(mContext.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    @JvmStatic
    fun getFileSuffix(name: String): String {
        if (TextUtils.isEmpty(name)) return ""
        val endP = name.lastIndexOf(".")
        return if (endP > -1) name.substring(endP) else ""
    }

    @JvmStatic
    fun getFilePrefixName(fileName: String): String {
        if (TextUtils.isEmpty(fileName)) return ""
        val start = fileName.lastIndexOf(".")
        return if (start > -1) fileName.substring(0, start) else fileName
    }

    private val snifferMatch = Pattern.compile(
        "http((?!http).){20,}?\\.(m3u8|mp4|flv|avi|mkv|rm|wmv|mpg)\\?.*|" +
                "http((?!http).){20,}\\.(m3u8|mp4|flv|avi|mkv|rm|wmv|mpg)|" +
                "http((?!http).)*?video/tos*|" +
                "http((?!http).){20,}?/m3u8\\?pt=m3u8.*|" +
                "http((?!http).)*?default\\.ixigua\\.com/.*|" +
                "http((?!http).)*?dycdn-tos\\.pstatp[^\\?]*|" +
                "http.*?/player/m3u8play\\.php\\?url=.*|" +
                "http.*?/player/.*?[pP]lay\\.php\\?url=.*|" +
                "http.*?/playlist/m3u8/\\?vid=.*|" +
                "http.*?\\.php\\?type=m3u8&.*|" +
                "http.*?/download.aspx\\?.*|" +
                "http.*?/api/up_api.php\\?.*|" +
                "https.*?\\.66yk\\.cn.*|" +
                "http((?!http).)*?netease\\.com/file/.*"
    )

    @JvmStatic
    fun isVideoFormat(url: String): Boolean {
        if (url.contains("=http")) return false
        return if (snifferMatch.matcher(url).find()) {
            !url.contains(".js") && !url.contains(".css") && !url.contains(".jpg") && !url.contains(".png") && !url.contains(".gif") && !url.contains(".ico") && !url.contains("rl=") && !url.contains(".html")
        } else false
    }

    @JvmStatic
    fun safeJsonString(obj: JsonObject, key: String, defaultVal: String): String {
        return try {
            if (obj.has(key)) {
                if (obj.get(key).isJsonObject || obj.get(key).isJsonArray) obj.get(key).toString().trim()
                else obj.getAsJsonPrimitive(key).asString.trim()
            } else defaultVal
        } catch (_: Throwable) {
            defaultVal
        }
    }

    @JvmStatic
    fun safeJsonInt(obj: JsonObject, key: String, defaultVal: Int): Int {
        return try {
            if (obj.has(key)) obj.getAsJsonPrimitive(key).asInt
            else defaultVal
        } catch (_: Throwable) {
            defaultVal
        }
    }

    @JvmStatic
    fun safeJsonStringList(obj: JsonObject, key: String): ArrayList<String> {
        val result = ArrayList<String>()
        try {
            if (obj.has(key)) {
                if (obj.get(key).isJsonObject) {
                    result.add(obj.get(key).asString)
                } else {
                    for (opt in obj.getAsJsonArray(key)) {
                        result.add(opt.asString)
                    }
                }
            }
        } catch (_: Throwable) {
        }
        return result
    }

    @JvmStatic
    fun checkReplaceProxy(urlOri: String): String {
        return if (urlOri.startsWith("proxy://"))
            urlOri.replace("proxy://", ControlManager.get().getAddress(true) + "proxy?")
        else urlOri
    }

    @JvmStatic
    fun StoragePermissionGroup(): Array<String> = arrayOf(Permission.MANAGE_EXTERNAL_STORAGE)

    @JvmStatic
    fun getPackageName(mContext: Context): String = packageName(mContext)
}
