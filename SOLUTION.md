# How I built Local Table

I kept this app focused on the assessment: browse restaurants, search, filter by Open now, open Details and handle loading or failed requests properly. I added persistent favorites as the extra improvement.

Room keeps the last successful list available after a failed refresh. Favorites have their own table so refreshing restaurant data does not overwrite them.

## Structure

The app uses MVVM. Compose displays the screen state, ViewModels handle actions, and the repository coordinates the mock API and Room.

```text
Compose UI
    ↓ actions                      ↑ screen state
ViewModel
    ↓ repository calls             ↑ Flow
RestaurantRepository
    ↓ fetch                        ↓ save / ↑ observe
RestaurantApi                 Room database
```

Room is the source of truth. The UI gets its restaurant list from Room, including after a successful refresh. That gives Browse and Details the same data path for fresh content, cached content and favorites.

I used one Gradle app module with separate `data`, `domain`, `ui`, `navigation` and `di` packages. For this size of project, that keeps the setup manageable while making the responsibilities easy to find.

## Why I used a JSON asset

I used `restaurants.json` with 200 fictional restaurant branches around Johannesburg. There is no backend to start or API key to provide.

The brief explicitly permits local assets as the JSON source and as an offline baseline. Room adds a persistent cache and lets the app demonstrate a failed refresh without losing the last successful list.

`MockRestaurantApi` reads the file through the `RestaurantApi` interface. It parses the JSON on `Dispatchers.IO` and adds a 900 ms delay so loading is visible during the demo.

Keeping the interface means I could add a real HTTP implementation later without moving data-loading code into the screens. Retrofit isn't included in this version.

The remote images are sample food photos. Coil handles loading and caching, with a restaurant icon as the fallback. The restaurant data itself is available without internet.

## What happens during refresh

1. Browse observes the saved restaurants from Room.
2. The ViewModel asks the repository to refresh.
3. The repository fetches through `RestaurantApi` and validates the response.
4. If it's valid, the DAO replaces the restaurant rows in a transaction.
5. Room emits the updated list to the screens.

If fetching or validation fails, the repository leaves the existing cache alone. It doesn't clear the database while waiting for the request.

A mutex prevents repository refreshes from running at the same time. Browse also avoids starting another refresh job while one is active. Refresh returns `Result<Unit>` so the ViewModel can show a useful error message. `CancellationException` is rethrown so cancelled work doesn't appear as an offline failure.

| Situation | What the user sees |
| --- | --- |
| First load with no saved data | Loading message and progress indicator |
| Refresh with saved data | Existing content stays visible |
| Successful refresh | Updated restaurants from Room |
| Search or filter has no matches | Empty results with Clear filters |
| Failed refresh with saved data | Refresh-failure message and Retry, with cached content still available |
| Failed refresh without saved data | Full error message with Retry |

`hasCache` comes from the full database list. This matters when a search has no matches: the app still has cached restaurants, even if the current filter hides them.

## Keeping favorites safe

Restaurant information goes in `restaurants`. Favorite IDs go in `favorite_restaurants`.

I separated them because refreshing restaurant information shouldn't change what the user has saved. The refresh transaction only replaces restaurant rows. Queries use `EXISTS` against the favorites table to include `isFavorite` in the result.

When someone changes a favorite, the repository updates its table and Room emits the new state. Browse and Details both observe those changes, so there's no extra code copying favorite state between screens.

Favorite IDs remain saved if a restaurant disappears from a later response. If that restaurant returns with the same ID, its favorite state is still there.

Persistent favorites are my selected improvement for Task 3. The measurable behavior is that a saved favorite survives a refresh and a database close/reopen; the Room test verifies both. A full app restart is also part of the manual walkthrough below.

To check this behavior, save a restaurant on Browse, open Details, change it there and go back. Then save it again, restart the app and refresh the list. The favorite should remain saved.

## Models and missing data

I kept separate models because each layer needs different information:

| Model | What it's for |
| --- | --- |
| `RestaurantDto` | Reading the JSON contract |
| `RestaurantEntity` | Storing restaurant content in Room |
| `FavoriteRestaurantEntity` | Storing the user's saved IDs |
| `RestaurantWithFavorite` | Reading restaurant content together with favorite state |
| `Restaurant` | Giving the UI a model without Room or JSON details |
| Browse and Details UI states | Representing what each screen should show |

The mapping requires usable IDs and names and trims them before saving. A blank ID or name, or duplicate normalized IDs, rejects the response before the database write. This keeps a bad response from replacing a good cache.

Optional fields are handled separately. Missing cuisines become an empty list. Invalid ratings, negative fees and invalid ETAs become unavailable. Missing open status becomes false. Only HTTPS image URLs are kept, and image failures have a fallback.

Prices stay as integer cents until display. `Formatting.kt` converts them to rand, shows “Free delivery” for zero and uses unavailable labels when data is missing. The ETA is shown as supplied in minutes.

## Browse, Details and navigation

Browse has a search field, Open now filter, Refresh action and a `LazyColumn` with restaurant IDs as keys. Search ignores case and trims surrounding whitespace. It combines with Open now in the ViewModel, outside the composables.

Search and filter values use `SavedStateHandle` so Android can restore them during recreation. The ViewModel combines them with Room data and refresh state, then exposes a StateFlow. Filtering runs on an injected default dispatcher, which also makes the ViewModel easier to test.

