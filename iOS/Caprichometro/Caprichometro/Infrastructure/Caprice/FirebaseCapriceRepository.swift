//
//  FirebaseCapriceRepository.swift
//  Caprichometro
//
//  Created by Danii on 24/10/2026.
//

import FirebaseFirestore

@MainActor
final class FirestoreCapriceRepository: CapriceRepository {
    private let database: Firestore

    init(database: Firestore) {
        self.database = database
    }

    func save(_ caprice: Caprice) async throws -> String {
        let document = try await database
            .collection("caprices")
            .addDocument(data: [
                "name": caprice.name,
                "price": caprice.price,
                "isCaprice": caprice.isCaprice,
                "timestamp": caprice.timestamp,
                "ownerId": caprice.ownerID
            ])

        return document.documentID
    }
}
