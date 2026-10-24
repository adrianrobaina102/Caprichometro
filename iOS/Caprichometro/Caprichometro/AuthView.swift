import SwiftUI
import Foundation


private enum AuthStyle {
    static let background = Color(red: 0.035, green: 0.035, blue: 0.04)
    static let surface = Color(white: 0.07)
    static let border = Color(white: 0.16)
    static let muted = Color(white: 0.57)
    static let lime = Color(red: 0.83, green: 1, blue: 0.36)
}

struct AuthView: View {
    @State private var isRegistering = false
    let viewModel: AuthViewModel

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 0) {
                    TopView()
                        .padding(.top, 48)
                        .padding(.bottom, 26)

                    AuthTabs(isRegistering: $isRegistering)
                        .padding(.bottom, 24)

                    AuthForm(isRegistering: isRegistering, viewModel: viewModel)

                    HStack(spacing: 6) {
                        Text(isRegistering ? "¿Ya tienes cuenta?" : "¿No tienes cuenta?")
                        Button {
                            isRegistering.toggle()
                        } label: {
                            Text(isRegistering ? "Inicia sesión" : "Regístrate")
                                .fontWeight(.semibold)
                                .underline()
                                .foregroundStyle(AuthStyle.lime)
                        }
                    }
                    .font(.subheadline)
                    .padding(.top, 18)

                    AuthSocialAccess {
                        Task { await viewModel.signInWithGoogle() }
                    }
                    .padding(.top, 24)

                    Spacer(minLength: 32)

                    Text("Al continuar aceptas los términos")
                        .font(.caption)
                        .foregroundStyle(AuthStyle.muted)
                        .padding(.bottom, 20)
                }
                .frame(maxWidth: 380)
                .padding(.horizontal, 28)
                .frame(maxWidth: .infinity)
                .frame(minHeight: geometry.size.height)
            }
            .scrollIndicators(.hidden)
            .scrollDismissesKeyboard(.interactively)
        }
        .background(AuthStyle.background.ignoresSafeArea())
        .foregroundStyle(Color(white: 0.96))
        .tint(AuthStyle.lime)
        .buttonStyle(.plain)
        .preferredColorScheme(.dark)
        .disabled(viewModel.isSubmitting)
    }
}

private struct AuthTabs: View {
    @Binding var isRegistering: Bool

    var body: some View {
        HStack(spacing: 0) {
            AuthTab(title: "Iniciar sesión", isSelected: !isRegistering) {
                isRegistering = false
            }
            AuthTab(title: "Registrarse", isSelected: isRegistering) {
                isRegistering = true
            }
        }
        .background(AuthStyle.surface, in: RoundedRectangle(cornerRadius: 16))
    }
}

private struct AuthTab: View {
    let title: LocalizedStringKey
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.body.weight(isSelected ? .semibold : .regular))
                .foregroundStyle(isSelected ? Color.white : AuthStyle.muted)
                .frame(maxWidth: .infinity, minHeight: 48)
                .background(isSelected ? Color(white: 0.105) : .clear,
                            in: RoundedRectangle(cornerRadius: 16))
                .overlay(alignment: .bottom) {
                    Capsule()
                        .fill(isSelected ? AuthStyle.lime : .clear)
                        .frame(height: 3)
                        .padding(.horizontal, 20)
                }
        }
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

private struct AuthForm: View {
    let isRegistering: Bool
    @Bindable var viewModel:  AuthViewModel
    @State private var showsPassword = false
    @FocusState private var focusedField: AuthViewModel.Field?

