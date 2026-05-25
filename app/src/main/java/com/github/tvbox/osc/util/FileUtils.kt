package com.github.tvbox.osc.util

import android.os.Environment
import android.text.TextUtils
import android.util.Base64

import com.github.tvbox.osc.base.App
import com.github.tvbox.osc.server.ControlManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpHeaders
import com.orhanobut.hawk.Hawk

import org.json.JSONObject

import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.regex.Pattern

object FileUtils {

    @JvmStatic
    fun open(str: String): File = File(getExternalCachePath() + "/qjscache_" + str + ".js")

    @JvmStatic
    fun writeSimple(data: ByteArray, dst: File): Boolean {
        return try {
            if (dst.exists()) dst.delete()
            BufferedOutputStream(FileOutputStream(dst)).use { it.write(data) }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    @JvmStatic
    fun readSimple(src: File): ByteArray? {
        return try {
            BufferedInputStream(FileInputStream(src)).use { bis ->
                val data = ByteArray(bis.available())
                bis.read(data)
                data
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun readFileToString(path: String, charsetName: String): String {
        val jsonString = StringBuilder()
        try {
            BufferedReader(InputStreamReader(FileInputStream(path), charsetName)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    jsonString.append(line)
                    line = reader.readLine()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return jsonString.toString()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun copyFile(source: File, dest: File) {
        FileInputStream(source).use { `is` ->
            FileOutputStream(dest).use { os ->
                val buffer = ByteArray(1024)
                var length = `is`.read(buffer)
                while (length > 0) {
                    os.write(buffer, 0, length)
                    length = `is`.read(buffer)
                }
            }
        }
    }

    @JvmStatic
    fun getRootPath(): String = Environment.getExternalStorageDirectory().absolutePath

    @JvmStatic
    fun recursiveDelete(file: File) {
        try {
            if (!file.exists()) return
            if (file.isDirectory) {
                file.listFiles()?.forEach { recursiveDelete(it) }
            }
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val URL_JOIN = Pattern.compile("^http.*\\.(js|txt|json|m3u)", Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

    @JvmStatic
    fun loadModule(name: String): String? {
        return try {
            val resolvedName = when {
                name.contains("gbk.js") -> "gbk.js"
                name.contains("模板.js") -> "模板.js"
                name.contains("cat.js") -> "cat.js"
                else -> name
            }
            LOG.i("echo-loadModule $resolvedName")
            val m = URL_JOIN.matcher(resolvedName)
            when {
                m.find() -> {
                    if (!Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
                        val cache = getCache(MD5.encode(resolvedName))
                        var rel = cache
                        if (cache.isNullOrEmpty()) {
                            val netStr = get(resolvedName)
                            if (!netStr.isNullOrEmpty()) {
                                setCache(604800, MD5.encode(resolvedName)!!, netStr)
                            }
                            rel = netStr
                        }
                        rel
                    } else {
                        get(resolvedName)
                    }
                }
                resolvedName.startsWith("assets://") -> getAsOpen(resolvedName.substring(9))
                isAsFile(resolvedName, "js/lib") -> getAsOpen("js/lib/$resolvedName")
                resolvedName.startsWith("file://") -> get(ControlManager.get().getAddress(true) + "file/" + resolvedName.replace("file:///", "").replace("file://", ""))
                resolvedName.startsWith("clan://localhost/") -> get(ControlManager.get().getAddress(true) + "file/" + resolvedName.replace("clan://localhost/", ""))
                resolvedName.startsWith("clan://") -> {
                    val substring = resolvedName.substring(7)
                    val indexOf = substring.indexOf(47.toChar())
                    get("http://" + substring.substring(0, indexOf) + "/file/" + substring.substring(indexOf + 1))
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            name
        }
    }

    private val cachedDirFiles = HashMap<String, MutableSet<String>>()

    @JvmStatic
    fun isAsFile(name: String, dir: String): Boolean {
        var files = cachedDirFiles[dir]
        if (files == null) {
            LOG.i("echo-读取AssetsList")
            try {
                val list = App.getInstance().assets.list(dir)!!
                files = HashSet(list.toList())
            } catch (e: IOException) {
                files = HashSet()
            }
            cachedDirFiles[dir] = files!!
        }
        return files!!.contains(name.trim())
    }

    @JvmStatic
    fun getAsOpen(name: String): String {
        return try {
            val `is` = App.getInstance().assets.open(name)
            val data = ByteArray(`is`.available())
            `is`.read(data)
            `is`.close()
            String(data, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    @JvmStatic
    fun getCache(name: String?): String? {
        return try {
            val file = open(name!!)
            if (!file.exists()) return ""
            val code = String(readSimple(file)!!)
            if (TextUtils.isEmpty(code)) return ""
            val asJsonObject = Gson().fromJson(code, JsonObject::class.java).asJsonObject
            if (asJsonObject.get("expires").asInt.toLong() <= System.currentTimeMillis() / 1000) {
                recursiveDelete(open(name))
            }
            asJsonObject.get("data").asString
        } catch (e: Exception) {
            ""
        }
    }

    @JvmStatic
    fun getCacheByte(name: String): ByteArray? {
        return try {
            val file = open("B_$name")
            if (file.exists()) readSimple(file) else null
        } catch (e: Exception) {
            null
        }
    }

    @JvmStatic
    fun setCache(time: Int, name: String?, data: String?) {
        try {
            val jSONObject = JSONObject()
            jSONObject.put("expires", time + System.currentTimeMillis() / 1000)
            jSONObject.put("data", data)
            writeSimple(jSONObject.toString().toByteArray(), open(name!!))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun setCacheByte(name: String, data: ByteArray) {
        try {
            writeSimple(byteMerger("//DRPY".toByteArray(), Base64.encode(data, Base64.URL_SAFE)), open("B_$name"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun byteMerger(bt1: ByteArray, bt2: ByteArray): ByteArray {
        val bt3 = ByteArray(bt1.size + bt2.size)
        System.arraycopy(bt1, 0, bt3, 0, bt1.size)
        System.arraycopy(bt2, 0, bt3, bt1.size, bt2.size)
        return bt3
    }

    @JvmStatic
    fun get(str: String): String? = get(str, null)

    @JvmStatic
    fun get(str: String, headerMap: Map<String, String>?): String? {
        return try {
            val response = if (headerMap != null) {
                val h = HttpHeaders()
                for ((key, value) in headerMap) {
                    h.put(key, value)
                }
                OkGo.get<String>(str).headers(h).execute()
            } else {
                OkGo.get<String>(str).headers("User-Agent", if (str.startsWith("https://gitcode.net/")) UA.random() else "okhttp/3.15").execute()
            }
            if (response.isSuccessful && response.body() != null) {
                String(response.body()!!.bytes(), Charset.forName("UTF-8"))
            } else ""
        } catch (e: IOException) {
            ""
        }
    }

    @JvmStatic
    fun getCacheDir(): File = App.getInstance().cacheDir

    @JvmStatic
    fun getExternalCacheDir(): File? = App.getInstance().externalCacheDir

    @JvmStatic
    fun getExternalCachePath(): String {
        val externalCacheDir = getExternalCacheDir()
        return if (externalCacheDir == null) getCachePath() else externalCacheDir.absolutePath
    }

    @JvmStatic
    fun getCachePath(): String = getCacheDir().absolutePath

    @JvmStatic
    fun cleanPlayerCache() {
        recursiveDelete(File("${getCachePath()}${File.separator}thunder"))
        recursiveDelete(File("${getExternalCachePath()}${File.separator}ijkcaches"))
        recursiveDelete(File("${getExternalCachePath()}${File.separator}jpali${File.separator}Downloads"))
    }

    @JvmStatic
    fun getFileName(filePath: String): String {
        if (TextUtils.isEmpty(filePath)) return ""
        val fileName = filePath
        val p = fileName.lastIndexOf(File.separatorChar)
        return if (p != -1) fileName.substring(p + 1) else fileName
    }

    @JvmStatic
    fun getFileNameWithoutExt(filePath: String): String {
        if (TextUtils.isEmpty(filePath)) return ""
        val fileName = filePath
        var p = fileName.lastIndexOf(File.separatorChar)
        val name = if (p != -1) fileName.substring(p + 1) else fileName
        p = name.indexOf('.')
        return if (p != -1) name.substring(0, p) else name
    }

    @JvmStatic
    fun getFileExt(fileName: String): String {
        if (TextUtils.isEmpty(fileName)) return ""
        val p = fileName.lastIndexOf('.')
        return if (p != -1) fileName.substring(p).lowercase() else ""
    }

    @JvmStatic
    fun hasExtension(path: String): Boolean {
        val lastDotIndex = path.lastIndexOf(".")
        val lastSlashIndex = maxOf(path.lastIndexOf("/"), path.lastIndexOf("\\"))
        return lastDotIndex > lastSlashIndex && lastDotIndex < path.length - 1
    }

    @JvmStatic
    fun read(path: String): String {
        return try {
            read(FileInputStream(getLocal(path)))
        } catch (e: Exception) {
            ""
        }
    }

    @JvmStatic
    fun read(`is`: InputStream): String {
        return try {
            val data = ByteArray(`is`.available())
            `is`.read(data)
            `is`.close()
            String(data, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    @JvmStatic
    fun getLocal(path: String): File {
        val file1 = File(path.replace("file:/", ""))
        val file2 = File(path.replace("file:/", Environment.getExternalStorageDirectory().absolutePath))
        return if (file2.exists()) file2 else if (file1.exists()) file1 else File(path)
    }

    @JvmStatic
    fun deleteFile(file: File) {
        if (!file.exists()) return
        if (file.isFile) {
            if (file.canWrite()) file.delete()
            return
        }
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files.isNullOrEmpty()) {
                if (file.canWrite()) file.delete()
                return
            }
            for (one in files) {
                deleteFile(one)
            }
        }
    }

    @JvmStatic
    fun getFilePath(): String = App.getInstance().filesDir.absolutePath

    @JvmStatic
    fun isWeekAgo(file: File): Boolean {
        val oneWeekMillis = 15L * 24 * 60 * 60 * 1000
        val timeDiff = System.currentTimeMillis() - file.lastModified()
        return timeDiff > oneWeekMillis
    }
}
