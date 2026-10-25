//
//  FriendRequestsView.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//


import SwiftUI

struct FriendRequestsView: View {
    @Bindable var viewModel: FriendRequestsViewModel

    var body: some View {
        List {
            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
            }

            if viewModel.isLoading && viewModel.requests.isEmpty {
                ProgressView("Cargando solicitudes…")
            } else if viewModel.requests.isEmpty {
                Text("No tienes solicitudes pendientes.")
                    .foregroundStyle(.secondary)
            } else {
                ForEach(viewModel.requests) { request in
                    VStack(alignment: .leading, spacing: 12) {
                        Label(
                            "@\(request.sender.username)",
                            systemImage: "person.crop.circle"
                        )

                        Text("Quiere ser tu amigo.")
                            .foregroundStyle(.secondary)

                        HStack {
                            Button("Aceptar") {
                                Task {
                                    await viewModel.respond(
                                        to: request,
                                        with: .accept
                                    )
                                }
                            }

                            Button("Rechazar", role: .destructive) {
                                Task {
                                    await viewModel.respond(
                                        to: request,
                                        with: .decline
                                    )
                                }
                            }
                        }
                        .disabled(viewModel.processingRequestID != nil)
                    }
                    .padding(.vertical, 8)
                }
            }
        }
        .navigationTitle("Solicitudes")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    Task {
                        await viewModel.load()
                    }
                } label: {
                    Image(systemName: "arrow.clockwise")
                }
                .accessibilityLabel("Actualizar solicitudes")
            }
        }
        .task {
            await viewModel.load()
        }
        .refreshable {
            await viewModel.load()
        }
    }
}
