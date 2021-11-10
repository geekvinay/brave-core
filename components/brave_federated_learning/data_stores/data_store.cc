/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "brave/components/brave_federated_learning/data_stores/data_store.h"

#include <string>

#include "base/bind.h"
#include "base/guid.h"
#include "base/json/json_reader.h"
#include "base/logging.h"
#include "base/macros.h"
#include "base/numerics/safe_conversions.h"
#include "base/strings/stringprintf.h"
#include "base/time/time.h"
#include "sql/meta_table.h"
#include "sql/recovery.h"
#include "sql/statement.h"
#include "sql/transaction.h"
#include "ui/base/page_transition_types.h"

// DEBUG
#include <iostream>

namespace {

void DatabaseErrorCallback(sql::Database* db,
                           const base::FilePath& db_path,
                           int extended_error,
                           sql::Statement* stmt) {
  if (sql::Recovery::ShouldRecover(extended_error)) {
    // Prevent reentrant calls.
    db->reset_error_callback();

    // After this call, the |db| handle is poisoned so that future calls will
    // return errors until the handle is re-opened.
    sql::Recovery::RecoverDatabase(db, db_path);

    // The DLOG(FATAL) below is intended to draw immediate attention to errors
    // in newly-written code.  Database corruption is generally a result of OS
    // or hardware issues, not coding errors at the client level, so displaying
    // the error would probably lead to confusion.  The ignored call signals the
    // test-expectation framework that the error was handled.
    ignore_result(sql::Database::IsExpectedSqliteError(extended_error));
    return;
  }

  // The default handling is to assert on debug and to ignore on release.
  if (!sql::Database::IsExpectedSqliteError(extended_error))
    DLOG(FATAL) << db->GetErrorMessage();
}

}  // namespace

namespace brave {

namespace federated_learning {

// DataStore -----------------------------------------------------

DataStore::DataStore(const base::FilePath& database_path)
    : db_({.exclusive_locking = true, .page_size = 4096, .cache_size = 500}),
      database_path_(database_path) {}

bool DataStore::Init(const std::string& task_id, const std::string& task_name) {
  task_id_ = task_id;
  task_name_ = task_name;

  db_.set_histogram_tag(task_name);

  // To recover from corruption.
  db_.set_error_callback(
      base::BindRepeating(&DatabaseErrorCallback, &db_, database_path_));

  // Attach the database to our index file.
  return db_.Open(database_path_) && EnsureTable();
}

DataStore::~DataStore() {}

bool DataStore::DeleteAllLogs() {
  if (!db_.Execute(
          base::StringPrintf("DELETE FROM %s", task_name_.c_str()).c_str()))
    return false;

  ignore_result(db_.Execute("VACUUM"));
  return true;
}

}  // namespace federated_learning

}  // namespace brave
