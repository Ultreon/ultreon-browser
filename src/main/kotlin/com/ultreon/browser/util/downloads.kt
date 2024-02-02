package com.ultreon.browser.util

import com.ultreon.browser.dialog.StandardDialog
import com.ultreon.browser.UltreonBrowser
import org.cef.callback.CefDownloadItem
import org.cef.callback.CefDownloadItemCallback
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.File
import java.time.Instant
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import javax.swing.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val Long.fileSize: String
    get() {
        val bytes = this
        if (bytes <= 0L) return "0 B"

        return when {
            bytes < 1024L -> "$bytes B"
            bytes < 1024L * 1024L -> "${bytes / 1024L} KiB"
            bytes < 1024L * 1024L * 1024L -> "${bytes / 1024L / 1024L} MiB"
            bytes < 1024L * 1024L * 1024L * 1024L -> "${bytes / 1024L / 1024L / 1024L} GiB"
            bytes < 1024L * 1024L * 1024L * 1024L * 1024L -> "${bytes / 1024L / 1024L / 1024L / 1024L} TiB"
            bytes < 1024L * 1024L * 1024L * 1024L * 1024L * 1024L -> "${bytes / 1024L / 1024L / 1024L / 1024L / 1024L} PiB"
            else -> "${bytes / 1024L / 1024L / 1024L / 1024L / 1024L / 1024L} EiB"
        }
    }

object DownloadManager : StandardDialog(UltreonBrowser.instance, "Download Manager", false) {
    private fun readResolve(): Any = DownloadManager
    internal lateinit var scrollPane: JScrollPane
    internal val downloadMap = mutableMapOf<Int, Download>()
    internal val downloads = Vector<Download>()
    internal val toCancel = CopyOnWriteArraySet<Int>()
    internal val toPause = CopyOnWriteArraySet<Int>()
    internal val toResume = CopyOnWriteArraySet<Int>()

    private val toolbar = DownloadToolbar()
    internal val list = JPanel().apply {
        layout = GridBagLayout()
    }

    init {
        contentPane = createContent()

        this.preferredSize = Dimension(400, 500)
        this.isResizable = false
        this.setLocationRelativeTo(UltreonBrowser.instance)
    }

    private fun createContent(): JPanel {
        minimumSize = Dimension(400, 400)

        val content = JPanel(BorderLayout())
        content.add(toolbar, BorderLayout.NORTH)

        scrollPane = JScrollPane(list).also {
            it.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            it.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            list.maximumSize = Dimension(400, Int.MAX_VALUE)
            content.add(it, BorderLayout.CENTER)
        }

        content.add(createButtonPanel(), BorderLayout.SOUTH)

        return content
    }

    fun onDownload(
        id: Int,
        fullPath: String,
        url: String,
        totalBytes: Long,
        receivedBytes: Long,
        percentComplete: Int,
        endTime: Date,
        isComplete: Boolean,
        isCanceled: Boolean
    ) {
        downloadMap[id] = Download(id, url, fullPath, Date.from(Instant.now()), isCanceled, isComplete).also {
            it.totalBytes = totalBytes
            it.receivedBytes = receivedBytes
            it.percent = percentComplete
            it.speed = 0
            downloads.add(it)
            list.add(it, GridBagConstraints().apply {
                this.fill = GridBagConstraints.HORIZONTAL
                this.weightx = 1.0
                this.weighty = 1.0
                this.anchor = GridBagConstraints.FIRST_LINE_END
                this.insets = Insets(5, 5, 5, 5)
                this.gridwidth = GridBagConstraints.REMAINDER
                this.gridheight
                this.gridx = 0
            })
            list.revalidate()
        }
    }

    fun onUpdate(
        id: Int,
        fullPath: String,
        url: String,
        totalBytes: Long,
        receivedBytes: Long,
        percentComplete: Int,
        currentSpeed: Long,
        endTime: Date,
        isComplete: Boolean,
        isCanceled: Boolean,
        isValid: Boolean,
        callback: CefDownloadItemCallback
    ) {
        val download = downloadMap[id]
        if (download != null) {
            download.receivedBytes = receivedBytes
            download.totalBytes = totalBytes
            download.percent = percentComplete
            download.speed = currentSpeed
            download.url = url
            download.path = fullPath

            download.isCanceled = isCanceled
            download.isComplete = isComplete

            if (toCancel.contains(id)) callback.cancel()
            if (toPause.contains(id)) callback.pause()
            if (toResume.contains(id)) callback.resume()
        } else {
            onDownload(id, fullPath, url, totalBytes, receivedBytes, percentComplete, endTime, isComplete, isCanceled)
        }
    }

