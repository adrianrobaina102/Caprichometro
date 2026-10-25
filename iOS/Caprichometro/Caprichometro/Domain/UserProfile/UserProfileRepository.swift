//
//  UserProfileRepository.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

enum UserProfileRepositoryError: Error {
    case usernameAlreadyTaken
    case invalidUserProfileData
}

@MainActor
protocol UserProfileRepository: AnyObject {
    func isUsernameAvailable(_ username: String) async throws -> Bool
    func findByUsername(_ username: String) async throws -> UserProfile?
    func save(_ profile: UserProfile) async throws
    func findByID(_ userID: String) async throws -> UserProfile?
}
