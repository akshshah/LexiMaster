package com.example.leximaster.di

import androidx.room.Room
import com.example.leximaster.data.local.dao.ContextDao
import com.example.leximaster.data.local.dao.QuizDao
import com.example.leximaster.data.local.dao.ScoreHistoryDao
import com.example.leximaster.data.local.dao.SynonymDao
import com.example.leximaster.data.local.dao.UserDao
import com.example.leximaster.data.local.dao.WordDao
import com.example.leximaster.data.local.database.LexiMasterDatabase
import com.example.leximaster.data.repository.LexiMasterRepository
import org.koin.dsl.module

val dataModule = module {

    single<LexiMasterDatabase> {
        Room.databaseBuilder(
            get(),
            LexiMasterDatabase::class.java,
            LexiMasterDatabase.DATABASE_NAME
        ).build()
    }

    // DAOs
    factory<WordDao> { get<LexiMasterDatabase>().wordDao() }
    factory<ContextDao> { get<LexiMasterDatabase>().contextDao() }
    factory<SynonymDao> { get<LexiMasterDatabase>().synonymDao() }
    factory<UserDao> { get<LexiMasterDatabase>().userDao() }
    factory<QuizDao> { get<LexiMasterDatabase>().quizDao() }
    factory<ScoreHistoryDao> { get<LexiMasterDatabase>().scoreHistoryDao() }

    // Repository
    single<LexiMasterRepository> {
        LexiMasterRepository(
            wordDao = get(),
            contextDao = get(),
            synonymDao = get(),
            userDao = get(),
            quizDao = get(),
            scoreHistoryDao = get(),
            geminiService = get(),
        )
    }
}
