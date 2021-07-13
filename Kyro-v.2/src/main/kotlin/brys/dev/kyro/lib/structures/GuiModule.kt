package brys.dev.kyro.lib.structures

import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Image
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import kotlin.system.exitProcess
/**
 * Some might ask about this if you want your own logo to be shown go to the assets folder and change it yourself it isnt rocket science
 */
object GuiModule {
    private val frame = JFrame("Kyro") // Might be changeable in the future ¯\_(ツ)_/¯
    private val stats = JLabel("Unknown Stats" ,JLabel.CENTER ) // Why would you want to change this
    private val shutdownButton = JButton(ImageIcon(ImageIcon("./assets/1.png").image.getScaledInstance(64,64, Image.SCALE_SMOOTH))) // These are the assets
    private val logs = JButton(ImageIcon(ImageIcon("./assets/logs.png").image.getScaledInstance(64, 64, Image.SCALE_SMOOTH))) // Kek
    private val logFrame = JFrame()
    private val logArea = JTextArea()
    /**
     * An actual pain in my ass
     */
    fun initGUI(): JLabel {
        stats.font = Font("Consolas", Font.BOLD, 35)
        stats.foreground = Color.decode("#47c462")
        shutdownButton.background = Color.decode("#23272A")
        shutdownButton.setBounds(64,64,64,64)
        shutdownButton.setLocation(10, 10)
        shutdownButton.preferredSize = (Dimension(10,10))
        shutdownButton.isBorderPainted = false
        logs.setBounds(64,64,64,64)
        logs.setLocation(1840,950)
        frame.setBounds(1920,1080,1920,1080)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.contentPane.background = Color.decode("#23272A")
        frame.iconImage = ImageIcon("./assets/ico.png").image
        frame.isVisible = true
        logs.isVisible = true
        logs.isBorderPainted = false
        logs.preferredSize = (Dimension(10,10))
        logs.background = Color.decode("#23272A")
        shutdownButton.isVisible = true
        frame.add(logs)
        frame.add(shutdownButton)
        frame.add(stats)
        shutdownButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) {
                    println("Shutdown has been issued for entire process.")
                    exitProcess(0)
            }

            override fun mouseEntered(e: MouseEvent?) {
                shutdownButton.background = Color.decode("#282d30")
            }

            override fun mouseExited(e: MouseEvent?) {
                shutdownButton.background = Color.decode("#23272A")
            }
        })
        logs.addMouseListener(object: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                logFrame.add(logArea)
                logArea.isVisible = true
                logFrame.isVisible = true
                logFrame.contentPane.background = Color.decode("#23272A")
                logArea.background = Color.decode("#23272A")
                logArea.foreground = Color.decode("#47c462")
                logFrame.setBounds(2350,1080,1920,1080)
                println("Logs issued by logs button")
                logArea.font = Font("Consolas", Font.BOLD, 35)
                logArea.isEditable = false
                logFrame.iconImage = ImageIcon("./assets/ico.png").image
            }
            override fun mouseEntered(e: MouseEvent?) {
                shutdownButton.background = Color.decode("#282d30")
            }

            override fun mouseExited(e: MouseEvent?) {
                shutdownButton.background = Color.decode("#23272A")
            }
        })
        logFrame.addWindowListener(object: WindowAdapter() {
            override fun windowActivated(e: WindowEvent?) {
                frame.isVisible = false
            }

            override fun windowClosing(e: WindowEvent?) {
                frame.isVisible = true
            }
        });
        return stats
    }
    fun warn(warning: String) {
        JOptionPane.showMessageDialog(null, warning)
    }
    fun grabStatus(): JLabel {
        return stats
    }
    fun grabLog(): JTextArea {
        return logArea
    }
}