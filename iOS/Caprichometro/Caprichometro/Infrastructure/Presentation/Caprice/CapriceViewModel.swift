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
    var name = ""
    var price = ""

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

    private var parsedPrice: Double? {
        let normalizedPrice = price
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: ",", with: ".")

        guard let value = Double(normalizedPrice),
              value.isFinite,
              value > 0 else {
            return nil
        }

        return value
    }

    var canSave: Bool {
        !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            && parsedPrice != nil
            && !isSaving
    }

    func save() async {
        guard canSave, let parsedPrice else { return }

        successMessage = nil
        errorMessage = nil

        guard let ownerID = currentUserIDProvider() else {
            errorMessage = "Inicia sesión antes de guardar un capricho."
            return
        }

        isSaving = true
        defer { isSaving = false }

        do {
            _ = try await saveCapriceUseCase.execute(
                name: name.trimmingCharacters(in: .whitespacesAndNewlines),
                price: parsedPrice,
                ownerID: ownerID
            )

            successMessage = "Capricho guardado en la nube."
            name = ""
            price = ""
        } catch {
            errorMessage = "No se pudo guardar el capricho: \(error.localizedDescription)"
        }
    }
}
