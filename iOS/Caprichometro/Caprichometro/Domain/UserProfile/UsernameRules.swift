//
//  UsernameRules.swift
//  Caprichometro
//
//  Created by Danii on 25/10/2026.
//

import Foundation

enum UsernameRules {
    static func normalize(_ rawValue: String) -> String {
        var value = rawValue.trimmingCharacters(in: .whitespacesAndNewlines)

        if value.hasPrefix("@") {
            value.removeFirst()
        }

        return value.lowercased()
    }

    static func isValid(_ rawValue: String) -> Bool {
        let username = normalize(rawValue)

        return username.range(
            of: "^[a-z0-9_]{3,20}$",
            options: .regularExpression
        ) != nil
    }
}
