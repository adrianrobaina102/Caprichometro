//
//  SearchUserViewModel.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

import Foundation
import Observation

@MainActor
@Observable
final class SearchUserViewModel {
    var username = ""

    private(set) var isSearching = false
    private(set) var result: UserProfile?
    private(set) var hasSearched = false
    private(set) var errorMessage: String?
    private(set) var isSendingFriendRequest = false
    private(set) var hasSentFriendRequest = false
    private(set) var requestAlreadyExists = false
    private(set) var friendRequestMessage: String?
    private(set) var friendRequestMessageIsError = false

    private let sendFriendRequestUseCase: SendFriendRequestUseCase
    private let currentUserIDProvider: @MainActor () -> String?

    private let searchUserByUsernameUseCase: SearchUserByUsernameUseCase

    init(
        searchUserByUsernameUseCase: SearchUserByUsernameUseCase,
        sendFriendRequestUseCase: SendFriendRequestUseCase,
        currentUserIDProvider: @escaping @MainActor () -> String?
    ) {
        self.searchUserByUsernameUseCase = searchUserByUsernameUseCase
        self.sendFriendRequestUseCase = sendFriendRequestUseCase
        self.currentUserIDProvider = currentUserIDProvider
    }

    var canSearch: Bool {
        !username.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            && !isSearching
    }

    func search() async {
        guard canSearch else { return }
        hasSentFriendRequest = false
        requestAlreadyExists = false
        friendRequestMessage = nil
        friendRequestMessageIsError = false
        result = nil
        hasSearched = false
        errorMessage = nil
        isSearching = true
        defer { isSearching = false }

        do {
            result = try await searchUserByUsernameUseCase.execute(
                username: username
            )
            hasSearched = true
        } catch SearchUserByUsernameError.invalidUsername {
            errorMessage =
                "Usa de 3 a 20 letras, números o guiones bajos."
        } catch {
            errorMessage = "No se pudo buscar el usuario. Inténtalo de nuevo."
        }
    }
    var isCurrentUserResult: Bool {
        guard let result,
              let currentUserID = currentUserIDProvider() else {
            return false
        }

        return result.id == currentUserID
    }

    var canSendFriendRequest: Bool {
        guard let result,
              let currentUserID = currentUserIDProvider() else {
            return false
        }

        return result.id != currentUserID
            && !isSearching
            && !isSendingFriendRequest
            && !hasSentFriendRequest
            && !requestAlreadyExists
    }

    func sendFriendRequest() async {
        guard canSendFriendRequest,
              let recipient = result,
              let requesterID = currentUserIDProvider() else {
            return
        }

        friendRequestMessage = nil
        friendRequestMessageIsError = false
        isSendingFriendRequest = true
        defer { isSendingFriendRequest = false }

        do {
            try await sendFriendRequestUseCase.execute(
                requesterID: requesterID,
                recipientID: recipient.id
            )

            hasSentFriendRequest = true
            friendRequestMessage = "Solicitud enviada a @\(recipient.username)."
        } catch FriendshipRepositoryError.relationshipAlreadyExists {
            requestAlreadyExists = true
            friendRequestMessage = "Ya existe una solicitud o ya sois amigos."
            friendRequestMessageIsError = true
        } catch {
            friendRequestMessage = "No se pudo enviar la solicitud. Inténtalo de nuevo."
            friendRequestMessageIsError = true
        }
    }
}
