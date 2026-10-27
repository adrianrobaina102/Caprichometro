//
//  CapriceUseCase.swift
//  Caprichometro
//
//  Created by Danii on 24/10/2026.
//

import Foundation

@MainActor
struct SaveCapriceUseCase {
    private let repository: any CapriceRepository

    init(repository: any CapriceRepository) {
        self.repository = repository
    }

    func execute(
        type: String,
        question: String,
        answer1: String,
        answer2: String,
        userID: String
    ) async throws -> String {
        let caprice = Caprice(
            userID: userID,
            type: type,
            question: question,
            answer1: answer1,
            answer2: answer2,
            timestamp: Int64(Date().timeIntervalSince1970 * 1_000)
        )

        return try await repository.save(caprice)
    }
}
