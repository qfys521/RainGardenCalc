package cn.qfys521.milthmgardencalc.notification

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cn.qfys521.milthmgardencalc.model.Plan
import cn.qfys521.milthmgardencalc.model.PotAction
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun schedulePlan(context: Context, plan: Plan) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(WORK_TAG)

        for (event in plan.loginSchedule) {
            val delayMinutes = (event.timeHours * 60).toLong()
            if (delayMinutes < 0) continue

            val actions = event.potActions.filter { it.action != PotAction.IDLE || it.note.isNotEmpty() }
            if (actions.isEmpty()) continue

            val title = "第${event.loginIndex + 1}次登录提醒"
            val message = actions.joinToString("\n") { pot ->
                val prefix = if (pot.potIndex >= 0) "花盆${pot.potIndex + 1}: " else ""
                when (pot.action) {
                    PotAction.PLANT -> "${prefix}种植 ${pot.batchCount}x ${pot.crop?.name}"
                    PotAction.HARVEST -> "${prefix}收获 ${pot.batchCount}x ${pot.crop?.name}"
                    PotAction.WATER -> "${prefix}${pot.note}"
                    PotAction.PLANT_AND_HARVEST -> "${prefix}种收 ${pot.crop?.name}"
                    PotAction.IDLE -> pot.note
                }
            }

            val data = Data.Builder()
                .putString(CropNotificationWorker.KEY_TITLE, title)
                .putString(CropNotificationWorker.KEY_MESSAGE, message)
                .build()

            val request = OneTimeWorkRequestBuilder<CropNotificationWorker>()
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setInputData(data)
                .addTag(WORK_TAG)
                .build()

            workManager.enqueue(request)
        }
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }

    private const val WORK_TAG = "crop_notifications"
}
