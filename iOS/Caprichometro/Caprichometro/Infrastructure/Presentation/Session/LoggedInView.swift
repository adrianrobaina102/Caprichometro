import SwiftUI

struct LoggedInView: View {
    @State private var viewModel = CapriceComposition.makeViewModel()

    var body: some View {
        NavigationStack {
            CreateCapriceView(viewModel: viewModel)
        }
    }
}

#Preview {
    LoggedInView()
}
