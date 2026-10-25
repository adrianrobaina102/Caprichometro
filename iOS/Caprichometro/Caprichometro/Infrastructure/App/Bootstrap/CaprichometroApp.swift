//
//  CaprichometroApp.swift
//  Caprichometro
//
//  Created by Danii on 23/10/2026.
//

import SwiftUI
import GoogleSignIn

@main

struct CaprichometroApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    @State private var authViewModel = AuthComposition.makeViewModel()

    var body: some Scene {
        WindowGroup {
            ContentView(viewModel: authViewModel)
                .onOpenURL { url in
                    _ = GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
