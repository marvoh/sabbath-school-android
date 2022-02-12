/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.di

import android.app.AlarmManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.cryart.sabbathschool.BuildConfig
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.model.AppConfig
import com.cryart.sabbathschool.reminder.DailyReminderManager
import com.cryart.sabbathschool.settings.DailyReminder
import com.cryart.sabbathschool.ui.login.GoogleSignInWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    fun provideGoogleSignInWrapper() = GoogleSignInWrapper()

    @Provides
    fun provideReminderManager(
        @ApplicationContext context: Context,
        ssPrefs: SSPrefs
    ): DailyReminderManager {
        return DailyReminderManager(
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager,
            notificationManager = NotificationManagerCompat.from(context),
            context = context,
            ssPrefs = ssPrefs,
        )
    }

    @Provides
    fun provideDailyReminder(manager: DailyReminderManager): DailyReminder = manager

    @Provides
    fun provideAppConfig(
        @ApplicationContext context: Context,
    ) = AppConfig(
        BuildConfig.VERSION_NAME,
        context.getString(R.string.default_web_client_id),
    )
}
