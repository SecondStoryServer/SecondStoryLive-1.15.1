package me.syari.sec_story.paper.live.world

import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.RunCommand.runCommandFromConsole
import me.syari.sec_story.paper.library.config.CreateConfig.config
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.run
import me.syari.sec_story.paper.live.Main.Companion.plugin
import org.bukkit.Bukkit.getWorld
import org.bukkit.command.CommandSender
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object Backup: FunctionInit {
    override fun init() {
        createCmd("backup", "Backup"){ _, _ ->
            val console = plugin.server.consoleSender
            run(plugin) {
                try {
                    console.sendWithPrefix("&fバックアップを開始します")
                    runCommandFromConsole("save-off")
                    runCommandFromConsole("save-all")
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                if (!Files.exists(Paths.get(backupFolderName))) {
                    File(backupFolderName).mkdir()
                }
                val timeStamp = SimpleDateFormat(timeFormat).format(Calendar.getInstance().time)
                for (name in backupWorldList) {
                    if(getWorld(name) == null) {
                        console.sendWithPrefix("&c${name}は存在しないワールドです")
                        continue
                    }
                    val dirPath = "$backupFolderName/$name"
                    console.sendWithPrefix("&a$name&fのバックアップをします")
                    if (!Files.exists(Paths.get(dirPath))) {
                        File(dirPath).mkdir()
                    }
                    zipIt(name, "$dirPath/$timeStamp.zip")
                    cleanup(dirPath)
                }
                console.sendWithPrefix("&fバックアップが終了しました")
                runCommandFromConsole("save-on")
            }
        }
    }

    private lateinit var backupFolderName: String
    private lateinit var timeFormat: String
    private var backupRateLimit = -1
    private lateinit var backupWorldList: List<String>

    fun loadConfig(output: CommandSender){
        config(plugin, output, "World/backup.yml", false){
            backupFolderName = getString("folder", "backup")
            timeFormat = getString("format", "yy.MM.dd_HH:mm")
            backupRateLimit = getInt("limit", 5)
            backupWorldList = getStringList("world", listOf())
        }
    }

    private fun cleanup(directory: String) {
        try {
            if (Files.list(Paths.get(directory)).count() > backupRateLimit) {
                val dir = Paths.get(directory)
                val lastFilePath = Files.list(dir)
                    .filter { f: Path ->
                        !Files.isDirectory(f)
                    }
                    .min(Comparator.comparingLong { f: Path ->
                        f.toFile().lastModified()
                    })
                if (lastFilePath.isPresent) {
                    File(lastFilePath.get().toString()).delete()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun zipIt(input: String, out: String) {
        val fileList = mutableListOf<String>()
        generateFileList(input, fileList, File(input))
        val buffer = ByteArray(1024)
        val source = File(input).name
        val fos: FileOutputStream?
        var zos: ZipOutputStream? = null
        try {
            fos = FileOutputStream(out)
            zos = ZipOutputStream(fos)
            for (file in fileList) {
                val ze = ZipEntry(source + File.separator + file)
                zos.putNextEntry(ze)
                var inputStream: FileInputStream? = null
                try {
                    inputStream = FileInputStream(input + File.separator + file)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } > 0) {
                        zos.write(buffer, 0, len)
                    }
                } finally {
                    inputStream?.close()
                }
            }
            zos.closeEntry()
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            try {
                zos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun generateFileList(input: String, fileList: MutableList<String>, node: File) {
        if (node.isFile) {
            fileList.add(generateZipEntry(input, node.toString()))
        }
        if (node.isDirectory) {
            val subNote = node.list() ?: return
            for (filename in subNote) {
                generateFileList(input, fileList, File(node, filename))
            }
        }
    }

    private fun generateZipEntry(input: String, file: String) = file.substring(input.length + 1, file.length)
}