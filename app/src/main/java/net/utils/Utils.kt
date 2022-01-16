package net.utils

import android.content.Context
import android.os.Environment
import es.dmoral.prefs.Prefs
import net.basicmodel.MyApplication
import java.io.*

object Utils {

    fun saveFile(content: String) {
        val filePath = Environment.getExternalStorageDirectory().absolutePath
        val fileName = "a.testupdate.txt"
        val file = File(filePath + File.separator + fileName)
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(content.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readrFile(filePath: String): String {
        val file = File(filePath)
        if (!file.exists()) return ""
        try {
            val reader = FileReader(filePath)
            val r = BufferedReader(reader)
            return r.readLine()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}