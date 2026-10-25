//
//  CapriceComposition.swift
//  Caprichometro
//
//  Created by Danii on 24/10/2026.
//

import FirebaseAuth
import FirebaseFirestore

@MainActor
enum CapriceComposition {
    static func makeViewModel() -> CreateCapriceViewModel {
        let auth = Auth.auth()
        let database = Firestore.firestore()

        let repository = FirestoreCapriceRepository(database: database)
        let useCase = SaveCapriceUseCase(repository: repository)

        return CreateCapriceViewModel(
            saveCapriceUseCase: useCase,
            currentUserIDProvider: {
                auth.currentUser?.uid
            }
        )
    }
}
