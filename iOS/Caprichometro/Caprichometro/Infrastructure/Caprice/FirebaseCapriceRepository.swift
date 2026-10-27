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
                "userId": caprice.userID,
                "type": caprice.type,
                "question": caprice.question,
                "answer1": caprice.answer1,
                "answer2": caprice.answer2,
                "timestamp": caprice.timestamp
            ])

        return document.documentID
    }
}
