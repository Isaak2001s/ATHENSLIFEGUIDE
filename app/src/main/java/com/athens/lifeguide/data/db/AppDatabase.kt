package com.athens.lifeguide.data.db

import android.content.Context
import androidx.room.*

// ══════════════════════════════════════════════════
//  EXISTING ENTITIES
// ══════════════════════════════════════════════════

@Entity(tableName = "users",
    indices = [Index(value = ["username"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "favorites",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId"), Index(value = ["userId", "placeId"], unique = true)]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val placeId: String,
    val placeName: String,
    val placeType: String,
    val latitude: Double,
    val longitude: Double,
    val description: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

// ══════════════════════════════════════════════════
//  NEW: PARKING ENTITIES
// ══════════════════════════════════════════════════

@Entity(tableName = "parking_areas")
data class ParkingAreaEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val totalSpots: Int,
    val availableSpots: Int,
    val pricePerHour: Double,
    val address: String,
    val occupancyLevel: String = "low" // "low" | "medium" | "high"
)

@Entity(
    tableName = "reservations",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val parkingAreaId: String,
    val parkingAreaName: String,
    val pricePerHour: Double,
    val reservedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 15 * 60 * 1000, // 15 min
    val status: String = "active" // "active" | "cancelled"
)

// ══════════════════════════════════════════════════
//  EXISTING DAOs
// ══════════════════════════════════════════════════

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :u AND passwordHash = :ph LIMIT 1")
    suspend fun login(u: String, ph: String): UserEntity?
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(fav: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE userId = :userId AND placeId = :placeId")
    suspend fun delete(userId: Int, placeId: String)

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY addedAt DESC")
    suspend fun getAll(userId: Int): List<FavoriteEntity>

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId AND placeId = :placeId")
    suspend fun count(userId: Int, placeId: String): Int
}

// ══════════════════════════════════════════════════
//  NEW: PARKING DAOs
// ══════════════════════════════════════════════════

@Dao
interface ParkingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(areas: List<ParkingAreaEntity>)

    @Query("SELECT * FROM parking_areas ORDER BY name")
    suspend fun getAll(): List<ParkingAreaEntity>

    @Query("SELECT * FROM parking_areas WHERE id = :id")
    suspend fun getById(id: String): ParkingAreaEntity?

    @Query("UPDATE parking_areas SET availableSpots = availableSpots - 1, occupancyLevel = :newLevel WHERE id = :id AND availableSpots > 0")
    suspend fun decrementSpot(id: String, newLevel: String): Int

    @Query("UPDATE parking_areas SET availableSpots = availableSpots + 1, occupancyLevel = :newLevel WHERE id = :id")
    suspend fun incrementSpot(id: String, newLevel: String)
}

@Dao
interface ReservationDao {
    @Insert
    suspend fun insert(reservation: ReservationEntity): Long

    @Query("SELECT * FROM reservations WHERE userId = :userId ORDER BY reservedAt DESC")
    suspend fun getByUser(userId: Int): List<ReservationEntity>

    @Query("UPDATE reservations SET status = 'cancelled' WHERE id = :id")
    suspend fun cancel(id: Int)
}

// ══════════════════════════════════════════════════
//  DATABASE (version bumped to 2)
// ══════════════════════════════════════════════════

@Database(
    entities = [
        UserEntity::class,
        FavoriteEntity::class,
        ParkingAreaEntity::class,
        ReservationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun parkingDao(): ParkingDao
    abstract fun reservationDao(): ReservationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "athens_life.db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}