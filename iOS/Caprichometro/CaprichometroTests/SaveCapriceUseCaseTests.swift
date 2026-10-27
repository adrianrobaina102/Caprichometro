import XCTest
@testable import Caprichometro

@MainActor
final class SaveCapriceUseCaseTests: XCTestCase {
    func testSaveCapriceStoresExpectedValuesAndReturnsDocumentID() async throws {
        let repository = TestCapriceRepository()
        repository.documentID = "firebase-doc-id-123"
        let useCase = SaveCapriceUseCase(repository: repository)
        let startedAt = Int64(Date().timeIntervalSince1970 * 1_000)

        let returnedID = try await useCase.execute(
            name: "Zapatos",
            price: 79.99,
            ownerID: "user-123"
        )

        let finishedAt = Int64(Date().timeIntervalSince1970 * 1_000)
        XCTAssertEqual(returnedID, "firebase-doc-id-123")
        XCTAssertEqual(repository.savedCaprices.count, 1)

        let savedCaprice = try XCTUnwrap(repository.savedCaprices.first)
        XCTAssertEqual(savedCaprice.name, "Zapatos")
        XCTAssertEqual(savedCaprice.price, 79.99, accuracy: 0.001)
        XCTAssertEqual(savedCaprice.ownerID, "user-123")
        XCTAssertFalse(savedCaprice.isCaprice)
        XCTAssertGreaterThanOrEqual(savedCaprice.timestamp, startedAt)
        XCTAssertLessThanOrEqual(savedCaprice.timestamp, finishedAt)
    }
}