    private fun remove(item: CefDownloadItem) {
        downloadMap.remove(item.id)
        downloads.removeIf {
            if (it.id == item.id) {
                list.remove(it)
                true
            } else false
        }
    }
}

class DownloadCellRenderer : ListCellRenderer<Download> {
    override fun getListCellRendererComponent(
        list: JList<out Download>?,
        value: Download?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component = value?.also {
        if (isSelected) {
            it.background = list?.selectionBackground
            it.foreground = list?.selectionForeground
        } else {
            it.background = list?.background
            it.foreground = list?.foreground
        }
    } ?: throw IllegalArgumentException("value cannot be null")

}

class DownloadToolbar : JToolBar("DownloadToolbar") {
    init {
        isFloatable = false
        isRollover = true

        add(action("Clear") {
            DownloadManager.downloads.removeIf {
                if (it.isComplete || it.isCanceled) {
                    DownloadManager.list.remove(it)
                    DownloadManager.downloadMap.remove(it.id)
                    DownloadManager.list.revalidate()
                    true
                }
                else false
            }
        })
        add(action("Cancel All") {
            DownloadManager.toCancel.addAll(DownloadManager.downloadMap.filter { !it.value.isComplete && !it.value.isCanceled }.keys )
        })
    }
}

class Download(val id: Int,
               url: String?,
               path: String?,
               val startTime: Date,
               isCanceled: Boolean = false,
               isComplete: Boolean = false
) : JPanel() {
    var isComplete = isComplete
        set(value) {
            if (value) label?.text = "Completed!"
            field = value
        }
    var isCanceled = isCanceled
        set(value) {
            if (value) label?.text = "Canceled!"
            field = value
        }
    var url = url
    var path = path
        set(value) {
            urlLabel?.text = value?.let { File(it).name } ?: "Unknown"
            field = value
        }
    private var progressBar: JProgressBar
    var label: JLabel? = null
    var urlLabel: JLabel? = null
    var speed: Long = 0
    var receivedBytes: Long = 0
        set(value) {
            label?.text = "${receivedBytes.fileSize}/${totalBytes.fileSize} ${duration.format()}"
            progressBar.value = value.toInt()
            field = value
        }
    var totalBytes: Long = 0
        set(value) {
            if (value > 0L) {
                progressBar.maximum = value.toInt()
                progressBar.isIndeterminate = false
                label?.text = "${receivedBytes.fileSize}/${totalBytes.fileSize} ${duration}"
            }
            else progressBar.isIndeterminate = true
            field = value
        }
    var percent: Int = 0
        set(value) {
            label?.text = "${receivedBytes.fileSize}/${totalBytes.fileSize} ${duration.toIsoString()}"
            field = value
        }

    init {
        layout = BorderLayout()
        minimumSize = Dimension(DownloadManager.list.width, 85)
        maximumSize = Dimension(DownloadManager.list.width, Int.MAX_VALUE)
        preferredSize = Dimension(DownloadManager.list.width, 85)

        add(JLabel(File(path).name).apply {
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
            urlLabel = this
            font = font.deriveFont(Font.BOLD, 12f)
            maximumSize = Dimension(DownloadManager.list.width - DownloadManager.scrollPane.verticalScrollBar.width, 10)
        }, BorderLayout.NORTH)

        add(JProgressBar().also {
            progressBar = it
            minimumSize = Dimension(200, 20)
            size = size.apply {
                height = 20
            }
        }, BorderLayout.CENTER)

        add(JToolBar().apply {
            add(action("Cancel") {
                DownloadManager.toCancel += id
            })
            add(action("Pause") {
                DownloadManager.toPause += id
            })
            add(action("Resume") {
                DownloadManager.toResume += id
            })
        }, BorderLayout.EAST)

        add(JLabel("0 B/0 B").apply {
            label = this
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        }, BorderLayout.SOUTH)
    }

    val duration: Duration
        get() = (Instant.now().toEpochMilli() - startTime.time).milliseconds

    fun cancel() {
        DownloadManager.toCancel += id
    }

    fun pause() {
        DownloadManager.toPause += id
    }

    fun resume() {
        DownloadManager.toResume += id
    }
}

private fun Duration.format(): String {
    val h = this.inWholeHours
    val m = this.inWholeMinutes
    val s = this.inWholeSeconds
    val pm = m.toString().padStart(2, '0')
    val ps = s.toString().padStart(2, '0')
    return when {
        this < 1.seconds -> "Any moment now..."
        this < 3.seconds -> "Almost done..."
        this < 1.minutes -> "$ps seconds"
        this < 1.hours -> "$m:$ps"
        else -> "$h:$pm:$ps"
    }
}
