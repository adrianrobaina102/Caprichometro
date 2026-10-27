//
//  UsernameRulesTests.swift
//  Caprichometro
//
//  Created by Danii on 27/10/2026.
//


import XCTest
@testable import Caprichometro

@MainActor
final class UsernameRulesTests: XCTestCase {

    func testNormalizeTrimsAtAndLowercasesUsername() {
        let result = UsernameRules.normalize("  @Dani_123  ")

        XCTAssertEqual(result, "dani_123")
    }

    func testIsValidAcceptsUsernameBoundaries() {
        XCTAssertTrue(UsernameRules.isValid("abc"))
        XCTAssertTrue(UsernameRules.isValid("abcdefghijklmnopqrst"))
        XCTAssertTrue(UsernameRules.isValid("@Dani_123"))
    }

    func testIsValidRejectsInvalidLengthsAndCharacters() {
        XCTAssertFalse(UsernameRules.isValid("ab"))
        XCTAssertFalse(UsernameRules.isValid("abcdefghijklmnopqrstu"))
        XCTAssertFalse(UsernameRules.isValid("dani.luzardo"))
        XCTAssertFalse(UsernameRules.isValid("dani luzardo"))
    }

    func testIsValidAcceptsLettersNumbersAndUnderscores() {
        XCTAssertTrue(UsernameRules.isValid("user123"))
        XCTAssertTrue(UsernameRules.isValid("un_nombre_largo"))
    }

    func testIsValidRejectsAtSignInsideUsername() {
        XCTAssertFalse(UsernameRules.isValid("adrian@dev"))
    }
}
