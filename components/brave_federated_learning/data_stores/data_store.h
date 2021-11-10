/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_DATA_STORES_DATA_STORE_H_
#define BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_DATA_STORES_DATA_STORE_H_

#include <map>
#include <string>
#include <vector>

#include "base/files/file_path.h"
#include "base/memory/ref_counted.h"
#include "sql/database.h"
#include "sql/meta_table.h"
#include "ui/base/page_transition_types.h"
#include "url/gurl.h"

namespace brave {

namespace federated_learning {

// Log Definition --------------------------------------------------------

class DataStore {
 public:
  typedef std::vector<std::string> SampleIDs;

  explicit DataStore(const base::FilePath& database_path);
  ~DataStore();

  DataStore(const DataStore&) = delete;
  DataStore& operator=(const DataStore&) = delete;

  bool Init(const std::string& task_id,
            const std::string& task_name);  // TODO(add retention policy)
  bool DeleteAllLogs();

  // virtual bool AddLog() = 0;
  // virtual void LoadLogs() = 0;

 protected:
  virtual bool EnsureTable() = 0;
  // bool CheckDataConformity();

  sql::Database db_;
  base::FilePath database_path_;

  std::string task_id_;
  std::string task_name_;
  sql::MetaTable meta_table_;
};

}  // namespace federated_learning

}  // namespace brave

#endif  // BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_DATA_STORES_DATA_STORE_H_
