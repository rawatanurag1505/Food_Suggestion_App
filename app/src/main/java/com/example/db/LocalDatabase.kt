package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import android.content.Context

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val email: String,
    val username: String,
    val passwordHash: String
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val restrictions: String = "",
    val cuisines: String = "",
    val healthGoal: String = "",
    val skillLevel: String = "",
    val equipment: String = "",
    val dislikes: String = ""
)

@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: String,
    val category: String,
    val expiryDate: String
)

@Dao
interface AppDao {
    @Query("SELECT * FROM user_accounts WHERE email = :email")
    suspend fun getUserAccount(email: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccount)

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("SELECT * FROM pantry_items ORDER BY expiryDate ASC")
    fun getAllPantryItems(): Flow<List<PantryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItem(item: PantryItem)

    @Delete
    suspend fun deletePantryItem(item: PantryItem)
}

@Database(entities = [UserProfile::class, PantryItem::class, UserAccount::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "mealmate_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    // Proactively trigger database opening to capture any migration/schema issues early
                    instance.openHelper.writableDatabase
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    try {
                        context.deleteDatabase("mealmate_database")
                        val instance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "mealmate_database"
                        )
                        .fallbackToDestructiveMigration()
                        .build()
                        instance.openHelper.writableDatabase
                        INSTANCE = instance
                        instance
                    } catch (ex: Exception) {
                        val instance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "mealmate_database"
                        )
                        .fallbackToDestructiveMigration()
                        .build()
                        INSTANCE = instance
                        instance
                    }
                }
            }
        }
    }
}

class AppRepository(private val appDao: AppDao) {
    val userProfile: Flow<UserProfile?> = appDao.getUserProfile()
    val pantryItems: Flow<List<PantryItem>> = appDao.getAllPantryItems()

    suspend fun getUserAccount(email: String): UserAccount? {
        return appDao.getUserAccount(email)
    }

    suspend fun insertUserAccount(user: UserAccount) {
        appDao.insertUserAccount(user)
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        appDao.saveUserProfile(profile)
    }

    suspend fun insertPantryItem(item: PantryItem) {
        appDao.insertPantryItem(item)
    }

    suspend fun deletePantryItem(item: PantryItem) {
        appDao.deletePantryItem(item)
    }
}
