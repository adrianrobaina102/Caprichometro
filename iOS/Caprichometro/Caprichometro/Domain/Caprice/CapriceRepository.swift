//
//  CapriceRepository.swift
//  Caprichometro
//
//  Created by Danii on 24/10/2026.
//

@MainActor
protocol CapriceRepository: AnyObject {
    func save(_ caprice: Caprice) async throws -> String
}
