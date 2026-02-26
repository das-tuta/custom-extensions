package com.github.dastuta.customextensions.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
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
		isOKActionEnabled = true
		init()
	}

	override fun createCenterPanel(): JPanel {
		val panel = JPanel(GridBagLayout())
		panel.preferredSize = Dimension(400, 200)
		val gbc = GridBagConstraints()

		fun addRow(yPos: Int, labelText: String, field: JBTextField) {
			gbc.gridx = 0
			gbc.gridy = yPos
			gbc.weightx = 0.0
			gbc.anchor = GridBagConstraints.WEST
			gbc.insets = Insets(0, 0, 5, 10)
			gbc.fill = GridBagConstraints.NONE
			panel.add(JLabel(labelText), gbc)

			gbc.gridx = 1
			gbc.weightx = 1.0
			gbc.anchor = GridBagConstraints.WEST
			gbc.fill = GridBagConstraints.HORIZONTAL
			gbc.insets = Insets(0, 0, 5, 0)
			panel.add(field, gbc)
		}

		labelField = JBTextField()
		labelField.text = initialLabel

		enField = JBTextField()
		deField = JBTextField()
		deSieField = JBTextField()

		addRow(0, "Label:", labelField)
		addRow(1, "EN:", enField)
		addRow(2, "DE:", deField)
		addRow(3, "DE_SIE:", deSieField)

		setupListeners()

		return panel
	}

	private fun setupListeners() {
		enField.addFocusListener(object : FocusAdapter() {
			override fun focusLost(e: FocusEvent) {
				val enText = enField.text
				if (deField.text.isEmpty() && enText.isNotEmpty()) {
					deField.text = enText
				}
				if (deSieField.text.isEmpty() && enText.isNotEmpty()) {
					deSieField.text = enText
				}
			}
		})

		deField.addFocusListener(object : FocusAdapter() {
			override fun focusLost(e: FocusEvent) {
				val deText = deField.text
				val enText = enField.text

				if (deSieField.text.equals(enText)) {
					deSieField.text = deText
				}
			}
		})

		// 3. Select All logic for DE and DE_SIE
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

	// This executes when OK is clicked, ensuring final sync even if focus wasn't lost
	override fun doOKAction() {
		val enText = enField.text

		if (deField.text.isEmpty() && enText.isNotEmpty()) {
			deField.text = enText
		}

		if (deSieField.text.isEmpty() && enText.isNotEmpty()) {
			deSieField.text = enText
		}

		if (enField.text.equals(deSieField.text)) {
			deSieField.text = deField.text
		}

		super.doOKAction()
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
