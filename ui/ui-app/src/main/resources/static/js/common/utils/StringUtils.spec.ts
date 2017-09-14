import "./StringUtils";

describe("StringUtils", function () {
    // quote
    it("should quote strings", function () {
        expect(StringUtils.quote("test")).toBe("test");
        expect(StringUtils.quote("\"test\"")).toBe("\\\"test\\\"");
    });

    // quoteSql
    it("should quote SQL identifiers", function () {
        expect(StringUtils.quoteSql("test")).toBe("test");
        expect(StringUtils.quoteSql("test`s")).toBe("test``s");
    });
});