    var body: some View {
        VStack(spacing: 12) {
            if isRegistering {
                HStack(spacing: 14) {
                    Image(systemName: "person.text.rectangle")
                        .foregroundStyle(AuthStyle.muted)
                        .frame(width: 20)
                        .accessibilityHidden(true)
                    TextField("Nombre real", text: $viewModel.realName,
                              prompt: Text("Nombre real").foregroundStyle(AuthStyle.muted))
                        .textContentType(.name)
                        .textInputAutocapitalization(.words)
                        .submitLabel(.next)
                        .focused($focusedField, equals: .realName)
                        .onSubmit { focusedField = .email }
                        .accessibilityLabel("Nombre real")
                }
                .modifier(AuthFieldStyle())

                AuthBirthdayField(birthday: $viewModel.birthday)
            }

            HStack(spacing: 14) {
                Image(systemName: "person")
                    .foregroundStyle(AuthStyle.muted)
                    .frame(width: 20)
                    .accessibilityHidden(true)
                TextField("Correo electrónico", text: $viewModel.email,
                          prompt: Text("Correo electrónico").foregroundStyle(AuthStyle.muted))
                    .textContentType(.emailAddress)
                    .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .submitLabel(.next)
                    .focused($focusedField, equals: .email)
                    .onSubmit { focusedField = .password }
                    .accessibilityLabel("Correo electrónico")
            }
            .modifier(AuthFieldStyle())

            HStack(spacing: 14) {
                Image(systemName: "lock")
                    .foregroundStyle(AuthStyle.muted)
                    .frame(width: 20)
                    .accessibilityHidden(true)
                Group {
                    if showsPassword {
                        TextField("Contraseña", text: $viewModel.password,
                                  prompt: Text("Contraseña").foregroundStyle(AuthStyle.muted))
                    } else {
                        SecureField("Contraseña", text: $viewModel.password,
                                    prompt: Text("Contraseña").foregroundStyle(AuthStyle.muted))
                    }
                }
                .textContentType(isRegistering ? .newPassword : .password)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .submitLabel(.go)
                .focused($focusedField, equals: .password)
                .onSubmit(submit)
                .accessibilityLabel("Contraseña")

                Button {
                    showsPassword.toggle()
                } label: {
                    Image(systemName: showsPassword ? "eye.slash" : "eye")
                        .foregroundStyle(AuthStyle.muted)
                        .frame(width: 44, height: 44)
                        .contentShape(Rectangle())
                }
                .accessibilityLabel(showsPassword ? "Ocultar contraseña" : "Mostrar contraseña")
                .padding(.trailing, -10)
            }
            .modifier(AuthFieldStyle())

            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
                    .font(.footnote)
                    .foregroundStyle(.red)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }

            Button(action: submit) {
                Text(viewModel.isSubmitting ? "Espera…" : (isRegistering ? "Crear cuenta" : "Entrar"))
                    .font(.headline)
                    .foregroundStyle(.black)
                    .frame(maxWidth: .infinity, minHeight: 52)
                    .background(AuthStyle.lime, in: RoundedRectangle(cornerRadius: 16))
            }
            .padding(.top, 8)
        }
        .onChange(of: isRegistering) {
            viewModel.clearError()
            focusedField = nil
        }
    }

    private func submit() {
        guard !viewModel.isSubmitting else { return }
        guard viewModel.validate(isRegistering: isRegistering) else {
            focusedField = viewModel.invalidField
            return
        }
        focusedField = nil
        Task {
            if isRegistering {
                await viewModel.register()
            } else {
                await viewModel.signIn()
            }
        }
    }
}

private struct AuthFieldStyle: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(.body)
            .padding(.horizontal, 18)
            .frame(minHeight: 56)
            .background(AuthStyle.surface, in: RoundedRectangle(cornerRadius: 16))
            .overlay {
                RoundedRectangle(cornerRadius: 16)
                    .strokeBorder(AuthStyle.border, lineWidth: 1)
            }
    }
}

private struct AuthSocialAccess: View {
    let action: () -> Void

    var body: some View {
        VStack(spacing: 18) {
            HStack(spacing: 16) {
                Rectangle().fill(AuthStyle.border).frame(height: 1)
                Text("o").font(.subheadline).foregroundStyle(AuthStyle.muted)
                Rectangle().fill(AuthStyle.border).frame(height: 1)
            }
            AuthProviderButton(action: action)
        }
    }
}

