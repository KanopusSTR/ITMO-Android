package hw.third.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

const val myRequestID = 362615

data class Contact(val name: String, val phoneNumber: String)

@SuppressLint("Range")
fun Context.fetchAllContacts(): List<Contact> {
    contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
    )
        .use { cursor ->
            if (cursor == null) return emptyList()
            val builder = ArrayList<Contact>()
            while (cursor.moveToNext()) {
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        ?: "N/A"
                val phoneNumber =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        ?: "N/A"

                builder.add(Contact(name, phoneNumber))
            }
            return builder
        }
}


class MainActivity : AppCompatActivity() {

    fun showContacts() {
        val myRecyclerView = findViewById<RecyclerView>(R.id.myRecyclerView)
        val list = fetchAllContacts()
        val viewManager = LinearLayoutManager(this)
        myRecyclerView.apply {
            layoutManager = viewManager
            adapter = UserAdapter(list) {
                val callIntent = Intent(Intent.ACTION_DIAL)
                val phoneNumber = it.phoneNumber
                callIntent.data = Uri.parse("tel:$phoneNumber")
                startActivity(callIntent)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            myRequestID -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showContacts()
                } else {
                    val myRecyclerView = findViewById<RecyclerView>(R.id.myRecyclerView)
                    val list = ArrayList<Contact>()
                    list.add(Contact("Error", "no permission"))
                    val viewManager = LinearLayoutManager(this)
                    myRecyclerView.apply {
                        layoutManager = viewManager
                        adapter = UserAdapter(list) {
                            Toast.makeText(
                                this@MainActivity,
                                "Clicked on User $it!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
                return
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                myRequestID
            )
        } else {
            showContacts()
        }

    }
}

class UserViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    val nameView: TextView = root.findViewById(R.id.name)
    private val phoneView: TextView = root.findViewById(R.id.phone)

    fun bind(user: Contact) {
        nameView.text = user.name
        phoneView.text = user.phoneNumber
    }
}

class UserAdapter(
    private val users: List<Contact>,
    private val onClick: (Contact) -> Unit
) : RecyclerView.Adapter<UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : UserViewHolder {
        val holder = UserViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        )
        holder.nameView.setOnClickListener {
            onClick(users[holder.adapterPosition])
        }
        return holder
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
        holder.bind(users[position])

}