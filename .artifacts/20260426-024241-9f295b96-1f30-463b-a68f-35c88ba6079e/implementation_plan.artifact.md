# Handle Google URL Redirects in Gmail

Gmail wraps links in a `google.com/url` redirector, which interferes with the `urlscan` API. This task aims to unwrap the original URL from these redirectors.

## User Review Required
- None.

## Proposed Changes

### URL Unwrapping Utility

Create a utility to detect and unwrap Google redirect URLs.

#### [NEW] [UrlUnwrapper.kt](file:///C:/Users/Dragos/StudioProjects/polihackplm2/app/src/main/java/com/example/polihackplm2/functionality/UrlUnwrapper.kt)

- Detects hosts matching `*.google.*` with path `/url`.
- Extracts the `q` or `url` query parameters.

### MainActivity Integration

#### [MainActivity.kt](file:///C:/Users/Dragos/StudioProjects/polihackplm2/app/src/main/java/com/example/polihackplm2/MainActivity.kt)

- Use `UrlUnwrapper` in `handleIntent` to normalize the URL before passing it to the ViewModel.

## Verification Plan

### Automated Tests
- Create a unit test `UrlUnwrapperTest.kt` to verify the unwrapping logic with various URL formats.
    - Run with: `./gradlew :app:testDebugUnitTest --tests "com.example.polihackplm2.functionality.UrlUnwrapperTest"`

### Manual Verification
- Test with sample Google redirect URLs:
    - `https://www.google.com/url?q=https://example.com/&sa=D&source=hangouts&ust=1234567890&usg=AFQjCN...`
    - `https://google.ro/url?q=https://google.com/`
