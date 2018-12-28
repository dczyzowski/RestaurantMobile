package pl.restaurant.restaurantmobile.database

import android.content.Context
import androidx.room.*

@Entity
data class User(
        @PrimaryKey var uid: Int,
        @ColumnInfo(name = "username") var username: String,
        @ColumnInfo(name = "first_name") var firstName: String?,
        @ColumnInfo(name = "auth_header") var authHeader: String,
        @ColumnInfo(name = "avatar") var avatar: Int?)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE username IN (:username)")
    fun loadUser(username: String): List<User>

    @Insert
    fun insertAll(vararg user: User)

    @Delete
    fun delete(user: User)
}

@Database(entities = arrayOf(User::class), version = 1)
abstract class UsersDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        private var INSTANCE: UsersDatabase? = null

        fun getInstance(context: Context): UsersDatabase? {
            if (INSTANCE == null) {
                synchronized(UsersDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            UsersDatabase::class.java, "users.db").allowMainThreadQueries()
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}