private struct AuthProviderButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Image("GoogleLogo")
                    .renderingMode(.original)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 20, height: 20)
                    .accessibilityHidden(true)
                Text("Continuar con Google")
                    .font(.body.weight(.semibold))
            }
            .foregroundStyle(Color.white)
            .frame(maxWidth: .infinity, minHeight: 52)
            .background(AuthStyle.surface,
                        in: RoundedRectangle(cornerRadius: 16))
            .overlay {
                RoundedRectangle(cornerRadius: 16)
                    .strokeBorder(AuthStyle.border, lineWidth: 1)
            }
        }
    }
}

private struct AuthBirthdayField: View {
    @Binding var birthday: Date?
    @State private var showsPicker = false
    @State private var selectedDate = Date()

    var body: some View {
        Button {
            selectedDate = birthday ?? Date()
            showsPicker = true
        } label: {
            HStack(spacing: 14) {
                Image(systemName: "calendar")
                    .frame(width: 20)
                    .accessibilityHidden(true)
                Text("Cumpleaños")
                Spacer(minLength: 4)
                if let birthday {
                    Text(birthday, format: .dateTime.day().month().year())
                        .foregroundStyle(.white)
                } else {
                    Text("Seleccionar")
                        .font(.subheadline)
                }
            }
            .foregroundStyle(AuthStyle.muted)
            .modifier(AuthFieldStyle())
        }
        .sheet(isPresented: $showsPicker) {
            NavigationStack {
                DatePicker("Fecha de nacimiento", selection: $selectedDate,
                           in: ...Date(), displayedComponents: .date)
                    .datePickerStyle(.wheel)
                    .labelsHidden()
                    .padding()
                    .navigationTitle("Cumpleaños")
                    .navigationBarTitleDisplayMode(.inline)
                    .toolbar {
                        ToolbarItem(placement: .cancellationAction) {
                            Button("Cancelar") { showsPicker = false }
                        }
                        ToolbarItem(placement: .confirmationAction) {
                            Button("Guardar") {
                                birthday = selectedDate
                                showsPicker = false
                            }
                        }
                    }
            }
            .presentationDetents([.medium])
        }
    }
}

struct TopView: View {
    var body: some View {
        VStack(spacing: 12) {
            CaprichometroMark()
                .frame(width: 112, height: 96)
                .padding(.bottom, 16)
                .accessibilityHidden(true)

            Text("Caprichómetro")
                .font(.largeTitle.weight(.semibold))

            Text("Tus caprichos, tus dudas y tus planes\nen 5 minutos")
                .font(.body)
                .foregroundStyle(AuthStyle.muted)
                .lineSpacing(5)
        }
        .multilineTextAlignment(.center)
    }
}

private struct CaprichometroMark: View {
    var body: some View {
        Canvas { context, size in
            let center = CGPoint(x: size.width / 2, y: size.height * 0.77)
            let radius = size.width * 0.4
            var track = Path()
            track.addArc(center: center, radius: radius,
                         startAngle: .degrees(166), endAngle: .degrees(374), clockwise: false)
            context.stroke(track, with: .color(Color(white: 0.15)),
                           style: StrokeStyle(lineWidth: 15, lineCap: .round))

            var progress = Path()
            progress.addArc(center: center, radius: radius,
                            startAngle: .degrees(166), endAngle: .degrees(260), clockwise: false)
            context.stroke(progress, with: .color(AuthStyle.lime),
                           style: StrokeStyle(lineWidth: 15, lineCap: .round))

            for angle in [-110.0, -90.0, -70.0] {
                let radians = angle * .pi / 180
                var ray = Path()
                ray.move(to: CGPoint(x: center.x + cos(radians) * (radius + 16),
                                     y: center.y + sin(radians) * (radius + 16)))
                ray.addLine(to: CGPoint(x: center.x + cos(radians) * (radius + 24),
                                       y: center.y + sin(radians) * (radius + 24)))
                context.stroke(ray, with: .color(AuthStyle.lime),
                               style: StrokeStyle(lineWidth: 4, lineCap: .round))
            }
        }
        .overlay(alignment: .bottom) {
            Image(systemName: "heart.fill")
                .font(.system(size: 29))
                .foregroundStyle(.white)
                .padding(.bottom, 6)
        }
    }
}



#Preview {
    AuthView(viewModel: AuthComposition.makePreviewViewModel())
}
