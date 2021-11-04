/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_BRAVE_FEDERATED_LEARNING_SERVICE_H_
#define BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_BRAVE_FEDERATED_LEARNING_SERVICE_H_

#include <memory>
#include <string>

#include "components/keyed_service/core/keyed_service.h"
#include "components/prefs/pref_change_registrar.h"

class PrefService;
class PrefRegistrySimple;

namespace network {
class SharedURLLoaderFactory;
}  // namespace network

namespace brave {

class BraveOperationalPatterns;

class BraveFederatedLearningService : public KeyedService {
 public:
  BraveFederatedLearningService(
      PrefService* prefs,
      PrefService* local_state,
      scoped_refptr<network::SharedURLLoaderFactory> url_loader_factory);
  ~BraveFederatedLearningService() override;

  BraveFederatedLearningService(const BraveFederatedLearningService&) = delete;
  BraveFederatedLearningService& operator=(
      const BraveFederatedLearningService&) = delete;

  static void RegisterProfilePrefs(PrefRegistrySimple* registry);

  void Start();

 private:
  void InitPrefChangeRegistrar();
  void OnPreferenceChanged(const std::string& key);

  bool ShouldStartOperationalPatterns();
  bool IsP3AEnabled();
  bool IsOperationalPatternsEnabled();

  PrefService* prefs_;
  PrefService* local_state_;
  PrefChangeRegistrar local_state_change_registrar_;
  std::unique_ptr<BraveOperationalPatterns> operational_patterns_;
  scoped_refptr<network::SharedURLLoaderFactory> url_loader_factory_;
};

}  // namespace brave

#endif  // BRAVE_COMPONENTS_BRAVE_FEDERATED_LEARNING_BRAVE_FEDERATED_LEARNING_SERVICE_H_
