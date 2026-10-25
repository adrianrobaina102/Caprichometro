//
//  UserProfileComposition.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

import FirebaseAuth

@MainActor
enum UserProfileComposition {
    static func makeSearchUserViewModel() -> SearchUserViewModel {
        let profileRepository = FirestoreUserProfileRepository()
        let searchUseCase = SearchUserByUsernameUseCase(
            userProfileRepository: profileRepository
        )

        let friendshipRepository = FirestoreFriendshipRepository()
        let sendRequestUseCase = SendFriendRequestUseCase(
            friendshipRepository: friendshipRepository
        )

        let auth = Auth.auth()

        return SearchUserViewModel(
            searchUserByUsernameUseCase: searchUseCase,
            sendFriendRequestUseCase: sendRequestUseCase,
            currentUserIDProvider: {
                auth.currentUser?.uid
            }
        )
    }
    static func makeFriendRequestsViewModel() -> FriendRequestsViewModel {
        let friendshipRepository = FirestoreFriendshipRepository()
        let profileRepository = FirestoreUserProfileRepository()

        let loadUseCase = LoadIncomingFriendRequestsUseCase(
            friendshipRepository: friendshipRepository,
            userProfileRepository: profileRepository
        )

        let respondUseCase = RespondToFriendRequestUseCase(
            friendshipRepository: friendshipRepository
        )

        let auth = Auth.auth()

        return FriendRequestsViewModel(
            loadUseCase: loadUseCase,
            respondUseCase: respondUseCase,
            currentUserIDProvider: {
                auth.currentUser?.uid
            }
        )
    }
}
