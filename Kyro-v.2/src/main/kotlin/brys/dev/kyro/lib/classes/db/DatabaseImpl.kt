package brys.dev.kyro.lib.classes.db

import brys.dev.kyro.lib.structures.MongoDatabase

data class DB(val MongoDB: MongoDatabase, val AddBotData: AddBotData.Companion, val AddServerSetting: AddServerSetting, val AddUserData: AddUserData, val FindBotData: FindBotData, val FindServerSetting: FindServerSetting, val FindUserData: FindUserData)