Compose collects state using `collectAsStateWithLifecycle()`. Loading happens through the ViewModel, not during recomposition. Favorite-write errors are kept separate from refresh errors.

Navigation passes an encoded restaurant ID through `restaurant/{restaurantId}`. Details observes that ID in Room, so it shows current data instead of a serialized copy from Browse. Back navigation uses `popBackStack()`.

If the ID is missing, blank or has no matching record, Details shows “Restaurant unavailable”. Navigation normally supplies the ID; the ViewModel also handles an absent argument without crashing.

## The failure switch

I added **Demo · Simulate failure** to debug builds so the offline and retry behavior is easy to reproduce.

`DemoSettings` stores the setting in SharedPreferences and exposes it as StateFlow. The user changes the switch, then presses Refresh or Retry. The next mock request fails while the switch is on. Release builds hide the control and disable simulated failure.

The setting survives restarts. That lets me demonstrate an error without cache by enabling failure and deleting only the database. The exact commands are in [README.md](README.md); they also delete saved favorites.

Airplane mode doesn't stop an asset read, so it isn't a substitute for this switch.

## Dependency injection

I used Hilt to provide Room, the DAO, the API, the repository, demo controls and the filtering dispatcher. ViewModels receive their dependencies through constructors.

The repository interface and `DemoControls` let tests use fakes. Injecting the dispatcher lets coroutine tests control when the filtering work runs.

## Tests

The tests focus on cache preservation, favorites and the main screen flows.

The unit tests cover the dataset, search and filtering, mapping, formatting, ViewModel state changes, favorite updates, failure recovery and cancellation.

The Room test checks that refresh saves data, failed or malformed refreshes preserve the old list, and refreshing doesn't overwrite favorites. It closes and reopens the database to check persistence. That is a database-reopening test, not a separate process-restart test.

The Compose tests check empty results, Retry, favorite callbacks and the ID used for navigation. The full app test uses the real Activity, Hilt, Room and navigation to load restaurants, change favorites, open Details, simulate failure, recover and search for a missing name. It waits for database-driven favorite updates before checking the UI.

The review run on 21 September 2026 passed 11 unit tests and 5 emulator tests on API 35, along with debug, unsigned release and Android test APK builds. Lint reported no errors and 18 warnings. The checks include missing, blank and unknown Details IDs, and cache and favorite preservation after duplicate normalized IDs. The commands are in the README.

## Trade-offs

The local mock makes the demo easy to run, but it doesn't test real HTTP requests, timeouts or a production service.

Room takes more setup than displaying the JSON directly, but it gives the app a real saved-data path to exercise when refresh fails. Separate favorite storage adds a query but keeps user choices independent of restaurant refreshes.

One app module is enough for this assessment. It shows the package boundaries without adding the setup cost of several Gradle modules.

I kept menus, ordering, payments, accounts, live location and deep links out of scope so I could focus on discovery, state, persistence and retry behavior.

## If the app grows

For a larger dataset, I'd look at a paginated service and Paging 3. Search and filtering could move to the backend when downloading the whole list stops making sense.

`LazyColumn`, stable keys, background parsing, async Room queries and Coil caching are already in place. I haven't measured frame performance. Baseline Profiles, Macrobenchmark and production monitoring would be later work, based on actual performance needs.

If multiple teams were working on this feature, the current packages could move into modules like these:

```text
:app
:core:model
:core:database
:core:network
:core:designsystem
:core:testing
:feature:restaurants:api
:feature:restaurants:data
:feature:restaurants:domain
:feature:restaurants:ui
```

The feature API would expose navigation entry points, data would own the sources and repository, and UI would own screens and state. I'd add domain use cases where they simplify real behavior. Shared modules would contain things several features actually use. This is a possible next step, not the current module structure.

The app module would assemble navigation; feature APIs would expose destinations taking stable IDs. Each feature would own its ViewModels and UI state, while repository interfaces would keep screens independent of network and database implementations. Shared UI components would live in the design system rather than depending on a feature's ViewModel.

For a larger app, I'd keep fast unit tests for mapping and state, database integration tests for transactions and persistence, and a small set of end-to-end tests for critical journeys. A real HTTP source would need contract and failure tests for timeouts, malformed responses and pagination. Scrolling benchmarks on representative devices would guide performance changes.

## AI help and the demo

I used Codex to help build the app, generate the fictional dataset, write tests and update the documentation. [AI_USAGE.md](AI_USAGE.md) contains project-specific versions of my supplied prompts. They're labeled as edited prompts, not an exact record of every conversation.

For ongoing development, I would use AI for bounded tasks such as drafting test cases, generating mock data and proposing small changes against an explicit requirement. Each change would need code review and checks against expected behavior before acceptance. Tests should cover observable outcomes and failure cases, rather than simply repeat the generated implementation. AI-written explanations and test-result claims also need checking against the code and actual reports. The dated verification results above record the checks run for this version; edited prompts alone are not verification.

For the demo, I'd show loading, search, Open now, empty results, Details and favorites first. Then I'd restart the app, demonstrate a failed refresh with cached data, recover with Retry and show the uncached error. After that I'd walk through the ViewModel, repository and Room transaction, show the test results and explain the trade-offs above.

Screenshots and the [demo recording](docs/Recording.mp4) are included in the README. The recording predates the wording and route-validation changes from this review.
