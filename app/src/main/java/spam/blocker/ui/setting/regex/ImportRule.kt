package spam.blocker.ui.setting.regex

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import spam.blocker.R
import spam.blocker.db.NumberRuleTable
import spam.blocker.db.RegexRule
import spam.blocker.ui.theme.Salmon
import spam.blocker.ui.theme.SkyBlue
import spam.blocker.ui.widgets.DropdownWrapper
import spam.blocker.ui.widgets.HtmlText
import spam.blocker.ui.widgets.ResIcon
import spam.blocker.ui.widgets.LabelItem
import spam.blocker.ui.widgets.LongPressButton
import spam.blocker.ui.widgets.PopupDialog
import spam.blocker.ui.widgets.Str
import spam.blocker.ui.widgets.rememberFileReadChooser
import spam.blocker.util.Csv
import spam.blocker.util.Lambda

@Composable
fun ImportRuleButton(
    ruleList: SnapshotStateList<RegexRule>,
    onClick: Lambda,
) {
    val ctx = LocalContext.current

    val fileReader = rememberFileReadChooser()
    fileReader.Compose(ctx)

    val warningTrigger = remember { mutableStateOf(false) }
    if (warningTrigger.value) {
        PopupDialog(
            trigger = warningTrigger,
            content = {
                HtmlText(html = ctx.getString(R.string.failed_to_import_from_csv))
            },
            icon = { ResIcon(R.drawable.ic_fail_red, color = Salmon) },
        )
    }


    val importRuleItems = remember {
        ctx.resources.getStringArray(R.array.import_csv_type).mapIndexed { menuItemIndex, label ->

            LabelItem(
                label = label,

                onClick = {
                    fileReader.popup { fn: String?, raw: ByteArray? ->
                        if (raw == null)
                            return@popup

                        val (headers, rowMaps) = Csv.parseToMaps(raw)
                        // show error if there is no column `pattern`, because it will generate empty rows.
                        if (!headers.contains("pattern")) {
                            warningTrigger.value = true
                            return@popup
                        }

                        val rules = rowMaps.map {
                            RegexRule.fromMap(it)
                        }

                        when (menuItemIndex) {
                            0 -> { // import as single rule
                                val joined = rules.map {
                                    spam.blocker.util.Util.clearNumber(it.pattern)
                                }.filter {
                                    it.isNotEmpty()
                                }.joinToString ( separator = "|" )

                                val rule = RegexRule().apply {
                                    pattern = "($joined)"
                                    description = fn ?: ""
                                }
                                // 1. add to db
                                val table = NumberRuleTable()
                                table.addNewRule(ctx, rule)

                                // 2. refresh gui
                                ruleList.clear()
                                ruleList.addAll(table.listAll(ctx))
                            }
                            1 -> { // import as multi rules
                                // 1. add to db
                                val table = spam.blocker.db.NumberRuleTable()
                                rules.forEach {
                                    table.addNewRule(ctx, it)
                                }

                                // 2. refresh gui
                                ruleList.clear()
                                ruleList.addAll(table.listAll(ctx))
                            }
                        }
                    }
                }
            )
        }
    }
    DropdownWrapper(items = importRuleItems) { expanded ->
        LongPressButton(
            label = Str(R.string.add),
            color = SkyBlue,
            onClick = onClick,
            onLongClick = {
                expanded.value = true
            },
        )
    }
}
