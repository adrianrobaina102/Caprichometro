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
        name: String,
        price: Double,
        ownerID: String
    ) async throws -> String {
        let caprice = Caprice(
            name: name,
            price: price,
            isCaprice: false,
            timestamp: Int64(Date().timeIntervalSince1970 * 1000),
            ownerID: ownerID
        )

        return try await repository.save(caprice)
    }
}
