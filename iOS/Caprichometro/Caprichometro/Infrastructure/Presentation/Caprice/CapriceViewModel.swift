//
//  CapriceViewModel.swift
//  Caprichometro
//
//  Created by Danii on 24/10/2026.
//

import Foundation
import Observation

@MainActor
@Observable
final class CreateCapriceViewModel {
    var type = "Compra"
    var question = ""
    var answer1 = "Sí, me lo compro"
    var answer2 = "No, mejor ahorro"

    private(set) var isSaving = false
    private(set) var successMessage: String?
    private(set) var errorMessage: String?

    private let saveCapriceUseCase: SaveCapriceUseCase
    private let currentUserIDProvider: @MainActor () -> String?

    init(
        saveCapriceUseCase: SaveCapriceUseCase,
        currentUserIDProvider: @escaping @MainActor () -> String?
    ) {
        self.saveCapriceUseCase = saveCapriceUseCase
        self.currentUserIDProvider = currentUserIDProvider
    }

    private func trimmed(_ value: String) -> String {
        value.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    var canSave: Bool {
        !trimmed(question).isEmpty
            && !trimmed(answer1).isEmpty
            && !trimmed(answer2).isEmpty
            && !isSaving
    }

    func save() async {
        guard canSave else { return }

        successMessage = nil
        errorMessage = nil

        guard let userID = currentUserIDProvider() else {
            errorMessage = "Inicia sesión antes de guardar un capricho."
            return
        }

        isSaving = true
        defer { isSaving = false }

        do {
            _ = try await saveCapriceUseCase.execute(
                type: type,
                question: trimmed(question),
                answer1: trimmed(answer1),
                answer2: trimmed(answer2),
                userID: userID
            )

            successMessage = "Capricho guardado en la nube."
            question = ""
            answer1 = "Sí, me lo compro"
            answer2 = "No, mejor ahorro"
            type = "Compra"
        } catch {
            errorMessage = "No se pudo guardar el capricho: \(error.localizedDescription)"
        }
    }
}
