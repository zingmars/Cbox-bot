/**
 * Created by kark on 12.04.2013.
 * Code found https://stackoverflow.com/a/15970455
 * Kotlinified and modified for this project by zingmars on 01.10.2015
 */
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayList
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

public class ZipUtil(private val SOURCE_FOLDER :String, private val OUTPUT_ZIP_FILE :String, private val ActiveFile :String, private val ActiveFile2 :String, private val ActiveFile3 :String, private val Logger :Logger? = null)
{
    private val fileList: MutableList<String>

    init
    {
        fileList = ArrayList<String>()
    }

    public fun getFileList(): MutableList<String>
    {
        return fileList
    }
    public fun clearOriginalFolder()
    {
        var source :String
        try {
            source = SOURCE_FOLDER.substring(SOURCE_FOLDER.lastIndexOf("\\") + 1, SOURCE_FOLDER.length)
        } catch (e: Exception) {
            source = SOURCE_FOLDER
        }
        for (file in fileList) {
            Logger?.LogMessage(4, file)
            if(file != ActiveFile && file != ActiveFile2 && file != ActiveFile3) File(source+file).delete()
        }
        Logger?.LogMessage(5)
    }
    public fun zipIt(zipFile: String = OUTPUT_ZIP_FILE)
    {
        val buffer = ByteArray(1024)
        var source :String
        var fos: FileOutputStream?
        var zos: ZipOutputStream? = null
        try {
            try {
                source = SOURCE_FOLDER.substring(SOURCE_FOLDER.lastIndexOf("\\") + 1, SOURCE_FOLDER.length)
            } catch (e: Exception) {
                source = SOURCE_FOLDER
            }

            fos = FileOutputStream(zipFile)
            zos = ZipOutputStream(fos)

            Logger?.LogMessage(6, zipFile)
            var `in`: FileInputStream? = null

            for (file in this.fileList) {
                Logger?.LogMessage(7, file)
                val ze = ZipEntry(source + File.separator + file)
                zos.putNextEntry(ze)
                try {
                    `in` = FileInputStream(SOURCE_FOLDER + File.separator + file)
                    var len: Int =`in`.read(buffer)
                    while (len > 0) {
                        zos.write(buffer, 0, len)
                        len = `in`.read(buffer)
                    }
                } finally {
                    `in`?.close()
                }
            }

            zos.closeEntry()
            Logger?.LogMessage(8)

        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            try {
                zos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
    public fun generateFileList(node: File = File(SOURCE_FOLDER))
    {
        // add file only
        if (node.isFile) {
            fileList.add(generateZipEntry(node.toString()))
        }

        if (node.isDirectory) {
            val subNote = node.list()
            for (filename in subNote) {
                generateFileList(File(node, filename))
            }
        }
    }
    private fun generateZipEntry(file: String): String
    {
        return file.substring(SOURCE_FOLDER.length, file.length)
    }
}