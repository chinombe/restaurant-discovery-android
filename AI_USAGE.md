# AI usage — Local Table

Tool used: OpenAI Codex.

# Prompts

## 1. Models and JSON

I'm building Local Table, a small restaurant discovery app in Kotlin and Compose for my assessment. Help me check the models and JSON.

I have `RestaurantDto`, `RestaurantEntity` and `Restaurant`. Keep them separate and check the mapping in `RestaurantMappers.kt`. Each restaurant needs an id, name, cuisines, rating, delivery fee in cents, ETA, open or closed status and an optional image URL. Favorites come from Room, not the JSON.

Some fields can be missing or null. Don't make up values for them. Check blank ids and names, ratings outside 0–5, negative fees and invalid ETAs. Keep the HTTPS image check and use an empty list when cuisines are missing.

### Restaurant data

Check my `restaurants.json` in `app/src/main/assets`. I need 200 fictional restaurant branches around Johannesburg with different cuisines, prices and opening states. Keep the ids and names unique and include some missing fields so I can test the fallbacks. The food images are just examples.

Run the dataset and mapping tests in `RestaurantTest`. Don't add menus or ordering.

## 2. Room database

Check my Room setup in `AppDatabase.kt`. I have a restaurants table and a separate favorites table because refreshing the list must not remove someone's favorites.

Keep Flow for reads and suspend functions for writes. I need to observe the full list with favorite state and also one restaurant by id. Keep the list sorted by name and the JSON converter for cuisines.

Check `replaceRestaurants()` properly. Deleting the old restaurants and inserting the new ones must happen in one transaction. If it fails, I should still have the old list. Don't clear `favorite_restaurants` during refresh.

Also check that both screens update when a favorite changes. Explain what happens to a saved favorite if its restaurant is missing from the next response. Fix only what needs fixing.

## 3. Mock API

The assessment allows a local JSON file so I don't need a real backend. Keep `RestaurantApi` and `MockRestaurantApi` and read the restaurants from assets.

Parse the file off the main thread using Kotlin serialization. Keep the 900 ms delay so I can show loading. The ViewModels should call the repository, not read the file themselves.

I also need the failure demo to work. Check `DemoSettings` and `DemoControls`. The debug switch should remember its setting after restarting the app. When it's on, Refresh or Retry should fail. Turn it off and the next request should work again.

Keep this disabled in release builds. Make the README clear that airplane mode won't stop the bundled JSON from loading, even though the images need internet.

## 4. Repository

Check `OfflineFirstRestaurantRepository`. Room should be the source of truth for the UI.

When I refresh, fetch through `RestaurantApi`, check the response, then save it in Room. Don't delete the cache before the new data is ready. If the request fails or the data has bad or duplicate ids, keep the old restaurants.

Keep `observeRestaurants()`, `observeRestaurant(id)`, `refreshRestaurants()` and `setFavorite()`. Favorite changes should go into their own table and update Browse and Details through Room.

Check the mutex so refreshes don't clash. Return refresh failures to the ViewModel, but don't swallow `CancellationException` or show raw exceptions to the user.

Please check for data-loss problems and keep any fixes small. I don't need extra architecture for this app.

## 5. ViewModels and state

Check my Browse and Details ViewModels. I want the screens to get their state from StateFlow.

Browse needs search, Open now, loading, refresh, retry and favorites. Keep search and Open now in `SavedStateHandle`. Do the filtering in the ViewModel using the injected dispatcher, not inside Compose.

Make sure these cases work:

- First load with no saved restaurants.
- Restaurants loaded normally.
- Search has no matches.
- Refresh fails but I still have saved restaurants.
- Refresh fails and there is nothing saved.

Check `hasCache` against the full list, not the search results. An unmatched search shouldn't make the app think the cache is gone. Keep favorite errors separate from refresh errors as well.

Don't start another refresh while one is already running. Keep the work in `viewModelScope`. Restaurant selection already uses the screen's navigation callback, so don't add another event for the same thing.

Details should observe the restaurant by id, show loading or a missing restaurant message, and update favorites. Check for lifecycle issues or duplicated state without rewriting everything.

## 6. Compose screens

Check the Browse and Details screens and keep the current Local Table look. I'm using Material 3 with the green and cream colours.

Browse needs the search field, Open now filter, Refresh, debug failure switch and restaurant list. Use restaurant ids as the `LazyColumn` keys. Keep the offline message visible with the cached list and give the user Retry. If search has no results, show Clear filters.

