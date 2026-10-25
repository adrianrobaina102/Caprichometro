//
//  SearchUserView.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

import SwiftUI

struct SearchUserView: View {
    @Bindable var viewModel: SearchUserViewModel
    @State private var friendRequestsViewModel =
        UserProfileComposition.makeFriendRequestsViewModel()

    var body: some View {
        List {
            Section {
                searchBar

                if let errorMessage = viewModel.errorMessage {
                    Text(errorMessage)
                        .foregroundStyle(.red)
                }

                if viewModel.hasSearched {
                    if let result = viewModel.result {
                        searchResult(result)
                    } else {
                        Text("No encontramos ese usuario.")
                            .foregroundStyle(.secondary)
                    }
                }
            }

            Section {
                if let errorMessage = friendRequestsViewModel.errorMessage {
                    Text(errorMessage)
                        .foregroundStyle(.red)
                }

                if friendRequestsViewModel.isLoading,
                   friendRequestsViewModel.requests.isEmpty {
                    ProgressView("Cargando solicitudes…")
                } else if friendRequestsViewModel.requests.isEmpty {
                    Text("No tienes solicitudes pendientes.")
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(friendRequestsViewModel.requests) { request in
                        friendRequestRow(request)
                    }
                }
            } header: {
                HStack {
                    Text("Solicitudes")

                    Spacer()

                    if !friendRequestsViewModel.requests.isEmpty {
                        NavigationLink("Ver todas") {
                            FriendRequestsView(
                                viewModel: friendRequestsViewModel
                            )
                        }
                    }
                }
            }

            Section("Amigos") {
                if friendRequestsViewModel.isLoading,
                   friendRequestsViewModel.friends.isEmpty {
                    ProgressView("Cargando amigos…")
                } else if friendRequestsViewModel.friends.isEmpty {
                    Text("Tus amigos aparecerán aquí.")
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(friendRequestsViewModel.friends) { friend in
                        HStack(spacing: 12) {
                            Image(systemName: "person.crop.circle.fill")
                                .font(.system(size: 42))
                                .foregroundStyle(.secondary)

                            Text("@\(friend.username)")
                                .font(.headline)
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
        }
        .listStyle(.insetGrouped)
        .scrollContentBackground(.hidden)
        .background(Color.black.ignoresSafeArea())
        .navigationTitle("Amigos")
        .navigationBarTitleDisplayMode(.inline)
        .preferredColorScheme(.dark)
        .task {
            await friendRequestsViewModel.load()
        }
        .refreshable {
            await friendRequestsViewModel.load()
        }
    }

    private var searchBar: some View {
        HStack(spacing: 10) {
            Image(systemName: "magnifyingglass")
                .foregroundStyle(.secondary)

            Text("@")
                .foregroundStyle(.secondary)

            TextField("Agrega o busca amigos", text: $viewModel.username)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .textContentType(.username)
                .submitLabel(.search)
                .onSubmit {
                    Task {
                        await viewModel.search()
                    }
                }

            if viewModel.isSearching {
                ProgressView()
                    .controlSize(.small)
            } else {
                Button {
                    Task {
                        await viewModel.search()
                    }
                } label: {
                    Image(systemName: "arrow.right.circle.fill")
                        .font(.title3)
                }
                .disabled(!viewModel.canSearch)
                .accessibilityLabel("Buscar usuario")
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(Color.white.opacity(0.09), in: Capsule())
        .listRowBackground(Color.clear)
        .listRowInsets(EdgeInsets(top: 8, leading: 0, bottom: 12, trailing: 0))
    }

    @ViewBuilder
    private func searchResult(_ result: UserProfile) -> some View {
        HStack(spacing: 12) {
            Image(systemName: "person.crop.circle.fill")
                .font(.system(size: 42))
                .foregroundStyle(.secondary)

            VStack(alignment: .leading, spacing: 4) {
                Text("@\(result.username)")
                    .font(.headline)

                if let message = viewModel.friendRequestMessage {
                    Text(message)
                        .font(.subheadline)
                        .foregroundStyle(
                            viewModel.friendRequestMessageIsError
                                ? Color.red
                                : Color.green
                        )
                }
            }

            Spacer()

            if viewModel.isCurrentUserResult {
                Text("Tú")
                    .foregroundStyle(.secondary)
            } else {
                Button {
                    Task {
                        await viewModel.sendFriendRequest()
                    }
                } label: {
                    if viewModel.isSendingFriendRequest {
                        ProgressView()
                    } else if viewModel.hasSentFriendRequest {
                        Label("Enviada", systemImage: "checkmark")
                    } else if viewModel.requestAlreadyExists {
                        Label("Existente", systemImage: "person.2")
                    } else {
                        Text("Añadir")
                    }
                }
                .buttonStyle(.bordered)
                .disabled(!viewModel.canSendFriendRequest)
            }
        }
        .padding(.vertical, 4)
    }

    private func friendRequestRow(
        _ request: IncomingFriendRequest
    ) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 12) {
                Image(systemName: "person.crop.circle.fill")
                    .font(.system(size: 42))
                    .foregroundStyle(.secondary)

                VStack(alignment: .leading, spacing: 4) {
                    Text("@\(request.sender.username)")
                        .font(.headline)

                    Text("Quiere ser tu amigo.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }

            HStack {
                Button("Aceptar") {
                    Task {
                        await friendRequestsViewModel.respond(
                            to: request,
                            with: .accept
                        )
                    }
                }
                .buttonStyle(.borderedProminent)

                Button("Rechazar", role: .destructive) {
                    Task {
                        await friendRequestsViewModel.respond(
                            to: request,
                            with: .decline
                        )
                    }
                }
                .buttonStyle(.bordered)
            }
            .disabled(friendRequestsViewModel.processingRequestID != nil)
        }
        .padding(.vertical, 6)
    }
}
