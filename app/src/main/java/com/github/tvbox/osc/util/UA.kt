package com.github.tvbox.osc.util

import com.github.tvbox.osc.base.App
import java.io.DataInputStream
import java.util.Random

object UA {
    @JvmStatic
    fun random(): String {
        return try {
            val fis = App.getInstance().assets.open("ua.db")
            val dis = DataInputStream(fis)
            val len = dis.readInt()
            val random = Random().nextInt(len)
            dis.skipBytes(random * 4)
            dis.skipBytes((len - 1 - random) * 4 + dis.readInt())
            val s = dis.readUTF()
            fis.close()
            s
        } catch (e: Exception) {
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36"
        }
    }
}
