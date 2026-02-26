package com.github.dastuta.customextensions.actions


import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBTextField
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class TranslationDialog(project: Project, private val initialLabel: String) : DialogWrapper(project) {

	private lateinit var labelField: JBTextField
	private lateinit var enField: JBTextField
	private lateinit var deField: JBTextField
	private lateinit var deSieField: JBTextField

	override fun getPreferredFocusedComponent(): JComponent? {
		return this.enField
	}

	init {
		title = "Add Translation"
		// Set OK/OK Enabled
		isOKActionEnabled = true

		// Initialize the dialog (this calls createCenterPanel)
		init()

	}

	override fun createCenterPanel(): JPanel {
		val panel = JPanel(GridBagLayout())
		panel.preferredSize = Dimension(400, 200)
		val gbc = GridBagConstraints()

		// Helper to add rows cleanly
		fun addRow(yPos: Int, labelText: String, field: JBTextField) {
			// Label Column
			gbc.gridx = 0
			gbc.gridy = yPos
			gbc.weightx = 0.0
			gbc.anchor = GridBagConstraints.WEST
			gbc.insets = Insets(0, 0, 5, 10)
			gbc.fill = GridBagConstraints.NONE
			panel.add(JLabel(labelText), gbc)

			// Field Column
			gbc.gridx = 1
			gbc.weightx = 1.0
			gbc.anchor = GridBagConstraints.WEST
			gbc.fill = GridBagConstraints.HORIZONTAL
			gbc.insets = Insets(0, 0, 5, 0)
			panel.add(field, gbc)
		}

		// 1. Initialize Label Field and set text
		labelField = JBTextField()
		labelField.text = initialLabel

		// 2. Initialize Fields
		enField = JBTextField()
		deField = JBTextField()
		deSieField = JBTextField()

		// 3. Add to Panel
		addRow(0, "Label:", labelField)
		addRow(1, "EN:", enField)
		addRow(2, "DE:", deField)
		addRow(3, "DE_SIE:", deSieField)

		// 4. Attach Listeners inside createCenterPanel
		setupListeners()

		return panel
	}

	private fun setupListeners() {
		// Listener for EN field to sync empty DE and DE_SIE
		enField.document.addDocumentListener(object : DocumentAdapter() {
			override fun textChanged(e: javax.swing.event.DocumentEvent) {
				val newText = enField.text
				if (deField.text.isEmpty()) {
					deField.text = newText
				}
				if (deSieField.text.isEmpty()) {
					deSieField.text = newText
				}
			}
		})

		// Focus Listeners for Select All logic
		val selectAllAdapter = object : FocusAdapter() {
			override fun focusGained(e: FocusEvent) {
				val source = e.source
				if (source is JBTextField) {
					source.selectAll()
				}
			}
		}

		deField.addFocusListener(selectAllAdapter)
		deSieField.addFocusListener(selectAllAdapter)
	}

	fun getFormData(): TranslationData {
		return TranslationData(
			label = labelField.text,
			en = enField.text,
			de = deField.text,
			deSie = deSieField.text
		)
	}

	override fun doValidate(): ValidationInfo? {
		if (labelField.text.isBlank()) {
			return ValidationInfo("Label cannot be empty", labelField)
		}
		return null
	}
}
