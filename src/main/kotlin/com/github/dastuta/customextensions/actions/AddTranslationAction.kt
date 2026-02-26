package com.github.dastuta.customextensions.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException

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
				// Define fixed relative paths to your files
				val basePath = project.basePath ?: return@runWriteCommandAction
				val keysPath = "$basePath/src/common/misc/TranslationKey.ts"
				val enPath = "$basePath/src/mail-app/translations/en.ts"
				val dePath = "$basePath/src/mail-app/translations/de.ts"
				val deSiePath = "$basePath/src/mail-app/translations/de_sie.ts"

				// 1. Edit translationKey.ts
				appendToFile(keysPath, "| \"${data.label}\"")

				// 2. Edit en.ts
				insertEntry(enPath, "  \"${data.label}\": \"${data.en}\",")

				// 3. Edit de.ts
				insertEntry(dePath, "  \"${data.label}\": \"${data.de}\",")

				// 4. Edit de_sie.ts
				insertEntry(deSiePath, "  \"${data.label}\": \"${data.deSie}\",")

			} catch (ex: IOException) {
				// Handle file errors (e.g., logging)
				ex.printStackTrace()
			}
		}
	}

	private fun appendToFile(filePath: String, content: String) {
		val file = java.io.File(filePath)
		if (file.exists()) {
			file.appendText("\n$content")
		} else {
			// Optional: Create parent directories if needed
			file.parentFile.mkdirs()
			file.writeText(content)
		}
	}


	/**
	 * Inserts content before the last closing brace } or semicolon ;.
	 * Handles the comma logic for the previous line if necessary.
	 */
	private fun insertEntry(file: File, content: String) {
		if (!file.exists()) {
			file.parentFile?.mkdirs()
			file.writeText(content)
			return
		}

		val lines = file.readLines().toMutableList()

		// Find the last line containing a closing brace or semicolon
		var insertIndex = -1

		for (i in lines.size - 1 downTo 0) {
			val trimmed = lines[i].trim()
			// Check for object closing } or type/list ending ;
			if (trimmed.endsWith("}") || trimmed.endsWith("},")) {
				insertIndex = i
				break
			}
			if (trimmed.endsWith(";") || trimmed.endsWith(";")) {
				insertIndex = i
				break
			}
		}

		if (insertIndex != -1) {
			// Insert the new content before the closing bracket
			lines.add(insertIndex, content)

			// (Optional) Check the line *before* our insertion (which is currently at insertIndex - 1)
			// If it is a key-value line missing a comma, we can add one to be safe,
			// though TS usually allows trailing commas.
			// Since this implementation adds a comma to 'content', the syntax is valid regardless.
		} else {
			// Fallback: simply append if no structure found (unlikely in this context)
			lines.add(content)
		}

		file.writeText(lines.joinToString("\n"))
	}
}

data class TranslationData(
	val label: String,
	val en: String,
	val de: String,
	val deSie: String
)
