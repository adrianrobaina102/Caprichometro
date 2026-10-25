import SwiftUI

struct ContentView: View {
    let viewModel: AuthViewModel

    var body: some View {
        @Bindable var viewModel = viewModel

        Group {
            if viewModel.isCheckingSession {
                ProgressView("Comprobando sesión…")
            } else if viewModel.isLoggedIn {
                VStack {
                    LoggedInView()

                    Button("Cerrar sesión", action: viewModel.signOut)
                        .padding()
                }
            } else {
                AuthView(viewModel: viewModel)
            }
        }
        .onAppear(perform: viewModel.startObservingSession)
        .onDisappear(perform: viewModel.stopObservingSession)
        .alert("Error al cerrar sesión", isPresented: $viewModel.showsSignOutError) {
            Button("Entendido", role: .cancel) {
                viewModel.showsSignOutError = false
            }
        } message: {
            Text(viewModel.signOutError ?? "")
        }
    }
}

#Preview {
    ContentView(viewModel: AuthComposition.makePreviewViewModel())
}
