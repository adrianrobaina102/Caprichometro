//
//  FriendRequestsViewModel.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//


import Foundation
import Observation

@MainActor
@Observable
final class FriendRequestsViewModel {
    private(set) var requests: [IncomingFriendRequest] = []
    private(set) var friends: [UserProfile] = []
    private(set) var isLoading = false
    private(set) var processingRequestID: String?
    private(set) var errorMessage: String?

    private let loadUseCase: LoadIncomingFriendRequestsUseCase
    private let respondUseCase: RespondToFriendRequestUseCase
    private let currentUserIDProvider: @MainActor () -> String?

    init(
        loadUseCase: LoadIncomingFriendRequestsUseCase,
        respondUseCase: RespondToFriendRequestUseCase,
        currentUserIDProvider: @escaping @MainActor () -> String?
    ) {
        self.loadUseCase = loadUseCase
        self.respondUseCase = respondUseCase
        self.currentUserIDProvider = currentUserIDProvider
    }

    func load() async {
        guard !isLoading else { return }

        guard let userID = currentUserIDProvider() else {
            errorMessage = "Inicia sesión para ver tus solicitudes."
            return
        }

        isLoading = true
        errorMessage = nil
        defer { isLoading = false }

        do {
            requests = try await loadUseCase.execute(for: userID)
        } catch {
            errorMessage = "No se pudieron cargar las solicitudes."
        }

        do {
            friends = try await loadUseCase.friends(for: userID)
        } catch {
            if errorMessage == nil {
                errorMessage = "No se pudieron cargar los amigos."
            }
        }
    }

    func respond(
        to request: IncomingFriendRequest,
        with decision: FriendRequestDecision
    ) async {
        guard processingRequestID == nil else { return }

        guard let userID = currentUserIDProvider() else {
            errorMessage = "Inicia sesión para responder."
            return
        }

        errorMessage = nil
        processingRequestID = request.id
        defer { processingRequestID = nil }

        do {
            try await respondUseCase.execute(
                friendshipID: request.friendshipID,
                userID: userID,
                decision: decision
            )

            requests.removeAll { $0.id == request.id }

            if case .accept = decision,
               !friends.contains(where: { $0.id == request.sender.id }) {
                friends.append(request.sender)
                friends.sort {
                    $0.username.localizedCaseInsensitiveCompare($1.username)
                        == .orderedAscending
                }
            }
        } catch FriendshipRepositoryError.friendshipNotFound {
            errorMessage = "La solicitud ya no existe. Actualiza la bandeja."
        } catch FriendshipRepositoryError.cannotRespondToFriendRequest {
            errorMessage = "La solicitud ya no está pendiente. Actualiza la bandeja."
        } catch {
            errorMessage = "No se pudo responder a la solicitud."
        }
    }
}
