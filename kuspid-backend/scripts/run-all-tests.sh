#!/bin/bash
# Run all tests for Kuspid Backend Microservices

echo "=========================================="
echo "  Kuspid Backend - Test Suite Runner"
echo "=========================================="

# Java Services (Maven)
echo ""
echo "🧪 Running Java Service Tests..."
echo "------------------------------------------"

cd "$(dirname "$0")/.."

echo "Testing Auth Service..."
cd services/auth-service && ./mvnw test -q
AUTH_RESULT=$?

echo "Testing Beat Service..."
cd ../beat-service && ./mvnw test -q
BEAT_RESULT=$?

echo "Testing Artist Service..."
cd ../artist-service && ./mvnw test -q
ARTIST_RESULT=$?

echo "Testing Analytics Service..."
cd ../analytics-service && ./mvnw test -q
ANALYTICS_RESULT=$?

echo "Testing Gateway..."
cd ../../gateway && ./mvnw test -q
GATEWAY_RESULT=$?



# Summary
echo ""
echo "=========================================="
echo "  Test Results Summary"
echo "=========================================="

print_result() {
    if [ $2 -eq 0 ]; then
        echo "✅ $1: PASSED"
    else
        echo "❌ $1: FAILED"
    fi
}

print_result "Auth Service" $AUTH_RESULT
print_result "Beat Service" $BEAT_RESULT
print_result "Artist Service" $ARTIST_RESULT
print_result "Analytics Service" $ANALYTICS_RESULT
print_result "Gateway" $GATEWAY_RESULT


# Exit with failure if any test failed
if [ $AUTH_RESULT -ne 0 ] || [ $BEAT_RESULT -ne 0 ] || [ $ARTIST_RESULT -ne 0 ] || \
   [ $ANALYTICS_RESULT -ne 0 ] || [ $GATEWAY_RESULT -ne 0 ]; then
    echo ""
    echo "❌ Some tests failed!"
    exit 1
else
    echo ""
    echo "✅ All tests passed!"
    exit 0
fi
