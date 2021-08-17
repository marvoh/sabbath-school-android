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

package app.ss.lessons.data.repository.quarterly

import app.ss.lessons.data.extensions.ValueEvent
import app.ss.lessons.data.extensions.singleEvent
import app.ss.lessons.data.model.Language
import app.ss.lessons.data.model.QuarterlyGroup
import app.ss.lessons.data.model.SSQuarterly
import app.ss.lessons.data.model.SSQuarterlyInfo
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.response.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class QuarterliesRepositoryImpl(
    private val firebaseDatabase: FirebaseDatabase,
    private val ssPrefs: SSPrefs
) : QuarterliesRepository {

    override suspend fun getLanguages(): Resource<List<Language>> {
        // Switch to API when we migrate
        return getLanguagesFirebase()
    }

    private suspend fun getLanguagesFirebase(): Resource<List<Language>> = suspendCoroutine { continuation ->
        firebaseDatabase.getReference(SSConstants.SS_FIREBASE_LANGUAGES_DATABASE)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Resource.error(error.toException()))
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val languages = snapshot.children.mapNotNull {
                        it.getValue(Language::class.java)
                    }
                    continuation.resume(Resource.success(languages))
                }
            })
    }

    override suspend fun getQuarterlies(languageCode: String?, group: QuarterlyGroup?): Resource<List<SSQuarterly>> {
        val code = languageCode ?: ssPrefs.getLanguageCode()

        val event = firebaseDatabase
            .getReference(SSConstants.SS_FIREBASE_QUARTERLIES_DATABASE)
            .child(code)
            .singleEvent()

        return when (event) {
            is ValueEvent.Cancelled -> Resource.error(event.error)
            is ValueEvent.DataChange -> {
                val quarterlies = event.snapshot.children.mapNotNull {
                    SSQuarterly(it)
                }
                val result = group?.let {
                    quarterlies.filter { it.quarterly_group == group }
                } ?: quarterlies
                Resource.success(result)
            }
        }
    }

    override suspend fun getQuarterlyInfo(index: String): Resource<SSQuarterlyInfo> {
        val event = firebaseDatabase.reference
            .child(SSConstants.SS_FIREBASE_QUARTERLY_INFO_DATABASE)
            .child(index)
            .singleEvent()

        return when (event) {
            is ValueEvent.Cancelled -> Resource.error(event.error)
            is ValueEvent.DataChange -> Resource.success(SSQuarterlyInfo(event.snapshot))
        }
    }
}
