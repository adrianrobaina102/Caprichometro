import SwiftUI

struct LoggedInView: View {
    @State private var viewModel = CapriceComposition.makeViewModel()
    @State private var searchViewModel =
        UserProfileComposition.makeSearchUserViewModel()

    var body: some View {
        NavigationStack {
            CreateCapriceView(viewModel: viewModel)
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        NavigationLink {
                            SearchUserView(viewModel: searchViewModel)
                        } label: {
                            Label("Buscar amigos", systemImage: "person.2")
                        }
                    }
                }
        }
    }
}

#Preview {
    LoggedInView()
}
