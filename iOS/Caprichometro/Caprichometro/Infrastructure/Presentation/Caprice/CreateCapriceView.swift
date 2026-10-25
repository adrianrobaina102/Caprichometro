//
//  CreateCapriceView.swift
//  Caprichometro
//
//  Created by Danii on 24/10/2026.
//

import SwiftUI

struct CreateCapriceView: View {
    @Bindable var viewModel: CreateCapriceViewModel

    var body: some View {
        Form {
            Section("Tu capricho") {
                TextField("¿Qué te quieres comprar?", text: $viewModel.name)

                TextField("Precio (€)", text: $viewModel.price)
                    .keyboardType(.decimalPad)
            }

            Button {
                Task {
                    await viewModel.save()
                }
            } label: {
                if viewModel.isSaving {
                    ProgressView()
                } else {
                    Text("Guardar capricho")
                }
            }
            .disabled(!viewModel.canSave)

            if let successMessage = viewModel.successMessage {
                Text(successMessage)
                    .foregroundStyle(.green)
            }

            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
            }
        }
        .navigationTitle("Nuevo capricho")
    }
}