Reuse my `RestaurantCard`, `RestaurantImage`, `FavoriteButton`, `RestaurantFacts` and `MessageContent`. Keep Coil and show the restaurant icon if an image is loading or fails.

Make sure the favorite buttons and failure switch have useful accessibility labels. Open and closed status should have text as well as colour.

Check `Formatting.kt` too. Prices should be in rand, zero delivery fee should say “Free delivery”, and missing information should say it's unavailable. Show the ETA we have, don't add a made-up time range.

Keep the screens focused on displaying state and sending callbacks. Don't put repository calls or filtering inside them.

## 7. Navigation

Check my navigation between Browse and Details in `AppNavHost`.

The routes are `browse` and `restaurant/{restaurantId}`. Only pass the id, not the whole restaurant. Keep the id encoded in the route and let Details read the current record from Room.

Keep `hiltViewModel()`, lifecycle-aware state collection and Back navigation. Check that tapping a restaurant doesn't open duplicate Details screens.

If the id has no matching restaurant, show “Restaurant unavailable”. Also check the `checkNotNull` in `DetailsViewModel`: the route normally supplies the id, but creating the ViewModel without it would fail. Don't say that case is handled if it isn't. Show me a small fix if needed.

## 8. Hilt setup

Check my Hilt setup and keep it simple. I already have `RestaurantApplication`, `MainActivity`, `DataModule` and the two ViewModels wired up.

I need the database, DAO, mock API, repository, demo settings and filtering dispatcher provided correctly. Keep the database, API and repository shared where they should be. Browse uses `DemoControls` so I can replace it in tests.

Read my Gradle files before changing versions. This project uses AGP's built-in Kotlin, KSP for Room and Hilt, SDK 36, minimum SDK 26 and JVM target 17. Don't add another Kotlin Android plugin or upgrade things without checking compatibility.

### Manual injection fallback

Use this prompt if Hilt starts taking too much time. The app currently uses Hilt.

Hilt is giving me configuration problems and I have limited time. Show me a simple manual constructor injection setup for this same app.

Create the database, DAO, demo settings, mock API and repository in the application setup and pass them into the classes that need them. Don't create a new database or repository every time a screen opens.

Show me the factories for both ViewModels. Keep `SavedStateHandle` working for search, Open now and the restaurant id. Show how navigation gets the ViewModels without `hiltViewModel()`.

I still need to replace the repository, demo controls and dispatcher in tests. Don't put service locators inside the ViewModels or composables. Show which Hilt annotations and dependencies to remove, but keep KSP for Room. Keep the existing offline and favorite behavior working.

## 9. Tests

Check my tests and focus on things that could go wrong during the demo.

I need coverage for search, Open now, both together, missing fields, prices, loading, retry and favorites. Check that the asset has 200 valid records with unique ids.

For Room, test that refresh saves restaurants, failed refresh keeps the old list and refreshing doesn't remove favorites. Close and reopen the database to check persistence. Don't call that a full process-restart test.

For Compose, check empty results, Retry, favorite clicks and the restaurant id passed to navigation. Keep the full app test that loads the data, opens Details, changes a favorite, simulates a failure and recovers.

Use the existing tests and fakes. Wait for Room updates before checking the favorite button so the tests aren't racing the database. Don't add tests just to increase coverage.

Run these and tell me what passed, what failed and anything you couldn't run:

```sh
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

## 10. Final review

Review the project before I submit it. Compare the actual code with `README.md` and `SOLUTION.md` and point out anything missing or overstated.

Check search, filtering, Details, favorites, loading, empty results, offline cache and Retry. Look for crashes from missing data, main-thread work, lifecycle problems or anything that could lose favorites.

Be clear that this uses a mock JSON source and sample food images. Paging, Retrofit, server-side search, multiple Gradle modules and performance benchmarks are future ideas. Don't describe them as already built.

Check the setup commands and the debug failure instructions. The release APK is unsigned. Also check that local settings, secrets and build files that shouldn't be committed are ignored. If the README links to an ignored document, flag it because that link won't work in a fresh clone.

Separate your findings into things I must fix, important improvements and optional polish. Give me the file and the smallest useful fix. Don't suggest ordering, payments or extra features.

Run the build and checks where possible:

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest assembleRelease
./gradlew connectedDebugAndroidTest
```

The earlier run passed 10 unit tests and 5 emulator tests. Check the current code rather than assuming those results still apply. The screenshots are included. The narrated video still needs recording.

## Recording what I used

When I use one of these prompts, I'll keep the exact prompt, note what I accepted or changed, and record the checks I actually ran.

This edit only changes the wording of the prompts. It doesn't mean they were all run again or that the app was retested.
