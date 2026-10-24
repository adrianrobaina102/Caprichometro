//
//  AppDelegate.swift
//  Caprichometro
//
//  Created by Danii on 23/10/2026.
//


import UIKit
import Firebase
import FirebaseAuth
import FirebaseCore
class AppDelegate: UIResponder, UIApplicationDelegate {

  var window: UIWindow?

  func application(_ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions:
    [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    FirebaseApp.configure()
    return true
  }
}
