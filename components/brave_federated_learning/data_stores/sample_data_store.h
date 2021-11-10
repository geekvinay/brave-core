/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_DATA_STORES_SAMPLE_DATA_STORE_H_
#define BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_DATA_STORES_SAMPLE_DATA_STORE_H_

#include <map>
#include <string>
#include <vector>

#include "base/files/file_path.h"
#include "base/memory/ref_counted.h"
#include "brave/components/brave_federated_learning/data_stores/data_store.h"
#include "sql/database.h"
#include "sql/meta_table.h"
#include "ui/base/page_transition_types.h"
#include "url/gurl.h"

namespace brave {

namespace federated_learning {

// Log Definition --------------------------------------------------------
class SampleTaskLog {
 public:
  explicit SampleTaskLog(const std::string& id,
                         const std::string& sample_text,
                         const int sample_number,
                         const std::string& creation_date);
  SampleTaskLog(const SampleTaskLog& other);
  SampleTaskLog();
  ~SampleTaskLog();

  std::string id;
  std::string sample_text;
  int sample_number;
  std::string creation_date;
};

class SampleDataStore : DataStore {
 public:
  explicit SampleDataStore(const base::FilePath& database_path);
  ~SampleDataStore();

  SampleDataStore(const SampleDataStore&) = delete;
  SampleDataStore& operator=(const SampleDataStore&) = delete;

  typedef std::map<std::string, SampleTaskLog> GuidToSampleTaskLogMap;

  using DataStore::DeleteAllLogs;
  using DataStore::Init;

  bool AddLog(const SampleTaskLog& log);
  void LoadLogs(GuidToSampleTaskLogMap* sample_logs);

 private:
  bool EnsureTable() override;
};

}  // namespace federated_learning

}  // namespace brave

#endif  // BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_DATA_STORES_SAMPLE_DATA_STORE_H_
