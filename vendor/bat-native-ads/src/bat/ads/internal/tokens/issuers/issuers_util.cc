/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/tokens/issuers/issuers_util.h"

#include <algorithm>

#include "bat/ads/ads_client.h"
#include "bat/ads/internal/account/confirmations/confirmations_state.h"
#include "bat/ads/internal/ads_client_helper.h"
#include "bat/ads/internal/tokens/issuers/issuer_info_aliases.h"
#include "bat/ads/internal/tokens/issuers/issuers_info.h"
#include "bat/ads/pref_names.h"

namespace ads {

namespace {

absl::optional<IssuerInfo> GetIssuerForType(const IssuerType issuer_type) {
  const IssuerList& issuers = ConfirmationsState::Get()->GetIssuers();

  const auto iter = std::find_if(issuers.begin(), issuers.end(),
                                 [&issuer_type](const IssuerInfo& issuer) {
                                   return issuer.type == issuer_type;
                                 });
  if (iter == issuers.end()) {
    return absl::nullopt;
  }

  return *iter;
}

bool PublicKeyExists(const IssuerInfo& issuer, const std::string& public_key) {
  const auto iter = issuer.public_keys.find(public_key);
  if (iter == issuer.public_keys.end()) {
    return false;
  }

  return true;
}

}  // namespace

void SetIssuers(const IssuersInfo& issuers) {
  AdsClientHelper::Get()->SetIntegerPref(prefs::kIssuerPing, issuers.ping);

  ConfirmationsState::Get()->SetIssuers(issuers.issuers);
  ConfirmationsState::Get()->Save();
}

IssuersInfo GetIssuers() {
  IssuersInfo issuers;
  issuers.ping = AdsClientHelper::Get()->GetIntegerPref(prefs::kIssuerPing);
  issuers.issuers = ConfirmationsState::Get()->GetIssuers();

  return issuers;
}

bool HasIssuersChanged(const IssuersInfo& issuers) {
  const IssuersInfo& last_issuers = GetIssuers();
  if (issuers == last_issuers) {
    return false;
  }

  return true;
}

bool IssuerExistsForType(const IssuerType issuer_type) {
  const absl::optional<IssuerInfo>& issuer_optional =
      GetIssuerForType(issuer_type);
  if (!issuer_optional) {
    return false;
  }

  return true;
}

bool PublicKeyExistsForIssuerType(const IssuerType issuer_type,
                                  const std::string& public_key) {
  const absl::optional<IssuerInfo>& issuer_optional =
      GetIssuerForType(issuer_type);
  if (!issuer_optional) {
    return false;
  }
  const IssuerInfo& issuer = issuer_optional.value();

  return PublicKeyExists(issuer, public_key);
}

double GetSmallestDenominationForIssuerType(const IssuerType issuer_type) {
  const absl::optional<IssuerInfo>& issuer_optional =
      GetIssuerForType(issuer_type);
  if (!issuer_optional) {
    return 0.0;
  }
  const IssuerInfo& issuer = issuer_optional.value();

  const PublicKeyList& public_keys = issuer.public_keys;

  const auto iter = std::min_element(
      std::begin(public_keys), std::end(public_keys),
      [](const auto& lhs, const auto& rhs) { return lhs.second < rhs.second; });
  if (iter == public_keys.end()) {
    return 0.0;
  }

  return iter->second;
}

}  // namespace ads
