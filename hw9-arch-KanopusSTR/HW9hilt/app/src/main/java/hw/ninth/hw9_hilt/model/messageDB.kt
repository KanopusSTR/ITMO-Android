package hw.ninth.hw9_hilt.model

import androidx.room.*

@Entity
data class MessageDB(
    @PrimaryKey val id : Int,
    @ColumnInfo val from: String,
    @ColumnInfo val to: String,
    @ColumnInfo val text: String?= null,
    @ColumnInfo val url: String? = null,
    @ColumnInfo val time: String? = null
)