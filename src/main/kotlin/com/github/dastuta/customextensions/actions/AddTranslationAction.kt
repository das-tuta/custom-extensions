package com.github.dastuta.customextensions.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import java.io.File


class AddTranslationAction : AnAction() {

	override fun actionPerformed(e: AnActionEvent) {
		val project = e.project ?: return
		val editor = e.getData(CommonDataKeys.EDITOR) ?: return
		val selectionModel = editor.selectionModel
		val selectedText = selectionModel.selectedText

		// Ensure text is selected
		if (selectedText.isNullOrBlank()) {
			return
		}

		// Show the dialog
		val dialog = TranslationDialog(project, selectedText)
		if (dialog.showAndGet()) {
			val data = dialog.getFormData()
			executeWriteAction(project, data)
		}
	}

	override fun update(e: AnActionEvent) {
		val editor = e.getData(CommonDataKeys.EDITOR)
		// Only enable if text is selected
		e.presentation.isEnabledAndVisible = editor != null &&
				editor.selectionModel.hasSelection()
	}

	private fun executeWriteAction(project: Project, data: TranslationData) {
		WriteCommandAction.runWriteCommandAction(project) {
			try {
				val basePath = project.basePath ?: return@runWriteCommandAction

				val keysPath = "$basePath/src/ui/utils/TranslationKey.ts"
				val enPath = "$basePath/src/ui/translations/en.ts"
				val dePath = "$basePath/src/ui/translations/de.ts"
				val deSiePath = "$basePath/src/ui/translations/de_sie.ts"

				val labelContent = "	| \"${data.label}\""
				insertEntrySpace(File(keysPath), labelContent)

				val enContent = "		\"${data.label}\": \"${data.en}\","
				insertEntry(File(enPath), enContent)

				val deContent = "		\"${data.label}\": \"${data.de}\","
				insertEntry(File(dePath), deContent)

				val deSieContent = "		\"${data.label}\": \"${data.deSie}\","
				insertEntry(File(deSiePath), deSieContent)

			} catch (ex: Exception) {
				ex.printStackTrace()
			}
		}
	}


	/**
	 * Insert key entry at last line and add one empty line as last
	 */
	private fun insertEntrySpace(file: File, content: String) {
		val lines = file.readLines().toMutableList()

		var insertIndex = lines.size

		lines.add(insertIndex, content + "\n")

		file.writeText(lines.joinToString("\n"))
	}

	/**
	 * Inserts content before the first closing brace } as it's where the last possible translation can be added.
	 */
	private fun insertEntry(file: File, content: String) {
		if (!file.exists()) {
			return
		}

		val lines = file.readLines().toMutableList()
		var insertIndex = -1

		for (i in lines.size - 1 downTo 0) {
			val trimmed = lines[i].trim()
			if (trimmed.endsWith("}")) {
				insertIndex = i
				break
			}
		}

		if (insertIndex != -1) {
			val lastEntryIndex = insertIndex - 2
			if (!lines[lastEntryIndex].trim().endsWith(",")) {
				lines[lastEntryIndex] = lines[lastEntryIndex] + ","
			}
			lines.add(insertIndex - 1, content)
		}

		// Add one extra newline
		file.writeText(lines.joinToString("\n") + "\n")
	}
}

data class TranslationData(
	val label: String,
	val en: String,
	val de: String,
	val deSie: String
)
