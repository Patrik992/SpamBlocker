package spam.blocker.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import spam.blocker.R
import spam.blocker.db.HistoryRecord
import spam.blocker.def.Def
import spam.blocker.service.Checker
import spam.blocker.ui.theme.LocalPalette
import spam.blocker.ui.theme.Salmon
import spam.blocker.ui.util.M
import spam.blocker.ui.widgets.DrawableImage
import spam.blocker.ui.widgets.ResImage
import spam.blocker.ui.widgets.RowCenter
import spam.blocker.ui.widgets.RowVCenter
import spam.blocker.util.AppInfo
import spam.blocker.util.Contacts
import spam.blocker.util.Util


const val CardHeight = 64 // the height when RegexStr is single line
const val CardPaddingVertical = 8 // the top/bottom padding
const val ItemHeight = CardHeight - 2 * CardPaddingVertical // the height of Avatar and Time

@Composable
fun HistoryCard(
    forType: Int,
    record: HistoryRecord,
    modifier: Modifier,
) {
    val C = LocalPalette.current
    val ctx = LocalContext.current

    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        border = BorderStroke(width = 0.5.dp, color = C.cardBorder),
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        RowVCenter(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier.padding(8.dp)
        ) {
            // 1. avatar
            val contact = Contacts.findByRawNumber(ctx, record.peer)
            val bmpAvatar = contact?.loadAvatar(ctx)
            if (bmpAvatar != null) {
                ComposeImage(bmpAvatar.asImageBitmap(), "", modifier = M.size(ItemHeight.dp))
            } else {
                // Use the hash code as color
                val toHash = contact?.name ?: record.peer
                val color = Color(toHash.hashCode().toLong() or 0xff808080/* for higher contrast */)
                ResImage(R.drawable.ic_account_circle, color = color, modifier = M.size(ItemHeight.dp))
            }

            // 2. Number / BlockReason / Message
            Column(
                modifier = M.weight(1f)
            ) {
                // Number
                Text(
                    text = contact?.name ?: record.peer,
                    color = if (record.isBlocked()) C.block else C.pass,
                    fontSize = 18.sp
                )
                // Block Reason
                RowCenter {
                    // Reason text
                    Text(
                        text = Checker.resultStr(ctx, record.result, record.reason),
                        color = C.textGrey,
                        fontSize = 16.sp,
                        maxLines = 10,
                    )
                    // Blocked by RecentApp
                    if (record.result == Def.RESULT_ALLOWED_BY_RECENT_APP) {
                        DrawableImage(
                            AppInfo.fromPackage(ctx, record.reason).icon,
                            modifier = M
                                .size(24.dp)
//                                .padding(horizontal = 2.dp)
                        )
                    }
                }

                // SMS Message
//                if (forType == Def.ForSms) {
//                    Text(
//                        text = record.sms_body,
//                        color = C.textGrey,
//                        fontSize = 16.sp,
//                        maxLines = 10,
//                    )
//                }
            }

            // 3. Time / Unread Indicator

            Box(
                modifier = M
                    .height(ItemHeight.dp)
            ) {
                // time
                Text(
                    text = Util.formatTime(ctx, record.time),
                    fontSize = 14.sp,
                    modifier = M
                        .padding(end = 8.dp)
                        .align(Alignment.Center),
                    color = C.textGrey,
                    textAlign = TextAlign.Center,
                )

                // Unread red dot
                if (!record.read) {
                    Canvas(
                        modifier = Modifier
                            .size(4.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        drawCircle(color = Salmon, radius = size.minDimension / 2)
                    }
                }
            }
        }
    }
}