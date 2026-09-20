package com.chinombe.restaurants.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cuisines: List<String>,
    val rating: Double?,
    val deliveryFeeCents: Int?,
    val etaMinutes: Int?,
    val isOpen: Boolean,
    val imageUrl: String?,
)

@Entity(tableName = "favorite_restaurants")
data class FavoriteRestaurantEntity(@PrimaryKey val restaurantId: String)

data class RestaurantWithFavorite(
    @Embedded val restaurant: RestaurantEntity,
    val isFavorite: Boolean,
)

class Converters {
    @TypeConverter fun encode(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter fun decode(value: String): List<String> = Json.decodeFromString(value)
}

@Dao
abstract class RestaurantDao {
    @Query(
        "SELECT r.*, EXISTS(SELECT 1 FROM favorite_restaurants f WHERE f.restaurantId = r.id) AS isFavorite FROM restaurants r ORDER BY r.name COLLATE NOCASE"
    )
    abstract fun observeAll(): Flow<List<RestaurantWithFavorite>>

    @Query(
        "SELECT r.*, EXISTS(SELECT 1 FROM favorite_restaurants f WHERE f.restaurantId = r.id) AS isFavorite FROM restaurants r WHERE r.id = :id"
    )
    abstract fun observeById(id: String): Flow<RestaurantWithFavorite?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<RestaurantEntity>)

    @Query("DELETE FROM restaurants") abstract suspend fun deleteRestaurants()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addFavorite(item: FavoriteRestaurantEntity)

    @Query("DELETE FROM favorite_restaurants WHERE restaurantId = :id")
    abstract suspend fun removeFavorite(id: String)

    @Transaction
    open suspend fun replaceRestaurants(items: List<RestaurantEntity>) {
        deleteRestaurants()
        insertAll(items)
    }
}

@Database(
    entities = [RestaurantEntity::class, FavoriteRestaurantEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
}
