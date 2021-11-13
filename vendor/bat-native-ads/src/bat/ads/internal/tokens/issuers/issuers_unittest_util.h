/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_TOKENS_ISSUERS_ISSUERS_UNITTEST_UTIL_H_
#define BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_TOKENS_ISSUERS_ISSUERS_UNITTEST_UTIL_H_

#include "bat/ads/internal/tokens/issuers/public_key_aliases.h"

namespace ads {

struct IssuersInfo;

IssuersInfo BuildIssuers(const int ping,
                         const PublicKeyList& confirmations_public_keys,
                         const PublicKeyList& payments_public_keys);

void BuildAndSetIssuers();

}  // namespace ads

#endif  // BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_TOKENS_ISSUERS_ISSUERS_UNITTEST_UTIL_H_