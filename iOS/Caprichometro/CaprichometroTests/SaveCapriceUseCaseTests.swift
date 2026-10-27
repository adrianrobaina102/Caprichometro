import XCTest
@testable import Caprichometro

@MainActor
final class SaveCapriceUseCaseTests: XCTestCase {
    func testSaveCapriceStoresQuestionAndOptionsAndReturnsDocumentID() async throws {
        let repository = TestCapriceRepository()
        repository.documentID = "firebase-doc-id-123"

        let useCase = SaveCapriceUseCase(repository: repository)
        let startedAt = Int64(Date().timeIntervalSince1970 * 1_000)

        let returnedID = try await useCase.execute(
            type: "Compra",
            question: "¿Me compro estos zapatos?",
            answer1: "Sí, me los compro",
            answer2: "No, mejor ahorro",
            userID: "user-123"
        )

        let finishedAt = Int64(Date().timeIntervalSince1970 * 1_000)

        XCTAssertEqual(returnedID, "firebase-doc-id-123")
        XCTAssertEqual(repository.savedCaprices.count, 1)

        let savedCaprice = try XCTUnwrap(repository.savedCaprices.first)

        XCTAssertEqual(savedCaprice.userID, "user-123")
        XCTAssertEqual(savedCaprice.type, "Compra")
        XCTAssertEqual(savedCaprice.question, "¿Me compro estos zapatos?")
        XCTAssertEqual(savedCaprice.answer1, "Sí, me los compro")
        XCTAssertEqual(savedCaprice.answer2, "No, mejor ahorro")
        XCTAssertGreaterThanOrEqual(savedCaprice.timestamp, startedAt)
        XCTAssertLessThanOrEqual(savedCaprice.timestamp, finishedAt)
    }
}
