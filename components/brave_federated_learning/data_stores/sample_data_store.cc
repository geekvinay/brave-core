/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "brave/components/brave_federated_learning/data_stores/sample_data_store.h"

#include <string>
#include <utility>

#include "base/bind.h"
#include "base/guid.h"
#include "base/json/json_reader.h"
#include "base/logging.h"
#include "base/macros.h"
#include "base/numerics/safe_conversions.h"
#include "base/strings/stringprintf.h"
#include "base/time/time.h"
#include "brave/components/brave_federated_learning/data_stores/data_store.h"
#include "sql/meta_table.h"
#include "sql/recovery.h"
#include "sql/statement.h"
#include "sql/transaction.h"
#include "ui/base/page_transition_types.h"

// DEBUG
#include <iostream>

namespace {

void BindSampleLogToStatement(
    const brave::federated_learning::SampleTaskLog& sample_log,
    sql::Statement* s) {
  // DCHECK(base::IsValidGUID(sample_log.id));
  s->BindString(0, sample_log.id);
  s->BindString(1, sample_log.sample_text);
  s->BindInt(2, sample_log.sample_number);
  s->BindString(3, sample_log.creation_date);
}

}  // namespace

namespace brave {

namespace federated_learning {

// SampleTaskLog ---------------------------------------

SampleTaskLog::SampleTaskLog(const std::string& id,
                             const std::string& sample_text,
                             const int sample_number,
                             const std::string& creation_date)
    : id(id),
      sample_text(sample_text),
      sample_number(sample_number),
      creation_date(creation_date) {}

SampleTaskLog::SampleTaskLog(const SampleTaskLog& other) = default;

SampleTaskLog::SampleTaskLog() {}
SampleTaskLog::~SampleTaskLog() {}

// SampleDataStore -----------------------------------------------------

SampleDataStore::SampleDataStore(const base::FilePath& database_path)
    : DataStore(database_path) {}

bool SampleDataStore::AddLog(const SampleTaskLog& log) {
  const SampleTaskLog& sample_log = (SampleTaskLog&)log;
  std::cerr << "Adding log with text: " << sample_log.sample_text;
  sql::Statement s(db_.GetUniqueStatement(
      base::StringPrintf(
          "INSERT INTO %s (id, sample_text, sample_number, creation_date) "
          "VALUES (?,?,?,?)",
          task_name_.c_str())
          .c_str()));
  BindSampleLogToStatement(sample_log, &s);
  return s.Run();
}

void SampleDataStore::LoadLogs(GuidToSampleTaskLogMap* sample_logs) {
  DCHECK(sample_logs);
  sql::Statement s(db_.GetUniqueStatement(
      base::StringPrintf(
          "SELECT id, sample_text, sample_number, creation_date FROM %s",
          task_name_.c_str())
          .c_str()));

  sample_logs->clear();
  while (s.Step()) {
    std::cerr << "Retrieving " << s.ColumnString(1);
    SampleTaskLog stl(s.ColumnString(0), s.ColumnString(1), s.ColumnInt(2),
                      s.ColumnString(3));
    sample_logs->insert(std::make_pair(s.ColumnString(0), stl));
  }
}

SampleDataStore::~SampleDataStore() {}

bool SampleDataStore::EnsureTable() {
  if (db_.DoesTableExist(task_name_))
    return true;

  std::cerr << "Creating new table" << std::endl;
  sql::Transaction transaction(&db_);

  // meta_table_.Init(&db_, kCurrentVersionNumber, kCompatibleVersionNumber)
  return transaction.Begin() &&
         db_.Execute(
             base::StringPrintf("CREATE TABLE %s (id TEXT PRIMARY KEY, "
                                "sample_text TEXT, sample_number INTEGER, "
                                "creation_date TEXT)",
                                task_name_.c_str())
                 .c_str()) &&
         transaction.Commit();
}

}  // namespace federated_learning

}  // namespace brave
