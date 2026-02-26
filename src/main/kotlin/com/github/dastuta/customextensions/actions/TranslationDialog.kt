package com.github.dastuta.customextensions.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class TranslationDialog(project: Project, private val initialLabel: String) : DialogWrapper(project) {

	private lateinit var panel: JPanel
	private lateinit var labelField: JBTextField
	private lateinit var enField: JBTextField
	private lateinit var deField: JBTextField
	private lateinit var deSieField: JBTextField

	init {
		init()
		title = "Add Translation"

		// Set initial value for Label
		labelField.text = initialLabel

		// Focus on EN field
		preferredFocusedComponent = enField

		// Setup listeners
		setupListeners()
	}

	override fun createCenterPanel(): JComponent {
		// Assuming you used the GUI Designer or build forms programmatically.
		// If using IntelliJ GUI Designer, bind this panel to the .form file.
		// Below is a programmatic setup if you don't have a .form file.

		panel = JPanel()
		panel.layout = java.awt.GridLayout(4, 2, 5, 5)

		// Label Row
		panel.add(javax.swing.JLabel("Label:"))
		labelField = JBTextField()
		labelField.text = initialLabel
		panel.add(labelField)

		// EN Row
		panel.add(javax.swing.JLabel("EN:"))
		enField = JBTextField()
		panel.add(enField)

		// DE Row
		panel.add(javax.swing.JLabel("DE:"))
		deField = JBTextField()
		panel.add(deField)

		// DE_SIE Row
		panel.add(javax.swing.JLabel("DE_SIE:"))
		deSieField = JBTextField()
		panel.add(deSieField)

		return panel
	}

	private fun setupListeners() {
		// Listener for EN field to sync empty DE and DE_SIE
		val syncListener = object : DocumentListener {
			override fun insertUpdate(e: DocumentEvent?) = sync()
			override fun removeUpdate(e: DocumentEvent?) = sync()
			override fun changedUpdate(e: DocumentEvent?) = sync()

			fun sync() {
				val newText = enField.text
				if (deField.text.isEmpty()) {
					deField.text = newText
				}
				if (deSieField.text.isEmpty()) {
					deSieField.text = newText
				}
			}
		}
		enField.document.addDocumentListener(syncListener)

		// Focus Listeners for Select All logic
		val selectAllListener = object : java.awt.event.FocusAdapter() {
			override fun focusGained(e: java.awt.event.FocusEvent?) {
				val source = e?.source as? JBTextField
				source?.selectAll()
			}
		}
		deField.addFocusListener(selectAllListener)
		deSieField.addFocusListener(selectAllListener)
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
