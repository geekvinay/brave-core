/* Copyright 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "brave/components/brave_federated_learning/brave_federated_learning_service.h"

#include <map>
#include <string>

#include "base/files/scoped_temp_dir.h"
#include "base/json/json_reader.h"
#include "base/path_service.h"
#include "brave/components/brave_federated_learning/brave_operational_patterns.h"
#include "brave/components/brave_federated_learning/brave_operational_patterns_features.h"
#include "brave/components/brave_federated_learning/data_stores/data_store.h"
#include "brave/components/brave_federated_learning/data_stores/sample_data_store.h"
#include "brave/components/p3a/pref_names.h"
#include "components/prefs/pref_change_registrar.h"
#include "components/prefs/pref_registry_simple.h"
#include "components/prefs/pref_service.h"
#include "services/network/public/cpp/shared_url_loader_factory.h"

// DEBUG
#include <iostream>

namespace brave {

BraveFederatedLearningService::BraveFederatedLearningService(
    PrefService* pref_service,
    scoped_refptr<network::SharedURLLoaderFactory> url_loader_factory)
    : local_state_(pref_service), url_loader_factory_(url_loader_factory) {}

BraveFederatedLearningService::~BraveFederatedLearningService() {}

void BraveFederatedLearningService::RegisterLocalStatePrefs(
    PrefRegistrySimple* registry) {
  BraveOperationalPatterns::RegisterLocalStatePrefs(registry);
}

void BraveFederatedLearningService::Start() {
  operational_patterns_.reset(
      new BraveOperationalPatterns(local_state_, url_loader_factory_));

  InitPrefChangeRegistrar();

  // Start of driver code --------------------------------------

  std::cerr << "Starting Sample Data Store \n";
  // create Data Store
  base::ScopedTempDir temp_dir;
  if (!temp_dir.CreateUniqueTempDir()) {
    return;
  }
  base::FilePath db_path(
      temp_dir.GetPath().AppendASCII("sample_federated_data_store.db"));
  federated_learning::SampleDataStore* ds =
      new federated_learning::SampleDataStore(db_path);
  bool success = ds->Init("0.2.0", "sample_federated_task");

  if (!success) {
    std::cerr << "Initialization failed. \n";
    return;
  }

  std::cerr << "Testing FederatedLog Class \n";
  // Test SampleFederatedLog class
  std::string id = "1";
  std::string sample_text =
      "This is some sample text, we are trying to retrieve";
  int sample_number = 42;
  std::string creation_date = "21-05-21 08:45:36Z";

  federated_learning::SampleTaskLog sample_log(id, sample_text, sample_number,
                                               creation_date);
  std::cerr << "Initialized FederatedLog \n";

  success = ds->AddLog(sample_log);
  if (success)
    std::cerr << "Added FederatedLog to FederatedDB. \n";

  std::map<std::string, federated_learning::SampleTaskLog> sample_logs;
  ds->LoadLogs(&sample_logs);
  std::cerr << "Retrived FederatedLogs stored. \n";

  for (std::map<std::string, federated_learning::SampleTaskLog>::const_iterator
           it(sample_logs.begin());
       it != sample_logs.end(); ++it) {
    const std::string key = it->first;
    const federated_learning::SampleTaskLog log = it->second;
    const std::string st = log.sample_text;

    std::cerr << "Retrived sample text: " << st << " for key: " << key
              << std::endl;
  }

  // End of driver code ----------------------------

  if (IsP3AEnabled() && IsOperationalPatternsEnabled()) {
    operational_patterns_->Start();
  }
}

void BraveFederatedLearningService::InitPrefChangeRegistrar() {
  local_state_change_registrar_.Init(local_state_);
  local_state_change_registrar_.Add(
      brave::kP3AEnabled,
      base::BindRepeating(&BraveFederatedLearningService::OnPreferenceChanged,
                          base::Unretained(this)));
}

void BraveFederatedLearningService::OnPreferenceChanged(
    const std::string& key) {
  if (!IsP3AEnabled() || !IsOperationalPatternsEnabled()) {
    operational_patterns_->Stop();
  } else if (IsP3AEnabled() && IsOperationalPatternsEnabled()) {
    operational_patterns_->Start();
  }
}

bool BraveFederatedLearningService::IsOperationalPatternsEnabled() {
  return operational_patterns::features::IsOperationalPatternsEnabled();
}

bool BraveFederatedLearningService::IsP3AEnabled() {
  return local_state_->GetBoolean(brave::kP3AEnabled);
}

}  // namespace brave
