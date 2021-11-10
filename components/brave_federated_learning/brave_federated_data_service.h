/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_BRAVE_FEDERATED_DATA_SERVICE_H_
#define BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_BRAVE_FEDERATED_DATA_SERVICE_H_

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

class BraveFederatedDataService {
 public:
  typedef std::map<std::string, DataStore> taskNameToDataStoreMap;

  explicit BraveFederatedDataService(const base::FilePath& base_database_path);
  ~BraveFederatedDataService();
  BraveFederatedDataService(const BraveFederatedDataService&) = delete;
  BraveFederatedDataService& operator=(const BraveFederatedDataService&) =
      delete;

  bool CreateDataStore(const std::string& task_id, const std::string& task_name)

      bool GetDataStore(const std::string& task_name);

  bool PurgeTaskDataStore(const std::string& task_name);
  bool PurgeDataStore();

  // Retention policies recurrent logic

 private:
  // bool CheckDataConformity();

  sql::Database db_;
  base::FilePath base_database_path_;
};

}  // namespace brave

#endif  // BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_BRAVE_FEDERATED_DATA_SERVICE_H_
