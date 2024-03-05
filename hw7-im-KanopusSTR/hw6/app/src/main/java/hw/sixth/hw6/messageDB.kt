package hw.sixth.hw6

import androidx.room.*

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg message: MessageDB?)

    @Query("SELECT * FROM MessageDB WHERE id = :id")
    fun getById(id: Int): MessageDB?

    @get:Query("SELECT * FROM MessageDB")
    val allPeople: List<MessageDB?>

    @Query("SELECT COUNT(id) FROM MessageDB")
    fun size(): Int
}

@Entity
data class MessageDB(
    @PrimaryKey val id : Int,
    @ColumnInfo val from: String,
    @ColumnInfo val to: String,
    @ColumnInfo val text: String?= null,
    @ColumnInfo val url: String? = null,
    @ColumnInfo val time: String?
)

@Database(
    entities = [MessageDB::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract val messageDao: MessageDao?
}