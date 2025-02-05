package org.thoughtcrime.securesms.database

import android.database.Cursor
import org.signal.spinner.ColumnTransformer
import org.signal.spinner.DefaultColumnTransformer
import org.thoughtcrime.securesms.database.model.MessageRecord
import org.thoughtcrime.securesms.database.model.UpdateDescription
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.util.CursorUtil

object GV2UpdateTransformer : ColumnTransformer {
  override fun matches(tableName: String?, columnName: String): Boolean {
    return columnName == MmsSmsColumns.BODY && (tableName == null || (tableName == SmsDatabase.TABLE_NAME || tableName == MmsDatabase.TABLE_NAME))
  }

  override fun transform(tableName: String?, columnName: String, cursor: Cursor): String {
    val type: Long = cursor.getMessageType()

    if (type == -1L) {
      return DefaultColumnTransformer.transform(tableName, columnName, cursor)
    }

    val body: String? = CursorUtil.requireString(cursor, MmsSmsColumns.BODY)

    return if (MmsSmsColumns.Types.isGroupV2(type) && MmsSmsColumns.Types.isGroupUpdate(type) && body != null) {
      val gv2ChangeDescription: UpdateDescription = MessageRecord.getGv2ChangeDescription(ApplicationDependencies.getApplication(), body)
      gv2ChangeDescription.string
    } else {
      body ?: ""
    }
  }
}

private fun Cursor.getMessageType(): Long {
  return when {
    getColumnIndex(SmsDatabase.TYPE) != -1 -> requireLong(SmsDatabase.TYPE)
    getColumnIndex(MmsDatabase.MESSAGE_BOX) != -1 -> requireLong(MmsDatabase.MESSAGE_BOX)
    else -> -1
  }
}
