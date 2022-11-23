@file:Suppress("LeakingThis")

package com.example.flatlaf

import com.example.flatlaf.dialog.AboutDialog
import com.example.flatlaf.dialog.settings.SettingsDialog
import com.example.flatlaf.intellijthemes.IJThemesPanel
import java.awt.Desktop
import java.awt.GraphicsEnvironment
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.net.URI
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.system.exitProcess

/*
 * InternalFrameDemo.java requires:
 *   MyInternalFrame.java
 */
open class MainFrame : JFrame("$APP_NAME - $APP_VERSION") {
    init {
        // Set instance
        instance = this

        themesPanel = IJThemesPanel()

        // Get screen information.
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screen = environment.defaultScreenDevice
        val displayMode = screen.displayMode
        val position = screen.defaultConfiguration.bounds.location
        this.setSize(1024, 640)

        this.iconImage = ImageIO.read(APP_ICON_REF)

        // Set the frame in the middle of the default screen.
        this.setLocation(
            position.x + (displayMode.width - this.width) / 2,
            position.y + (displayMode.height - this.height) / 2
        )

        defaultCloseOperation = DO_NOTHING_ON_CLOSE

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                quit()
            }
        })

        TODO("Add widgets here")
        TODO("Implement first initialization.")

        contentPane = TODO("Set content pane.")
        jMenuBar = createMenuBar()
    }

    protected fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()

        //Set up the lone menu.
        val windowMenu = JMenu("File")
        windowMenu.mnemonic = KeyEvent.VK_W
        menuBar.add(windowMenu)

        //Set up the first menu item.
        val openItem = JMenuItem("Open...")
        openItem.mnemonic = KeyEvent.VK_O
        openItem.accelerator = KeyStroke.getKeyStroke("control O")
        openItem.action = action("Open") { openFile() }
        windowMenu.add(openItem)

        //Set up the first menu item.
        val settingsItem = JMenuItem("Settings...")
        settingsItem.mnemonic = KeyEvent.VK_O
        settingsItem.accelerator = KeyStroke.getKeyStroke("control O")
        settingsItem.action = action("Settings") { configureTheme() }
        windowMenu.add(settingsItem)

        //Set up the second menu item.
        val quitItem = JMenuItem("Quit")
        quitItem.mnemonic = KeyEvent.VK_Q
        quitItem.accelerator = KeyStroke.getKeyStroke("alt F4")
        quitItem.action = action("Quit") { quit() }
        windowMenu.add(quitItem)

        val helpMenu = JMenu("Help")
        windowMenu.mnemonic = KeyEvent.VK_H
        menuBar.add(helpMenu)

        //Set up the first menu item.
        val aboutItem = JMenuItem("About")
        aboutItem.mnemonic = KeyEvent.VK_A
        aboutItem.accelerator = KeyStroke.getKeyStroke("F1")
        aboutItem.action = action("About") { showAbout() }
        helpMenu.add(aboutItem)

        //Set up the first menu item.
        val newIssueItem = JMenuItem("New Issue")
        newIssueItem.mnemonic = KeyEvent.VK_I
        newIssueItem.accelerator = KeyStroke.getKeyStroke("F8")
        newIssueItem.action = action("New Issue") { openNewIssuePage() }
        helpMenu.add(newIssueItem)

        //Set up the first menu item.
        val issueTrackerItem = JMenuItem("Issue Tracker")
        issueTrackerItem.mnemonic = KeyEvent.VK_S
        issueTrackerItem.accelerator = KeyStroke.getKeyStroke("control F8")
        issueTrackerItem.action = action("Issue Tracker") { openIssueTracker() }
        helpMenu.add(issueTrackerItem)

        return menuBar
    }

    /**
     * Opens the issues tracker page in the default browser.
     */
    private fun openIssueTracker() {
        Desktop.getDesktop().browse(URI(ISSUES_URL))
    }

    private fun openNewIssuePage() {
        Desktop.getDesktop().browse(URI(NEW_ISSUE_URL))
    }

    private fun showAbout() {
        AboutDialog(this, "About $APP_NAME", true).apply {
            setLocationRelativeTo(this@MainFrame)
            isVisible = true
        }
    }

    private fun configureTheme() {
        SettingsDialog(this, "Settings", true).apply {
            setLocationRelativeTo(this@MainFrame)
            isVisible = true
        }
    }

    protected fun openFile() {
        val fileChooser = JFileChooser()
        fileChooser.showOpenDialog(this)
        val selectedFile = fileChooser.selectedFile
        selectedFile?.let {
            openFile(it)
        }
    }

    private fun openFile(@Suppress("UNUSED_PARAMETER") selectedFile: File) {
        TODO("Not yet implemented")
    }

    //Quit the application.
    protected fun quit() {
        exitProcess(0)
    }

    companion object {
        lateinit var instance: MainFrame
            private set

        lateinit var themesPanel: IJThemesPanel

        /**
         * Create the GUI and show it.  For thread safety,
         * this method should be invoked from the
         * event-dispatching thread.
         */
        fun start() {
            //Make sure we have nice window decorations.
            setDefaultLookAndFeelDecorated(true)

            //Create and set up the window.
            val frame = MainFrame()

            //Display the window.
            frame.isVisible = true
        }
    }
}