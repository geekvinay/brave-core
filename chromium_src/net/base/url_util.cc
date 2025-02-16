// Copyright 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "../../../../net/base/url_util.cc"

#include <iostream>
#include <string>

#include "net/base/registry_controlled_domains/registry_controlled_domain.h"
#include "url/origin.h"
#include "url/third_party/mozilla/url_parse.h"
#include "url/url_canon_ip.h"

namespace net {

std::string URLToEphemeralStorageDomain(const GURL& url) {
  std::string domain = registry_controlled_domains::GetDomainAndRegistry(
      url, registry_controlled_domains::INCLUDE_PRIVATE_REGISTRIES);

  // GetDomainAndRegistry might return an empty string if this host is an IP
  // address or a file URL.
  if (domain.empty())
    domain = url::Origin::Create(url.GetOrigin()).Serialize();

  return domain;
}

}  // namespace net
