package dev.cyberdeck.ui

import android.content.ContentResolver
import android.provider.ContactsContract

internal data class CallContact(
    val name: String,
    val phoneNumber: String
)

internal fun loadCallContacts(
    resolver: ContentResolver
): List<CallContact> {
    val contacts = mutableListOf<CallContact>()

    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    resolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        null,
        null,
        "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
    )?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        val numberIndex = cursor.getColumnIndex(
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        if (nameIndex >= 0 && numberIndex >= 0) {
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)?.trim().orEmpty()
                val number = cursor.getString(numberIndex)?.trim().orEmpty()

                if (name.isNotBlank() && number.isNotBlank()) {
                    contacts += CallContact(name, number)
                }
            }
        }
    }

    return contacts.distinctBy {
        it.name.lowercase() to it.phoneNumber.filter(Char::isDigit)
    }
}